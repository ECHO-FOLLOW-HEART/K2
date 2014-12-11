package controllers.taozi;

import aizou.core.GuideAPI;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import exception.AizouException;
import exception.ErrorCode;
import formatter.JsonFormatter;
import formatter.taozi.guide.*;
import models.geo.Locality;
import models.guide.AbstractGuide;
import models.guide.DestGuideInfo;
import models.guide.Guide;
import models.misc.ImageItem;
import models.poi.Dinning;
import models.poi.Shopping;
import org.bson.types.ObjectId;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import utils.Utils;

import java.util.*;

/**
 * Created by topy on 2014/11/5.
 */
public class GuideCtrl extends Controller {

    /**
     * 更新攻略中相应信息
     *
     * @return
     */
    public static Result createGuide() {
        JsonNode data = request().body().asJson();
        ObjectNode node;
        try {
            String tmp = request().getHeader("UserId");
            if (tmp == null)
                return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, "User id is null.");
            Integer selfId = Integer.parseInt(tmp);
            Iterator<JsonNode> iterator = data.get("locId").iterator();
            List<ObjectId> ids = new ArrayList<>();
            for (; iterator.hasNext(); ) {
                ids.add(new ObjectId(iterator.next().asText()));
            }
            Guide temp = GuideAPI.getGuideByDestination(ids, selfId);
            node = (ObjectNode) new GuideFormatter().format(temp);
        } catch (NullPointerException | IllegalArgumentException e) {
            return Utils.createResponse(ErrorCode.DATA_NOT_EXIST, "Date error.");
        } catch (AizouException e) {
            return Utils.createResponse(e.getErrCode(), e.getMessage());
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
                node = (ObjectNode) new SimpleGuideFormatter().format(guide);
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
            JsonFormatter jsonFormatter;
            ObjectId guideId = new ObjectId(id);
            List<String> fields = new ArrayList<>();
            Collections.addAll(fields, Guide.fdId, Guide.fnUserId, Guide.fnTitle, Guide.fnLocalities, Guide.fnUpdateTime);
            switch (part) {
                case AbstractGuide.fnItinerary:
                    jsonFormatter = new ItineraryFormatter();
                    fields.add(Guide.fnItinerary);
                    fields.add(Guide.fnItineraryDays);
                    break;
                case AbstractGuide.fnShopping:
                    jsonFormatter = new ShoppingFormatter();
                    fields.add(Guide.fnShopping);
                    break;
                case AbstractGuide.fnRestaurant:
                    jsonFormatter = new RestaurantFormatter();
                    fields.add(Guide.fnRestaurant);
                    break;
                case "all":
                    jsonFormatter = new GuideFormatter();
                    fields.add(Guide.fnItinerary);
                    fields.add(Guide.fnItineraryDays);
                    fields.add(Guide.fnShopping);
                    fields.add(Guide.fnRestaurant);
                    break;
                default:
                    throw new AizouException(ErrorCode.INVALID_ARGUMENT, String.format("Error guide part."));
            }
            Guide guide = GuideAPI.getGuideById(guideId, fields);
            // TODO 数据完备后开启
            // 填充攻略信息
            GuideAPI.fillGuideInfo(guide);
            ObjectNode node = (ObjectNode) jsonFormatter.format(guide);
            return Utils.createResponse(ErrorCode.NORMAL, node);
        } catch (AizouException e) {
            return Utils.createResponse(e.getErrCode(), e.getMessage());
        } catch (IllegalArgumentException e) {
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
            return Utils.createResponse(ErrorCode.NORMAL, "success");
        } catch (AizouException | NullPointerException | IllegalArgumentException e) {
            return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, "INVALID_ARGUMENT".toLowerCase());
        }

    }

    /**
     * @param node
     * @param typeInfo
     * @return
     * @throws NullPointerException
     * @throws exception.AizouException
     */
    public static Object getShoppingFromNode(JsonNode node, String typeInfo) throws NullPointerException, AizouException {
        // TODO ObjectId可能不合法
        switch (typeInfo) {
            case "shopping":
                Shopping shopping = new Shopping();
                shopping.setId(new ObjectId(node.get("_id").asText()));
                shopping.name = node.get("zhName").asText();
                shopping.enName = node.get("enName").asText();
                shopping.price = node.get("price").asDouble();
                shopping.rating = node.get("rating").asDouble();
                return shopping;
            case "dinning":
                Dinning dinning = new Dinning();
                dinning.setId(new ObjectId(node.get("_id").asText()));
                dinning.name = node.get("zhName").asText();
                dinning.enName = node.get("enName").asText();
                dinning.price = node.get("price").asDouble();
                dinning.rating = node.get("rating").asDouble();
                return dinning;
            default:
                throw new AizouException(ErrorCode.INVALID_ARGUMENT, "INVALID_ARGUMENT".toLowerCase());
        }

    }

    /**
     * 获取目的地的攻略信息
     *
     * @param id
     * @return
     */
    public static Result getDestinationGuideInfo(String id, String guidePart) {
        try {
            DestGuideInfo destGuideInfo = GuideAPI.getDestinationGuideInfo(new ObjectId(id));
            ObjectNode node = (ObjectNode) new DestGuideFormatter().format(destGuideInfo, guidePart);
            return Utils.createResponse(ErrorCode.NORMAL, node);
        } catch (AizouException | NullPointerException | IllegalArgumentException e) {
            return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, "INVALID_ARGUMENT".toLowerCase());
        }

    }

}
