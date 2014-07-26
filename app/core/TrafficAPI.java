package core;

import exception.ErrorCode;
import exception.TravelPiException;
import models.MorphiaFactory;
import models.morphia.traffic.AirRoute;
import models.morphia.traffic.RouteIterator;
import models.morphia.traffic.TrainRoute;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
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
    public static AirRoute getAirRouteByCode(String flightCode, Calendar cal) throws TravelPiException {
        if (flightCode == null || flightCode.isEmpty())
            throw new TravelPiException(ErrorCode.INVALID_ARGUMENT, "Invalid flight code.");

        flightCode = flightCode.toUpperCase();
        Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.TRAFFIC);
        Query<AirRoute> query = ds.createQuery(AirRoute.class);
        query.field("code").equal(flightCode);
        AirRoute route = query.get();
        Calendar depTime = Calendar.getInstance();
        depTime.setTime(route.depTime);
        Calendar arrTime = Calendar.getInstance();
        arrTime.setTime(route.arrTime);
        long dt = arrTime.getTimeInMillis() - depTime.getTimeInMillis();

        depTime.set(Calendar.YEAR, cal.get(Calendar.YEAR));
        depTime.set(Calendar.MONTH, cal.get(Calendar.MONTH));
        depTime.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH));
        arrTime.setTimeInMillis(depTime.getTimeInMillis() + dt);

        route.depTime = depTime.getTime();
        route.arrTime = arrTime.getTime();

        return route;
    }

    /**
     * 搜索航班信息。
     *
     * @param depId
     * @param arrId        @throws TravelPiException
     * @param baseCal
     * @param depTimeLimit
     * @param arrTimeLimit
     * @param priceLimits
     */
    public static RouteIterator searchAirRoutes(ObjectId depId, ObjectId arrId, Calendar baseCal, final List<Calendar> depTimeLimit, final List<Calendar> arrTimeLimit, final List<Calendar> epTimeLimits, final List<Double> priceLimits, final SortField sortField, int sortType, int page, int pageSize) throws TravelPiException {

        Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.TRAFFIC);
        Query<AirRoute> query = ds.createQuery(AirRoute.class);

        query.or(query.criteria("depStop.id").equal(depId), query.criteria("depLoc.id").equal(depId));
        query.or(query.criteria("arrStop.id").equal(arrId), query.criteria("arrLoc.id").equal(arrId));

        // 时间节点过滤
        for (Map.Entry<String, List<Calendar>> entry : new HashMap<String, List<Calendar>>() {
            {
                put("depTime", depTimeLimit);
                put("arrTime", arrTimeLimit);
            }
        }.entrySet()) {
            String k = entry.getKey();
            List<Calendar> timeLimits = entry.getValue();

            if (timeLimits != null && timeLimits.size() == 2) {
                Calendar lower = Calendar.getInstance();
                lower.setTimeInMillis(timeLimits.get(0).getTimeInMillis());
                Calendar upper = Calendar.getInstance();
                upper.setTimeInMillis(timeLimits.get(1).getTimeInMillis());
                long elapse = upper.getTimeInMillis() - lower.getTimeInMillis();
                lower.set(1980, Calendar.JANUARY, 1);
                upper.setTimeInMillis(lower.getTimeInMillis() + elapse);
                query.and(query.criteria(k).greaterThanOrEq(lower.getTime()), query.criteria(k).lessThanOrEq(upper.getTime()));
            }
        }

        // 时间节点过滤
        if (epTimeLimits != null && epTimeLimits.size() == 2) {
            Calendar lower = Calendar.getInstance();
            lower.setTimeInMillis(epTimeLimits.get(0).getTimeInMillis());
            Calendar upper = Calendar.getInstance();
            upper.setTimeInMillis(epTimeLimits.get(1).getTimeInMillis());
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

        // 排序
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
        query.order(String.format("%s%s", sortType > 0 ? "" : "-", stKey));
        query.offset(page * pageSize).limit(pageSize);
        Iterator<AirRoute> it = query.iterator();

        Calendar cal;
        if (epTimeLimits != null && epTimeLimits.size() == 2)
            cal = epTimeLimits.get(0);
        else
            cal = baseCal;
        return RouteIterator.getInstance(it, (cal != null ? cal.getTime() : null));
    }

    /**
     * 获得航班信息。
     *
     * @param trainCode 航班号。
     * @return 航班信息。如果没有找到，返回null。
     * @throws TravelPiException
     */
    public static TrainRoute getTrainRouteByCode(String trainCode, Calendar cal) throws TravelPiException {
        if (trainCode == null || trainCode.isEmpty())
            throw new TravelPiException(ErrorCode.INVALID_ARGUMENT, "Invalid train code.");

        trainCode = trainCode.toUpperCase();
        Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.TRAFFIC);
        Query<TrainRoute> query = ds.createQuery(TrainRoute.class);

        TrainRoute route = query.field("code").equal(trainCode).get();

        Calendar depTime = Calendar.getInstance();
        depTime.setTime(route.depTime);
        Calendar arrTime = Calendar.getInstance();
        arrTime.setTime(route.arrTime);
        long dt = arrTime.getTimeInMillis() - depTime.getTimeInMillis();

        depTime.set(Calendar.YEAR, cal.get(Calendar.YEAR));
        depTime.set(Calendar.MONTH, cal.get(Calendar.MONTH));
        depTime.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH));
        arrTime.setTimeInMillis(depTime.getTimeInMillis() + dt);

        route.depTime = depTime.getTime();
        route.arrTime = arrTime.getTime();

        return route;
    }
}
