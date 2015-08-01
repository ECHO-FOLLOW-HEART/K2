package controllers.app

import aizou.core.{ TravelNoteAPI, PoiAPI }
import aspectj.UsingOcsCache
import aspectj.Key
import com.fasterxml.jackson.databind.ObjectMapper
import exception.{ AizouException, ErrorCode }
import formatter.FormatterFactory
import formatter.taozi.geo.DetailsEntryFormatter
import formatter.taozi.misc.TravelNoteFormatter
import models.poi._
import org.bson.types.ObjectId
import play.api.mvc.{ Action, Controller }
import utils.Utils

import scala.collection.JavaConversions._

/**
 * Created by pengyt on 2015/7/25.
 */
object TravelNoteCtrlScala extends Controller {

  @UsingOcsCache(key = "travelNoteDetails({noteId})")
  def travelNoteDetail(@Key(tag = "noteId") noteId: String) = Action {
    request =>
      {
        val travelNote = TravelNoteAPI.getNoteById(new ObjectId(noteId))

        if (travelNote == null)
          throw new AizouException(ErrorCode.INVALID_ARGUMENT, "TravelNote is null.Id:" + noteId)
        // 判断请求是何种格式
        val reqDataFormat = request.headers.get("Accept").get.toString
        if (reqDataFormat.contains("application/json")) {
          // 获取图片宽度
          val imgWidthStr = request.getQueryString("imgWidth")
          val imgWidth: Integer = if ((imgWidthStr nonEmpty) && (imgWidthStr != null)) imgWidthStr.get.toInt else 0
          val travelNoteFormatter = FormatterFactory.getInstance(classOf[TravelNoteFormatter], imgWidth)
          travelNoteFormatter.setLevel(TravelNoteFormatter.Level.DETAILED)
          Utils.createResponse(ErrorCode.NORMAL, travelNoteFormatter.formatNode(travelNote)).toScala
        } else if (reqDataFormat.contains("text/html")) {
          Ok(views.html.noteDetail(travelNote))
        } else {
          Utils.createResponse(ErrorCode.INVALID_ARGUMENT, String.format("Invalid http accept type: %s.", reqDataFormat)).toScala
        }
      }
  }

}