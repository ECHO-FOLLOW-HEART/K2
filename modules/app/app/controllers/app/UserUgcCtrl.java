package controllers.app;

import aizou.core.UserUgcAPI;
import aspectj.CheckUser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import exception.AizouException;
import exception.ErrorCode;
import formatter.FormatterFactory;
import formatter.taozi.geo.SimpleLocalityWithLocationFormatter;
import formatter.taozi.misc.AlbumFormatter;
import formatter.taozi.user.TrackFormatter;
import formatter.taozi.user.UserInfoSimpleFormatter;
import models.geo.Locality;
import models.misc.Album;
import models.misc.Track;
import models.user.UserInfo;
import org.bson.types.ObjectId;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import utils.Utils;

import java.util.*;

/**
 * Created by topy on 2015/6/29.
 */
public class  UserUgcCtrl extends Controller {

    public static final List<Long> expertUserIds = Arrays.asList(Long.valueOf(11000), Long.valueOf(100000), Long.valueOf(100003),
            Long.valueOf(100057), Long.valueOf(100076), Long.valueOf(100093), Long.valueOf(100001),
            Long.valueOf(100015), Long.valueOf(100025), Long.valueOf(100002), Long.valueOf(100004),
            Long.valueOf(100005), Long.valueOf(100009), Long.valueOf(100010), Long.valueOf(100011),
            Long.valueOf(100012), Long.valueOf(100014), Long.valueOf(100031), Long.valueOf(100035),
            Long.valueOf(100040), Long.valueOf(100056), Long.valueOf(100067), Long.valueOf(100068),
            Long.valueOf(100073), Long.valueOf(100089), Long.valueOf(100090), Long.valueOf(100091));

    /**
     * 取得用户的相册
     *
     * @param id
     * @return
     * @throws AizouException
     */

    public static Result getUserAlbums(Long id) throws AizouException {
        // 获取图片宽度
        String imgWidthStr = request().getQueryString("imgWidth");
        int imgWidth = 0;
        if (imgWidthStr != null)
            imgWidth = Integer.valueOf(imgWidthStr);
        List<Album> albums = UserUgcAPI.getUserAlbums(id);
        AlbumFormatter formatter = FormatterFactory.getInstance(AlbumFormatter.class, imgWidth);
        return Utils.status(formatter.format(albums));
    }

    /**
     * 删除用户相册
     *
     * @param id
     * @return
     * @throws AizouException
     */
    //@CheckUser
    public static Result deleteUserAlbums(Long id, String picId) throws AizouException {

        ObjectId oid = new ObjectId(picId);
        UserUgcAPI.deleteUserAlbums(id, oid);
        return Utils.createResponse(ErrorCode.NORMAL, Json.toJson("successful"));
    }

    public static Result getUserTracks(Long id) throws AizouException {
        // 获取图片宽度
        String imgWidthStr = request().getQueryString("imgWidth");
        int imgWidth = 0;
        if (imgWidthStr != null)
            imgWidth = Integer.valueOf(imgWidthStr);
        List<Track> tracks = UserUgcAPI.getUserTracks(id);
        List<Locality> localities = new ArrayList<>();
        for (Track track : tracks)
            localities.add(track.getLocality());
        SimpleLocalityWithLocationFormatter formatter = FormatterFactory.getInstance(SimpleLocalityWithLocationFormatter.class, imgWidth);
        return Utils.status(formatter.format(localities));
    }


    /**
     * 取得所有达人用户去过的目的地
     * <p>
     * 此处type未使用，以备扩展
     *
     * @param type
     * @return
     * @throws AizouException
     */
    public static Result getLocalitiesOfExpertUserTracks(String type) throws AizouException {
        // 获取图片宽度
        String imgWidthStr = request().getQueryString("imgWidth");
        int imgWidth = 0;
        if (imgWidthStr != null)
            imgWidth = Integer.valueOf(imgWidthStr);

        List<Track> userTracks = UserUgcAPI.getUserTracks(expertUserIds);

        Map<ObjectId, Track> map = new HashMap<>();
        for (Track track : userTracks)
            map.put(track.getLocality().getId(), track);

        Map<String, List<Track>> mapCountry = new HashMap<>();
        List<Track> tempList;
        for (Track track : map.values()) {
            tempList = mapCountry.get(track.getCountry().getZhName());
            if (tempList == null) {
                List<Track> tempTracks = new ArrayList<>();
                tempTracks.add(track);
                mapCountry.put(track.getCountry().getZhName(), tempTracks);
            } else {
                tempList.add(track);
                mapCountry.put(track.getCountry().getZhName(), tempList);
            }
        }
        TrackFormatter formatter = FormatterFactory.getInstance(TrackFormatter.class, imgWidth);
        ObjectNode node = Json.newObject();

        for (Map.Entry<String, List<Track>> entry : mapCountry.entrySet())
            node.put(entry.getKey(), formatter.formatNode(entry.getValue()));
        return Utils.createResponse(ErrorCode.NORMAL, node);
    }


    /**
     * 根据拼音排序（未完成）
     *
     * @param rmdProvinceList
     */
    public static List<Locality> sortLocalityByPinyin(List<Locality> rmdProvinceList) {
        Collections.sort(rmdProvinceList, new Comparator<Locality>() {
            public int compare(Locality arg0, Locality arg1) {
                return arg0.getZhName().compareTo(arg1.getZhName()) > 0 ? 1 : -1;
            }
        });
        return rmdProvinceList;
    }


    /**
     * 取得包含此足迹的所有达人
     *
     * @return
     * @throws AizouException
     */
    public static Result getExpertUserByTracks(String code) throws AizouException {
        JsonNode data = request().body().asJson();
        List<ObjectId> countryIds = Arrays.asList(new ObjectId(code));

        Config config = ConfigFactory.load();
        List experts = config.getLongList("experts.id");
        // 取得足迹
        List<Track> expertUserByCountry = UserUgcAPI.getExpertUserByCountry(countryIds, experts);

        // 取得用户信息
        Set<Long> usersUnDup = new HashSet<>();
        for (Track track : expertUserByCountry)
            usersUnDup.add(track.getUserId());
        List<Long> users = new ArrayList(usersUnDup);
        Map<Long, UserInfo> userInfoMap = UserCtrlScala.getUsersInfoValue(users);

        // 组装信息
        JsonNode result = Json.newObject();
        ObjectNode node;
        List<JsonNode> nodeList = new ArrayList<>();
        UserInfoSimpleFormatter formatter = new UserInfoSimpleFormatter();
        Map<Long, List<Track>> mapCountry = new HashMap<>();
        List<Track> tempList;
        for (Track track : expertUserByCountry) {
            tempList = mapCountry.get(track.getUserId());
            if (tempList == null) {
                tempList = new ArrayList();
                tempList.add(track);
                mapCountry.put(track.getUserId(), tempList);
            } else {
                tempList.add(track);
                mapCountry.put(track.getUserId(), tempList);
            }
        }
        for (Map.Entry<Long, List<Track>> entry : mapCountry.entrySet()) {
            node = (ObjectNode) formatter.formatNode(userInfoMap.get(entry.getKey()));
            node.put("localityCnt", entry.getValue().size());
            nodeList.add(node);
        }
        result = Json.toJson(nodeList);

        return Utils.createResponse(ErrorCode.NORMAL, result);
    }

    /**
     * 修改用户足迹
     *
     * @param id
     * @return
     * @throws AizouException
     */
    public static Result modifyTracks(@CheckUser Long id) throws AizouException {
        JsonNode data = request().body().asJson();
        Iterator<JsonNode> iterator = data.get("tracks").elements();
        String action = data.get("action").asText();
        UserUgcAPI.modifyTracksWithSearch(id, action, iterator);
        return Utils.createResponse(ErrorCode.NORMAL, "Success.");
    }

}
