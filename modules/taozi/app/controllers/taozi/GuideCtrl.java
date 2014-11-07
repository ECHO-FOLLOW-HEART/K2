package controllers.taozi;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import exception.ErrorCode;
import models.guide.ItinerItem;
import models.poi.AbstractPOI;
import models.poi.ViewSpot;
import play.mvc.Controller;
import org.bson.types.ObjectId;
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
}
