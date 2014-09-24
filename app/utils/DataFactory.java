package utils;

import models.morphia.geo.Locality;
import models.morphia.misc.SimpleRef;
import models.morphia.plan.PlanItem;
import models.morphia.traffic.AbstractRoute;
import models.morphia.traffic.AirRoute;
import models.morphia.traffic.TrainRoute;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by topy on 2014/9/19.
 */
public class DataFactory {

    public static List<?> asList(Iterator<?> it) {
        List l = new ArrayList();
        for (; it.hasNext(); ) {
            Object element = (Object) it.next();
            l.add(element);
        }
        return l;

    }

    public static PlanItem createLocality(Locality locality) {
        if (null == locality) {
            return null;
        }
        PlanItem nearCap = new PlanItem();
        SimpleRef ref = new SimpleRef();
        ref.id = locality.id;
        ref.zhName = locality.zhName;
        nearCap.item = ref;
        nearCap.type = "loc";
        SimpleRef loc = new SimpleRef();
        loc.id = locality.id;
        loc.zhName = locality.zhName;
        nearCap.loc = loc;
        return nearCap;
    }

    public static PlanItem createDepStop(AbstractRoute route) {
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

        return depItem;
    }

    public static PlanItem createArrStop(AbstractRoute route) {
        String subType;
        if (route instanceof AirRoute)
            subType = "airport";
        else if (route instanceof TrainRoute)
            subType = "trainStation";
        else
            subType = "";

        PlanItem arrItem = new PlanItem();
        arrItem.item = route.arrStop;
        arrItem.loc = route.arrLoc;
        arrItem.ts = route.arrTime;
        arrItem.type = "traffic";
        arrItem.subType = subType;

        return arrItem;
    }

    public static PlanItem createTrafficInfo(AbstractRoute route) {
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

        return trafficInfo;
    }
}
