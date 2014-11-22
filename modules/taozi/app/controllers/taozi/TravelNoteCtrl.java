package controllers.taozi;

import aizou.core.LocalityAPI;
import aizou.core.PoiAPI;
import aizou.core.TravelNoteAPI;
import com.fasterxml.jackson.databind.JsonNode;
import exception.ErrorCode;
import exception.TravelPiException;
import models.geo.Locality;
import models.misc.TravelNote;
import models.poi.ViewSpot;
import org.bson.types.ObjectId;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import utils.Utils;
import utils.formatter.taozi.TravelNote.TravelNoteFormatter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 游记攻略
 *
 * @author Zephyre
 */
public class TravelNoteCtrl extends Controller {

    public static Result searchNotes(String locId, int pageSize) {
        try {

            ObjectId oid = new ObjectId(locId);
            Locality locality = LocalityAPI.getLocality(oid);
            List<TravelNote> noteList = TravelNoteAPI.searchNoteByLoc(Arrays.asList(locality.zhName), null, pageSize);
            List<JsonNode> ret = new ArrayList<>();
            for (TravelNote note : noteList)
                ret.add(note.toJson());

            return Utils.createResponse(ErrorCode.NORMAL, Json.toJson(ret));
        } catch (TravelPiException e) {
            return Utils.createResponse(e.errCode, e.getMessage());
        }

    }

    /**
     * 特定目的地的游记
     *
     * @param id
     * @param pageSize
     * @return
     */
    public static Result getNotes(String id, int pageSize) {
        try {
            ObjectId objectId = new ObjectId(id);
            List<JsonNode> nodeList = new ArrayList<>();
            Locality locality = LocalityAPI.getLocality(objectId);
            ViewSpot vs = PoiAPI.getVsDetails(objectId);
            List<TravelNote> noteList = TravelNoteAPI.getTravelNote(Arrays.asList(locality.zhName), Arrays.asList(vs.zhName), pageSize);
            for (TravelNote note : noteList) {
                nodeList.add(new TravelNoteFormatter().format(note));
            }
            return Utils.createResponse(ErrorCode.NORMAL, Json.toJson(nodeList));
        } catch (TravelPiException e) {
            return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, "INVALID_ARGUMENT");
        }
    }
}

