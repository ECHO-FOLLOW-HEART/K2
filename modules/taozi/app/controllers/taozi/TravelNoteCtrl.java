package controllers.taozi;

import aizou.core.LocalityAPI;
import aizou.core.TravelNoteAPI;
import com.fasterxml.jackson.databind.JsonNode;
import exception.ErrorCode;
import exception.TravelPiException;
import models.geo.Locality;
import models.misc.TravelNote;
import org.bson.types.ObjectId;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import utils.Utils;

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
            List<TravelNote> noteList = TravelNoteAPI.searchNoteByLoc(Arrays.asList(locality.zhName), null,pageSize);
            List<JsonNode> ret = new ArrayList<>();
            for (TravelNote note : noteList)
                ret.add(note.toJson());

            return Utils.createResponse(ErrorCode.NORMAL, Json.toJson(ret));
        } catch (TravelPiException e) {
            return Utils.createResponse(e.errCode, e.getMessage());
        }
    }
}
