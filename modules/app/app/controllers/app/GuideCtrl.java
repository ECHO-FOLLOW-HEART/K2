package controllers.app;

import aizou.core.GeoAPI;
import aizou.core.GuideAPI;
import aspectj.Key;
import aspectj.UsingOcsCache;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import exception.AizouException;
import exception.ErrorCode;
import formatter.FormatterFactory;
import formatter.taozi.guide.GuideFormatter;
import formatter.taozi.guide.SimpleGuideFormatter;
import models.geo.Locality;
import models.guide.AbstractGuide;
import models.guide.Guide;
import models.misc.ImageItem;
import org.bson.types.ObjectId;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import utils.Constants;
import utils.LogUtils;
import utils.TaoziDataFilter;
import utils.Utils;

import java.util.*;

/**
 * Created by topy on 2014/11/5.
 */
public class GuideCtrl extends Controller {

    public static final String HAS_RECOMMENDATION = "recommend";

    public static final String GUIDE_DETAIL_URL = "http://h5.taozilvxing.com/planshare.php?pid=";

    public static Result guides(int uid) throws AizouException {
        JsonNode data = request().body().asJson();
        String action = data.has("action") ? data.get("action").asText() : "";
        if (action.equals("create")) {
            return createGuide(uid, data);
        } else if (action.equals("fork")) {
            return copyGuide(uid, data);
        } else {
            return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, "action is wrong");
        }
    }

    /**
     * 更新攻略中相应信息
     *
     * @return
     */
    public static Result createGuide(int uid, JsonNode data) throws AizouException {
        ObjectNode node;
        Guide result;

        // 获取图片宽度
        String imgWidthStr = request().getQueryString("imgWidth");
        int imgWidth = 0;
        if (imgWidthStr != null)
            imgWidth = Integer.valueOf(imgWidthStr);
        Integer selfId = Integer.valueOf(uid);//tmp);
        Iterator<JsonNode> iterator = data.get("locId").iterator();
        Boolean initViewSpots = data.has("initViewSpots") ? data.get("initViewSpots").asBoolean() : false;
        List<ObjectId> ids = new ArrayList<>();
        while (iterator.hasNext()) {
            ids.add(new ObjectId(iterator.next().asText()));
        }
        // 如果用户需要推荐攻略，就根据目的地推荐攻略
        if (initViewSpots) {//action.equals(HAS_RECOMMENDATION)) {
            result = GuideAPI.getGuideByDestination(ids, selfId);
            GuideAPI.fillGuideInfo(result);
        } else
            result = GuideAPI.getEmptyGuide(ids, selfId);

        GuideFormatter formatter = FormatterFactory.getInstance(GuideFormatter.class, imgWidth);
        node = (ObjectNode) formatter.formatNode(result);
        node.put("detailUrl", GUIDE_DETAIL_URL + result.getId());

        return Utils.createResponse(ErrorCode.NORMAL, node);
    }

    /**
     * 复制攻略
     *
     * @param uid
     * @return
     */
//    public static Result copyGuide(String guideId) throws AizouException {
    public static Result copyGuide(int uid, JsonNode data) throws AizouException {
        Integer selfId = uid;//Integer.parseInt(request().getHeader("UserId"));
        String guideId = data.has("guideId") ? data.get("guideId").asText() : "";

        ObjectId oGuideId = new ObjectId(guideId);
        Guide guide = GuideAPI.getGuideById(oGuideId, null);
        GuideAPI.saveGuideByUser(guide, selfId);
        ObjectNode result = Json.newObject();
        result.put("id", guide.getId().toString());
        result.put("detailUrl", GUIDE_DETAIL_URL + guideId);

        return Utils.createResponse(ErrorCode.NORMAL, result);
    }

    /**
     * 保存攻略或更新攻略
     *
     * @return
     */
    public static Result saveGuide(Long userId, String guideIdStr) throws AizouException {

        JsonNode data = request().body().asJson();

        ObjectId guideId = new ObjectId(guideIdStr);
        ObjectMapper m = new ObjectMapper();
        Guide guideUpdate = m.convertValue(data, Guide.class);
        //保存攻略
        GuideAPI.updateGuide(guideId, guideUpdate, userId);

        return Utils.createResponse(ErrorCode.NORMAL, "Success.");
    }

    /**
     * 取得用户攻略列表
     *
     * @param page
     * @param pageSize
     * @return
     */
    public static Result getGuidesByUser(Long uid, int page, int pageSize) throws AizouException {

        // 获取图片宽度
        String imgWidthStr = request().getQueryString("imgWidth");
        int imgWidth = 0;
        if (imgWidthStr != null)
            imgWidth = Integer.valueOf(imgWidthStr);

        Long selfId = uid;

        String targetIdStr = request().getQueryString("userId");
        Long resultUserId;
        boolean isSelf;
        // 判断是查询自己的攻略还是他人的攻略
        if (targetIdStr == null) {
            resultUserId = selfId;
            isSelf = true;
        } else {
            resultUserId = Long.parseLong(targetIdStr);
            isSelf = false;
        }

        String statusStr = request().getQueryString("status");

        List<String> fields = Arrays.asList(Guide.fdId, Guide.fnTitle, Guide.fnUpdateTime,
                Guide.fnLocalities, Guide.fnImages, Guide.fnItineraryDays, Guide.fnStatus);
        List<Guide> guides = GuideAPI.getGuideByUser(resultUserId, fields, isSelf, statusStr, page, pageSize);
        List<Guide> result = new ArrayList<>();
        for (Guide guide : guides) {
            guide.images = TaoziDataFilter.getOneImage(guide.images);
            addGuideInfoToNode(guide);
            result.add(guide);
        }
        SimpleGuideFormatter simpleGuideFormatter = FormatterFactory.getInstance(SimpleGuideFormatter.class, imgWidth);
        return Utils.createResponse(ErrorCode.NORMAL, simpleGuideFormatter.formatNode(result));

    }

    /**
     * 添加攻略列表中需要额外展示的字段
     *
     * @param guide 攻略实体
     */
    private static void addGuideInfoToNode(Guide guide) {
        // 添加攻略天数
        guide.setDayCnt(guide.getItineraryDays());

        // 添加攻略摘要
        List<Locality> dests = guide.localities;
        if (dests == null) {
            guide.setSummary("");
            return;
        }
        StringBuilder sb = new StringBuilder();
        List<ImageItem> images = new ArrayList();

        for (Locality des : dests) {
            sb.append(des.getZhName());
            sb.append(Constants.SYMBOL_BLANK + Constants.SYMBOL_BIG + Constants.SYMBOL_BLANK);
            if (des.getImages() != null)
                images.addAll(des.getImages());
        }
        String summary = sb.toString();
        guide.setSummary(summary.substring(0, summary.length() - 3));

    }

    /**
     * 取得攻略详情
     *
     * @param id   攻略ID
     * @param part 攻略部分
     * @return
     */
    public static Result getGuideInfo(int uid, String id, String part) throws AizouException {

        // 获取图片宽度
        String imgWidthStr = request().getQueryString("imgWidth");
        int imgWidth = 0;
        if (imgWidthStr != null)
            imgWidth = Integer.valueOf(imgWidthStr);
        ObjectId guideId = new ObjectId(id);
        List<String> fields = new ArrayList<>();
        Collections.addAll(fields, Guide.fdId, Guide.fnUserId, Guide.fnTitle, Guide.fnLocalities, Guide.fnUpdateTime,
                Guide.fnImages, Guide.fnStatus);
        switch (part) {
            case AbstractGuide.fnItinerary:
                fields.add(Guide.fnItinerary);
                fields.add(Guide.fnItineraryDays);
                break;
            case AbstractGuide.fnShopping:
                fields.add(Guide.fnShopping);
                break;
            case AbstractGuide.fnRestaurant:
                fields.add(Guide.fnRestaurant);
                break;
            case "all":
                fields.add(Guide.fnItinerary);
                fields.add(Guide.fnItineraryDays);
                fields.add(Guide.fnShopping);
                fields.add(Guide.fnRestaurant);
                break;
            default:
                throw new AizouException(ErrorCode.INVALID_ARGUMENT, String.format("Error guide part."));
        }
        Guide guide = GuideAPI.getGuideById(guideId, fields);
        if (guide == null)
            return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, "Guide ID is invalid. ID:" + id);
        // 填充攻略信息
        GuideAPI.fillGuideInfo(guide);
        GuideFormatter formatter = FormatterFactory.getInstance(GuideFormatter.class, imgWidth);
        ObjectNode node = (ObjectNode) formatter.formatNode(guide);
        node.put("detailUrl", GUIDE_DETAIL_URL + guide.getId() + "&uid=" + uid);
        return Utils.createResponse(ErrorCode.NORMAL, node);

    }

    /**
     * 删除攻略
     *
     * @param id 攻略ID
     * @return
     */
    public static Result deleteGuide(String id) throws AizouException {
        ObjectId guideId = new ObjectId(id);
        GuideAPI.deleteGuideById(guideId);
        return Utils.createResponse(ErrorCode.NORMAL, "Success");
    }

    /**
     * 保存攻略标题
     *
     * @param id
     * @return
     */
    public static Result setGuideTitle(String id) throws AizouException {
        JsonNode req = request().body().asJson();
        String title = req.get("title").asText();
        Long userId = Long.parseLong(request().getHeader("UserId"));
        GuideAPI.saveGuideTitle(new ObjectId(id), title, userId);
        return Utils.createResponse(ErrorCode.NORMAL, "Success.");
    }

    /**
     * 获取目的地的攻略信息
     *
     * @param id
     * @return
     */
    @UsingOcsCache(key = "getLocalityGuideInfo({id},{type})", expireTime = 3600)
    public static Result getLocalityGuideInfo(@Key(tag = "id") String id,
                                              @Key(tag = "type") String guidePart) throws AizouException {
        List<String> fields = new ArrayList<>();
        Collections.addAll(fields, Locality.fnDinningIntro, Locality.fnShoppingIntro);
        Locality locality = GeoAPI.locDetails(new ObjectId(id), fields);
        String content = null;
        ObjectNode result = Json.newObject();
        if (guidePart.equals("shopping")) {
            content = locality.getShoppingIntro();
            // 显示城市购物介绍URL
            result.put("detailUrl", "http://h5.taozilvxing.com/city/shopping.php?tid=" + id);
        } else if (guidePart.equals("restaurant")) {
            content = locality.getDiningIntro();
            // 显示城市美食介绍URL
            result.put("detailUrl", "http://h5.taozilvxing.com/city/dining.php?tid=" + id);
        }
        result.put("desc", content != null ? removeH5Label(content) : "");
        return Utils.createResponse(ErrorCode.NORMAL, result);
    }

    private static String removeH5Label(String content) {
        List<String> regExList = Arrays.asList("<p>", "</p>", "<div>", "</div>");
        for (String regEx : regExList) {
            content = content.replace(regEx, "");
        }

        return content;
    }


}
