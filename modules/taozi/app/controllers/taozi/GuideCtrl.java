package controllers.taozi;

import aizou.core.GuideAPI;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import exception.ErrorCode;
import exception.TravelPiException;
import models.guide.AbstractGuide;
import models.guide.Guide;
import models.guide.ItinerItem;
import models.poi.AbstractPOI;
import models.poi.ViewSpot;
import org.bson.types.ObjectId;
import play.libs.Json;
import models.poi.Dinning;
import models.poi.Shopping;
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
     * 更新攻略中相应信息
     *
     * @param id 攻略ID
     * @return
     */
    public static Result updateGuide(String id,String typeInfo) {

        JsonNode data = request().body().asJson();
        try {
            ObjectId guideId = new ObjectId(id);
            ObjectMapper m =  new ObjectMapper();
            Guide guideUpdate = m.convertValue(data,Guide.class);
            List<String> guideParts = Arrays.asList(AbstractGuide.fnItinerary,AbstractGuide.fnShopping,AbstractGuide.fnDinning);
            if(!guideParts.contains(typeInfo))
                return Utils.createResponse(ErrorCode.DATA_NOT_EXIST, String.format("s% is not a part of guide.",typeInfo));
            //保存攻略
            GuideAPI.updateGuide(guideId, guideUpdate, typeInfo);
        } catch (NullPointerException e) {
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
        }

    }

    public static Result deleteGuide(String id) {
        try {
            ObjectId guideId = new ObjectId(id);
            GuideAPI.deleteGuideById(guideId);
            return Utils.createResponse(ErrorCode.NORMAL, "Success");
        } catch (TravelPiException e) {
            return Utils.createResponse(e.errCode, e.getMessage());
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
        } catch (TravelPiException | NullPointerException e) {
            return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, "INVALID_ARGUMENT".toLowerCase());
        }

    }
}
