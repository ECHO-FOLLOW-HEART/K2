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
import org.apache.solr.client.solrj.SolrServerException;
import org.bson.types.ObjectId;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import utils.Utils;
import utils.formatter.taozi.TravelNote.DetailTravelNoteFormatter;
import utils.formatter.taozi.TravelNote.SimpTravelNoteFormatter;

import java.text.ParseException;
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
            Locality locality = LocalityAPI.getLocality(objectId, Arrays.asList(Locality.fnZhName, Locality.fnTags, Locality.fnAlias));
            ViewSpot vs = PoiAPI.getVsDetail(objectId, Arrays.asList(ViewSpot.simpZhName, ViewSpot.fnTags, ViewSpot.detAlias));
            List<String> locNames = new ArrayList<>();
            List<String> vsNames = new ArrayList<>();
            if (locality == null && vs == null)
                return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, "INVALID_ARGUMENT");
            else if (vs == null) {
                if (locality.alias != null)
                    locNames.addAll(locality.alias);
                if (locality.tags != null)
                    locNames.addAll(locality.tags);
                if (locality.zhName != null)
                    locNames.add(locality.zhName);
            } else if (locality == null) {
                if (vs.alias != null)
                    vsNames.addAll(vs.alias);
                if (vs.tags != null)
                    vsNames.addAll(vs.tags);
                if (vs.zhName != null)
                    vsNames.add(vs.zhName);
            } else {
                if (locality.alias != null)
                    locNames.addAll(locality.alias);
                if (locality.tags != null)
                    locNames.addAll(locality.tags);
                if (locality.zhName != null)
                    locNames.add(locality.zhName);
                if (vs.alias != null)
                    vsNames.addAll(vs.alias);
                if (vs.tags != null)
                    vsNames.addAll(vs.tags);
                if (vs.zhName != null)
                    vsNames.add(vs.zhName);
            }

            List<TravelNote> noteList = TravelNoteAPI.getTravelNote(locNames, vsNames, pageSize);
            for (TravelNote note : noteList) {
                nodeList.add(new SimpTravelNoteFormatter().format(note));
            }
            return Utils.createResponse(ErrorCode.NORMAL, Json.toJson(nodeList));
        } catch (ParseException | TravelPiException | NullPointerException e) {
            return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, "INVALID_ARGUMENT");
        } catch (SolrServerException e) {
            return Utils.createResponse(ErrorCode.UNKOWN_ERROR, "solr wrong");
        }
    }

    /**
     * 获得游记详情
     *
     * @param noteId
     * @return
     */
    public static Result getTravelNoteDetail(String noteId) {
        try {
            List<TravelNote> travelNoteList = TravelNoteAPI.getTravelNoteDetailApi(noteId);
            List<JsonNode> nodeList = new ArrayList<>();
            for (TravelNote note : travelNoteList) {
                nodeList.add(new DetailTravelNoteFormatter().format(note));
            }
            return Utils.createResponse(ErrorCode.NORMAL, Json.toJson(nodeList));
        } catch (ParseException | SolrServerException e) {
            return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, "INVALID_ARGUMENT");
        }
    }
}

