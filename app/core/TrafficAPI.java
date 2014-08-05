package core;

import exception.ErrorCode;
import exception.TravelPiException;
import models.MorphiaFactory;
import models.morphia.traffic.*;
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
        PRICE, DEP_TIME, ARR_TIME, TIME_COST,CODE
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
    public static RouteIterator searchAirRoutes(ObjectId depId, ObjectId arrId, Calendar baseCal, final List<Calendar> depTimeLimit,

 final List<Calendar> arrTimeLimit, final List<Calendar> epTimeLimits, final List<Double> priceLimits,
 final SortField sortField, int sortType, int page, int pageSize) throws TravelPiException {

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

    /**
     * 搜索火车信息。
     *
     * @param depId
     * @param arrId        @throws TravelPiException
     * @param baseCal
     * @param depTimeLimit
     * @param arrTimeLimit
     * @param priceLimits
     */
    public static RouteIterator searchTrainRoutes(ObjectId depId, ObjectId arrId,String trainType, Calendar baseCal, final List<Calendar> depTimeLimit,
                  final List<Calendar> arrTimeLimit, final List<Calendar> epTimeLimits, final List<Double> priceLimits, final SortField sortField,
                  int sortType, int page, int pageSize) throws TravelPiException {

        Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.TRAFFIC);
        Query<TrainRoute> query = ds.createQuery(TrainRoute.class);

        // 解析列车型号类型查询，例如：DGC
        String typeStr = (trainType != null ? trainType.trim().toUpperCase() : "");

        List<CriteriaContainerImpl> critList = new ArrayList<>();
        for (char typeEle : typeStr.toCharArray())
            critList.add(query.criteria("type").equal(typeEle));
        if (!critList.isEmpty())
            query.or(critList.toArray(new CriteriaContainerImpl[]{null}));

        query.or(query.criteria("details.stop.id").equal(depId), query.criteria("details.loc.id").equal(depId));
        query.or(query.criteria("details.stop.id").equal(arrId), query.criteria("details.loc.id").equal(arrId));

        Iterator<TrainRoute> it = query.iterator();
        List<TrainRoute> validRoutes = new ArrayList<>();
        for (TrainRoute route :query.asList() ) {
            // dep必须出现在arr前面，这样方向才是正确的，否则线路无效。
            boolean isDep = false;
            int depIdx = -1;
            for (int i = 0; i < route.details.size(); i++) {
                TrainEntry entry = route.details.get(i);
                if (entry.loc.id.toString().equals(depId.toString()) || entry.stop.id.toString().equals(depId.toString())  ) {
                    // 遇到出发站点
                    if (isDep)
                        break;
                    else {
                        isDep = true;
                        depIdx = i;
                    }
                } else if (entry.loc.id.toString().equals(arrId.toString()) || entry.stop.id.toString().equals(arrId.toString())) {
                    // 遇到到达站点
                    if (!isDep)
                        break;
                    else {
                        TrainEntry depEntry = route.details.get(depIdx);
                        TrainEntry arrEntry = route.details.get(i);

                        route.distance = arrEntry.distance - depEntry.distance;
                        route.depTime = depEntry.depTime;
                        route.arrTime = arrEntry.arrTime;
                        route.depLoc = depEntry.loc;
                        route.arrLoc = arrEntry.loc;
                        route.depStop = depEntry.stop;
                        route.arrStop = arrEntry.stop;
                        route.timeCost = (int) (route.arrTime.getTime() - route.depTime.getTime()) / (1000 * 60);

                        validRoutes.add(route);
                        break;
                    }
                }
            }
        }

        if(null!=depTimeLimit){
        Calendar lower = Calendar.getInstance();
        lower.setTimeInMillis(depTimeLimit.get(0).getTimeInMillis());
        Calendar upper = Calendar.getInstance();
        upper.setTimeInMillis(depTimeLimit.get(1).getTimeInMillis());
        long elapse = upper.getTimeInMillis() - lower.getTimeInMillis();
        lower.set(1980, Calendar.JANUARY, 1);
        upper.setTimeInMillis(lower.getTimeInMillis() + elapse);

        TrainRoute tempTrainRoute = null;
        Iterator<TrainRoute> itTime = validRoutes.iterator();
        Date dateDepTime = null;
        boolean isRightTime = false;
        while(itTime.hasNext()){
            tempTrainRoute = itTime.next();
            dateDepTime = tempTrainRoute.depTime;
            if(null!= dateDepTime){
                isRightTime = dateDepTime.getTime()>lower.getTimeInMillis()&&
                        dateDepTime.getTime()<upper.getTimeInMillis();
                if(!isRightTime){
                    itTime.remove();
                }
            }
        }
        }
        // 排序
        String stKey = null;
        switch (sortField) {
            case CODE:
                stKey = "code";
                break;
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
//        query.order(String.format("%s%s", sortType > 0 ? "" : "-", stKey));
//        query.offset(page * pageSize).limit(pageSize);
//        Iterator<TrainRoute> it = query.iterator();

        Calendar cal;
        if (epTimeLimits != null && epTimeLimits.size() == 2)
            cal = epTimeLimits.get(0);
        else
            cal = baseCal;
        return RouteIterator.getInstance(validRoutes.iterator(), (cal != null ? cal.getTime() : null));
    }
}
