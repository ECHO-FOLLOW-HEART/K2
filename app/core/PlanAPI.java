package core;

import exception.ErrorCode;
import exception.TravelPiException;
import models.MorphiaFactory;
import models.morphia.plan.Plan;
import models.morphia.plan.PlanDayEntry;
import models.morphia.plan.PlanItem;
import models.morphia.traffic.AirRoute;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

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
            cal.setTimeInMillis(0);
            for (int field : new int[]{Calendar.YEAR, Calendar.MONTH, Calendar.DAY_OF_MONTH})
                cal.set(field, firstDate.get(field));
            cal.add(Calendar.DAY_OF_YEAR, idx);
            dayEntry.date = cal.getTime();
            idx++;
        }

        // 加入大交通
        addTelomere(plan, fromLoc, backLoc);

        return plan;
    }

    private static void addTelomere(Plan plan, ObjectId fromLoc, ObjectId backLoc) throws TravelPiException {
        List<PlanDayEntry> details = plan.details;
        if (details==null||details.isEmpty())
            return;
        PlanDayEntry dayEntry = details.get(0);
        if(dayEntry==null || dayEntry.actv==null||dayEntry.actv.isEmpty())
            return;
        // 取得第一项活动
        PlanItem actv = dayEntry.actv.get(0);
        if (dayEntry.date==null)
            return;
        Calendar cal1=Calendar.getInstance();
        Calendar cal2=Calendar.getInstance();
        cal1.setTime(dayEntry.date);
        // 允许的日期从前一天17:00到第二天12:00
        cal1.add(Calendar.DAY_OF_YEAR,-1);
        cal1.set(Calendar.HOUR_OF_DAY, 17);
        cal1.set(Calendar.MINUTE, 0);
        cal1.set(Calendar.SECOND,0);
        cal1.set(Calendar.MILLISECOND,0);
        cal2.setTimeInMillis(cal1.getTimeInMillis());
        cal2.add(Calendar.DAY_OF_YEAR,1);
        cal2.set(Calendar.HOUR_OF_DAY, 12);
        List<Calendar> timeLimits=new ArrayList<>();
        timeLimits.add(cal1);
        timeLimits.add(cal2);

        ObjectId destination = actv.loc.id;
        Iterator<AirRoute> it = TrafficAPI.searchAirRoutes(fromLoc, destination, null, null, null, timeLimits, TrafficAPI.SortField.PRICE, -1, 0, 1, null
        );
        if (!it.hasNext())
            return;

        AirRoute route = it.next();
//        PlanItem dep = route.
    }
}
