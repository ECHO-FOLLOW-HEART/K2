package utils;

import com.google.common.collect.HashBiMap;
import org.bson.types.ObjectId;

import java.util.HashMap;
import java.util.Map;

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

    /**
     * 有些地区需要映射到具体的县才有交通
     * @param key
     * @return
     */
    public static String localMapping(String key){

        Map<String,String> locMap = new HashMap<String,String>();
        //大兴安岭地区-映射到漠河县，key-value
        locMap.put("53aa9a6410114e3fd47836cf","53aa9a6410114e3fd47836d2");
        if(locMap.containsKey(key)){
            return locMap.get(key);
        }else{
            return key;
        }
    }
}
