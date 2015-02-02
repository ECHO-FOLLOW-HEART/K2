package utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ValueNode;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
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
     * @param id
     * @return
     */
    public static ObjectId localMapping(ObjectId id) {
        if (id == null)
            return null;
        String result;
        String key = id.toString();
        Map<String, String> locMap = new HashMap<String, String>();
        //大兴安岭地区-映射到漠河县，key-value
        locMap.put("53aa9a6410114e3fd47836cf", "53aa9a6410114e3fd47836d2");
        //湘西土家族苗族自治州-吉首市
        locMap.put("53aa9a6510114e3fd4783b4e", "53aa9a6510114e3fd4783b4f");
        if (locMap.containsKey(key)) {
            result = locMap.get(key);
        } else {
            result = key;
        }
        return new ObjectId(result);
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
        if (!isAppRequest(request))
            return json;

        //列表时
        if (json.isArray() && json.findValues("imageList") != null && json.findValues("imageList").size() > 0) {
            for (JsonNode node : json) {
                tempObjNode = (ObjectNode) node;
                tempJsImg = tempObjNode.get("imageList");
                if (tempJsImg == null || !tempJsImg.isArray())
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
        // 首页推荐中
        if (json.has("loc")) {
            traversalLocJson(json, Constants.BIG_PIC);
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
     * 遍历loc中内容，添加图片规格。
     */
    public static void traversalLocJson(JsonNode jsNode, int size) {

        ObjectNode tempObjNode;
        JsonNode oNode, tempJsImg;
        List<JsonNode> newNodeList;
        String fullUrl;

        if (jsNode.has("loc")) {
            oNode = jsNode.get("loc");
            //列表时
            if (oNode.isArray() && oNode.findValues("imageList") != null && oNode.findValues("imageList").size() > 0) {
                for (JsonNode node : oNode) {
                    tempObjNode = (ObjectNode) node;
                    tempJsImg = tempObjNode.get("imageList");
                    if (tempJsImg == null || !tempJsImg.isArray())
                        continue;

                    newNodeList = new ArrayList<>();
                    for (JsonNode imgNode : tempJsImg) {
                        fullUrl = imgNode.asText() + Constants.SYMBOL_QUESTION + getPictureUrl(size);
                        newNodeList.add(Json.toJson(fullUrl));
                    }
                    tempObjNode.put("imageList", Json.toJson(newNodeList));
                }
            }
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

    /**
     * app请求图片图片地址时，添加图片的规格
     *
     * @param json
     * @param request
     * @return
     */
    public static JsonNode appDescFilter(JsonNode json, Request request) {

        String descFilter;
        ObjectNode tempJson;

        // 非App请求
        if (!isAppRequest(request))
            return json;

        // app请求，截断过长的描述
        if (json.get("desc") != null && (!json.get("desc").equals(""))) {
            descFilter = StringUtils.abbreviate(json.get("desc").asText(), Constants.ABBREVIATE_LEN);
            tempJson = (ObjectNode) json;
            tempJson.put("desc", descFilter);
        }
        // app请求，不显示description
        if (json.has("description")) {
            tempJson = (ObjectNode) json;
            tempJson.put("description", "");
        }
        // app请求，不显示moreDesc
        if (json.has("moreDesc")) {
            tempJson = (ObjectNode) json;
            tempJson.put("moreDesc", "");
        }
        return json;
    }

    /**
     * app请求推荐时的过滤
     *
     * @param json
     * @param request
     * @return
     */
    public static JsonNode appRecommendFilter(JsonNode json, Request request) {
        ObjectNode tempObjNode;

        // 非App请求
        if (!isAppRequest(request))
            return json;
        //App请求，不显示image
        if (json.isArray() && json.findValues("image") != null && json.findValues("image").size() > 0) {
            for (JsonNode node : json) {
                tempObjNode = (ObjectNode) node;
                tempObjNode.put("image", "");
            }
        }
        return json;
    }

    /**
     * 判断是否是App请求
     *
     * @param request
     * @return
     */
    public static boolean isAppRequest(Request request) {
        if (request.getQueryString("platform") == null)
            return false;
        String platform = request.getQueryString("platform");
        if (!platform.toUpperCase().contains("IOS") && (!platform.toUpperCase().contains("ANDROID")))
            return false;
        return true;
    }

    /**
     * 在规划路线时，在交通中，去掉相同的车站
     * 例如：北京机场-飞机-哈尔滨机场-哈尔滨机场-飞机-漠河机场
     *
     * @param plan 路线计划
     * @param ep   去程还是返程
     */
//    public static void trafficSameStopFilter(Plan plan, boolean ep) {
//        if (plan == null || plan.details == null || plan.details.isEmpty()) {
//            return;
//        }
//        int index = ep ? 0 : plan.details.size() - 1;
//        List<PlanItem> actvs = plan.details.get(index).actv;
//        String frontStop;
//        List<PlanItem> newActvs = new ArrayList<>();
//        PlanItem tempItem, tempItemNext;
//        int size = actvs.size();
//        for (int i = 0; i < actvs.size() - 1; i++) {
//            tempItem = actvs.get(i);
//            tempItemNext = actvs.get(i + 1);
//            if (tempItem.item.zhName.equals(tempItemNext.item.zhName)) {
//                continue;
//            }
//            newActvs.add(tempItem);
//            if (i == actvs.size() - 2) {
//                newActvs.add(tempItemNext);
//            }
//        }
//        plan.details.get(index).actv = newActvs;
//    }

}
