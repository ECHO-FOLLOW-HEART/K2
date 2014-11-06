package controllers;

import aizou.core.TravelNoteAPI;
import com.fasterxml.jackson.databind.JsonNode;
import exception.ErrorCode;
import exception.TravelPiException;
import models.misc.TravelNote;
import org.bson.types.ObjectId;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import utils.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * 游记攻略
 *
 * @author Zephyre
 */
public class TravelNoteCtrl extends Controller {
    public static Result searchNotes() {
        String planId = request().getQueryString("planId");
        if (planId == null)
            return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, "PLAN ID NOT FOUND.");

        try {
            List<TravelNote> noteList = TravelNoteAPI.searchNoteByPlan(new ObjectId(planId));
            List<JsonNode> ret = new ArrayList<>();
            for (TravelNote note : noteList)
                ret.add(note.toJson());

            return Utils.createResponse(ErrorCode.NORMAL, Json.toJson(ret));
        } catch (TravelPiException e) {
            return Utils.createResponse(e.errCode, e.getMessage());
        }
    }
}
