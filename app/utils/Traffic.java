package utils;

import com.mongodb.*;
import controllers.TravelPiException;
import org.bson.types.ObjectId;
import java.net.UnknownHostException;
import java.util.*;

/**
 * 交通
 *
 * @author Zephyre
 */
public class Traffic {

    public enum SortField {PRICE, DEP_TIME, ARR_TIME, TIME_COST}

    public enum SortType {ASC, DESC}

    /**
     * 搜索列车信息。
     *
     * @param depId
     * @param arrId
     * @param priceLimits
     * @param depTimeLimit
     * @param arrTimeLimit
     * @param trainClasses
     * @throws TravelPiException
     */
    public static BasicDBList searchTrainRoute(String depId, String arrId, final List<Double> priceLimits,
                                               final List<Date> depTimeLimit, final List<Date> arrTimeLimit,
                                               final List<String> trainClasses,
                                               final SortField sortField, final SortType sortType) throws TravelPiException {
        MongoClient client;
        try {
            client = Utils.getMongoClient();
        } catch (UnknownHostException | NullPointerException e) {
            throw new TravelPiException(e);
        }
        assert client != null;
        DB db = client.getDB("traffic");
        DBCollection col = db.getCollection("train_route");

        ObjectId dep, arr;
        try {
            dep = new ObjectId(depId);
            arr = new ObjectId(arrId);
        } catch (IllegalArgumentException e) {
            throw new TravelPiException(String.format("Invalid departure/arrival ID: %s / %s.", depId, arrId), e);
        }

        QueryBuilder query1 = new QueryBuilder().or(QueryBuilder.start("details.locId").is(dep).get(),
                QueryBuilder.start("details.stopId").is(dep).get());
        QueryBuilder query2 = new QueryBuilder().or(QueryBuilder.start("details.locId").is(arr).get(),
                QueryBuilder.start("details.stopId").is(arr).get());
        DBCursor cursor = col.find(new QueryBuilder().and(query1.get(), query2.get()).get());

        BasicDBList routeList = new BasicDBList();
        while (cursor.hasNext()) {
            DBObject route = cursor.next();

            // 到达节点必须晚于出发节点
            DBObject arrStop = null;
            DBObject depStop = null;
            for (Object detail : (BasicDBList) (route.get("details"))) {
                DBObject stop = (DBObject) detail;
                if (stop.get("stopId").equals(dep) || stop.get("locId").equals(dep))
                    depStop = stop;
                else if (stop.get("stopId").equals(arr) || stop.get("locId").equals(arr))
                    arrStop = stop;

                if (arrStop != null && depStop != null)
                    // 出发地和到达地都已经找到
                    break;
                if (arrStop != null && depStop == null)
                    // 出现先到达再出发的情况：反向车次
                    break;
            }
            if (arrStop == null || depStop == null)
                continue;

            // 建立节点
            BasicDBObjectBuilder builder = BasicDBObjectBuilder.start();

            int dayLag = (int) arrStop.get("dayLag") - (int) depStop.get("dayLag");
            builder.add("dayLag", dayLag);

            final DBObject finalDepStop = depStop;
            final DBObject finalArrStop = arrStop;
            for (Map.Entry<String, DBObject> entry : new HashMap<String, DBObject>() {{
                put("dep", finalDepStop);
                put("arr", finalArrStop);
            }}.entrySet()) {
                DBObject stop = entry.getValue();
                builder.add(entry.getKey(),
                        BasicDBObjectBuilder.start().add("locId", stop.get("locId")).add("locName", stop.get("locName"))
                                .add("stopId", stop.get("stopId")).add("stopName", stop.get("stopName")).get());
            }

            Date depTime = (Date) depStop.get("depTime");
            Date arrTime = (Date) arrStop.get("arrTime");
            builder.add("depTime", depTime);
            builder.add("arrTime", arrTime);
            builder.add("timeCost", (int) (arrTime.getTime() - depTime.getTime()) / (60 * 1000));

            // 路程
            builder.add("totalDist", route.get("distance"));
            builder.add("distance", (int) arrStop.get("distance") - (int) depStop.get("distance"));

            // 票价
            BasicDBObjectBuilder priceBuilder = BasicDBObjectBuilder.start();
            DBObject arrPrice = (DBObject) arrStop.get("price");
            DBObject depPrice = (DBObject) depStop.get("price");
            for (String priceKey : arrPrice.keySet()) {
                double arrVal = (double) arrPrice.get(priceKey);
                double depVal = (depPrice != null && depPrice.containsField(priceKey) ? (double) depPrice.get(priceKey) : 0);
                priceBuilder.add(priceKey, arrVal - depVal);
            }
            DBObject priceList = priceBuilder.get();
            builder.add("priceList", priceList);
            // 最低票价
            if (!priceList.keySet().isEmpty())
                builder.add("price", Collections.min(priceList.toMap().values()));

            builder.add("_id", route.get("_id"));
            builder.add("code", route.get("code"));
            builder.add("type", route.get("type"));

            routeList.add(builder.get());
        }

        // 过滤
        final List<FilterDelegate<Object>> filterRules = new ArrayList<>();

        // 出发时间和到达时间的过滤
        for (Map.Entry<String, List<Date>> entry : new HashMap<String, List<Date>>() {{
            put("depTime", depTimeLimit);
            put("arrTime", arrTimeLimit);
        }}.entrySet()) {
            final List<Date> timeLimit = entry.getValue();
            final String k = entry.getKey();
            if (timeLimit != null && timeLimit.size() == 2) {
                filterRules.add(new FilterDelegate<Object>() {
                    @Override
                    public boolean filter(Object item) {
                        DBObject routeItem = (DBObject) item;
                        Calendar cal = Calendar.getInstance();
                        cal.setTime((Date) routeItem.get(k));
                        Calendar lower = Calendar.getInstance();
                        lower.setTime(depTimeLimit.get(0));
                        Calendar upper = Calendar.getInstance();
                        upper.setTime(arrTimeLimit.get(1));
                        lower.set(
                                cal.get(Calendar.YEAR),
                                cal.get(Calendar.MONTH),
                                cal.get(Calendar.DAY_OF_YEAR)
                        );
                        upper.set(
                                cal.get(Calendar.YEAR),
                                cal.get(Calendar.MONTH),
                                cal.get(Calendar.DAY_OF_YEAR)
                        );

                        return (cal.after(lower) && cal.before(upper));
                    }
                });
            }
        }

        // 价格的过滤
        if (priceLimits != null && priceLimits.size() == 2) {
            filterRules.add(new FilterDelegate<Object>() {
                @Override
                @SuppressWarnings("unchecked")
                public boolean filter(Object item) {

                    DBObject routeItem = (DBObject) item;
                    List<Double> priceValues = (List<Double>) ((Map<String, Double>) ((DBObject) routeItem.get("priceList")).toMap()).values();
                    priceValues = FPUtils.filter(priceValues, new FilterDelegate<Double>() {
                        @Override
                        public boolean filter(Double item) {
                            return (item >= priceLimits.get(0) && item <= priceLimits.get(1));
                        }
                    });
                    return (priceValues.size() > 0);
                }
            });
        }

        // 车次类型的过滤
        if (trainClasses != null) {
            filterRules.add(new FilterDelegate<Object>() {
                @Override
                public boolean filter(Object item) {
                    return trainClasses.contains(((DBObject) item).get("type").toString());
                }
            });
        }

        for (FilterDelegate<Object> rule : filterRules) {
            routeList = (BasicDBList) FPUtils.filter(routeList, rule);
        }

        // 排序
        if (sortField!=null && sortType!=null) {
            final boolean asc = (sortType == SortType.ASC);
            Comparator<Object> cmp;
            switch (sortField) {
                case PRICE:
                    cmp = new Comparator<Object>() {
                        @Override
                        public int compare(Object o1, Object o2) {
                            double price1 = (double) ((DBObject) o1).get("price");
                            double price2 = (double) ((DBObject) o2).get("price");
                            int val = (int) (price1 - price2);
                            return (asc ? val : -val);
                        }
                    };
                    break;
                case TIME_COST:
                    cmp = new Comparator<Object>() {
                        @Override
                        public int compare(Object o1, Object o2) {
                            int t1 = (int) ((DBObject) o1).get("timeCost");
                            int t2 = (int) ((DBObject) o2).get("timeCost");
                            int val = t1 - t2;
                            return (asc ? val : -val);
                        }
                    };
                    break;
                case DEP_TIME:
                case ARR_TIME:
                    final String k = (sortField == SortField.DEP_TIME ? "depTime" : "arrTime");
                    cmp = new Comparator<Object>() {
                        @Override
                        public int compare(Object o1, Object o2) {
                            Date time1 = (Date) ((DBObject) o1).get(k);
                            Date time2 = (Date) ((DBObject) o2).get(k);
                            int val = (int) (time1.getTime() - time2.getTime());
                            return (asc ? val : -val);
                        }
                    };
                    break;
                default:
                    cmp = null;
            }
            Collections.sort(routeList, cmp);
        }

        return routeList;
    }
}
