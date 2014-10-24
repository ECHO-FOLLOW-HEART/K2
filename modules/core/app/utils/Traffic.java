package utils;

import com.mongodb.*;
import exception.TravelPiException;
import org.bson.types.ObjectId;

import java.util.*;

/**
 * 交通
 *
 * @author Zephyre
 */
public class Traffic {

    /**
     * 搜索航班信息。
     *
     * @param depId
     * @param arrId
     * @param priceLimits
     * @param depTimeLimit
     * @param arrTimeLimit
     * @throws TravelPiException
     */
    public static BasicDBList searchAirRoutes(String depId, String arrId, final List<Double> priceLimits,
                                              final List<Calendar> depTimeLimit, final List<Calendar> arrTimeLimit,
                                              final List<Calendar> epTimeLimit,
                                              final SortField sortField, final SortType sortType, int page, int pageSize) throws TravelPiException {
        MongoClient client;
        try {
            client = Utils.getMongoClient();
        } catch (NullPointerException e) {
            throw new TravelPiException(e);
        }
        assert client != null;
        DB db = client.getDB("traffic");
        DBCollection col = db.getCollection("air_route");

        ObjectId dep, arr;
        try {
            dep = new ObjectId(depId);
            arr = new ObjectId(arrId);
        } catch (IllegalArgumentException e) {
            throw new TravelPiException(String.format("Invalid departure/arrival ID: %s / %s.", depId, arrId), e);
        }

        QueryBuilder query1 = new QueryBuilder().or(QueryBuilder.start("arrAirport._id").is(arr).get(),
                QueryBuilder.start("arr._id").is(arr).get());
        QueryBuilder query2 = new QueryBuilder().or(QueryBuilder.start("depAirport._id").is(dep).get(),
                QueryBuilder.start("dep._id").is(dep).get());
        QueryBuilder query = new QueryBuilder().and(query1.get(), query2.get());

        if (priceLimits != null && priceLimits.size() == 2) {
            Double lower = priceLimits.get(0);
            Double upper = priceLimits.get(1);
            query.and("price.price").greaterThanEquals(lower).and("price.price").lessThanEquals(upper);
        }

//        for (Map.Entry<String, List<Calendar>> entry : new HashMap<String, List<Calendar>>() {
//            {
//                put("depTime", depTimeLimit);
//                put("arrTime", arrTimeLimit);
//            }
//        }.entrySet()) {
//            String k = entry.getKey();
//            List<Calendar> timeLimit = entry.getValue();
//
//            if (timeLimit != null && timeLimit.size() == 2) {
//                Calendar lower = timeLimit.get(0);
//                Calendar upper = timeLimit.get(1);
//
//                lower.set(1980, Calendar.JANUARY, 1);
//                upper.set(1980, Calendar.JANUARY, 1);
//
//                query.and(k).greaterThan(lower.getTime()).and(k).lessThanEquals(upper.getTime());
//            }
//        }

        DBCursor cursor = col.find(new QueryBuilder().and(query1.get(), query2.get()).get());

        // 排序
        int st = 0;
        if (sortType != null) {
            switch (sortType) {
                case ASC:
                    st = 1;
                    break;
                case DESC:
                    st = -1;
                    break;
            }
        }
        String stKey = null;
        switch (sortField) {
            case PRICE:
                stKey = "price.price";
                break;
            case TIME_COST:
                stKey = "timeCost";
                break;
            case DEP_TIME:
                stKey = "depTime";
                break;
            case ARR_TIME:
                stKey = "arrTime";
                break;
        }

        if (st != 0 && stKey != null)
            cursor.sort(BasicDBObjectBuilder.start(stKey, st).get());

        // 过滤
        final List<FilterDelegate<Object>> filterRules = new ArrayList<>();

        // 出发时间和到达时间的过滤
        for (Map.Entry<String, List<Calendar>> entry : new HashMap<String, List<Calendar>>() {{
            put("depTime", depTimeLimit);
            put("arrTime", arrTimeLimit);
        }}.entrySet()) {
            final List<Calendar> timeLimit = entry.getValue();
            final String k = entry.getKey();
            if (timeLimit != null && timeLimit.size() == 2) {
                filterRules.add(new FilterDelegate<Object>() {
                    @Override
                    public boolean filter(Object item) {
                        DBObject routeItem = (DBObject) item;
                        Calendar lower = timeLimit.get(0);
                        Calendar upper = timeLimit.get(1);
                        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Shanghai"));
                        cal.setTime((Date) routeItem.get(k));
                        // cal是否可以处于lower/upper之间。将cal置为刚好大于lower，然后判断cal是否小于upper
                        for (int field : new int[]{Calendar.YEAR, Calendar.DAY_OF_YEAR})
                            cal.set(field, lower.get(field));
                        cal.add(Calendar.DAY_OF_YEAR, -1);
                        while (cal.before(lower))
                            cal.add(Calendar.DAY_OF_YEAR, 1);

                        return cal.before(upper);
                    }
                });
            }
        }

        // 端点时间过滤
        if (epTimeLimit != null && epTimeLimit.size() == 2) {
            filterRules.add(new FilterDelegate<Object>() {
                @Override
                public boolean filter(Object item) {
                    DBObject routeItem = (DBObject) item;
                    Calendar lower = epTimeLimit.get(0);
                    Calendar upper = epTimeLimit.get(1);
                    Calendar dep = Calendar.getInstance(Utils.getDefaultTimeZone());
                    dep.setTime((Date) routeItem.get("depTime"));
                    Calendar arr = Calendar.getInstance(Utils.getDefaultTimeZone());
                    arr.setTime((Date) routeItem.get("arrTime"));
                    long duration = arr.getTimeInMillis() - dep.getTimeInMillis();

                    // dep是否可以处于lower/upper之间。将dep置为刚好大于lower，然后判断cal是否小于upper
                    for (int field : new int[]{Calendar.YEAR, Calendar.DAY_OF_YEAR})
                        dep.set(field, lower.get(field));
                    dep.add(Calendar.DAY_OF_YEAR, -1);
                    while (dep.before(lower))
                        dep.add(Calendar.DAY_OF_YEAR, 1);
                    arr.setTimeInMillis(dep.getTimeInMillis() + duration);

                    return arr.before(upper);
                }
            });
        }

        // 如果不存在手动过滤，则可以直接使用cursor的分页机制
        if (filterRules.isEmpty())
            cursor.skip(page * pageSize).limit(pageSize);

        BasicDBList results = new BasicDBList();
        while (cursor.hasNext())
            results.add(cursor.next());

        if (!filterRules.isEmpty()) {
            for (FilterDelegate<Object> rule : filterRules) {
                List<Object> filtered = FPUtils.filter(results, rule);
                results.clear();
                for (Object obj : filtered)
                    results.add(obj);
            }
            List<Object> filtered;
            int fromIdx = page * pageSize;
            if (fromIdx >= results.size())
                filtered = new ArrayList<>();
            else {
                int toIdx = (page + 1) * pageSize;
                if (toIdx > results.size())
                    toIdx = results.size();
                filtered = results.subList(fromIdx, toIdx);
            }
            BasicDBList r = new BasicDBList();
            for (Object obj : filtered)
                r.add(obj);
            results = r;
        }

        return results;
    }

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
                                               final List<Calendar> depTimeLimit, final List<Calendar> arrTimeLimit,
                                               final List<Calendar> epTimeLimit, final List<String> trainClasses,
                                               final SortField sortField, final SortType sortType) throws TravelPiException {
        MongoClient client;
        try {
            client = Utils.getMongoClient();
        } catch (NullPointerException e) {
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
            if (arrPrice != null) {
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
            }

            builder.add("_id", route.get("_id"));
            builder.add("code", route.get("code"));
            builder.add("type", route.get("type"));

            routeList.add(builder.get());
        }

        // 过滤
        final List<FilterDelegate<Object>> filterRules = new ArrayList<>();

        // 出发时间和到达时间的过滤
        for (Map.Entry<String, List<Calendar>> entry : new HashMap<String, List<Calendar>>() {{
            put("depTime", depTimeLimit);
            put("arrTime", arrTimeLimit);
        }}.entrySet()) {
            final List<Calendar> timeLimit = entry.getValue();
            final String k = entry.getKey();
            if (timeLimit != null && timeLimit.size() == 2) {
                filterRules.add(new FilterDelegate<Object>() {
                    @Override
                    public boolean filter(Object item) {
                        DBObject routeItem = (DBObject) item;
                        Calendar lower = timeLimit.get(0);
                        Calendar upper = timeLimit.get(1);
                        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Shanghai"));
                        cal.setTime((Date) routeItem.get(k));
                        // cal是否可以处于lower/upper之间。将cal置为刚好大于lower，然后判断cal是否小于upper
                        for (int field : new int[]{Calendar.YEAR, Calendar.DAY_OF_YEAR})
                            cal.set(field, lower.get(field));
                        cal.add(Calendar.DAY_OF_YEAR, -1);
                        while (cal.before(lower))
                            cal.add(Calendar.DAY_OF_YEAR, 1);

                        return cal.before(upper);
                    }
                });
            }
        }

        // 端点时间过滤
        if (epTimeLimit != null && epTimeLimit.size() == 2) {
            filterRules.add(new FilterDelegate<Object>() {
                @Override
                public boolean filter(Object item) {
                    DBObject routeItem = (DBObject) item;
                    Calendar lower = epTimeLimit.get(0);
                    Calendar upper = epTimeLimit.get(1);
                    Calendar dep = Calendar.getInstance(Utils.getDefaultTimeZone());
                    dep.setTime((Date) routeItem.get("depTime"));
                    Calendar arr = Calendar.getInstance(Utils.getDefaultTimeZone());
                    arr.setTime((Date) routeItem.get("arrTime"));
                    long duration = arr.getTimeInMillis() - dep.getTimeInMillis();

                    // dep是否可以处于lower/upper之间。将dep置为刚好大于lower，然后判断cal是否小于upper
                    for (int field : new int[]{Calendar.YEAR, Calendar.DAY_OF_YEAR})
                        dep.set(field, lower.get(field));
                    dep.add(Calendar.DAY_OF_YEAR, -1);
                    while (dep.before(lower))
                        dep.add(Calendar.DAY_OF_YEAR, 1);
                    arr.setTimeInMillis(dep.getTimeInMillis() + duration);

                    return arr.before(upper);
                }
            });
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
            List<Object> filtered = FPUtils.filter(routeList, rule);
            routeList.clear();
            for (Object obj : filtered)
                routeList.add(obj);
        }

        // 排序
        if (sortField != null && sortType != null) {
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

    public enum SortField {PRICE, DEP_TIME, ARR_TIME, TIME_COST}

    public enum SortType {ASC, DESC}
}
