package controllers.taozi;

import aizou.core.GuideAPI;
import com.fasterxml.jackson.databind.JsonNode;
import exception.ErrorCode;
import exception.TravelPiException;
import models.guide.ItinerItem;
import models.poi.Dinning;
import models.poi.Shopping;
import org.bson.types.ObjectId;
import play.mvc.Controller;
import play.mvc.Result;
import utils.Utils;

/**
 * Created by topy on 2014/11/5.
 */
public class GuideCtrl extends Controller {

//    public static Result getTemplateGuide(String locId) {
//        try {
//                ObjectId locObjid = new ObjectId(locId);
//
//
//
//        } catch (TravelPiException e) {
//            return Utils.createResponse(e.errCode, e.getMessage());
//        }
//    }

    public static Result saveItinerary() {

        JsonNode data = request().body().asJson();

        ItinerItem itemBean;
        JsonNode itineraries = data.get("itinerary");
        if (null != itineraries && itineraries.isArray() && itineraries.findValues("itinerary") != null) {
            for (JsonNode node : itineraries) {


            }
        }


        return Utils.createResponse(ErrorCode.NORMAL, "");
    }

//    private static ItinerItem transJsonToItem(JsonNode node) throws NullPointerException {
//        String type;
//        AbstractPOI poiBean;
//        ItinerItem itemBean = new ItinerItem();
//        ObjectNode itemObject = (ObjectNode) node;
//
//        itemBean.dayIndex = itemObject.get("dayIdx").asInt();
//        type = itemObject.get("type").asText();
//        itemBean.type = type;
//        switch (type) {
//            case "vs":
//                poiBean = new ViewSpot();
//                break;
//        }
//        ObjectNode poiObject = (ObjectNode) itemObject.get("poi");
//        String poiId = poiObject.get("_id").asText();
//        poiBean.id = new ObjectId(poiId);
//        poiBean.name = poiObject.get("zhName").asText();
//        poiBean.enName = poiObject.get("enName").asText();
//        poiBean.desc = poiObject.get("desc").asText();
//        poiBean.cover = poiObject.get("cover").asText();
//        itemBean.poi = poiBean;
//        return itemBean;
//
//    }

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

    public static Result setGuideInfo(String id, String typeInfo) {
        try {
            JsonNode req = request().body().asJson();
            String zhName = req.get("name").asText();
            String enName = req.get("enName").asText();
            Double price = req.get("price").asDouble();
            if (typeInfo.equals("shopping")) {
                Shopping shopping = new Shopping();
                shopping.name = zhName;
                shopping.enName = enName;
                shopping.price = price;
                GuideAPI.savaGuideShopping(new ObjectId(id), shopping);
            }
            if (typeInfo.equals("dinning")) {
                Dinning dinning = new Dinning();
                dinning.name = zhName;
                dinning.enName = enName;
                dinning.price = price;
                GuideAPI.savaGuideDinning(new ObjectId(id), dinning);
            }
            return Utils.createResponse(ErrorCode.NORMAL, "success");
        } catch (TravelPiException | NullPointerException e) {
            return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, "INVALID_ARGUMENT".toLowerCase());
        }
    }
}
