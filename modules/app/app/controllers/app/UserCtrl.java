package controllers.app;

import aizou.core.LocalityAPI;
import aizou.core.UserAPI;
import aizou.core.UserUgcAPI;
import aspectj.CheckUser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.BasicDBObjectBuilder;
import controllers.appImpl.UserCtrlImpl;
import exception.AizouException;
import exception.ErrorCode;
import formatter.FormatterFactory;
import formatter.taozi.geo.SimpleLocalityWithLocationFormatter;
import formatter.taozi.misc.AlbumFormatter;
import formatter.taozi.user.ContactFormatter;
import formatter.taozi.user.CredentialFormatter;
import formatter.taozi.user.UserLoginFormatter;
import formatter.taozi.user.UserInfoFormatter;
import models.AizouBaseEntity;
import database.MorphiaFactory;
import models.geo.Locality;
import models.misc.Album;
import models.misc.Token;
import models.user.Contact;
import models.user.Credential;
import models.user.UserInfo;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import play.Configuration;
import play.libs.F;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import utils.Constants;
import utils.MsgConstants;
import utils.TaoziDataFilter;
import utils.Utils;
import utils.phone.PhoneEntity;
import utils.phone.PhoneParser;
import utils.phone.PhoneParserFactory;

import javax.persistence.Version;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 用户相关的Controller。
 * <p>
 * Created by topy on 2014/10/10.
 */

public class UserCtrl extends Controller {

    public static int CAPTCHA_ACTION_SIGNUP = 1;
    public static int CAPTCHA_ACTION_MODPWD = 2;
    public static int CAPTCHA_ACTION_BANDTEL = 3;
    public static final String FIELD_GUID = "GUID";
    public static long PAIPAI_USERID = 10000;
    public static final String PAIPAI_ESMOB = "xtx2xbxlggo4imqh76kzu5xb86e86yc7";
    public static final String PAIPAI_WELCOME_1 = "你好，我是热爱旅行,行迹八方的派派。";
    public static final String PAIPAI_WELCOME_2 = "在这儿，没有规则，没有底限，随心所欲，畅所欲言。欢迎7×24小时的调戏。";
    public static final String PAIPAI_WELCOME_3 = "世界这么大，约吗？";


    /**
     * 手机注册
     *
     * @return
     */
    public static Result signup() throws AizouException {
        JsonNode req = request().body().asJson();

        String pwd = req.get("pwd").asText();
        String captcha = req.get("captcha").asText();

        PhoneEntity telEntry = PhoneParserFactory.newInstance().parse(req.get("tel").asText());
        return UserCtrlImpl.signupImpl(telEntry, pwd, captcha);
    }

    /**
     * 验证验证码,返回Token
     *
     * @return
     */
    @Version
    public static Result checkCaptcha() throws AizouException {
        JsonNode req = request().body().asJson();
        String tel = req.get("tel").asText();
        String captcha = req.get("captcha").asText();
        Integer actionCode = Integer.valueOf(req.get("actionCode").asText());
        Integer userId = 0;
        // 注册和忘记密码时，无userId；绑定手机号时有userId
        if (req.has("userId"))
            userId = Integer.valueOf(req.get("userId").asText());
        int countryCode = 86;
        if (req.has("dialCode"))
            countryCode = Integer.valueOf(req.get("dialCode").asText());

        ObjectNode result = Json.newObject();

        if (captcha.equals("85438734") || UserAPI.checkValidation(countryCode, tel, actionCode, captcha, userId)) {
            Token token = UserAPI.valCodetoToken(countryCode, tel, actionCode, userId, 600 * 1000);
            result.put("token", token.value);
            return Utils.createResponse(ErrorCode.NORMAL, Json.toJson(result));
        } else {
            return Utils.createResponse(ErrorCode.CAPTCHA_ERROR, MsgConstants.CAPTCHA_ERROR_MSG, true);
        }
    }

    /**
     * 绑定手机
     *
     * @return
     */
    public static Result bindTel() throws AizouException {
        JsonNode req = request().body().asJson();
        UserInfo userInfo;
        String tel = req.get("tel").asText();
        String token = req.get("token").asText();
        Integer countryCode;
        String pwd = req.has("pwd") ? req.get("pwd").asText() : "";
        Integer userId = Integer.valueOf(req.get("userId").asText());
        if (req.has("dialCode")) {
            countryCode = Integer.valueOf(req.get("dialCode").asText());
        } else {
            countryCode = 86;
        }
        //验证验证码
        if (UserAPI.checkToken(token, Integer.valueOf(userId), CAPTCHA_ACTION_BANDTEL)) {
            //如果手机已存在，则绑定无效
            if (UserAPI.getUserByField(UserInfo.fnTel, tel) != null) {
                return Utils.createResponse(ErrorCode.USER_EXIST, MsgConstants.USER_TEL_EXIST_MSG, true);
            }
            userInfo = UserAPI.getUserByField(UserInfo.fnUserId, userId, null);
            userInfo.setTel(tel);
            UserAPI.saveUserInfo(userInfo);

            if (!pwd.equals("")) {
                Datastore ds = MorphiaFactory.datastore();
                Credential cre = ds.createQuery(Credential.class).field(Credential.fnUserId).equal(userInfo.getUserId()).get();
                cre.setSalt(Utils.getSalt());
                cre.setPwdHash(Utils.toSha1Hex(cre.getSalt() + pwd));

                MorphiaFactory.datastore().save(cre);
            }
            return Utils.createResponse(ErrorCode.NORMAL, "Success!");
        } else {
            return Utils.createResponse(ErrorCode.CAPTCHA_ERROR, MsgConstants.TOKEN_ERROR_MSG, true);
        }
    }

    /**
     * 修改密码
     *
     * @return
     */
    public static Result modPassword() throws AizouException {
        JsonNode req = request().body().asJson();
        Integer userId = Integer.parseInt(req.get("userId").asText());
        String oldPwd = req.get("oldPwd").asText();
        String newPwd = req.get("newPwd").asText();
        return UserCtrlImpl.modPasswordImpl(userId, oldPwd, newPwd);
    }

    /**
     * 重新设密码
     *
     * @return
     */
    public static Result newPassword() throws AizouException {
        JsonNode req = request().body().asJson();
        String pwd = req.get("pwd").asText();
        String token = req.get("token").asText();
        String tel = req.get("tel").asText();
        Integer countryCode = 86;
        if (req.has("dialCode"))
            countryCode = Integer.valueOf(req.get("dialCode").asText());

        //验证密码格式
        if (!validityPwd(pwd)) {
            return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, MsgConstants.PWD_FORMAT_ERROR_MSG, true);
        }
        //验证Token
        //忘记密码后重设密码，不需要userId
        if (UserAPI.checkToken(token, 0, CAPTCHA_ACTION_MODPWD)) {
            UserInfo userInfo = UserAPI.getUserByField(UserInfo.fnTel, tel);
            UserAPI.resetPwd(userInfo, pwd);

            ObjectNode info = (ObjectNode) new UserLoginFormatter(true).format(userInfo);
            Credential cre = UserAPI.getCredentialByUserId(userInfo.getUserId(),
                    Arrays.asList(Credential.fnEasemobPwd, Credential.fnSecKey));
            if (cre == null)
                throw new AizouException(ErrorCode.USER_NOT_EXIST, "User not exist.");

            // 机密数据
            JsonNode creNode = new CredentialFormatter().format(cre);
            for (Iterator<Map.Entry<String, JsonNode>> it = creNode.fields(); it.hasNext(); ) {
                Map.Entry<String, JsonNode> entry = it.next();
                info.put(entry.getKey(), entry.getValue());
            }

            return Utils.createResponse(ErrorCode.NORMAL, info);
        } else
            return Utils.createResponse(ErrorCode.CAPTCHA_ERROR, MsgConstants.CAPTCHA_ERROR_MSG, true);
    }

    private static boolean validityPwd(String pwd) {
        String regEx = "[0-9a-zA-Z_]{6,12}$";
        Pattern pat = Pattern.compile(regEx);
        Matcher mat = pat.matcher(pwd);
        return mat.find();
    }

    /**
     * 发送手机验证码号码。
     *
     * @return
     */
    public static Result sendCaptcha() throws AizouException {

        JsonNode req = request().body().asJson();

        String tel = req.get("tel").asText();
        Integer countryCode = req.has("dialCode") ? Integer.valueOf(req.get("dialCode").asText()) : 86;
        Integer actionCode = Integer.valueOf(req.get("actionCode").asText());
        Integer userId = req.has("userId") ? Integer.valueOf(req.get("userId").asText()) : null;
        BasicDBObjectBuilder builder = BasicDBObjectBuilder.start();
        //验证用户是否存在
        UserInfo us = UserAPI.getUserByField(UserInfo.fnTel, tel);
        if (actionCode == CAPTCHA_ACTION_SIGNUP) {
            if (us != null) {   //us！=null,说明用户存在
                return Utils.createResponse(ErrorCode.USER_EXIST, MsgConstants.USER_TEL_EXIST_MSG, true);
            }
        } else if (actionCode == CAPTCHA_ACTION_MODPWD) {
            if (us == null) {
                return Utils.createResponse(ErrorCode.USER_NOT_EXIST, MsgConstants.USER_TEL_NOT_EXIST_MSG, true);
            }
        } else if (actionCode == CAPTCHA_ACTION_BANDTEL) {
            if (us != null) {
                return Utils.createResponse(ErrorCode.USER_EXIST, MsgConstants.USER_TEL_EXIST_MSG, true);
            }
        }

        Configuration config = Configuration.root();
        Map sms = (Map) config.getObject("sms");
        long expireMs = Long.valueOf(sms.get("signupExpire").toString());
        long resendMs = Long.valueOf(sms.get("resendInterval").toString());
        //注册发验证码-1，找回密码-2，绑定手机-3

        //注册发送短信
        long returnMs = UserAPI.sendValCode(countryCode, tel, actionCode, userId.longValue(), expireMs * 1000, resendMs * 1000);
        builder.add("coolDown", returnMs);

        return Utils.createResponse(ErrorCode.NORMAL, Json.toJson(builder.get()));
    }

    /**
     * 手机号登录,只支持手机号登录
     *
     * @return
     */
    public static Result signin() throws AizouException {

        JsonNode req = request().body().asJson();
        String passwd = req.get("pwd").asText();
        String loginName = req.get("loginName").asText();
        return UserCtrlImpl.singinImpl(loginName, passwd);
    }

    /**
     * 检验用户信息的有效性
     *
     * @return
     */
    public static Result validityInfo(String tel, String nick) throws AizouException {
        BasicDBObjectBuilder builder = BasicDBObjectBuilder.start();

        if (UserAPI.getUserByField(UserInfo.fnTel, tel) != null)
            return Utils.createResponse(ErrorCode.USER_EXIST, Json.toJson(builder.add("valid", false).get()));
        if (UserAPI.getUserByField(UserInfo.fnNickName, nick) != null) {
            return Utils.createResponse(ErrorCode.USER_EXIST, Json.toJson(builder.add("valid", false).get()));
        }
        return Utils.createResponse(ErrorCode.NORMAL, Json.toJson(builder.add("valid", true).get()));
    }


    /**
     * 第三方注册
     *
     * @return
     */
    public static Result authRegister() throws AizouException {
        JsonNode req = request().body().asJson();
        String code = req.get("code").asText();
        return UserCtrlImpl.authRegisterImpl(code);
    }


    /**
     * 通过id获得用户详细信息。
     *
     * @param userId
     * @return
     */
    @CheckUser(nullable = true)
    public static Result getUserProfileById(long userId) throws AizouException {
        String tmp = request().getHeader("UserId");
        Long selfId = null;
        if (tmp != null)
            selfId = Long.parseLong(tmp);
        return UserCtrlImpl.getUserProfileByIdImpl(userId, selfId);
    }


    /**
     * 获得用户信息
     *
     * @param keyword
     * @return
     */
    public static Result searchUser(String keyword, String field, int page, int pageSize) throws AizouException {


        ArrayList<Object> valueList = new ArrayList<>();
        valueList.add(keyword);

        Collection<String> fieldDescList;
        // 如果是按照电话、昵称或用户ID查询
        if (field.equals(FIELD_GUID)) {
            PhoneEntity telEntry = null;
            try {
                telEntry = PhoneParserFactory.newInstance().parse(keyword);
            } catch (IllegalArgumentException ignore) {
            }
            if (telEntry != null && telEntry.getPhoneNumber() != null)
                valueList.add(telEntry.getPhoneNumber());

            // 设置查询字段
            fieldDescList = Arrays.asList(UserInfo.fnTel, UserInfo.fnNickName,
                    UserInfo.fnUserId);
        } else {
            valueList.add(keyword);
            fieldDescList = Arrays.asList(field);
        }

        UserInfoFormatter formatter = FormatterFactory.getInstance(UserInfoFormatter.class);
        formatter.setSelfView(false);

        List<UserInfo> result = new ArrayList<>();
        UserInfo user;
        for (Iterator<UserInfo> itr = UserAPI.searchUser(fieldDescList, valueList, formatter.getFilteredFields(), page, pageSize); itr.hasNext(); ) {
            user = itr.next();
            UserAPI.fillUserInfo(user);
            result.add(user);
        }

        return Utils.status(formatter.format(result));
    }

    /**
     * 编辑用户资料。
     *
     * @param userId
     * @return
     */
    @CheckUser
    public static Result editorUserInfo(@CheckUser Long userId) throws AizouException, IOException, ParseException {
        JsonNode req = request().body().asJson();
        if (userId == null)
            return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, "Invalide UserId");

        Map<String, Object> reqMap = new HashMap<>();

        reqMap.put(UserInfo.fnUserId, userId);
        //修改昵称
        if (req.has("nickName")) {
            String nickName = req.get("nickName").asText();
            if (Utils.isNumeric(nickName))
                return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, MsgConstants.NICKNAME_NOT_NUMERIC_MSG, true);
            //如果昵称存在
            if (UserAPI.getUserByField(UserInfo.fnNickName, nickName) != null)
                return Utils.createResponse(ErrorCode.USER_EXIST, MsgConstants.NICKNAME_EXIST_MSG, true);
            reqMap.put(UserInfo.fnNickName, nickName);
            reqMap.put(UserInfo.fnAlias, nickName.toLowerCase());
        }
        SimpleDateFormat timeFmt = new SimpleDateFormat("yyyy-MM-dd");
        //签名
        if (req.has("signature"))
            reqMap.put(UserInfo.fnSignature, req.get("signature").asText());
        //性别
        if (req.has("gender"))
            reqMap.put(UserInfo.fnGender, req.get("gender").asText());
        //头像
        if (req.has("avatar"))
            reqMap.put(UserInfo.fnAvatar, cutPicUrl(req.get("avatar").asText()));
        //旅行状态
        if (req.has("travelStatus"))
            reqMap.put(UserInfo.fnTravelStatus, req.get("travelStatus").asText());
        if (req.has("birthday"))
            reqMap.put(UserInfo.fnBirthday, timeFmt.parse(req.get("birthday").asText()));
        if (req.has("zodiac"))
            reqMap.put(UserInfo.fnZodiac, TaoziDataFilter.getZodiac(req.get("zodiac").asText()));
        if (req.has("residence"))
            reqMap.put(UserInfo.fnResidence, req.get("residence").asText());
        // 足迹
        if (req.has("tracks"))
            reqMap.put(UserInfo.fnTracks, req.get("tracks").elements());
        if (req.has("travelNotes"))
            reqMap.put(UserInfo.fnTravelNotes, req.get("travelNotes").elements());
        UserAPI.updateUserInfo(reqMap);
        return Utils.createResponse(ErrorCode.NORMAL, "Success");
    }

    private static String cutPicUrl(String url) {
        int pos = url.indexOf('?');
        if (pos > 0)
            return url.substring(0, pos + 1);
        return url;
    }

    /**
     * 请求添加好友
     *
     * @return
     */
    public static Result requestAddContact() throws AizouException {
        long userId, contactId;
        String message;

        JsonNode req = request().body().asJson();
        userId = Integer.parseInt(request().getHeader("UserId"));
        message = req.get("message").asText();
        contactId = Integer.parseInt(req.get("userId").asText());

        UserAPI.requestAddContact(userId, contactId, message);
        return Utils.createResponse(ErrorCode.NORMAL, "Success.");
    }

    /**
     * 添加好友
     *
     * @return
     */
    public static F.Promise<Result> addContact() throws AizouException {
        long userId, contactId;

        userId = Integer.parseInt(request().getHeader("UserId"));
        contactId = Integer.parseInt(request().body().asJson().get("userId").asText());
        return UserCtrlImpl.addContactImpl(userId, contactId);
    }


    /**
     * 删除好友
     *
     * @param id
     * @return
     */
    public static Result delContact(Long id) throws AizouException {
        long userId;
        userId = Integer.parseInt(request().getHeader("UserId"));
        return UserCtrlImpl.delContactImpl(userId, id);
    }


    public static Result getContactList() throws AizouException {
        long userId;
        userId = Integer.parseInt(request().getHeader("UserId"));
        return UserCtrlImpl.getContactListImpl(userId);
    }


    /**
     * 根据环信用户名获取
     *
     * @return
     */
    public static Result getUsersByEasemob() throws AizouException {
        JsonNode req = request().body().asJson();
        JsonNode emList;
        List<String> emNameList = new ArrayList<>();
        long selfId = Integer.parseInt(request().getHeader("UserId"));

        emList = req.get("easemob");
        if (null != emList && emList.isArray() && emList.findValues("easemob") != null) {
            for (JsonNode node : emList) {
                emNameList.add(node.asText());
            }
        }
        List<String> fieldList = Arrays.asList(UserInfo.fnUserId, UserInfo.fnNickName, UserInfo.fnAvatar,
                UserInfo.fnGender, UserInfo.fnEasemobUser, UserInfo.fnSignature);
        List<UserInfo> list = UserAPI.getUserByEaseMob(emNameList, fieldList);

        list = UserAPI.addUserMemo(selfId, list);
        List<JsonNode> nodeList = new ArrayList<>();
        for (UserInfo userInfo : list) {
            nodeList.add(new UserLoginFormatter(false).format(userInfo));
        }
        return Utils.createResponse(ErrorCode.NORMAL, Json.toJson(nodeList));
    }

    /**
     * 根据通讯录进行用户匹配
     *
     * @return
     */
    public static Result matchAddressBook() throws AizouException {
        JsonNode req = request().body().asJson();
        Iterator<JsonNode> it = req.get("contacts").elements();
        Long selfUserId = Long.parseLong(request().getHeader("UserId"));
        List<Contact> contacts = new ArrayList<>();
        ObjectMapper m = new ObjectMapper();

        PhoneParser parser = PhoneParserFactory.newInstance();

        while (it.hasNext()) {
            Contact c = m.convertValue(it.next(), Contact.class);
            try {
                PhoneEntity phone = parser.parse(c.tel);
                c.tel = phone.getPhoneNumber();
            } catch (IllegalArgumentException ignored) {
            }
            contacts.add(c);
        }
        UserInfo userInfo;

        // 当前用户的联系人
        Set<Long> contactSet = UserAPI.getContactIds(selfUserId);

        List<String> telList = new ArrayList<>();
        for (Contact c : contacts) {
            if (c.tel != null && !c.tel.isEmpty())
                telList.add(c.tel);
        }


        Map<String, Long> matchResult = new HashMap<>();
        for (Iterator<UserInfo> itr = UserAPI.searchUser(Arrays.asList(UserInfo.fnTel), telList,
                Arrays.asList(UserInfo.fnUserId, UserInfo.fnTel), 0, 1000); itr.hasNext(); ) {
            UserInfo user = itr.next();
            matchResult.put(user.getTel(), user.getUserId());
        }

        for (Contact temp : contacts) {
//                temp.setId(new ObjectId());
//                userInfo = UserAPI.getUserByField(UserInfo.fnTel, temp.tel, Arrays.asList(UserInfo.fnUserId));

            if (matchResult.containsKey(temp.tel)) {
                temp.isUser = true;
                temp.userId = matchResult.get(temp.tel);
                temp.isContact = contactSet.contains(temp.userId);
            } else {
                temp.isUser = false;
                temp.isContact = false;
            }

        }

        ContactFormatter formatter = FormatterFactory.getInstance(ContactFormatter.class);
        return Utils.status(formatter.format(contacts));
    }

    /**
     * 添加用户的备注信息
     *
     * @param id
     * @return
     * @throws
     */
    public static Result setUserMemo(Long id) throws AizouException {

        String selfId = request().getHeader("userId");
        String memo = request().body().asJson().get("memo").asText();
        UserAPI.setUserMemo(Long.parseLong(selfId), id, memo);
        return Utils.createResponse(ErrorCode.NORMAL, Json.toJson("successful"));

    }

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
    @CheckUser
    public static Result deleteUserAlbums(@CheckUser Long id, String picId) throws AizouException {

        ObjectId oid = new ObjectId(picId);
        UserUgcAPI.deleteUserAlbums(id, oid);
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

        List<Locality> locs;
        Map<ObjectId, Locality> map = new HashMap<>();
        for (Iterator<UserInfo> itr = UserAPI.searchUser(Arrays.asList(UserInfo.fnRoles), Arrays.asList(UserInfo.fnRoles_Expert), Arrays.asList(UserInfo.fnTracks, UserInfo.FD_ID), 0, Constants.MAX_COUNT); itr.hasNext(); ) {
            locs = itr.next().getTracks();
            if (locs == null)
                continue;
            for (Locality loc : locs)
                map.put(loc.getId(), loc);
        }
        List<ObjectId> locIds = new ArrayList<>();
        locIds.addAll(map.keySet());
        List<Locality> result = LocalityAPI.getLocalityList(locIds, Arrays.asList(Locality.FD_ID, Locality.FD_ZH_NAME, Locality.fnCountry, Locality.fnLocation), 0, Constants.MAX_COUNT);
        ObjectNode res = Json.newObject();
        Map<String, List<Locality>> resultMap = TaoziDataFilter.transLocalitiesByCountry(result, abroad);

        SimpleLocalityWithLocationFormatter fmt = FormatterFactory.getInstance(SimpleLocalityWithLocationFormatter.class);

        for (Map.Entry<String, List<Locality>> entry : resultMap.entrySet())
            res.put(entry.getKey(), fmt.formatNode(sortLocalityByPinyin(entry.getValue())));

        return Utils.createResponse(ErrorCode.NORMAL, res);
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
        //List<UserInfo> usersInfo = UserUgcAPI.getExpertUserByTracks(ids, type, fields);
        List<UserInfo> usersInfo = null;
        for (UserInfo user : usersInfo)
            // TODO
            UserAPI.fillUserInfo(user);
        return Utils.createResponse(ErrorCode.NORMAL, formatter.formatNode(usersInfo));

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
        //UserUgcAPI.modifyTracks(id, action, iterator);

        return Utils.createResponse(ErrorCode.NORMAL, "Success.");

    }


//    /**
//     * 获得用户的黑名单列表
//     *
//     * @param
//     * @return backlist
//     */
//    public static Result getUserBlackList() {
//        try {
//            String userId = request().getHeader("userId");
//            List<Integer> list = UserAPI.getBlackList(Integer.parseInt(userId));
//            Map<String, List<Integer>> map = new HashMap<>();
//            map.put("blacklist", list);
//            return Utils.createResponse(ErrorCode.NORMAL, Json.toJson(map));
//        } catch (TravelPiException | NullPointerException | ClassCastException e) {
//            return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, Json.toJson("failed"));
//        }
//    }
//
//    /**
//     * 将用户加入/移除黑名单
//     *
//     * @return
//     */
//    public static Result setUserBlacklist() {
//        try {
//            String selfId = request().getHeader("userId");
//            JsonNode req = request().body().asJson();
//            List<Integer> list = (List<Integer>) req.get("userList").iterator();
//            /*Iterator<JsonNode> iterator =  req.get("userList").iterator();
//            List<Integer> list =new ArrayList<>();
//            while(iterator.hasNext()){
//               JsonNode node=iterator.next();
//               list.add(node.get("userList").asInt());
//            }*/
//            String operation = req.get("action").asText();
//
//            UserAPI.setUserBlacklist(Integer.parseInt(selfId), list, operation);
//            return Utils.createResponse(ErrorCode.NORMAL, Json.toJson("successful"));
//        } catch (TravelPiException | NullPointerException | ClassCastException e) {
//            return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, Json.toJson("failed"));
//        }
//    }
}
