package core;

import exception.ErrorCode;
import exception.TravelPiException;
import models.MorphiaFactory;
import models.morphia.traffic.AirRoute;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.CriteriaContainerImpl;
import org.mongodb.morphia.query.Query;

import java.util.*;

/**
 * 交通相关核心API。
 *
 * @author Zephyre
 */
public class TrafficAPI {

    /**
     * 排序的字段。
     */
    public enum SortField {
        PRICE, DEP_TIME, ARR_TIME, TIME_COST
    }

    /**
     * 获得航班信息。
     *
     * @param flightCode 航班号。
     * @return 航班信息。如果没有找到，返回null。
     * @throws TravelPiException
     */
    public static AirRoute getAirRoute(String flightCode) throws TravelPiException {
        if (flightCode == null || flightCode.isEmpty())
            throw new TravelPiException(ErrorCode.INVALID_ARGUMENT, "Invalid flight code.");

        flightCode = flightCode.toUpperCase();
        Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.TRAFFIC);
        return ds.createQuery(AirRoute.class).field("flightCode").equal(flightCode).get();
    }

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
    public static Iterator<AirRoute> searchAirRoutes(ObjectId depId, ObjectId arrId, final List<Double> priceLimits,
                                                     final List<Calendar> depTimeLimit, final List<Calendar> arrTimeLimit,
                                                     final List<Calendar> epTimeLimit, final SortField sortField,
                                                     int sortType, int page, int pageSize) throws TravelPiException {

        Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.TRAFFIC);
        Query<AirRoute> query = ds.createQuery(AirRoute.class);
        CriteriaContainerImpl q1 = query.criteria("depAirport").equal(depId);
        CriteriaContainerImpl q2 = query.criteria("depLoc").equal(depId);
        CriteriaContainerImpl q3 = query.criteria("arrAirport").equal(arrId);
        CriteriaContainerImpl q4 = query.criteria("arrLoc").equal(arrId);

        query.and(q1.or(q2), q3.or(q4));

        // 时间节点过滤
        for (Map.Entry<String, List<Calendar>> entry : new HashMap<String, List<Calendar>>() {
            {
                put("depTime", depTimeLimit);
                put("arrTime", arrTimeLimit);
            }
        }.entrySet()) {
            String k = entry.getKey();
            List<Calendar> timeLimit = entry.getValue();

            if (timeLimit != null && timeLimit.size() == 2) {
                Calendar lower = timeLimit.get(0);
                Calendar upper = timeLimit.get(1);
                long elapse = upper.getTimeInMillis() - lower.getTimeInMillis();

                lower.set(1980, Calendar.JANUARY, 1);
                upper.setTimeInMillis(lower.getTimeInMillis() + elapse);

                query.and(query.criteria(k).greaterThanOrEq(lower.getTime()), query.criteria(k).lessThanOrEq(upper.getTime()));
            }
        }

        // 时间节点过滤
        if (epTimeLimit != null && epTimeLimit.size() == 2) {
            Calendar lower = epTimeLimit.get(0);
            Calendar upper = epTimeLimit.get(1);
            long elapse = upper.getTimeInMillis() - lower.getTimeInMillis();

            lower.set(1980, Calendar.JANUARY, 1);
            upper.setTimeInMillis(lower.getTimeInMillis() + elapse);
            query.and(query.criteria("depTime").greaterThanOrEq(lower.getTime()),
                    query.criteria("arrTime").lessThanOrEq(upper.getTime()));
        }

        // 价格过滤
        if (priceLimits != null && priceLimits.size() == 2) {
            Double lower = priceLimits.get(0);
            Double upper = priceLimits.get(1);
            query.and(query.criteria("price.price").greaterThanOrEq(lower),
                    query.criteria("price.price").lessThanOrEq(upper));
        }

        query.offset(page * pageSize).limit(pageSize);

        return query.iterator();


//        MongoClient client;
//        try {
//            client = Utils.getMongoClient();
//        } catch (NullPointerException e) {
//            throw new TravelPiException(e);
//        }
//        assert client != null;
//        DB db = client.getDB("traffic");
//        DBCollection col = db.getCollection("air_route");
//
//        ObjectId dep, arr;
//        try {
//            dep = new ObjectId(depId);
//            arr = new ObjectId(arrId);
//        } catch (IllegalArgumentException e) {
//            throw new TravelPiException(String.format("Invalid departure/arrival ID: %s / %s.", depId, arrId), e);
//        }
//
//        QueryBuilder query1 = new QueryBuilder().or(QueryBuilder.start("arrAirport._id").is(arr).get(),
//                QueryBuilder.start("arr._id").is(arr).get());
//        QueryBuilder query2 = new QueryBuilder().or(QueryBuilder.start("depAirport._id").is(dep).get(),
//                QueryBuilder.start("dep._id").is(dep).get());
//        QueryBuilder query = new QueryBuilder().and(query1.get(), query2.get());
//
//        if (priceLimits != null && priceLimits.size() == 2) {
//            Double lower = priceLimits.get(0);
//            Double upper = priceLimits.get(1);
//            query.and("price.price").greaterThanEquals(lower).and("price.price").lessThanEquals(upper);
//        }

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

//        DBCursor cursor = col.find(new QueryBuilder().and(query1.get(), query2.get()).get());
//
//        // 排序
//        int st = 0;
//        if (sortType != null) {
//            switch (sortType) {
//                case ASC:
//                    st = 1;
//                    break;
//                case DESC:
//                    st = -1;
//                    break;
//            }
//        }
//        String stKey = null;
//        switch (sortField) {
//            case PRICE:
//                stKey = "price.price";
//                break;
//            case TIME_COST:
//                stKey = "timeCost";
//                break;
//            case DEP_TIME:
//                stKey = "depTime";
//                break;
//            case ARR_TIME:
//                stKey = "arrTime";
//                break;
//        }
//
//        if (st != 0 && stKey != null)
//            cursor.sort(BasicDBObjectBuilder.start(stKey, st).get());
//
//        // 过滤
//        final List<FilterDelegate<Object>> filterRules = new ArrayList<>();
//
//        // 出发时间和到达时间的过滤
//        for (Map.Entry<String, List<Calendar>> entry : new HashMap<String, List<Calendar>>() {{
//            put("depTime", depTimeLimit);
//            put("arrTime", arrTimeLimit);
//        }}.entrySet()) {
//            final List<Calendar> timeLimit = entry.getValue();
//            final String k = entry.getKey();
//            if (timeLimit != null && timeLimit.size() == 2) {
//                filterRules.add(new FilterDelegate<Object>() {
//                    @Override
//                    public boolean filter(Object item) {
//                        DBObject routeItem = (DBObject) item;
//                        Calendar lower = timeLimit.get(0);
//                        Calendar upper = timeLimit.get(1);
//                        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Shanghai"));
//                        cal.setTime((Date) routeItem.get(k));
//                        // cal是否可以处于lower/upper之间。将cal置为刚好大于lower，然后判断cal是否小于upper
//                        for (int field : new int[]{Calendar.YEAR, Calendar.DAY_OF_YEAR})
//                            cal.set(field, lower.get(field));
//                        cal.add(Calendar.DAY_OF_YEAR, -1);
//                        while (cal.before(lower))
//                            cal.add(Calendar.DAY_OF_YEAR, 1);
//
//                        return cal.before(upper);
//                    }
//                });
//            }
//        }
//
//        // 端点时间过滤
//        if (epTimeLimit != null && epTimeLimit.size() == 2) {
//            filterRules.add(new FilterDelegate<Object>() {
//                @Override
//                public boolean filter(Object item) {
//                    DBObject routeItem = (DBObject) item;
//                    Calendar lower = epTimeLimit.get(0);
//                    Calendar upper = epTimeLimit.get(1);
//                    Calendar dep = Calendar.getInstance(Utils.getDefaultTimeZone());
//                    dep.setTime((Date) routeItem.get("depTime"));
//                    Calendar arr = Calendar.getInstance(Utils.getDefaultTimeZone());
//                    arr.setTime((Date) routeItem.get("arrTime"));
//                    long duration = arr.getTimeInMillis() - dep.getTimeInMillis();
//
//                    // dep是否可以处于lower/upper之间。将dep置为刚好大于lower，然后判断cal是否小于upper
//                    for (int field : new int[]{Calendar.YEAR, Calendar.DAY_OF_YEAR})
//                        dep.set(field, lower.get(field));
//                    dep.add(Calendar.DAY_OF_YEAR, -1);
//                    while (dep.before(lower))
//                        dep.add(Calendar.DAY_OF_YEAR, 1);
//                    arr.setTimeInMillis(dep.getTimeInMillis() + duration);
//
//                    return arr.before(upper);
//                }
//            });
//        }
//
//        // 如果不存在手动过滤，则可以直接使用cursor的分页机制
//        if (filterRules.isEmpty())
//            cursor.skip(page * pageSize).limit(pageSize);
//
//        BasicDBList results = new BasicDBList();
//        while (cursor.hasNext())
//            results.add(cursor.next());
//
//        if (!filterRules.isEmpty()) {
//            for (FilterDelegate<Object> rule : filterRules) {
//                List<Object> filtered = FPUtils.filter(results, rule);
//                results.clear();
//                for (Object obj : filtered)
//                    results.add(obj);
//            }
//            List<Object> filtered;
//            int fromIdx = page * pageSize;
//            if (fromIdx >= results.size())
//                filtered = new ArrayList<>();
//            else {
//                int toIdx = (page + 1) * pageSize;
//                if (toIdx > results.size())
//                    toIdx = results.size();
//                filtered = results.subList(fromIdx, toIdx);
//            }
//            BasicDBList r = new BasicDBList();
//            for (Object obj : filtered)
//                r.add(obj);
//            results = r;
//        }
//
//        return results;
    }
}
