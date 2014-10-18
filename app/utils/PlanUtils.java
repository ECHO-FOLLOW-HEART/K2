package utils;

import com.fasterxml.jackson.databind.JsonNode;
import models.morphia.traffic.AbstractRoute;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

/**
 * Created by topy on 2014/9/25.
 */
public class PlanUtils {

    public static String TRANS_FROM_FIRST = "from.2.1";
    public static String TRANS_FROM_NEXT = "from.2.2";
    public static String TRANS_BACK_FIRST = "back.2.1";
    public static String TRANS_BACK_NEXT = "back.2.2";

    public static String NO_TRANS_FROM = "no.from";
    public static String NO_TRANS_BACK = "no.back";


    public static boolean matchRoutes(AbstractRoute firstRoute, AbstractRoute secondRoute) {
        Calendar depTime = Calendar.getInstance();
        depTime.setTime(secondRoute.depTime);
        Calendar c1 = Calendar.getInstance();
        c1.setTime(firstRoute.arrTime);
        c1.add(Calendar.HOUR, 1);

        Calendar c2 = Calendar.getInstance();
        c2.setTime(firstRoute.arrTime);
        c2.add(Calendar.HOUR, 5);
        //相隔时间不能小于一小时
        if (c1.after(depTime)) {
            return false;
            //相隔时间不能大于五小时
        } else if (c2.before(depTime)) {
            return false;
        }
        return true;
    }

    public static List<AbstractRoute> getFitRoutes(List<AbstractRoute> first, List<AbstractRoute> second) {
        if (first.isEmpty() || second.isEmpty()) {
            return Collections.emptyList();
        }
        AbstractRoute firstRoute = null;
        AbstractRoute secondRoute = null;
        boolean findFlag = false;
        for (AbstractRoute fRoute : first) {
            for (AbstractRoute sRoute : second) {
                if (findFlag)
                    break;
                if (matchRoutes(fRoute, sRoute)) {
                    firstRoute = fRoute;
                    secondRoute = sRoute;
                    findFlag = true;
                    break;
                }
            }
        }
        if (findFlag) {
            List<AbstractRoute> result = new ArrayList<>();
            result.add(firstRoute);
            result.add(secondRoute);

            return result;
        }
        return Collections.emptyList();
    }

    public static boolean isTransferTraffic(JsonNode item) {

        if (item.get("transfer") != null && !item.get("transfer").asText().startsWith("no")) {
            return true;
        }
        return false;
    }

    public static boolean isFromTraffic(JsonNode item) {
        if (item.get("transfer") != null && item.get("transfer").asText().startsWith("from")) {
            return true;
        }
        if (item.get("transfer") != null && item.get("transfer").asText().endsWith("from")) {
            return true;
        }
        return false;
    }
}
