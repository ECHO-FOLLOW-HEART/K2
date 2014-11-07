package controllers.taozi;

import aizou.core.GuideAPI;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import exception.ErrorCode;
import exception.TravelPiException;
import models.guide.Guide;
import models.guide.ItinerItem;
import models.poi.AbstractPOI;
import models.poi.ViewSpot;
import org.bson.types.ObjectId;
import play.mvc.Controller;
import play.mvc.Result;
import utils.Utils;

import java.util.ArrayList;
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
            Guide guide = GuideAPI.getGuideById(guideId, null);
            JsonNode itineraries = data.get("itinerary");
            if (itineraries == null)
                throw new TravelPiException(ErrorCode.INVALID_ARGUMENT, "Put data doesn't contain itinerary.");
            for (JsonNode node : itineraries) {
                itemBean = getItemFromJson(node);
                itemBeanList.add(itemBean);
            }
            //保存攻略
            GuideAPI.saveItinerary(guide, itemBeanList);

        } catch (NullPointerException e1) {
            return Utils.createResponse(ErrorCode.DATA_NOT_EXIST, "NullPointerException happened.");
        } catch (TravelPiException e) {
            return Utils.createResponse(e.errCode, e.getMessage());
        }
        return Utils.createResponse(ErrorCode.NORMAL, "Success.");
    }

    /**
     * 读取Json中的数据生成Bean
     *
     * @param node PUT数据
     * @return
     * @throws NullPointerException
     * @throws TravelPiException
     */
    private static ItinerItem getItemFromJson(JsonNode node) throws NullPointerException, TravelPiException {
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
            default:
                throw new TravelPiException(ErrorCode.INVALID_ARGUMENT, String.format("Error poi type."));
        }
        ObjectNode poiObject = (ObjectNode) itemObject.get("poi");
        String poiId = poiObject.get("_id").asText();
        poiBean.id = new ObjectId(poiId);
        poiBean.name = poiObject.get("zhName").asText();
        poiBean.enName = poiObject.get("enName").asText();
        poiBean.desc = poiObject.get("desc").asText();
        poiBean.cover = poiObject.get("cover").asText();
        itemBean.poi = poiBean;
        return itemBean;
    }
}
