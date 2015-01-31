package controllers.taozi;

import aizou.core.GeoAPI;
import aizou.core.MiscAPI;
import aizou.core.PoiAPI;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.CacheKey;
import controllers.CheckLastModify;
import controllers.UsingCache;
import exception.AizouException;
import exception.ErrorCode;
import formatter.FormatterFactory;
import formatter.taozi.geo.DetailedLocalityFormatter;
import formatter.taozi.geo.DetailsEntryFormatter;
import formatter.taozi.geo.SimpleCountryFormatter;
import formatter.taozi.geo.SimpleLocalityFormatter;
import models.geo.Country;
import models.geo.DetailsEntry;
import models.geo.Locality;
import org.bson.types.ObjectId;
import play.Configuration;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import utils.Constants;
import utils.Utils;

import java.text.ParseException;
import java.util.*;


/**
 * 地理相关
 * <p/>
 * Created by zephyre on 14-6-20.
 */
public class GeoCtrl extends Controller {
    /**
     * 根据id查看城市详情
     *
     * @param id 城市ID
     * @return
     */
    @UsingCache(key = "getLocality({id})", expireTime = 3600)
    public static Result getLocality(@CacheKey(tag="id") String id) {
        try {
            // 获取图片宽度
            String imgWidthStr = request().getQueryString("imgWidth");
            int imgWidth = 0;
            if (imgWidthStr != null)
                imgWidth = Integer.valueOf(imgWidthStr);
            Long userId;
            if (request().hasHeader("UserId"))
                userId = Long.parseLong(request().getHeader("UserId"));
            else
                userId = null;
            Locality locality = GeoAPI.locDetails(id);
            if (locality == null)
                return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, "Locality not exist.");
            //是否被收藏
            MiscAPI.isFavorite(locality, userId);
            ObjectNode response = (ObjectNode) new DetailedLocalityFormatter().setImageWidth(imgWidth).format(locality);
            // 显示图集的数量
            response.put("imageCnt", MiscAPI.getLocalityAlbumCount(locality.getId()));
            return Utils.createResponse(ErrorCode.NORMAL, response);
        } catch (AizouException e) {
            return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, e.getMessage());
        }
    }

    /**
     * 获得城市图集列表
     *
     * @param id
     * @param page
     * @param pageSize
     * @return
     */
    @UsingCache(key = "getLocalityAlbums({id},{page},{pageSize})", expireTime = 3600)
    public static Result getLocalityAlbums(@CacheKey(tag = "id") String id,
                                           @CacheKey(tag = "page") int page,
                                           @CacheKey(tag = "pageSize") int pageSize) {
        return MiscCtrl.getAlbums(id, page, pageSize);
    }

    @UsingCache(key = "getLMD()", expireTime = 30)
    public static long getLMD(boolean abroad, int page) {
        return System.currentTimeMillis()-3600;
    }

    /**
     * 获得国内国外目的地
     *
     * @param abroad
     * @param page
     * @param pageSize
     * @return
     */
    @UsingCache(key = "destinations(abroad={abroad})", expireTime = 3600)
    @CheckLastModify(callback = "getLMD", args = "{abroad}|{page}")
    public static Result exploreDestinations(@CacheKey(tag = "abroad") boolean abroad,
                                             @CacheKey(tag = "page")int page,
                                             int pageSize) {
        try {
            long t0 = System.currentTimeMillis();
            Http.Request req = request();
            Http.Response rsp = response();
            // 获取图片宽度
            String imgWidthStr = request().getQueryString("imgWidth");
            int imgWidth = 0;
            if (imgWidthStr != null)
                imgWidth = Integer.valueOf(imgWidthStr);
            Configuration config = Configuration.root();
            Map destnations = (Map) config.getObject("destinations");
            String lastModify = destnations.get("lastModify").toString();
            // 添加缓存用的相应头
            Utils.addCacheResponseHeader(rsp, lastModify);
            if (Utils.useCache(req, lastModify))
                return status(304, "Content not modified, dude.");

            List<ObjectNode> objs = new ArrayList<>();

            if (abroad) {
                String countrysStr = destnations.get("country").toString();
                List<String> countryNames = Arrays.asList(countrysStr.split(Constants.SYMBOL_SLASH));
                List<Country> countryList = GeoAPI.searchCountryByName(countryNames, Constants.ZERO_COUNT,
                        Constants.MAX_COUNT);

                SimpleCountryFormatter formatter = new SimpleCountryFormatter();
                if (imgWidth > 0)
                    formatter.setImageWidth(imgWidth);
                JsonNode destResult = formatter.formatNode(countryList);

                for (Iterator<JsonNode> itr = destResult.elements(); itr.hasNext(); ) {
                    ObjectNode cNode = (ObjectNode) itr.next();
                    JsonNode localities = getDestinationsNodeByCountry(new ObjectId(cNode.get("id").asText()), page, 30);
                    cNode.put("destinations", localities);
                }

                return Utils.createResponse(ErrorCode.NORMAL, destResult);
            } else {
                Map<String, Object> mapConf = Configuration.root().getConfig("domestic").asMap();
                Map<String, Object> pinyinConf = Configuration.root().getConfig("pinyin").asMap();
                String k;
                Object v, pinyinObj;
                ObjectNode node;
                String zhName = null;
                String pinyin = null;
                for (Map.Entry<String, Object> entry : mapConf.entrySet()) {
                    k = entry.getKey();
                    v = entry.getValue();
                    if (v != null)
                        zhName = v.toString();

                    pinyinObj = pinyinConf.get(k);
                    if (pinyinObj != null)
                        pinyin = pinyinObj.toString();

                    node = Json.newObject();
                    node.put("id", k);
                    node.put("zhName", zhName);
                    node.put("enName", "");
                    node.put("pinyin", pinyin);
                    objs.add(node);
                }

                return Utils.createResponse(rsp, lastModify, ErrorCode.NORMAL, Json.toJson(objs));
            }
        } catch (AizouException | ParseException e) {
            return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, e.getMessage());
        } catch (ReflectiveOperationException | JsonProcessingException e) {
            return Utils.createResponse(ErrorCode.UNKOWN_ERROR, e.getMessage());
        }
    }

    /**
     * 根据国家取得目的地
     *
     * @param id
     * @param page
     * @param pageSize
     * @return
     * @throws exception.AizouException
     */
    private static JsonNode getDestinationsNodeByCountry(ObjectId id, int page, int pageSize)
            throws AizouException, ReflectiveOperationException, JsonProcessingException {
        List<Locality> localities = GeoAPI.getDestinationsByCountry(id, page, pageSize);

        SimpleLocalityFormatter formatter = FormatterFactory.getInstance(SimpleLocalityFormatter.class);
        return formatter.formatNode(localities);
    }

    /**
     * 游玩攻略-H5
     *
     * @param locId
     * @param field
     * @return
     */
    public static Result getTravelGuide(String locId, String field) {
        try {
            if (field == null || field.isEmpty())
                return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, "INVALID_ARGUMENT");
            List<String> fieldList = new ArrayList<>();
            switch (field) {
                case "remoteTraffic":
                    fieldList.add(Locality.fnRemoteTraffic);
                    break;
                case "localTraffic":
                    fieldList.add(Locality.fnLocalTraffic);
                    break;
                case "activities":
                    Collections.addAll(fieldList, Locality.fnActivityIntro, Locality.fnActivities);
                    break;
                case "tips":
                    fieldList.add(Locality.fnTips);
                    break;
                case "specials":
                    fieldList.add(Locality.fnSpecials);
                    break;
                case "geoHistory":
                    fieldList.add(Locality.fnGeoHistory);
                    break;
                case "dining":
                    Collections.addAll(fieldList, Locality.fnDinningIntro, Locality.fnCuisines);
                    break;
                case "shopping":
                    Collections.addAll(fieldList, Locality.fnShoppingIntro, Locality.fnCommodities);
                    break;
                case "desc":
                    fieldList.add(Locality.fnDesc);
                    break;
                default:
                    throw new AizouException(ErrorCode.INVALID_ARGUMENT, "INVALID_ARGUMENT");
            }
            Locality locality = PoiAPI.getLocalityByField(new ObjectId(locId), fieldList);
            if (locality == null)
                return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, "Locality is not exist.ID:" + locId);
            ObjectNode result = Json.newObject();
            if (field.equals("remoteTraffic")) {
                result.put("desc", "");
                result.put("contents", Json.toJson(contentsToList(locality.getRemoteTraffic())));
            } else if (field.equals("localTraffic")) {
                result.put("desc", "");
                result.put("contents", Json.toJson(contentsToList(locality.getLocalTraffic())));
            } else if (field.equals("activities")) {
                result.put("desc", locality.getActivityIntro());
                result.put("contents", Json.toJson(contentsToList(locality.getActivities())));
            } else if (field.equals("tips")) {
                result.put("desc", "");
                result.put("contents", Json.toJson(contentsToList(locality.getTips())));
            } else if (field.equals("geoHistory")) {
                result.put("desc", "");
                result.put("contents", Json.toJson(contentsToList(locality.getGeoHistory())));
            } else if (field.equals("specials")) {
                result.put("desc", "");
                result.put("contents", Json.toJson(contentsToList(locality.getSpecials())));
            } else if (field.equals("desc")) {
                result.put("desc", locality.getDesc());
                result.put("contents", Json.toJson(new ArrayList<>()));
            } else if (field.equals("dining")) {
                result.put("desc", locality.getDiningIntro());
                result.put("contents", Json.toJson(contentsToList(locality.getCuisines())));
            } else if (field.equals("shopping")) {
                result.put("desc", locality.getShoppingIntro());
                result.put("contents", Json.toJson(contentsToList(locality.getCommodities())));
            }
            return Utils.createResponse(ErrorCode.NORMAL, result);
        } catch (AizouException | NullPointerException | NumberFormatException e) {
            return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, "INVALID_ARGUMENT");
        }
    }

    public static List<ObjectNode> contentsToList(List<DetailsEntry> entries) {
        if (entries == null)
            return new ArrayList<>();
        List<ObjectNode> objs = new ArrayList<>();
        for (DetailsEntry entry : entries) {
            objs.add((ObjectNode) new DetailsEntryFormatter().format(entry));
        }
        return objs;
    }

    /**
     * 游玩攻略概览-H5
     *
     * @param locId
     * @return
     */
    public static Result getTravelGuideOutLine(String locId) {
        Locality loc = null;
        try {
            loc = GeoAPI.locDetails(new ObjectId(locId), Arrays.asList("zhName"));
        } catch (AizouException e) {
        }

        ObjectNode node;
        List<ObjectNode> result = new ArrayList<>();
        node = Json.newObject();
        node.put("title", "如何去" + (loc == null ? "" : loc.getZhName()));
        node.put("fields", Json.toJson(Arrays.asList("remoteTraffic")));
        result.add(node);

        node = Json.newObject();
        node.put("title", "当地交通");
        node.put("fields", Json.toJson(Arrays.asList("localTraffic")));
        result.add(node);

        node = Json.newObject();
        node.put("title", "节庆与民俗活动");
        node.put("fields", Json.toJson(Arrays.asList("activities")));
        result.add(node);

        node = Json.newObject();
        node.put("title", "旅行游玩小贴士");
        node.put("fields", Json.toJson(Arrays.asList("tips")));
        result.add(node);

        node = Json.newObject();
        node.put("title", "宗教、文化与历史");
        node.put("fields", Json.toJson(Arrays.asList("geoHistory")));
        result.add(node);

        node = Json.newObject();
        node.put("title", "不可错过的游玩体验");
        node.put("fields", Json.toJson(Arrays.asList("specials")));
        result.add(node);

//        node = Json.newObject();
//        node.put("title", "描述");
//        node.put("fields", Json.toJson(Arrays.asList("desc")));
//        result.add(node);

//        node = Json.newObject();
//        node.put("title", "地道美食");
//        node.put("fields", Json.toJson(Arrays.asList("dining")));
//        result.add(node);
//
//        node = Json.newObject();
//        node.put("title", "购物指南");
//        node.put("fields", Json.toJson(Arrays.asList("shopping")));
//        result.add(node);

        return Utils.createResponse(ErrorCode.NORMAL, Json.toJson(result));

    }

}
