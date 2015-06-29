package aizou.core;

import exception.AizouException;
import exception.ErrorCode;
import com.lvxingpai.k2.core.MorphiaFactory;
import models.misc.SimpleRef;
import models.plan.Plan;
import models.plan.PlanDayEntry;
import models.plan.PlanItem;
import models.plan.UgcPlan;
import models.poi.AbstractPOI;
import models.poi.Hotel;
import models.poi.Restaurant;
import models.traffic.AbstractRoute;
import models.traffic.RouteIterator;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;
import play.mvc.Http;
import utils.DataFactory;
import utils.DataFilter;
import utils.PlanUtils;

import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

/**
 * Created by topy on 2014/12/5.
 */
public class WebPlanAPI {

    public static final String TRAFFIC_FLAG_NULL = "none";
    public static final String TRAFFIC_FLAG_TRAIN = "trainRoute";
    public static final String TRAFFIC_FLAG_AIR = "airRoute";
    public static final String HOTEL_FLAG_NULL = "none";
    public static final String HOTEL_FLAG_STAR = "star";
    public static final String HOTEL_FLAG_BUDGET = "budget";
    public static final String HOTEL_FLAG_YOUTH = "youth";
    public static final String HOTEL_FLAG_FOLK = "folk";
    public static final String HOTEL_FLAG_YOUTH_AND_FOLK = "youthandfolk";
    //酒店类型：空-类型不限 1-星级酒店 2-经济型酒店 3-青年旅社 4-民俗酒店 5-青年旅社和民俗酒店

    public static final String REST_FLAG_NULL = "none";
    public static final String REST_FLAG_REPUTATION = "reputation";
    public static final String REST_FLAG_SPECIAL = "special";

    /**
     * 从模板中提取路线，进行初步规划。
     *
     * @param planId
     * @param fromLoc
     * @param backLoc
     * @return
     */
    public static UgcPlan doPlanner(String planId, String fromLoc, String backLoc, Calendar firstDate, Http.Request req,
                                    String trafficFlag, String hotelFlag, String restaurantFlag) throws AizouException {
        try {
            if (backLoc == null || backLoc.isEmpty())
                backLoc = fromLoc;
            return doPlanner(new ObjectId(planId), new ObjectId(fromLoc), new ObjectId(backLoc), firstDate, req,
                    trafficFlag, hotelFlag, restaurantFlag);
        } catch (IllegalArgumentException | NoSuchFieldException | IllegalAccessException e) {
            throw new AizouException(ErrorCode.INVALID_ARGUMENT, String.format("Invalid plan ID: %s.", planId));
        }
    }

    /**
     * 从模板中提取路线，进行初步规划。
     *
     * @param planId
     * @param fromLoc
     * @param backLoc
     * @return
     */
    public static UgcPlan doPlanner(ObjectId planId, ObjectId fromLoc, ObjectId backLoc, Calendar firstDate, Http.Request req,
                                    String trafficFlag, String hotelFlag, String restaurantFlag) throws AizouException, NoSuchFieldException, IllegalAccessException {
        // 获取模板路线信息
        Plan plan = getPlan(planId, false);
        if (plan == null)
            throw new AizouException(ErrorCode.INVALID_ARGUMENT, String.format("Invalid plan ID: %s.", planId.toString()));

        if (backLoc == null)
            backLoc = fromLoc;

        if (plan.getDetails() == null || plan.getDetails().isEmpty())
            return new UgcPlan(plan);

        // 进行日期标注
        int idx = 0;
        for (PlanDayEntry dayEntry : plan.getDetails()) {
            Calendar cal = Calendar.getInstance(firstDate.getTimeZone());
            cal.set(0, Calendar.JANUARY, 0, 0, 0, 0);
            for (int field : new int[]{Calendar.YEAR, Calendar.MONTH, Calendar.DAY_OF_MONTH})
                cal.set(field, firstDate.get(field));
            cal.add(Calendar.DAY_OF_YEAR, idx);
            dayEntry.date = cal.getTime();
            idx++;
        }

        if (!trafficFlag.equals(TRAFFIC_FLAG_NULL)) {
            // 加入大交通
            addTelomere(true, plan, fromLoc, trafficFlag);
            addTelomere(false, plan, backLoc, trafficFlag);
        }

        if (!hotelFlag.equals(HOTEL_FLAG_NULL) || !restaurantFlag.equals(REST_FLAG_NULL))
            // 加入酒店
            addHotelsAndRestaurants(plan.getDetails(), hotelFlag, restaurantFlag);


        //模板路线生成ugc路线
        return new UgcPlan(plan);
    }

    /**
     * 获得路线规划。
     *
     * @param planId
     * @param isUgc
     * @return
     */
    public static Plan getPlan(ObjectId planId, boolean isUgc) throws AizouException {
        Class<? extends Plan> cls = isUgc ? UgcPlan.class : Plan.class;
        Datastore ds = MorphiaFactory.datastore();
        return ds.createQuery(cls).field("_id").equal(planId).field("enabled").equal(Boolean.TRUE).get();
    }

    private static Plan addTelomere(boolean epDep, Plan plan, ObjectId remoteLoc, String trafficFlag) throws AizouException {
        List<PlanDayEntry> details = plan.getDetails();
        if (details == null || details.isEmpty())
            return plan;

        int epIdx = (epDep ? 0 : plan.getDetails().size() - 1);
        // 正式旅行的第一天或最后一天
        PlanDayEntry dayEntry = details.get(epIdx);
        if (dayEntry == null || dayEntry.actv == null || dayEntry.actv.isEmpty())
            return plan;
        // 取得第一项活动
        PlanItem actv = dayEntry.actv.get(0);
        if (dayEntry.date == null)
            return plan;
        ObjectId travelLoc = actv.loc.id;

        // 大交通筛选
        List<Calendar> timeLimits = DataFactory.createTrafficTimeFilter(dayEntry.date, epDep);
        Calendar calLower = timeLimits.get(0);

        //对特殊的地点做过滤
        travelLoc = new ObjectId(DataFilter.localMapping(travelLoc.toString()));
        AbstractRoute route = epDep ? searchOneWayRoutes(remoteLoc, travelLoc, calLower, timeLimits, TrafficAPI.SortField.PRICE, trafficFlag) :
                searchOneWayRoutes(travelLoc, remoteLoc, calLower, timeLimits, TrafficAPI.SortField.PRICE, trafficFlag);

        // 如果没有交通，就去掉时间过滤
        if (route == null) {
            route = epDep ? searchOneWayRoutes(remoteLoc, travelLoc, calLower, null, TrafficAPI.SortField.PRICE, trafficFlag) :
                    searchOneWayRoutes(travelLoc, remoteLoc, calLower, null, TrafficAPI.SortField.PRICE, trafficFlag);
        }

        if (route != null) {
            // 构造出发、到达和交通信息三个item
            PlanItem depItem = DataFactory.createDepStop(route);
            PlanItem arrItem = DataFactory.createArrStop(route);
            PlanItem trafficInfo = DataFactory.createTrafficInfo(route);
            depItem.transfer = epDep ? PlanUtils.NO_TRANS_FROM : PlanUtils.NO_TRANS_BACK;
            arrItem.transfer = epDep ? PlanUtils.NO_TRANS_FROM : PlanUtils.NO_TRANS_BACK;
            trafficInfo.transfer = epDep ? PlanUtils.NO_TRANS_FROM : PlanUtils.NO_TRANS_BACK;

            if (epDep) {
                addTrafficItem(true, plan, arrItem);
                addTrafficItem(true, plan, trafficInfo);
                addTrafficItem(true, plan, depItem);
            } else {
                addTrafficItem(false, plan, depItem);
                addTrafficItem(false, plan, trafficInfo);
                addTrafficItem(false, plan, arrItem);
            }
        }

        return plan;
    }

    private static AbstractRoute searchOneWayRoutes(ObjectId remoteLoc, ObjectId travelLoc, Calendar calLower,
                                                    final List<Calendar> timeLimits, TrafficAPI.SortField sortField, String trafficFlag) throws AizouException {
        RouteIterator it;
        if (trafficFlag.equals(TRAFFIC_FLAG_AIR))
            it = TrafficAPI.searchAirRoutes(remoteLoc, travelLoc, calLower, null, null, timeLimits, null, sortField, -1, 0, 1);
        else if (trafficFlag.equals(TRAFFIC_FLAG_TRAIN))
            it = TrafficAPI.searchTrainRoutes(remoteLoc, travelLoc, "", calLower, null, null, timeLimits, null, sortField, -1, 0, 1);
        else
            return null;
        return it.hasNext() ? it.next() : null;
    }

    /**
     * 将交通信息插入到路线规划中。
     *
     * @param epDep 出发地。
     * @param plan
     * @param item
     * @return
     */
    private static Plan addTrafficItem(boolean epDep, Plan plan, PlanItem item) {
        if (plan.getDetails() == null)
            return new Plan();

        PlanDayEntry dayEntry = null;
        Calendar itemDate = Calendar.getInstance();
        itemDate.setTime(item.ts);

        if (!plan.getDetails().isEmpty()) {
            int epIdx = (epDep ? 0 : plan.getDetails().size() - 1);
            dayEntry = plan.getDetails().get(epIdx);
            Calendar epDate = Calendar.getInstance();
            epDate.setTime(dayEntry.date);

            if (epDate.get(Calendar.DAY_OF_YEAR) != itemDate.get(Calendar.DAY_OF_YEAR))
                dayEntry = null;
        }

        if (dayEntry == null) {

            Calendar tmpDate = Calendar.getInstance();
            tmpDate.setTimeInMillis(itemDate.getTimeInMillis());
            tmpDate.set(Calendar.HOUR_OF_DAY, 0);
            tmpDate.set(Calendar.MINUTE, 0);
            tmpDate.set(Calendar.SECOND, 0);
            tmpDate.set(Calendar.MILLISECOND, 0);
            dayEntry = new PlanDayEntry(tmpDate);

            if (epDep)
                plan.getDetails().add(0, dayEntry);
            else
                plan.getDetails().add(dayEntry);
        }

        if (epDep)
            dayEntry.actv.add(0, item);
        else
            dayEntry.actv.add(item);

        return plan;
    }

    /**
     * 给路线规划添加酒店。
     *
     * @param entryList
     * @return
     */
    public static void addHotelsAndRestaurants(List<PlanDayEntry> entryList, String hotelFlag, String restaurantFlag) {
        if (entryList == null)
            return;

        int cnt = entryList.size();
        SimpleRef lastLoc = null;
        for (int i = 0; i < cnt - 1; i++) {
            PlanDayEntry dayEntry = entryList.get(i);
            if (dayEntry == null || dayEntry.actv == null)
                continue;

            // 查找activities中是否已经存在酒店
            boolean hasHotel = false;
            for (PlanItem item : dayEntry.actv) {
                if (item.type != null && item.type.equals("hotel")) {
                    hasHotel = true;
                    break;
                }
            }
            //删除跨天交通间的酒店
            //对于路线的第一天或倒数第二天,且路线大于一天
            if ((i == 0 || i == dayEntry.actv.size() - 2) && (dayEntry.actv.size() > 1)) {
                List<PlanItem> todayActs = dayEntry.actv;
                //如果今天最后一项是酒店并且酒店上面是交通
                if (todayActs.get(todayActs.size() - 1).type.equals("hotel")
                        && todayActs.get(todayActs.size() - 2).type.equals("traffic")) {
                    //取得明天的活动
                    List<PlanItem> nextDayActs = entryList.get(i + 1).actv;
                    //取得明天第一项活动的类型
                    String nextDayFirstActType = "";
                    if (null != nextDayActs && nextDayActs.size() > 0)
                        nextDayFirstActType = nextDayActs.get(0).type;
                    // 如果明天一早还在交通上
                    if (nextDayFirstActType != null && nextDayFirstActType.equals("traffic")) {
                        todayActs.remove(todayActs.size() - 1);
                    }
                }
            }

            if (hasHotel)
                continue;

            if (!dayEntry.actv.isEmpty()) {
                PlanItem lastItem = dayEntry.actv.get(dayEntry.actv.size() - 1);
                // 如果最后一条记录为trainRoute活着airRoute，说明游客还在路上，不需要添加酒店。
                if (lastItem.subType != null && (lastItem.subType.equals("trainRoute") || lastItem.subType.equals("airRoute")))
                    continue;
                lastLoc = lastItem.loc;
            }
            if (lastLoc == null)
                continue;

            // 需要添加酒店
            Iterator<? extends AbstractPOI> itHotel = null;
            Iterator<? extends AbstractPOI> itRest = null;
            try {
                itHotel = WebPlanAPI.exploreHotelByType(lastLoc.id, hotelFlag, 0, 1);
                itRest = WebPlanAPI.exploreRestaurantByType(lastLoc.id, restaurantFlag, 0, 1);

                if (itRest.hasNext()) {
                    Restaurant rest = (Restaurant) itRest.next();
                    PlanItem hotelItem = new PlanItem();
                    SimpleRef ref = new SimpleRef();
                    ref.id = rest.getId();
                    ref.zhName = rest.name;
                    hotelItem.item = ref;

                    hotelItem.loc = rest.addr.loc;
                    hotelItem.type = "hotel";
                    hotelItem.ts = dayEntry.date;

                    dayEntry.actv.add(hotelItem);
                }

                if (itHotel.hasNext()) {
                    Hotel hotel = (Hotel) itHotel.next();
                    PlanItem hotelItem = new PlanItem();
                    SimpleRef ref = new SimpleRef();
                    ref.id = hotel.getId();
                    ref.zhName = hotel.name;
                    hotelItem.item = ref;

                    hotelItem.loc = hotel.addr.loc;
                    hotelItem.type = "hotel";
                    hotelItem.ts = dayEntry.date;

                    dayEntry.actv.add(hotelItem);
                }
            } catch (AizouException e) {
                e.printStackTrace();
            }
        }
        return;
    }

    /**
     * 发现POI。
     *
     * @param page
     * @param pageSize
     * @return
     */
    public static Iterator<? extends AbstractPOI> exploreHotelByType(ObjectId locId,
                                                                     String hotelType, int page, int pageSize) throws AizouException {
        Datastore ds = MorphiaFactory.datastore();
        Query<? extends AbstractPOI> query = ds.createQuery(Hotel.class);
        query.field("targets").equal(locId);
        Double hotelTypeValue = null;
        switch (hotelType) {
            case HOTEL_FLAG_STAR:
                hotelTypeValue = 1.0d;
                break;
            case HOTEL_FLAG_BUDGET:
                hotelTypeValue = 2.0d;
                break;
            case HOTEL_FLAG_YOUTH:
                hotelTypeValue = 3.0d;
                break;
            case HOTEL_FLAG_FOLK:
                hotelTypeValue = 4.0d;
                break;
            case HOTEL_FLAG_YOUTH_AND_FOLK:
                query.or(query.criteria("type").equal(3.0), query.criteria("type").equal(4.0));
                break;
        }
        if (hotelTypeValue != null)
            query.field("type").equal(hotelTypeValue);
        return query.offset(page * pageSize).limit(pageSize).order(String.format("-%s", AbstractPOI.fnHotness))
                .iterator();
    }

    /**
     * 发现POI。
     *
     * @param page
     * @param pageSize
     * @return
     */
    public static Iterator<? extends AbstractPOI> exploreRestaurantByType(ObjectId locId,
                                                                          String restaurantType, int page, int pageSize) throws AizouException {
        Datastore ds = MorphiaFactory.datastore();
        Query<? extends AbstractPOI> query = ds.createQuery(Restaurant.class);
        query.field("targets").equal(locId);
        switch (restaurantType) {
            case REST_FLAG_REPUTATION:
                query.order(String.format("-%s", "ratings.score"));
                break;
            case REST_FLAG_SPECIAL:
                query.order(String.format("-%s", "ratings.ranking"));
                break;
        }
        query.offset(page * pageSize).limit(pageSize);
        return query.iterator();
    }
}
