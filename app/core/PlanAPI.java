package core;

import exception.ErrorCode;
import exception.TravelPiException;
import models.MorphiaFactory;
import models.morphia.misc.SimpleRef;
import models.morphia.plan.Plan;
import models.morphia.plan.PlanDayEntry;
import models.morphia.plan.PlanItem;
import models.morphia.poi.AbstractPOI;
import models.morphia.poi.Hotel;
import models.morphia.traffic.AbstractRoute;
import models.morphia.traffic.AirRoute;
import models.morphia.traffic.RouteIterator;
import models.morphia.traffic.TrainRoute;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;

import java.util.*;

/**
 * 路线规划相关API。
 *
 * @author Zephyre
 */
public class PlanAPI {

    /**
     * 发现路线
     *
     * @param locId
     * @param poiId
     * @param sort
     * @param tag
     * @param page
     * @param pageSize
     * @param sortField @return
     * @throws TravelPiException
     */
    public static Iterator<Plan> explore(ObjectId locId, ObjectId poiId, String sort, String tag, int page, int pageSize, String sortField) throws TravelPiException {
        Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.PLAN);
        Query<Plan> query = ds.createQuery(Plan.class);

        if (locId != null)
            query.field("target.id").equal(locId);

        if (poiId != null)
            query.field("details.actv.item.id").equal(poiId);

        if (tag != null && !tag.isEmpty())
            query.field("tags").equal(tag);

        query.order("-ratings.viewCnt").offset(page * pageSize).limit(pageSize);

        return query.iterator();
    }

    /**
     * 发现路线
     *
     * @param locId
     * @param poiId
     * @param sort
     * @param tag
     * @param page
     * @param pageSize
     * @param sortField @return
     * @throws TravelPiException
     */
    public static Iterator<Plan> explore(String locId, String poiId, String sort, String tag, int page, int pageSize, String sortField) throws TravelPiException {
        try {
            return explore(
                    locId != null && !locId.isEmpty() ? new ObjectId(locId) : null,
                    poiId != null && !poiId.isEmpty() ? new ObjectId(poiId) : null,
                    sort, tag, page, pageSize, sortField);
        } catch (IllegalArgumentException e) {
            throw new TravelPiException(ErrorCode.INVALID_OBJECTID, String.format("Invalid locality ID: %s, or POI ID: %s.", locId, poiId));
        }
    }

    /**
     * 获得路线规划。
     *
     * @param planId
     * @param ugc
     * @return
     */
    public static Plan getPlan(ObjectId planId, boolean ugc) throws TravelPiException {
        Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.PLAN);
        return ds.createQuery(Plan.class).field("_id").equal(planId).get();
    }

    /**
     * 获得路线规划。
     *
     * @param planId
     * @param ugc
     * @return
     */
    public static Plan getPlan(String planId, boolean ugc) throws TravelPiException {
        try {
            return getPlan(new ObjectId(planId), ugc);
        } catch (IllegalArgumentException e) {
            throw new TravelPiException(ErrorCode.INVALID_OBJECTID, String.format("Invalid plan ID: %s.", planId));
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
    public static Plan doPlanner(String planId, String fromLoc, String backLoc, Calendar firstDate) throws TravelPiException {
        try {
            if (backLoc == null || backLoc.isEmpty())
                backLoc = fromLoc;
            return doPlanner(new ObjectId(planId), new ObjectId(fromLoc), new ObjectId(backLoc), firstDate);
        } catch (IllegalArgumentException e) {
            throw new TravelPiException(ErrorCode.INVALID_OBJECTID, String.format("Invalid plan ID: %s.", planId));
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
    public static Plan doPlanner(ObjectId planId, ObjectId fromLoc, ObjectId backLoc, Calendar firstDate) throws TravelPiException {
        Plan plan = getPlan(planId, false);
        plan.id = new ObjectId();

        // TODO 景点需要照片、描述等内容。

        if (backLoc == null)
            backLoc = fromLoc;

        if (plan.details == null || plan.details.isEmpty())
            return plan;

        // 进行日期标注
        int idx = 0;
        for (PlanDayEntry dayEntry : plan.details) {
            Calendar cal = Calendar.getInstance(firstDate.getTimeZone());
            cal.set(0, Calendar.JANUARY, 0, 0, 0, 0);
            for (int field : new int[]{Calendar.YEAR, Calendar.MONTH, Calendar.DAY_OF_MONTH})
                cal.set(field, firstDate.get(field));
            cal.add(Calendar.DAY_OF_YEAR, idx);
            dayEntry.date = cal.getTime();
            idx++;
        }

        // 加入大交通
        addTelomere(true, plan, fromLoc);
        addTelomere(false, plan, backLoc);

        // 加入酒店
        addHotels(plan);

        return plan;
    }


    /**
     * 给路线规划添加酒店。
     *
     * @param plan
     * @return
     */
    private static Plan addHotels(Plan plan) {
        int cnt = plan.details.size();
        for (int i = 0; i < cnt - 1; i++) {
            PlanDayEntry dayEntry = plan.details.get(i);
            // 查找activities中是否已经存在酒店
            boolean hasHotel = false;
            for (PlanItem item : dayEntry.actv) {
                if (item.type.equals("type")) {
                    hasHotel = true;
                    break;
                }
            }
            if (hasHotel)
                continue;

            // 需要添加酒店
            PlanItem lastItem = dayEntry.actv.get(dayEntry.actv.size() - 1);
            try {
                Iterator<? extends AbstractPOI> itr = PoiAPI.explore(PoiAPI.POIType.HOTEL, lastItem.loc.id, 0, 5);
                if (itr.hasNext()) {
                    Hotel hotel = (Hotel) itr.next();
                    PlanItem hotelItem = new PlanItem();
                    SimpleRef ref = new SimpleRef();
                    ref.id = hotel.id;
                    ref.zhName = hotel.name;
                    hotelItem.item = ref;

                    hotelItem.loc = hotel.addr.loc;
                    hotelItem.type = "hotel";

                    Calendar cal = Calendar.getInstance();
                    cal.setTime(dayEntry.date);
                    cal.set(Calendar.HOUR_OF_DAY, 20);
                    cal.set(Calendar.MINUTE, 0);
                    cal.set(Calendar.SECOND, 0);
                    cal.set(Calendar.MILLISECOND, 0);
                    hotelItem.ts = cal.getTime();

                    dayEntry.actv.add(hotelItem);
                }
            } catch (TravelPiException ignored) {
            }
        }
        return plan;
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
        if (plan.details == null)
            plan.details = new ArrayList<>();

        PlanDayEntry dayEntry = null;
        Calendar itemDate = Calendar.getInstance();
        itemDate.setTime(item.ts);

        if (!plan.details.isEmpty()) {
            int epIdx = (epDep ? 0 : plan.details.size() - 1);
            dayEntry = plan.details.get(epIdx);
            Calendar epDate = Calendar.getInstance();
            epDate.setTime(dayEntry.date);

            if (epDate.get(Calendar.DAY_OF_YEAR) != itemDate.get(Calendar.DAY_OF_YEAR))
                dayEntry = null;
        }

        if (dayEntry == null) {
            dayEntry = new PlanDayEntry();
            Calendar tmpDate = Calendar.getInstance();
            tmpDate.setTimeInMillis(itemDate.getTimeInMillis());
            tmpDate.set(Calendar.HOUR_OF_DAY, 0);
            tmpDate.set(Calendar.MINUTE, 0);
            tmpDate.set(Calendar.SECOND, 0);
            tmpDate.set(Calendar.MILLISECOND, 0);
            dayEntry.date = tmpDate.getTime();
            dayEntry.actv = new ArrayList<>();

            if (epDep)
                plan.details.add(0, dayEntry);
            else
                plan.details.add(dayEntry);
        }

        if (epDep)
            dayEntry.actv.add(0, item);
        else
            dayEntry.actv.add(item);

        return plan;
    }

    private static Plan addTelomere(boolean epDep, Plan plan, ObjectId remoteLoc) throws TravelPiException {
        List<PlanDayEntry> details = plan.details;
        if (details == null || details.isEmpty())
            return plan;

        int epIdx = (epDep ? 0 : plan.details.size() - 1);
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
        List<Calendar> timeLimits;
        Calendar calLower, calUpper;

        if (epDep) {
            calLower = Calendar.getInstance();
            calLower.setTime(dayEntry.date);
            // 允许的日期从前一天17:00到第二天12:00
            calLower.add(Calendar.DAY_OF_YEAR, -1);
            calLower.set(Calendar.HOUR_OF_DAY, 17);
            calLower.set(Calendar.MINUTE, 0);
            calLower.set(Calendar.SECOND, 0);
            calLower.set(Calendar.MILLISECOND, 0);
            calUpper = Calendar.getInstance();
            calUpper.setTime(dayEntry.date);
            calUpper.set(Calendar.HOUR_OF_DAY, 12);
            calUpper.set(Calendar.MINUTE, 0);
            calUpper.set(Calendar.SECOND, 0);
            calUpper.set(Calendar.MILLISECOND, 0);
        } else {
            calLower = Calendar.getInstance();
            calLower.setTime(dayEntry.date);
            // 允许的日期从前一天12:00到第二天12:00
            calLower.set(Calendar.HOUR_OF_DAY, 12);
            calLower.set(Calendar.MINUTE, 0);
            calLower.set(Calendar.SECOND, 0);
            calLower.set(Calendar.MILLISECOND, 0);
            calUpper = Calendar.getInstance();
            calUpper.setTime(dayEntry.date);
            calUpper.add(Calendar.DAY_OF_YEAR, 1);
            calUpper.set(Calendar.HOUR_OF_DAY, 12);
            calUpper.set(Calendar.MINUTE, 0);
            calUpper.set(Calendar.SECOND, 0);
            calUpper.set(Calendar.MILLISECOND, 0);
        }
        timeLimits = Arrays.asList(calLower, calUpper);

        RouteIterator it = (epDep ?
                TrafficAPI.searchAirRoutes(remoteLoc, travelLoc, calLower, null, null, timeLimits, null, TrafficAPI.SortField.PRICE, -1, 0, 1)
                :
                TrafficAPI.searchAirRoutes(travelLoc, remoteLoc, calLower, null, null, timeLimits, null, TrafficAPI.SortField.PRICE, -1, 0, 1));
        if (!it.hasNext())
            return plan;

        AbstractRoute route = it.next();
        Calendar depTime = Calendar.getInstance();
        depTime.setTime(route.depTime);
        Calendar firstDay = Calendar.getInstance();
        firstDay.setTime(dayEntry.date);

        // 构造出发、到达和交通信息三个item

        String subType;
        if (route instanceof AirRoute)
            subType = "airport";
        else if (route instanceof TrainRoute)
            subType = "trainStation";
        else
            subType = "";

        PlanItem depItem = new PlanItem();
        depItem.item = route.depStop;
        depItem.loc = route.depLoc;
        depItem.ts = route.depTime;
        depItem.type = "traffic";
        depItem.subType = subType;

        PlanItem arrItem = new PlanItem();
        arrItem.item = route.arrStop;
        arrItem.loc = route.arrLoc;
        arrItem.ts = route.arrTime;
        arrItem.type = "traffic";
        arrItem.subType = subType;

        PlanItem trafficInfo = new PlanItem();
        SimpleRef ref = new SimpleRef();
        ref.id = route.id;
        ref.zhName = route.code;
        trafficInfo.item = ref;
        trafficInfo.ts = route.depTime;
        trafficInfo.extra = route;
        trafficInfo.type = "traffic";
        if (route instanceof AirRoute)
            trafficInfo.subType = "airRoute";
        else if (route instanceof TrainRoute)
            trafficInfo.subType = "trainRoute";
        else
            trafficInfo.subType = "";

        if (route instanceof AirRoute)
            depItem.subType = "airport";
        else if (route instanceof TrainRoute)
            depItem.subType = "trainStation";
        else
            depItem.subType = "";

        if (epDep) {
            addTrafficItem(true, plan, arrItem);
            addTrafficItem(true, plan, trafficInfo);
            addTrafficItem(true, plan, depItem);
        } else {
            addTrafficItem(false, plan, depItem);
            addTrafficItem(false, plan, trafficInfo);
            addTrafficItem(false, plan, arrItem);
        }

        return plan;
    }
}