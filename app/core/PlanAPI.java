package core;

import exception.ErrorCode;
import exception.TravelPiException;
import models.MorphiaFactory;
import models.morphia.geo.Locality;
import models.morphia.misc.SimpleRef;
import models.morphia.plan.*;
import models.morphia.poi.AbstractPOI;
import models.morphia.poi.Hotel;
import models.morphia.traffic.AbstractRoute;
import models.morphia.traffic.RouteIterator;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;
import utils.DataFactory;
import utils.DataFilter;
import utils.GEOUtils;
import utils.PlanUtils;

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
    public static Iterator<Plan> explore(ObjectId locId, ObjectId poiId, String sort, String tag, int minDays, int maxDays, int page, int pageSize, String sortField) throws TravelPiException {
        Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.PLAN);
        Query<Plan> query = ds.createQuery(Plan.class);

        if (locId == null && poiId == null && tag == null)
            return new ArrayList<Plan>().iterator();

        if (locId != null)
            query.field("targets.id").equal(locId);
        if (poiId != null)
            query.field("details.actv.item.id").equal(poiId);
        if (tag != null && !tag.isEmpty())
            query.field("tags").equal(tag);

        query.and(query.criteria("days").greaterThanOrEq(minDays),
                query.criteria("days").lessThanOrEq(maxDays));

        query.field("enabled").equal(Boolean.TRUE);
        query.order("manualPriority").offset(page * pageSize).limit(pageSize);

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
    public static Iterator<Plan> explore(String locId, String poiId, String sort, String tag, int minDays, int maxDays, int page, int pageSize, String sortField) throws TravelPiException {
        try {
            return explore(
                    locId != null && !locId.isEmpty() ? new ObjectId(locId) : null,
                    poiId != null && !poiId.isEmpty() ? new ObjectId(poiId) : null,
                    sort, tag != null && !tag.isEmpty() ? tag : null, minDays, maxDays, page, pageSize, sortField);
        } catch (IllegalArgumentException e) {
            throw new TravelPiException(ErrorCode.INVALID_OBJECTID, String.format("Invalid locality ID: %s, or POI ID: %s.", locId, poiId));
        }
    }

    /**
     * 获得路线规划。
     *
     * @param planId
     * @param isUgc
     * @return
     */
    public static Plan getPlan(ObjectId planId, boolean isUgc) throws TravelPiException {
        Class<? extends Plan> cls = isUgc ? UgcPlan.class : Plan.class;
        Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.PLAN);
        return ds.createQuery(cls).field("_id").equal(planId).field("enabled").equal(Boolean.TRUE).get();
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
     * 获得用户的路线规划。
     *
     * @param userId
     * @return
     */
    public static Iterator<UgcPlan> getPlanByUser(String userId, int page, int pageSize) throws TravelPiException {
        Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.PLAN);
        Query<UgcPlan> query = ds.createQuery(UgcPlan.class);
        ObjectId userOid = new ObjectId(userId);
        query.field("uid").equal(userOid).field("enabled").equal(Boolean.TRUE);
        query.offset(page * pageSize).limit(pageSize);
        query.order(String.format("%s%s", "-", "updateTime"));
        if (!query.iterator().hasNext())
            return new ArrayList<UgcPlan>().iterator();
        return query.iterator();
    }

    /**
     * 获得用户的路线规划。
     *
     * @param ugcPlanId
     * @return
     */
    public static UgcPlan getPlanById(String ugcPlanId) throws TravelPiException {
        Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.PLAN);
        Query<UgcPlan> query = ds.createQuery(UgcPlan.class);
        ObjectId ugcOPlanId = new ObjectId(ugcPlanId);
        query.field("_id").equal(ugcOPlanId).field("enabled").equal(Boolean.TRUE);
        if (!query.iterator().hasNext())
            throw new TravelPiException(ErrorCode.INVALID_OBJECTID, String.format("Invalid plan ID: %s.", ugcPlanId));
        return query.get();
    }

    /**
     * 获得分享的路线规划。
     *
     * @param planId
     * @return
     */
    public static SharePlan getSharePlanById(String planId) throws TravelPiException {
        Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.PLAN);
        Query<SharePlan> query = ds.createQuery(SharePlan.class);
        ObjectId sharePlanPlanId = new ObjectId(planId);
        query.field("_id").equal(sharePlanPlanId).field("enabled").equal(Boolean.TRUE);
        if (!query.iterator().hasNext())
            throw new TravelPiException(ErrorCode.INVALID_OBJECTID, String.format("Invalid sharePlan ID: %s.", planId));
        return query.get();
    }

    /**
     * 从模板中提取路线，进行初步规划。
     *
     * @param planId
     * @param fromLoc
     * @param backLoc
     * @return
     */
    public static UgcPlan doPlanner(String planId, String fromLoc, String backLoc, Calendar firstDate) throws TravelPiException {
        try {
            if (backLoc == null || backLoc.isEmpty())
                backLoc = fromLoc;
            return doPlanner(new ObjectId(planId), new ObjectId(fromLoc), new ObjectId(backLoc), firstDate);
        } catch (IllegalArgumentException | NoSuchFieldException | IllegalAccessException e) {
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
    public static UgcPlan doPlanner(ObjectId planId, ObjectId fromLoc, ObjectId backLoc, Calendar firstDate) throws TravelPiException, NoSuchFieldException, IllegalAccessException {
        // 获取模板路线信息
        Plan plan = getPlan(planId, false);
        if (plan == null)
            throw new TravelPiException(ErrorCode.INVALID_OBJECTID, String.format("Invalid plan ID: %s.", planId.toString()));

        // TODO 景点需要照片、描述等内容。

        if (backLoc == null)
            backLoc = fromLoc;

        if (plan.details == null || plan.details.isEmpty())
            return new UgcPlan(plan);

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
        addHotels(plan.details);

        //模板路线生成ugc路线
        UgcPlan ugcPlan = new UgcPlan(plan);
        ugcPlan.id = new ObjectId();
        return ugcPlan;
    }


    public static void pseudoOptimize(List<PlanDayEntry> entryList) {
        if (entryList.size() <= 2)
            return;

        // 第一天如果有大交通，则景点不宜太多
        PlanDayEntry entry = entryList.get(1);
        boolean trafficTail = false;
        for (PlanItem item : entry.actv) {
            if (item.type.equals("traffic")) {
                trafficTail = true;
                break;
            }
        }

        if (trafficTail && entry.actv.size() > 2) {

            // 取出最后一个景点
            for (int i = entry.actv.size() - 1; i >= 0; i--) {
                PlanItem item = entry.actv.get(i);
                if (item.type.equals("vs")) {
                    entry.actv.remove(i);
                    entryList.get(2).actv.add(0, item);
                    break;
                }
            }
        }

        int iterCnt = 0;
        boolean modified = true;
        while (iterCnt < 10 && modified) {
            iterCnt++;
            modified = false;

            // 取出景点安排最多的一天。如果比其前后两天多，则昀一下
            int[] vsCnt = new int[entryList.size()];
            for (int i = 0; i < entryList.size(); i++)
                vsCnt[i] = entryList.get(i).actv.size();

            int idxMax = -1;
            int max = 0;
            for (int i = 0; i < vsCnt.length; i++) {
                if (vsCnt[i] > max) {
                    idxMax = i;
                    max = vsCnt[i];
                }
            }

            if (idxMax >= 0) {
                // 是否比旁边的景点多2？
                boolean shift = false;
                int from = 0;
                int to = 0;
                if (idxMax == 0) {
                    if (vsCnt[0] - vsCnt[1] > 2) {
                        shift = true;
                        from = 0;
                        to = 1;
                    }
                } else if (idxMax == vsCnt.length - 1) {
                    if (vsCnt[vsCnt.length - 1] - vsCnt[vsCnt.length - 2] > 2) {
                        shift = true;
                        from = vsCnt.length - 1;
                        to = vsCnt.length - 2;
                    }
                } else if (vsCnt[idxMax] - vsCnt[idxMax - 1] > 2) {
                    shift = true;
                    from = idxMax;
                    to = idxMax - 1;
                } else if (vsCnt[idxMax] - vsCnt[idxMax + 1] > 2) {
                    shift = true;
                    from = idxMax;
                    to = idxMax + 1;
                }
                if (shift) {
                    modified = true;
                    // 取出最后一个景点
                    PlanDayEntry entryFrom = entryList.get(from);
                    PlanDayEntry entryTo = entryList.get(to);

                    for (int i = entryFrom.actv.size() - 1; i >= 0; i--) {
                        PlanItem item = entryFrom.actv.get(i);
                        if (item.type.equals("vs")) {
                            entryFrom.actv.remove(i);
                            entryTo.actv.add(0, item);
                            break;
                        }
                    }

                }
            }
        }
    }


    /**
     * 给路线规划添加酒店。
     *
     * @param entryList
     * @return
     */
    public static void addHotels(List<PlanDayEntry> entryList) {
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
            //对于路线的第一天或倒数第二天
            if (i == 0 || i == dayEntry.actv.size() - 2) {
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
            try {
                Iterator<? extends AbstractPOI> itr = PoiAPI.explore(PoiAPI.POIType.HOTEL, lastLoc.id, 0, 1);
                if (itr.hasNext()) {
                    Hotel hotel = (Hotel) itr.next();
                    PlanItem hotelItem = new PlanItem();
                    SimpleRef ref = new SimpleRef();
                    ref.id = hotel.id;
                    ref.zhName = hotel.name;
                    hotelItem.item = ref;

                    hotelItem.loc = hotel.addr.loc;
                    hotelItem.type = "hotel";
                    hotelItem.ts = dayEntry.date;

                    dayEntry.actv.add(hotelItem);
                }
            } catch (TravelPiException ignored) {
            }
        }
        return;
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

            Calendar tmpDate = Calendar.getInstance();
            tmpDate.setTimeInMillis(itemDate.getTimeInMillis());
            tmpDate.set(Calendar.HOUR_OF_DAY, 0);
            tmpDate.set(Calendar.MINUTE, 0);
            tmpDate.set(Calendar.SECOND, 0);
            tmpDate.set(Calendar.MILLISECOND, 0);
            dayEntry = new PlanDayEntry(tmpDate);

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

    /**
     * 扫描一遍，确保plan中每个item的时间戳都是正确的。
     *
     * @param plan
     * @return
     */
    private static Plan reorder(Plan plan) {
        List<PlanDayEntry> details = plan.details;
        if (details == null || details.isEmpty())
            return plan;

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
        List<Calendar> timeLimits = DataFactory.createTrafficTimeFilter(dayEntry.date, epDep);
        Calendar calLower = timeLimits.get(0);

        //对特殊的地点做过滤
        travelLoc = new ObjectId(DataFilter.localMapping(travelLoc.toString()));
        AbstractRoute route = epDep ? searchOneWayRoutes(remoteLoc, travelLoc, calLower, timeLimits, TrafficAPI.SortField.PRICE) :
                searchOneWayRoutes(travelLoc, remoteLoc, calLower, timeLimits, TrafficAPI.SortField.PRICE);

        // 如果没有交通，就去掉时间过滤
        if (route == null) {
            route = epDep ? searchOneWayRoutes(remoteLoc, travelLoc, calLower, null, TrafficAPI.SortField.PRICE) :
                    searchOneWayRoutes(travelLoc, remoteLoc, calLower, null, TrafficAPI.SortField.PRICE);
        }
        //如果没有交通，就添加转乘交通
        if (route == null) {
            //取得中转站
            Locality midLocality = GEOUtils.getInstance().getNearCap(travelLoc);
            //取得转乘交通
            List<AbstractRoute> twoRoutes = searchMoreWayRoutes(epDep, midLocality, remoteLoc, travelLoc, calLower, timeLimits);
            if (twoRoutes.isEmpty()) {
                return plan;
            } else {
                PlanItem depItem = DataFactory.createDepStop(twoRoutes.get(0));
                PlanItem arrItem = DataFactory.createArrStop(twoRoutes.get(0));
                PlanItem trafficInfo = DataFactory.createTrafficInfo(twoRoutes.get(0));

                PlanItem depItemTwo = DataFactory.createDepStop(twoRoutes.get(1));
                PlanItem arrItemTwo = DataFactory.createArrStop(twoRoutes.get(1));
                PlanItem trafficInfoTwo = DataFactory.createTrafficInfo(twoRoutes.get(1));

                if (epDep) {
                    addTrafficItem(epDep, plan, arrItemTwo);
                    addTrafficItem(epDep, plan, trafficInfoTwo);
                    addTrafficItem(epDep, plan, depItemTwo);
                    addTrafficItem(epDep, plan, arrItem);
                    addTrafficItem(epDep, plan, trafficInfo);
                    addTrafficItem(epDep, plan, depItem);
                } else {
                    addTrafficItem(epDep, plan, depItem);
                    addTrafficItem(epDep, plan, trafficInfo);
                    addTrafficItem(epDep, plan, arrItem);
                    addTrafficItem(epDep, plan, depItemTwo);
                    addTrafficItem(epDep, plan, trafficInfoTwo);
                    addTrafficItem(epDep, plan, arrItemTwo);
                }
                return plan;
            }

        }


        // 构造出发、到达和交通信息三个item
        PlanItem depItem = DataFactory.createDepStop(route);
        PlanItem arrItem = DataFactory.createArrStop(route);
        PlanItem trafficInfo = DataFactory.createTrafficInfo(route);

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

    private static AbstractRoute searchOneWayRoutes(ObjectId remoteLoc, ObjectId travelLoc, Calendar calLower, final List<Calendar> timeLimits, TrafficAPI.SortField sortField) throws TravelPiException {
        RouteIterator it = TrafficAPI.searchAirRoutes(remoteLoc, travelLoc, calLower, null, null, timeLimits, null, sortField, -1, 0, 1);
        //次推火车
        if (!it.hasNext()) {
            it = TrafficAPI.searchTrainRoutes(remoteLoc, travelLoc, "", calLower, null, null, timeLimits, null, sortField, -1, 0, 1);
        }
        return it.hasNext() ? it.next() : null;
    }


    private static List<AbstractRoute> searchMoreWayRoutes(boolean epDep, Locality midLocality, ObjectId remoteLocPara, ObjectId travelLocPara, Calendar calLower, final List<Calendar> timeLimits) throws TravelPiException {

        int MAX_ROUTES = 20;
        ObjectId remoteLoc, travelLoc;
        if (epDep) {
            remoteLoc = remoteLocPara;
            travelLoc = travelLocPara;
        } else {
            remoteLoc = travelLocPara;
            travelLoc = remoteLocPara;
        }

        //先推飞机-飞机
        RouteIterator firstAirIt = TrafficAPI.searchAirRoutes(remoteLoc, midLocality.id, calLower, null, null, timeLimits, null, TrafficAPI.SortField.TIME_COST, -1, 0, MAX_ROUTES);
        List firstAirList = DataFactory.asList(firstAirIt);
        RouteIterator nextAirIt = TrafficAPI.searchAirRoutes(midLocality.id, travelLoc, calLower, null, null, timeLimits, null, TrafficAPI.SortField.TIME_COST, -1, 0, MAX_ROUTES);
        List nextAirList = DataFactory.asList(nextAirIt);

        List<AbstractRoute> airAirRoutes = PlanUtils.getFitRoutes(firstAirList, nextAirList);
        if (!airAirRoutes.isEmpty()) {
            return airAirRoutes;
        }

        //再推飞机火车
        RouteIterator nextTrainIt = TrafficAPI.searchTrainRoutes(midLocality.id, travelLoc, "", calLower, null, null, timeLimits, null, TrafficAPI.SortField.TIME_COST, -1, 0, MAX_ROUTES);
        List nextTrainList = DataFactory.asList(nextTrainIt);

        List<AbstractRoute> airTrainRoutes = PlanUtils.getFitRoutes(firstAirList, nextTrainList);
        if (!airTrainRoutes.isEmpty()) {
            return airTrainRoutes;
        }

        List firstTrainList = null;
        //再推火车飞机
        if (!nextAirList.isEmpty()) {
            RouteIterator firstTrainIt = TrafficAPI.searchTrainRoutes(remoteLoc, midLocality.id, "", calLower, null, null, timeLimits, null, TrafficAPI.SortField.TIME_COST, -1, 0, MAX_ROUTES);
            firstTrainList = DataFactory.asList(firstTrainIt);

            List<AbstractRoute> trainAirRoutes = PlanUtils.getFitRoutes(firstTrainList, nextAirList);
            if (!airTrainRoutes.isEmpty()) {
                return airTrainRoutes;
            }
        }
        //再推火车火车
        if (firstTrainList != null && (!firstTrainList.isEmpty()) && (!nextTrainList.isEmpty())) {
            List<AbstractRoute> trainTrainRoutes = PlanUtils.getFitRoutes(firstTrainList, nextTrainList);
            if (!trainTrainRoutes.isEmpty()) {
                return trainTrainRoutes;
            }
        }
        return Collections.emptyList();
    }


    public static void saveUGCPlan(UgcPlan ugcPlan) throws TravelPiException {
        Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.PLAN);
        ds.save(ugcPlan);
    }

    public static void saveSharePlan(SharePlan sharePlan) throws TravelPiException {
        Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.PLAN);
        ds.save(sharePlan);
    }

    public static void updateUGCPlanByFiled(ObjectId ugcPlanId, String filed, String filedValue) throws TravelPiException, NoSuchFieldException, IllegalAccessException {
        Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.PLAN);
        Query<UgcPlan> query = ds.createQuery(UgcPlan.class);
        query.field("_id").equal(ugcPlanId);
        Iterator<UgcPlan> it = query.iterator();
        if (!it.hasNext())
            throw new TravelPiException(ErrorCode.INVALID_OBJECTID, String.format("Invalid ugcPlan ID: %s.", ugcPlanId.toString()));

        UpdateOperations<UgcPlan> ops = ds.createUpdateOperations(UgcPlan.class);
        ops.set(filed, new ObjectId(filedValue));
        ops.set("enabled", true);
        ops.set("updateTime", (new Date()).getTime());
        ds.update(query, ops, true);
    }

    public static void deleteUGCPlan(String ugcPlanId) throws TravelPiException {
        Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.PLAN);
        Query<UgcPlan> query = ds.createQuery(UgcPlan.class);
        ObjectId ugcOPlanId = new ObjectId(ugcPlanId);
        query.field("_id").equal(ugcOPlanId);
        ds.delete(query);
    }

}
