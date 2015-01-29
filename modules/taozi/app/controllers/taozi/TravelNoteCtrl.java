package controllers.taozi;

import aizou.core.LocalityAPI;
import aizou.core.MiscAPI;
import aizou.core.PoiAPI;
import aizou.core.TravelNoteAPI;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import exception.AizouException;
import exception.ErrorCode;
import formatter.taozi.TravelNote.DetailTravelNoteFormatter;
import formatter.taozi.TravelNote.SimpTravelNoteFormatter;
import models.AizouBaseEntity;
import models.geo.Locality;
import models.misc.TravelNote;
import models.poi.ViewSpot;
import org.apache.solr.client.solrj.SolrServerException;
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

    /**
     * 游记搜索
     *
     * @param keyWord
     * @param locId
     * @param page
     * @param pageSize
     * @return
     */
    public static Result searchTravelNotes(String keyWord, String locId, int page, int pageSize) {
        List<TravelNote> noteList;
        List<JsonNode> result = new ArrayList<>();
        JsonNode note;
        String url = "http://h5.taozilvxing.com/dayDetail.php?id=";
        //通过关键字查询游记
        try {
            Long userId = null;
            if (request().hasHeader("UserId"))
                userId = Long.parseLong(request().getHeader("UserId"));

            if (!keyWord.isEmpty()) {
                noteList = TravelNoteAPI.searchNotesByWord(keyWord, page, pageSize);
                // 判断是否被收藏
                MiscAPI.isFavorite(noteList, userId);
                for (TravelNote travelNote : noteList) {
                    note = new SimpTravelNoteFormatter().format(travelNote);
                    ((ObjectNode) note).put("detailUrl", url + travelNote.getId());
                    result.add(note);
                }

                return Utils.createResponse(ErrorCode.NORMAL, Json.toJson(result));
            } else if (locId != null && !locId.isEmpty()) {
                noteList = TravelNoteAPI.searchNoteByLocId(locId, page, pageSize);
                // 判断是否被收藏
                MiscAPI.isFavorite(noteList, userId);
                for (TravelNote travelNote : noteList) {
                    note = new SimpTravelNoteFormatter().format(travelNote);
                    ((ObjectNode) note).put("detailUrl", url + travelNote.getId());
                    result.add(note);
                }
                return Utils.createResponse(ErrorCode.NORMAL, Json.toJson(result));
            } else
                return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, "fail");
        } catch (SolrServerException | AizouException e) {
            return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, e.getMessage());
        }
    }

    /**
     * 获得游记详情
     *
     * @param noteId
     * @return
     */
    public static Result travelNoteDetail(String noteId) {
        try {
            // 获取图片宽度
            String imgWidthStr = request().getQueryString("imgWidth");
            int imgWidth = 0;
            if (imgWidthStr != null)
                imgWidth = Integer.valueOf(imgWidthStr);
            TravelNote travelNote = TravelNoteAPI.getNoteById(new ObjectId(noteId));
            if (travelNote == null)
                return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, "TravelNote is null.Id:" + noteId);
            return Utils.createResponse(ErrorCode.NORMAL, new DetailTravelNoteFormatter().setImageWidth(imgWidth).format(travelNote));
        } catch (AizouException e) {
            return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, "INVALID_ARGUMENT");
        }
    }
}

