package controllers.taozi;

import aizou.core.MiscAPI;
import aizou.core.TravelNoteAPI;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import controllers.Key;
import controllers.UsingOcsCache;
import exception.AizouException;
import exception.ErrorCode;
import formatter.FormatterFactory;
import formatter.taozi.misc.TravelNoteFormatter;
import models.misc.TravelNote;
import org.apache.solr.client.solrj.SolrServerException;
import org.bson.types.ObjectId;
import play.mvc.Controller;
import play.mvc.Result;
import utils.Utils;

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
    @UsingOcsCache(key = "searchTravelNotes({keyword},{locId},{page},{pageSize})", expireTime = 3600)
    public static Result searchTravelNotes(@Key(tag = "keyword") String keyWord,
                                           @Key(tag = "locId") String locId,
                                           @Key(tag = "page") int page, @Key(tag = "pageSize") int pageSize) {
        List<TravelNote> noteList;
        JsonNode result;
        JsonNode note;
        String url = "http://h5.taozilvxing.com/dayDetail.php?id=";
        //通过关键字查询游记
        try {
            String imgWidthStr = request().getQueryString("imgWidth");
            int imgWidth = 0;
            if (imgWidthStr != null)
                imgWidth = Integer.valueOf(imgWidthStr);

            TravelNoteFormatter travelNoteFormatter = FormatterFactory.getInstance(TravelNoteFormatter.class, imgWidth);
            travelNoteFormatter.setLevel(TravelNoteFormatter.Level.SIMPLE);
            Long userId = null;
            if (request().hasHeader("UserId"))
                userId = Long.parseLong(request().getHeader("UserId"));

            if (!keyWord.isEmpty()) {
                noteList = TravelNoteAPI.searchNotesByWord(keyWord, page, pageSize);
                // 判断是否被收藏
                MiscAPI.isFavorite(noteList, userId);
                result = travelNoteFormatter.formatNode(noteList);
                return Utils.createResponse(ErrorCode.NORMAL, result);
            } else if (locId != null && !locId.isEmpty()) {
                noteList = TravelNoteAPI.searchNoteByLocId(locId, page, pageSize);
                // 判断是否被收藏
                MiscAPI.isFavorite(noteList, userId);
                result = travelNoteFormatter.formatNode(noteList);
                return Utils.createResponse(ErrorCode.NORMAL, result);
            } else
                return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, "fail");
        } catch (SolrServerException | AizouException | JsonProcessingException e) {
            return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, e.getMessage());
        }
    }

    /**
     * 获得游记详情
     *
     * @param noteId
     * @return
     */
    @UsingOcsCache(key = "travelNoteDetails({noteId})")
    public static Result travelNoteDetail(@Key(tag = "noteId") String noteId) {
        try {
            // 获取图片宽度
            String imgWidthStr = request().getQueryString("imgWidth");
            int imgWidth = 0;
            if (imgWidthStr != null)
                imgWidth = Integer.valueOf(imgWidthStr);
            TravelNote travelNote = TravelNoteAPI.getNoteById(new ObjectId(noteId));
            if (travelNote == null)
                return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, "TravelNote is null.Id:" + noteId);

            TravelNoteFormatter travelNoteFormatter = FormatterFactory.getInstance(TravelNoteFormatter.class, imgWidth);
            travelNoteFormatter.setLevel(TravelNoteFormatter.Level.DETAILED);
            return Utils.createResponse(ErrorCode.NORMAL, travelNoteFormatter.formatNode(travelNote));
        } catch (AizouException | JsonProcessingException e) {
            return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, "INVALID_ARGUMENT");
        }
    }
}

