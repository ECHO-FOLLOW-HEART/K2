package utils;

import aizou.core.TrafficAPI;
import exception.TravelPiException;
import models.misc.SimpleRef;
import models.plan.Plan;
import models.plan.PlanDayEntry;
import models.plan.PlanItem;
import models.traffic.AbstractRoute;
import models.traffic.AirRoute;
import models.traffic.RouteIterator;
import models.traffic.TrainRoute;
import org.bson.types.ObjectId;
import play.mvc.Http;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

/**
 * Created by topy on 2014/9/27.
 */
public class AdapterUtils {

    public static int VERSION_120 = 10200;

    public static boolean isVerUnder120(Http.Request req) {
        if (req.getQueryString("v") != null) {
            String[] verStr = req.getQueryString("v").split("\\.");
            int ver = Integer.valueOf(verStr[0]) * 10000 + Integer.valueOf(verStr[1]) * 100 + Integer.valueOf(verStr[2]);
            if (ver <= VERSION_120) {
                return true;
            }
        }
        return false;
    }

    public static Plan addTelomere_120(boolean epDep, Plan plan, ObjectId remoteLoc) throws TravelPiException {
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
        if (!it.hasNext()) {
            it = (epDep ?
                    TrafficAPI.searchTrainRoutes(remoteLoc, travelLoc, "", calLower, null, null, timeLimits, null, TrafficAPI.SortField.PRICE, -1, 0, 1)
                    :
                    TrafficAPI.searchTrainRoutes(travelLoc, remoteLoc, "", calLower, null, null, timeLimits, null, TrafficAPI.SortField.ARR_TIME, 1, 0, 1));
        }
        if (!it.hasNext()) {

            return plan;
        }

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
        ref.id = route.getId();
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
            plan.setDetails(new ArrayList<PlanDayEntry>());

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
}
