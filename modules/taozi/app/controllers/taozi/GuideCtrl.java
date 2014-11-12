package controllers.taozi;

import aizou.core.GuideAPI;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import exception.ErrorCode;
import exception.TravelPiException;
import models.guide.Guide;
import models.guide.ItinerItem;
import models.poi.AbstractPOI;
import models.poi.Dinning;
import models.poi.Shopping;
import models.poi.ViewSpot;
import org.bson.types.ObjectId;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import utils.Utils;
import utils.formatter.JsonFormatter;
import utils.formatter.taozi.guide.DinningFormatter;
import utils.formatter.taozi.guide.ItineraryFormatter;
import utils.formatter.taozi.guide.ShoppingFormatter;
import utils.formatter.taozi.guide.SimpleGuideFormatter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by topy on 2014/11/5.
 */
public class GuideCtrl extends Controller {

    /**
     * 更新攻略中行程单内容
     *
     * @param id 攻略ID
     * @return
     */
    public static Result updateItinerary(String id) {

        JsonNode data = request().body().asJson();
        ItinerItem itemBean;
        List<ItinerItem> itemBeanList = new ArrayList<>();
        try {
            ObjectId guideId = new ObjectId(id);
            JsonNode itineraries = data.get("itinerary");
            for (JsonNode node : itineraries) {
                itemBean = getItemFromJson(node);
                itemBeanList.add(itemBean);
            }
            //保存攻略
            GuideAPI.updateItinerary(guideId, itemBeanList);

        } catch (NullPointerException | IllegalArgumentException e) {
            return Utils.createResponse(ErrorCode.DATA_NOT_EXIST, "Date error.");
        } catch (TravelPiException e) {
            return Utils.createResponse(e.errCode, e.getMessage());
        }
        return Utils.createResponse(ErrorCode.NORMAL, "Success.");
    }

    /**
     * 取得用户攻略列表
     *
     * @return
     */
    public static Result getGuidesByUser(int page, int pageSize) {
        try {
            String tmp = request().getHeader("UserId");
            Integer selfId = null;
            if (tmp != null)
                selfId = Integer.parseInt(tmp);
            List<String> fields = Arrays.asList(Guide.fdId, Guide.fnTitle);
            List<Guide> guides = GuideAPI.getGuideByUser(selfId, fields, page, pageSize);

            List<JsonNode> result = new ArrayList<>();
            ObjectNode node;
            for (Guide guide : guides) {
                node = (ObjectNode) new SimpleGuideFormatter().format(guide);
                result.add(node);
            }
            return Utils.createResponse(ErrorCode.NORMAL, Json.toJson(result));
        } catch (TravelPiException e) {
            return Utils.createResponse(e.errCode, e.getMessage());
        }
    }

    /**
     * 读取Json中的数据生成Bean
     *
     * @param node PUT数据
     * @return
     * @throws TravelPiException
     */
    private static ItinerItem getItemFromJson(JsonNode node) throws TravelPiException {
        String type;
        AbstractPOI poiBean;
        ItinerItem itemBean = new ItinerItem();
        ObjectNode itemObject = (ObjectNode) node;
        itemBean.dayIndex = itemObject.get("dayIdx").asInt();
        type = itemObject.get("type").asText();
        itemBean.type = type;
        switch (type) {
            case "vs":
                poiBean = new ViewSpot();
                break;
            case "hotel":
                poiBean = new ViewSpot();
                break;
            case "shopping":
                poiBean = new ViewSpot();
                break;
            case "dinning":
                poiBean = new ViewSpot();
                break;
            default:
                throw new TravelPiException(ErrorCode.INVALID_ARGUMENT, String.format("Error poi type."));
        }
        ObjectNode poiObject = (ObjectNode) itemObject.get("poi");
        String poiId = poiObject.get("_id").asText();
        // TODO poiId可能不合法
        poiBean.id = new ObjectId(poiId);
        poiBean.name = poiObject.get("zhName").asText();
        poiBean.enName = poiObject.get("enName").asText();
        poiBean.desc = poiObject.get("desc").asText();
        poiBean.cover = poiObject.get("cover").asText();
        itemBean.poi = poiBean;
        return itemBean;
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
            List<String> fields = Arrays.asList(Guide.fdId, Guide.fnUserId, Guide.fnTitle);
            switch (part) {
                case "itinerary":
                    jsonFormatter = new ItineraryFormatter();
                    fields.add(Guide.fnItinerary);
                    break;
                case "shopping":
                    jsonFormatter = new ShoppingFormatter();
                    fields.add(Guide.fnShopping);
                    break;
                case "dinning":
                    jsonFormatter = new DinningFormatter();
                    fields.add(Guide.fnDinning);
                    break;
                default:
                    throw new TravelPiException(ErrorCode.INVALID_ARGUMENT, String.format("Error guide part."));
            }
            Guide guide = GuideAPI.getGuideById(guideId, fields);
            ObjectNode node = (ObjectNode) jsonFormatter.format(guide);
            return Utils.createResponse(ErrorCode.NORMAL, node);
        } catch (TravelPiException e) {
            return Utils.createResponse(e.errCode, e.getMessage());
        } catch (IllegalArgumentException e) {
            return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, "");
        }

    }

    public static Result deleteGuide(String id) {
        try {
            ObjectId guideId = new ObjectId(id);
            GuideAPI.deleteGuideById(guideId);
            return Utils.createResponse(ErrorCode.NORMAL, "Success");
        } catch (TravelPiException e) {
            return Utils.createResponse(e.errCode, e.getMessage());
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
        } catch (TravelPiException | NullPointerException | IllegalArgumentException e) {
            return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, "INVALID_ARGUMENT".toLowerCase());
        }

    }


    /**
     * @param node
     * @param typeInfo
     * @return
     * @throws NullPointerException
     * @throws TravelPiException
     */
    public static Object getShoppingFromNode(JsonNode node, String typeInfo) throws NullPointerException, TravelPiException {
        // TODO ObjectId可能不合法
        switch (typeInfo) {
            case "shopping":
                Shopping shopping = new Shopping();
                shopping.id = new ObjectId(node.get("_id").asText());
                shopping.name = node.get("zhName").asText();
                shopping.enName = node.get("enName").asText();
                shopping.price = node.get("price").asDouble();
                shopping.rating = node.get("rating").asDouble();
                return shopping;
            case "dinning":
                Dinning dinning = new Dinning();
                dinning.id = new ObjectId(node.get("_id").asText());
                dinning.name = node.get("zhName").asText();
                dinning.enName = node.get("enName").asText();
                dinning.price = node.get("price").asDouble();
                dinning.rating = node.get("rating").asDouble();
                return dinning;
            default:
                throw new TravelPiException(ErrorCode.INVALID_ARGUMENT, "INVALID_ARGUMENT".toLowerCase());
        }

    }

    /**
     * 保存用户的美食和购物攻略
     *
     * @param id
     * @param typeInfo
     * @return
     */
    public static Result setGuideInfo(String id, String typeInfo) {
        try {
            JsonNode req = request().body().asJson();
            switch (typeInfo) {
                case "shopping":
                    JsonNode shoppings = req.get("shopping");
                    List<Shopping> shoppingList = new ArrayList<>();
                    Shopping shopping;
                    for (JsonNode node : shoppings) {
                        shopping = (Shopping) getShoppingFromNode(node, "shopping");
                        shoppingList.add(shopping);
                    }
                    GuideAPI.savaGuideShopping(new ObjectId(id), shoppingList);
                    return Utils.createResponse(ErrorCode.NORMAL, "success");
                case "dinning":
                    JsonNode dinnings = req.get("dinning");
                    List<Dinning> dinningList = new ArrayList<>();
                    Dinning dinning;
                    for (JsonNode node : dinnings) {
                        dinning = (Dinning) getShoppingFromNode(node, "dinning");
                        dinningList.add(dinning);
                    }
                    GuideAPI.savaGuideDinning(new ObjectId(id), dinningList);
                    return Utils.createResponse(ErrorCode.NORMAL, "success");
                default:
                    return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, "INVALID_ARGUMENT".toLowerCase());
            }
        } catch (TravelPiException | IllegalArgumentException | NullPointerException e) {
            return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, "INVALID_ARGUMENT".toLowerCase());
        }
    }
}
