package utils;

/**
 * Created by topy on 2014/9/12.
 */
public class DataFilter {

    public static String priceDescFilter(String desc) {
        if (null == desc) {
            return "";
        }
        if (desc.equals("None"))
            return "未知";
        return desc;
    }

    public static String openTimeFilter(String time) {
        if (null == time) {
            return "全天";
        }
        if (time.equals("None"))
            return "全天";
        return time;
    }

    public static String timeCostFilter(Double timeCost,String name) {

        if(timeCost== null || timeCost < 0)
           return  POIUtils.ViewSpotClassifierForTime(name);
        if( timeCost > 8.0)
            return "8";
        if( timeCost < 1.0)
            return "0.5";
        String timeStr = String.valueOf(timeCost);
        String resylt = timeStr.substring(0,timeStr.length()-2);
        return resylt;
    }
}
