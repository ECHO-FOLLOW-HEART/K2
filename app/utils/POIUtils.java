package utils;


/**
 * POI工具类
 * Created by topy on 2014/9/2.
 */
public class POIUtils {

    public static String ViewSpotClassifierForTime(String vsName) {
        if (vsName.endsWith("馆") || vsName.endsWith("大学") || vsName.endsWith("寺") || vsName.endsWith("祠")
                || vsName.endsWith("楼") || vsName.endsWith("溪") || vsName.endsWith("庙") || vsName.endsWith("故居")) {
            return "1";
        }
        if (vsName.endsWith("街") || vsName.endsWith("路")) {
            return "1.5";
        }
        if (vsName.endsWith("园")) {
            return "2";
        }
        if (vsName.endsWith("动物园") || vsName.endsWith("山") || vsName.endsWith("自然保护区") || vsName.endsWith("温泉") || vsName.endsWith("谷")
                || vsName.endsWith("公园") || vsName.endsWith("海底世界")) {

            return "4";
        }
        return "2";
    }
}
