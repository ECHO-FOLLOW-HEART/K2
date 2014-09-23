package utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ValueNode;
import play.Configuration;
import play.libs.Json;
import play.mvc.Http.Request;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

    public static String timeCostFilter(Double timeCost, String name) {

        if (timeCost == null || timeCost < 0)
            return POIUtils.ViewSpotClassifierForTime(name);
        if (timeCost > 8.0)
            return "8";
        if (timeCost < 1.0)
            return "0.5";
        String timeStr = String.valueOf(timeCost);
        return timeStr.substring(0, timeStr.length() - 2);
    }

    /**
     * 有些地区需要映射到具体的县才有交通
     *
     * @param key
     * @return
     */
    public static String localMapping(String key) {

        Map<String, String> locMap = new HashMap<String, String>();
        //大兴安岭地区-映射到漠河县，key-value
        locMap.put("53aa9a6410114e3fd47836cf", "53aa9a6410114e3fd47836d2");
        if (locMap.containsKey(key)) {
            return locMap.get(key);
        } else {
            return key;
        }
    }


    /**
     * app请求图片图片地址时，添加图片的规格
     *
     * @param json
     * @param request
     * @param picSize
     * @return
     */
    public static JsonNode appJsonFilter(JsonNode json, Request request, int picSize) {

        JsonNode tempJsImg = null;
        ObjectNode tempObjNode = null;
        List<JsonNode> newNodeList = null;
        String fullUrl = null;
        String picUrl = getPictureUrl(picSize);
        if (picUrl == null)
            return json;

        // 非App请求
        if (request.getQueryString("platform") == null)
            return json;

        // 非App请求
        String platform = request.getQueryString("platform");
        if (!platform.toUpperCase().contains("IOS") && (!platform.toUpperCase().contains("ANDROID")))
            return json;

        //列表时
        if (json.isArray() && json.findValues("imageList") != null && json.findValues("imageList").size() > 0) {
            for (JsonNode node : json) {
                tempObjNode = (ObjectNode) node;
                tempJsImg = tempObjNode.get("imageList");
                if (!tempJsImg.isArray())
                    continue;

                newNodeList = new ArrayList<>();
                for (JsonNode imgNode : tempJsImg) {
                    fullUrl = imgNode.asText() + Constants.SYMBOL_QUESTION + picUrl;
                    newNodeList.add(Json.toJson(fullUrl));
                }
                tempObjNode.put("imageList", Json.toJson(newNodeList));
            }
        }
        //一条数据
        if (json.has("imageList")) {
            traverImageListNode(json, Constants.BIG_PIC);
        }
        //详情中
        if (json.has("details")) {
            traversalJson(json);
        }
        return json;
    }

    /**
     * 遍历details内容，添加图片规格。
     */
    public static void traversalJson(JsonNode jsNode) {

        if (jsNode.iterator() == null)
            return;

        if (jsNode.has("details")) {
            if (jsNode.get("details").has("imageList")) {
                traverImageListNode(jsNode.get("details"), Constants.SMALL_PIC);
            }
            for (JsonNode imgNode : jsNode.get("details")) {
                if (!(imgNode instanceof ValueNode) && (!(imgNode instanceof ArrayNode))) {
                    traversalJson(imgNode);

                }
            }
        }
        
        if (jsNode.has("actv")) {
            for (JsonNode imgNode : jsNode.get("actv")) {
                traversalJson(imgNode);
            }
        }
        if (jsNode.has("imageList")) {
            traverImageListNode(jsNode, Constants.SMALL_PIC);
        }
    }

    /**
     * 根据URL获得图片
     *
     * @param picSize
     * @return
     */
    private static String getPictureUrl(int picSize) {

        Configuration config = Configuration.root();
        Map image = (Map) config.getObject("image");
        return picSize == Constants.BIG_PIC ? image.get("big") == null ? "" : image.get("big").toString() : image.get("small") == null ? "" : image.get("small").toString();

    }

    /**
     * 设置imageList中图片地址的规格
     *
     * @param json
     * @param picSize
     */
    public static void traverImageListNode(JsonNode json, int picSize) {

        ObjectNode tempObjNode = (ObjectNode) json;
        JsonNode tempJsImg = tempObjNode.get("imageList");

        if (tempJsImg.get(0) == null || tempJsImg.get(0).asText().contains(Constants.SYMBOL_QUESTION)) {
            return;
        }
        List<JsonNode> newNodeList;
        String fullUrl;


        newNodeList = new ArrayList<>();
        for (JsonNode imgNode : tempJsImg) {
            fullUrl = imgNode.asText() + Constants.SYMBOL_QUESTION + getPictureUrl(picSize);
            newNodeList.add(Json.toJson(fullUrl));
        }
        tempObjNode.put("imageList", Json.toJson(newNodeList));

    }

}
