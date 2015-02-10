package controllers.taozi;

import aizou.core.GeoAPI;
import aizou.core.GuideAPI;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.CacheKey;
import controllers.UsingCache;
import exception.AizouException;
import exception.ErrorCode;
import formatter.FormatterFactory;
import formatter.taozi.guide.GuideFormatter;
import formatter.taozi.guide.GuideFormatterOld;
import formatter.taozi.guide.SimpleGuideFormatter;
import formatter.taozi.user.ContactFormatter;
import models.geo.Locality;
import models.guide.AbstractGuide;
import models.guide.Guide;
import models.misc.ImageItem;
import org.bson.types.ObjectId;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import utils.LogUtils;
import utils.TaoziDataFilter;
import utils.Utils;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * Created by topy on 2014/11/5.
 */
public class GuideCtrl extends Controller {

    public static final String HAS_RECOMMENDATION = "recommend";

    public static final String GUIDE_DETAIL_URL = "http://h5.taozilvxing.com/planshare.php?pid=";

    /**
     * 更新攻略中相应信息
     *
     * @return
     */
    public static Result createGuide() {
        JsonNode data = request().body().asJson();
        ObjectNode node;
        Guide result;
        try {
            // 获取图片宽度
            String imgWidthStr = request().getQueryString("imgWidth");
            int imgWidth = 0;
            if (imgWidthStr != null)
                imgWidth = Integer.valueOf(imgWidthStr);
            String tmp = request().getHeader("UserId");
            if (tmp == null)
                return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, "User id is null.");
            Integer selfId = Integer.parseInt(tmp);
            String action = data.has("action") ? data.get("action").asText() : "";
            Iterator<JsonNode> iterator = data.get("locId").iterator();
            List<ObjectId> ids = new ArrayList<>();
            while (iterator.hasNext()) {
                ids.add(new ObjectId(iterator.next().asText()));
            }
            // 如果用户需要推荐攻略，就根据目的地推荐攻略
            if (action.equals(HAS_RECOMMENDATION)) {
                result = GuideAPI.getGuideByDestination(ids, selfId);
                GuideAPI.fillGuideInfo(result);
            } else
                result = GuideAPI.getEmptyGuide(ids, selfId);

            GuideFormatter formatter = FormatterFactory.getInstance(GuideFormatter.class, imgWidth);
            node = (ObjectNode) formatter.formatNode(result);
            node.put("detailUrl", GUIDE_DETAIL_URL + result.getId());
        } catch (NullPointerException | IllegalArgumentException e) {
            return Utils.createResponse(ErrorCode.DATA_NOT_EXIST, "Date error.");
        } catch (AizouException | JsonProcessingException | InstantiationException | IllegalAccessException e) {
            return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, e.getMessage());
        }
        return Utils.createResponse(ErrorCode.NORMAL, node);
    }

    /**
     * 保存攻略或更新攻略
     *
     * @return
     */
    public static Result saveGuide() {

        JsonNode data = request().body().asJson();
        LogUtils.info(GuideCtrl.class, request());
        try {
            String tmp = request().getHeader("UserId");
            if (tmp == null)
                return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, "User id is null.");
            Integer selfId = Integer.parseInt(tmp);
            ObjectId guideId = new ObjectId(data.get("id").asText());
            ObjectMapper m = new ObjectMapper();
            Guide guideUpdate = m.convertValue(data, Guide.class);
            //保存攻略
            GuideAPI.updateGuide(guideId, guideUpdate, selfId);
        } catch (NullPointerException | IllegalArgumentException e) {
            return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, e.getMessage());
        } catch (AizouException e) {
            return Utils.createResponse(e.getErrCode(), e.getMessage());
        }
        return Utils.createResponse(ErrorCode.NORMAL, "Success.");
    }

    /**
     * 取得用户攻略列表
     *
     * @param page
     * @param pageSize
     * @return
     */
    public static Result getGuidesByUser(int page, int pageSize) {
        try {
            // 获取图片宽度
            String imgWidthStr = request().getQueryString("imgWidth");
            int imgWidth = 0;
            if (imgWidthStr != null)
                imgWidth = Integer.valueOf(imgWidthStr);
            String tmp = request().getHeader("UserId");
            if (tmp == null)
                return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, "User id is null.");
            Integer selfId = Integer.parseInt(tmp);
            List<String> fields = Arrays.asList(Guide.fdId, Guide.fnTitle, Guide.fnUpdateTime,
                    Guide.fnLocalities, Guide.fnImages, Guide.fnItineraryDays);
            List<Guide> guides = GuideAPI.getGuideByUser(selfId, fields, page, pageSize);
            List<JsonNode> result = new ArrayList<>();
            ObjectNode node;
            for (Guide guide : guides) {
                guide.images = TaoziDataFilter.getOneImage(guide.images);
                node = (ObjectNode) new SimpleGuideFormatter().setImageWidth(imgWidth).format(guide);
                addGuideInfoToNode(guide, node);
                result.add(node);
            }
            return Utils.createResponse(ErrorCode.NORMAL, Json.toJson(result));
        } catch (IllegalArgumentException | NullPointerException e) {
            return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, e.getMessage());
        } catch (AizouException e) {
            return Utils.createResponse(e.getErrCode(), e.getMessage());
        }
    }

    /**
     * 添加攻略列表中需要额外展示的字段
     *
     * @param guide 攻略实体
     * @param node  攻略JSON内容
     */
    private static void addGuideInfoToNode(Guide guide, ObjectNode node) {
        // 添加攻略天数
        node.put("dayCnt", guide.itineraryDays);

        // 添加攻略摘要
        List<Locality> dests = guide.localities;
        if (dests == null) {
            node.put("summary", "");
            return;
        }
        StringBuilder sb = new StringBuilder();
        List<ImageItem> images = new ArrayList();

        for (Locality des : dests) {
            sb.append(des.getZhName());
            sb.append("、");
            if (des.getImages() != null)
                images.addAll(des.getImages());
        }
        String summary = sb.toString();
        node.put("summary", summary.substring(0, summary.length() - 1));

    }

    /**
     * 取得攻略详情
     *
     * @param id   攻略ID
     * @param part 攻略部分
     * @return
     */
    public static Result getGuideInfo(String id, String part) {
        try {
            // 获取图片宽度
            String imgWidthStr = request().getQueryString("imgWidth");
            int imgWidth = 0;
            if (imgWidthStr != null)
                imgWidth = Integer.valueOf(imgWidthStr);
            ObjectId guideId = new ObjectId(id);
            List<String> fields = new ArrayList<>();
            Collections.addAll(fields, Guide.fdId, Guide.fnUserId, Guide.fnTitle, Guide.fnLocalities, Guide.fnUpdateTime,
                    Guide.fnImages);
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
            // ObjectNode node = (ObjectNode) new GuideFormatterOld().setImageWidth(imgWidth).format(guide);
            GuideFormatter formatter = FormatterFactory.getInstance(GuideFormatter.class, imgWidth);
            ObjectNode node = (ObjectNode) formatter.formatNode(guide);
            node.put("detailUrl", GUIDE_DETAIL_URL + guide.getId());
            return Utils.createResponse(ErrorCode.NORMAL, node);
        } catch (AizouException e) {
            return Utils.createResponse(e.getErrCode(), e.getMessage());
        } catch (IllegalArgumentException | JsonProcessingException | InstantiationException | IllegalAccessException e) {
            return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, "");
        }
    }

    /**
     * 删除攻略
     *
     * @param id 攻略ID
     * @return
     */
    public static Result deleteGuide(String id) {
        try {
            ObjectId guideId = new ObjectId(id);
            GuideAPI.deleteGuideById(guideId);
            return Utils.createResponse(ErrorCode.NORMAL, "Success");
        } catch (AizouException e) {
            return Utils.createResponse(e.getErrCode(), e.getMessage());
        } catch (IllegalArgumentException e) {
            return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, "");
        }


    }

    /**
     * 保存攻略标题
     *
     * @param id
     * @return
     */
    public static Result setGuideTitle(String id) {
        try {
            JsonNode req = request().body().asJson();
            String title = req.get("title").asText();
            GuideAPI.saveGuideTitle(new ObjectId(id), title);
            return Utils.createResponse(ErrorCode.NORMAL, "Success.");
        } catch (AizouException | NullPointerException | IllegalArgumentException e) {
            return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, "INVALID_ARGUMENT".toLowerCase());
        }

    }

    /**
     * 获取目的地的攻略信息
     *
     * @param id
     * @return
     */
    @UsingCache(key = "getLocalityGuideInfo({id},{type})", expireTime = 3600)
    public static Result getLocalityGuideInfo(@CacheKey(tag = "id") String id,
                                              @CacheKey(tag = "type") String guidePart) {
        try {
            List<String> fields = new ArrayList<>();
            Collections.addAll(fields, Locality.fnDinningIntro, Locality.fnShoppingIntro);
            Locality locality = GeoAPI.locDetails(new ObjectId(id), fields);
            String content = null;
            ObjectNode result = Json.newObject();
            if (guidePart.equals("shopping")) {
                content = locality.getShoppingIntro();
                // 显示城市购物介绍URL
                result.put("detailUrl", "http://h5.taozilvxing.com/shopping.php?tid=" + id);
            } else if (guidePart.equals("restaurant")) {
                content = locality.getDiningIntro();
                // 显示城市美食介绍URL
                result.put("detailUrl", "http://h5.taozilvxing.com/dining.php?tid=" + id);
            }
            result.put("desc", content != null ? removeH5Label(content) : "");
            return Utils.createResponse(ErrorCode.NORMAL, result);
        } catch (AizouException | NullPointerException | IllegalArgumentException e) {
            return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, "INVALID_ARGUMENT".toLowerCase());
        }

    }

    private static String removeH5Label(String content) {
        List<String> regExList = Arrays.asList("<p>", "</p>", "<div>", "</div>");
        for (String regEx : regExList) {
            content = content.replace(regEx, "");
        }

        return content;
    }

    /**
     * 复制攻略
     *
     * @param guideId
     * @return
     */
    public static Result copyGuide(String guideId) {
        try {
            Integer selfId = Integer.parseInt(request().getHeader("UserId"));

            ObjectId oGuideId = new ObjectId(guideId);

            Guide guide = GuideAPI.getGuideById(oGuideId, null);

            GuideAPI.saveGuideByUser(guide, selfId);

            ObjectNode result = Json.newObject();

            result.put("id", guide.getId().toString());
            result.put("detailUrl", GUIDE_DETAIL_URL + guideId);

            return Utils.createResponse(ErrorCode.NORMAL, result);
        } catch (AizouException | NullPointerException | IllegalArgumentException e) {
            return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, "INVALID_ARGUMENT".toLowerCase());
        }
    }
}
