package controllers.app;

import aizou.core.UserAPI;
import aspectj.CheckUser;
import com.fasterxml.jackson.databind.JsonNode;
import exception.AizouException;
import exception.ErrorCode;
import formatter.FormatterFactory;
import formatter.taozi.misc.AlbumFormatter;
import formatter.taozi.user.UserInfoFormatter;
import models.AizouBaseEntity;
import models.geo.Locality;
import models.misc.Album;
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
public class UserUgcCtrl extends Controller {

    /**
     * 取得用户的相册
     *
     * @param id
     * @return
     * @throws AizouException
     */
    @CheckUser
    public static Result getUserAlbums(@CheckUser Long id) throws AizouException {
        // 获取图片宽度
        String imgWidthStr = request().getQueryString("imgWidth");
        int imgWidth = 0;
        if (imgWidthStr != null)
            imgWidth = Integer.valueOf(imgWidthStr);
        List<Album> albums = UserAPI.getUserAlbums(id);
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
    @CheckUser
    public static Result deleteUserAlbums(@CheckUser Long id, String picId) throws AizouException {

        ObjectId oid = new ObjectId(picId);
        UserAPI.deleteUserAlbums(id, oid);
        return Utils.createResponse(ErrorCode.NORMAL, Json.toJson("successful"));
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
    public static Result getLocalitiesOfExpertUserTracks(String type, boolean abroad) throws AizouException {

        // TODO 分国内国外
        // TODO
        return Utils.createResponse(ErrorCode.AUTH_ERROR, "接口缺少调用");
    }

    /**
     * 根据拼音排序（未完成）
     *
     * @param rmdProvinceList
     */
    private static List<Locality> sortLocalityByPinyin(List<Locality> rmdProvinceList) {
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
     * @param type
     * @return
     * @throws AizouException
     */
    public static Result getExpertUserByTracks(String type) throws AizouException {
        JsonNode data = request().body().asJson();
        Iterator<JsonNode> iterator = data.get("locId").iterator();
        List<ObjectId> ids = new ArrayList<>();
        while (iterator.hasNext()) {
            ids.add(new ObjectId(iterator.next().asText()));
        }

        UserInfoFormatter formatter = FormatterFactory.getInstance(UserInfoFormatter.class);
        formatter.setSelfView(false);
        List<String> fields = Arrays.asList(AizouBaseEntity.FD_ID, UserInfo.fnEasemobUser, UserInfo.fnUserId, UserInfo.fnNickName,
                UserInfo.fnAvatar, UserInfo.fnAvatarSmall, UserInfo.fnGender, UserInfo.fnSignature, UserInfo.fnTel,
                UserInfo.fnDialCode, UserInfo.fnRoles, UserInfo.fnTravelStatus, UserInfo.fnTracks, UserInfo.fnTravelNotes,
                UserInfo.fnResidence, UserInfo.fnBirthday, UserInfo.fnZodiac, UserInfo.fnLevel);
        // TODO 取得包含此足迹的所有达人
        // TODO
        //UserAPI.fillUserInfo(user);
        return Utils.createResponse(ErrorCode.AUTH_ERROR, "接口缺少调用");

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
        // TODO
        return Utils.createResponse(ErrorCode.AUTH_ERROR, "接口缺少调用");

    }
}
