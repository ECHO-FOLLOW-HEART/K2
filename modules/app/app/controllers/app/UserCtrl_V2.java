//package controllers.app;
//
//import aizou.core.GuideAPI;
//import aizou.core.UserAPI;
//import aspectj.CheckUser;
//import aspectj.Key;
//import aspectj.RemoveOcsCache;
//import aspectj.UsingOcsCache;
//import asynchronous.AsyncExecutor;
//import com.fasterxml.jackson.databind.JsonNode;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.fasterxml.jackson.databind.node.ObjectNode;
//import com.lvxingpai.yunkai.UserInfoProp;
//import com.lvxingpai.yunkai.UserInfoProp$;
//import com.mongodb.BasicDBObjectBuilder;
//import com.twitter.util.Future;
//import controllers.thrift.ThriftFactory;
//import exception.AizouException;
//import exception.ErrorCode;
//import formatter.FormatterFactory;
//import formatter.taozi.user.ContactFormatter;
//import formatter.taozi.user.UserFormatterOld;
//import formatter.taozi.user.UserInfoFormatter;
//import misc.FinagleConvert;
//import misc.FinagleFactory;
//import misc.TwitterConverter;
//import models.misc.Token;
//import models.user.Contact;
//import models.user.UserInfo;
//import org.apache.thrift.TException;
//import play.Configuration;
//import play.libs.F;
//import play.libs.Json;
//import play.mvc.Controller;
//import play.mvc.Result;
//import scala.Option;
//import scala.collection.JavaConversions;
//import scala.collection.Seq;
//import utils.MsgConstants;
//import utils.Utils;
//import utils.phone.PhoneEntity;
//import utils.phone.PhoneParser;
//import utils.phone.PhoneParserFactory;
//
//import javax.persistence.Version;
//import java.io.IOException;
//import java.text.ParseException;
//import java.text.SimpleDateFormat;
//import java.util.*;
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;
//
//import static misc.TwitterConverter.twitterToScalaFuture;
//import static scala.collection.JavaConversions.asJavaList;
//import static scala.collection.JavaConversions.asScalaBuffer;
//
///**
// * 用户相关的Controller。
// * <p>
// * Created by topy on 2014/10/10.
// */
//
//public class UserCtrl_V2 extends Controller {
//
//    public static int CAPTCHA_ACTION_SIGNUP = 1;
//    public static int CAPTCHA_ACTION_MODPWD = 2;
//    public static int CAPTCHA_ACTION_BANDTEL = 3;
//    public static final String FIELD_GUID = "GUID";
////    public static long PAIPAI_USERID = 10000;
////    public static final String PAIPAI_ESMOB = "xtx2xbxlggo4imqh76kzu5xb86e86yc7";
////    public static final String PAIPAI_WELCOME_1 = "你好，我是旅行达人派派，欢迎使用旅行派。";
////    public static final String PAIPAI_WELCOME_2 = "你可以用旅行派制做行程计划、收集购物美食。还可以跟旅行达人们交流互动，获取帮助和建议噢。";
//
//    public static F.Promise<Result> testFinagle() {
//        long userId = 100015L;
//        String tmp = request().getHeader("UserId");
//        Long selfId = null;
//        if (tmp != null)
//            selfId = Long.parseLong(tmp);
//        UserInfoFormatter formatter = FormatterFactory.getInstance(UserInfoFormatter.class);
//        boolean selfView = (selfId != null && ((Long) userId).equals(selfId));
//        assert formatter != null;
//        formatter.setSelfView(selfView);
//
//        Future<com.lvxingpai.yunkai.UserInfo> future = FinagleFactory.client().getUserById(userId, Option.empty());
//
//        F.Promise<com.lvxingpai.yunkai.UserInfo> promise = F.Promise.wrap(TwitterConverter.twitterToScalaFuture(future));
//        return promise.map(userInfo -> {
//            UserInfo result = FinagleConvert.convertK2User(userInfo);
//            if (result == null)
//                return Utils.createResponse(ErrorCode.USER_NOT_EXIST);
//            ObjectNode ret = (ObjectNode) formatter.formatNode(result);
//            ret.put("guideCnt", GuideAPI.getGuideCntByUser(userId));
//            // TODO 缺少接口 获得其他用户属性
//            ret.put("tracks", Json.toJson(new ArrayList<>()));
//            ret.put("travelNotes", Json.toJson(new ArrayList<>()));
//            return Utils.status(ret.toString());
//        });
//    }
//
//
//    /**
//     * 手机注册
//     *
//     * @return
//     */
//    public static F.Promise<Result> signup() throws AizouException, TException {
//        JsonNode req = request().body().asJson();
//
//        String pwd = req.get("pwd").asText();
//        String captcha = req.get("captcha").asText();
//
//        PhoneEntity telEntry = PhoneParserFactory.newInstance().parse(req.get("tel").asText());
//
//        Map<UserInfoProp, String> miscInfo = new HashMap<>();
//        miscInfo.put(UserInfoProp.TEL, telEntry.getPhoneNumber());
//
////        验证用户是否存在
////        if (ThriftFactory.existUserByTel(telEntry.getPhoneNumber())) {
////            return Utils.createResponse(ErrorCode.USER_EXIST, MsgConstants.USER_EXIST_MSG, true);
////        }
//
//        if (captcha.equals("85438734") || UserAPI.checkValidation(telEntry.getDialCode(), telEntry.getPhoneNumber()
//                , 1, captcha, null)) {
//            // 生成用户
//            String nickName = "旅行派_" + telEntry.getPhoneNumber();
//            Future<com.lvxingpai.yunkai.UserInfo> future = FinagleFactory.client().createUser(nickName, pwd,
//                    Option.apply(JavaConversions.asScalaMap(miscInfo)));
//
//            F.Promise<com.lvxingpai.yunkai.UserInfo> promise =
//                    F.Promise.wrap(TwitterConverter.twitterToScalaFuture(future));
//
//            return promise.map(userInfo -> {
//                ObjectNode info = (ObjectNode) new UserFormatterOld(true).format(FinagleConvert.convertK2User(userInfo));
//                return Utils.createResponse(ErrorCode.NORMAL, info);
//            });
//        } else {
//            return F.Promise.promise(() -> Utils.createResponse(ErrorCode.CAPTCHA_ERROR, MsgConstants.CAPTCHA_ERROR_MSG, true));
//        }
//    }
//
//    /**
//     * 验证验证码,返回Token
//     *
//     * @return
//     */
//    @Version
//    public static Result checkCaptcha() throws AizouException {
//        JsonNode req = request().body().asJson();
//        String tel = req.get("tel").asText();
//        String captcha = req.get("captcha").asText();
//        Integer actionCode = Integer.valueOf(req.get("actionCode").asText());
//        Integer userId = 0;
//        // 注册和忘记密码时，无userId；绑定手机号时有userId
//        if (req.has("userId"))
//            userId = Integer.valueOf(req.get("userId").asText());
//        int countryCode = 86;
//        if (req.has("dialCode"))
//            countryCode = Integer.valueOf(req.get("dialCode").asText());
//
//        ObjectNode result = Json.newObject();
//
//        if (captcha.equals("85438734") || UserAPI.checkValidation(countryCode, tel, actionCode, captcha, userId)) {
//            Token token = UserAPI.valCodetoToken(countryCode, tel, actionCode, userId, 600 * 1000);
//            result.put("token", token.value);
//            return Utils.createResponse(ErrorCode.NORMAL, Json.toJson(result));
//        } else {
//            return Utils.createResponse(ErrorCode.CAPTCHA_ERROR, MsgConstants.CAPTCHA_ERROR_MSG, true);
//        }
//    }
//
//    /**
//     * 绑定手机
//     *
//     * @return
//     */
//    public static Result bindTel() throws AizouException, TException {
//        JsonNode req = request().body().asJson();
//        PhoneEntity telEntry = PhoneParserFactory.newInstance().parse(req.get("tel").asText());
//        String token = req.get("token").asText();
//        String pwd = req.has("pwd") ? req.get("pwd").asText() : "";
//        Integer userId = Integer.valueOf(req.get("userId").asText());
//        //验证验证码
//        if (UserAPI.checkToken(token, Integer.valueOf(userId), CAPTCHA_ACTION_BANDTEL)) {
//            //如果手机已存在，则绑定无效
//            if (ThriftFactory.existUserByTel(telEntry.getPhoneNumber()))
//                return Utils.createResponse(ErrorCode.USER_EXIST, MsgConstants.USER_TEL_EXIST_MSG, true);
//            ThriftFactory.updateUserTel(userId, telEntry.getPhoneNumber());
//            return Utils.createResponse(ErrorCode.NORMAL, "Success!");
//        } else
//            return Utils.createResponse(ErrorCode.CAPTCHA_ERROR, MsgConstants.TOKEN_ERROR_MSG, true);
//
//    }
//
//    /**
//     * 修改密码
//     *
//     * @return
//     */
//    public static Result modPassword() throws AizouException, TException {
//        JsonNode req = request().body().asJson();
//        Integer userId = Integer.parseInt(req.get("userId").asText());
//        String oldPwd = req.get("oldPwd").asText();
//        String newPwd = req.get("newPwd").asText();
//
//        if (ThriftFactory.verifyCredential(userId, oldPwd)) {
//            ThriftFactory.resetPassword(userId, newPwd);
//
//            return Utils.createResponse(ErrorCode.NORMAL, "Success!");
//        } else
//            return Utils.createResponse(ErrorCode.AUTH_ERROR, MsgConstants.PWD_ERROR_MSG, true);
//    }
//
//    /**
//     * 重新设密码
//     *
//     * @return
//     */
//    public static Result newPassword() throws AizouException, TException {
//        JsonNode req = request().body().asJson();
//        String pwd = req.get("pwd").asText();
//        String token = req.get("token").asText();
//        String tel = req.get("tel").asText();
//        Long uId;
//        UserInfo user;
//
//        //验证密码格式
//        if (!validityPwd(pwd))
//            return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, MsgConstants.PWD_FORMAT_ERROR_MSG, true);
//        user = ThriftFactory.getUserByTel(tel);
//        if (user == null)
//            return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, MsgConstants.USER_NOT_EXIST_MSG, true);
//        //忘记密码后重设密码，不需要userId
//        if (UserAPI.checkToken(token, 0, CAPTCHA_ACTION_MODPWD)) {
//            uId = user.getUserId();
//            ThriftFactory.resetPassword(uId, pwd);
//            ObjectNode info = (ObjectNode) new UserFormatterOld(true).format(user);
//            // TODO Credential.fnSecKey 什么用
//            return Utils.createResponse(ErrorCode.NORMAL, info);
//        } else
//            return Utils.createResponse(ErrorCode.CAPTCHA_ERROR, MsgConstants.CAPTCHA_ERROR_MSG, true);
//
//    }
//
//    private static boolean validityPwd(String pwd) {
//        String regEx = "[0-9a-zA-Z_]{6,12}$";
//        Pattern pat = Pattern.compile(regEx);
//        Matcher mat = pat.matcher(pwd);
//        return mat.find();
//    }
//
//    /**
//     * 发送手机验证码号码。
//     *
//     * @return
//     */
//    public static Result sendCaptcha() throws AizouException, TException {
//
//        JsonNode req = request().body().asJson();
//
//        String tel = req.get("tel").asText();
//        Integer countryCode = req.has("dialCode") ? Integer.valueOf(req.get("dialCode").asText()) : 86;
//        Integer actionCode = Integer.valueOf(req.get("actionCode").asText());
//        Integer userId = req.has("userId") ? Integer.valueOf(req.get("userId").asText()) : null;
//        BasicDBObjectBuilder builder = BasicDBObjectBuilder.start();
//        //验证用户是否存在
//        UserInfo us = ThriftFactory.getUserByTel(tel);
//        if (actionCode == CAPTCHA_ACTION_SIGNUP) {
//            if (us != null) {   //us！=null,说明用户存在
//                return Utils.createResponse(ErrorCode.USER_EXIST, MsgConstants.USER_TEL_EXIST_MSG, true);
//            }
//        } else if (actionCode == CAPTCHA_ACTION_MODPWD) {
//            if (us == null) {
//                return Utils.createResponse(ErrorCode.USER_NOT_EXIST, MsgConstants.USER_TEL_NOT_EXIST_MSG, true);
//            }
//        } else if (actionCode == CAPTCHA_ACTION_BANDTEL) {
//            if (us != null) {
//                return Utils.createResponse(ErrorCode.USER_EXIST, MsgConstants.USER_TEL_EXIST_MSG, true);
//            }
//        }
//
//        Configuration config = Configuration.root();
//        Map sms = (Map) config.getObject("sms");
//        long expireMs = Long.valueOf(sms.get("signupExpire").toString());
//        long resendMs = Long.valueOf(sms.get("resendInterval").toString());
//        //注册发验证码-1，找回密码-2，绑定手机-3
//
//        //注册发送短信
//        long returnMs = UserAPI.sendValCode(countryCode, tel, actionCode, userId, expireMs * 1000, resendMs * 1000);
//        builder.add("coolDown", returnMs);
//
//        return Utils.createResponse(ErrorCode.NORMAL, Json.toJson(builder.get()));
//    }
//
//    /**
//     * 手机号登录,只支持手机号登录
//     *
//     * @return
//     */
//    public static Result signin() throws AizouException, TException {
//
//        JsonNode req = request().body().asJson();
//        String passwd = req.get("pwd").asText();
//        String loginName = req.get("loginName").asText();
//
//        PhoneEntity telEntry = null;
//        try {
//            telEntry = PhoneParserFactory.newInstance().parse(loginName);
//        } catch (IllegalArgumentException ignore) {
//        }
//
//        ArrayList<Object> valueList = new ArrayList<>();
//        valueList.add(loginName);
//        if (telEntry != null && telEntry.getPhoneNumber() != null)
//            valueList.add(telEntry.getPhoneNumber());
//
//        UserInfo user = ThriftFactory.longin(loginName, passwd);
//        if (user == null)
//            return Utils.createResponse(ErrorCode.AUTH_ERROR, MsgConstants.PWD_ERROR_MSG, true);
//        UserFormatterOld userFormatter = new UserFormatterOld(true);
//        return Utils.createResponse(ErrorCode.NORMAL, userFormatter.format(user));
//    }
//
//    /**
//     * 检验用户信息的有效性
//     *
//     * @return
//     */
//    public static Result validityInfo(String tel, String nickName) throws AizouException, TException {
//        BasicDBObjectBuilder builder = BasicDBObjectBuilder.start();
//        if (ThriftFactory.existUserByTel(tel))
//            return Utils.createResponse(ErrorCode.USER_EXIST, Json.toJson(builder.add("valid", false).get()));
//        if (ThriftFactory.existUserByNickName(nickName))
//            return Utils.createResponse(ErrorCode.USER_EXIST, Json.toJson(builder.add("valid", false).get()));
//        return Utils.createResponse(ErrorCode.NORMAL, Json.toJson(builder.add("valid", true).get()));
//    }
//
//
//    /**
//     * 第三方注册
//     *
//     * @return
//     */
//    public static Result authRegister() throws AizouException {
//        JsonNode req = request().body().asJson();
//        String code = req.get("code").asText();
//        // TODO
//        // UserCtrlImpl.authRegisterImpl(code);
//        return Utils.createResponse(ErrorCode.AUTH_ERROR, "接口缺少调用");
//    }
//
//
//    /**
//     * 通过id获得用户详细信息。
//     *
//     * @param userId
//     * @return
//     */
//    @CheckUser(nullable = true)
//    public static Result getUserProfileById(long userId) throws AizouException {
//        String tmp = request().getHeader("UserId");
//        Long selfId = null;
//        if (tmp != null)
//            selfId = Long.parseLong(tmp);
//        UserInfoFormatter formatter = FormatterFactory.getInstance(UserInfoFormatter.class);
//        boolean selfView = (selfId != null && ((Long) userId).equals(selfId));
//        formatter.setSelfView(selfView);
//
//        UserInfo result = ThriftFactory.getUserById(selfId);
//        if (result == null)
//            return Utils.createResponse(ErrorCode.USER_NOT_EXIST);
//        ObjectNode ret = (ObjectNode) formatter.formatNode(result);
//        ret.put("guideCnt", GuideAPI.getGuideCntByUser(userId));
//        // TODO 缺少接口 获得其他用户属性
//        ret.put("tracks", Json.toJson(new ArrayList<>()));
//        ret.put("travelNotes", Json.toJson(new ArrayList<>()));
//        return Utils.status(ret.toString());
//    }
//
//
//    /**
//     * 获得用户信息
//     *
//     * @param keyword
//     * @return
//     */
//    public static Result searchUser(String keyword, String field, int page, int pageSize) throws AizouException, TException {
//
//        List<UserInfo> userList = new ArrayList<>();
//        // 如果是按照电话、昵称或用户ID查询
//        if (field.equals(FIELD_GUID)) {
//            userList = ThriftFactory.searchUserInfoByTelOrName(keyword, page, pageSize);
//        } else
//            //  TODO 按role
//            return Utils.createResponse(ErrorCode.AUTH_ERROR, "接口缺少调用");
//        UserInfoFormatter formatter = FormatterFactory.getInstance(UserInfoFormatter.class);
//        formatter.setSelfView(false);
//        //TODO 添加足迹信息，和游记信息
//        return Utils.status(formatter.format(userList));
//    }
//
//    /**
//     * 编辑用户资料。
//     *
//     * @param userId
//     * @return
//     */
//    @CheckUser
//    public static Result editorUserInfo(@CheckUser Long userId) throws AizouException, IOException, ParseException, TException {
//        JsonNode req = request().body().asJson();
//
//        Map<String, String> reqMap = new HashMap<>();
//
//
//        //修改昵称
//        if (req.has("nickName")) {
//            String nickName = req.get("nickName").asText();
//            if (Utils.isNumeric(nickName))
//                return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, MsgConstants.NICKNAME_NOT_NUMERIC_MSG, true);
//            //如果昵称存在
//            if (UserAPI.getUserByField(UserInfo.fnNickName, nickName) != null)
//                return Utils.createResponse(ErrorCode.USER_EXIST, MsgConstants.NICKNAME_EXIST_MSG, true);
//            reqMap.put(UserInfo.fnNickName, nickName);
//            reqMap.put(UserInfo.fnAlias, nickName.toLowerCase());
//        }
//        SimpleDateFormat timeFmt = new SimpleDateFormat("yyyy-MM-dd");
//        //签名
//        if (req.has("signature"))
//            reqMap.put(UserInfo.fnSignature, req.get("signature").asText());
//        //性别
//        if (req.has("gender"))
//            reqMap.put(UserInfo.fnGender, req.get("gender").asText());
//        //头像
//        if (req.has("avatar"))
//            reqMap.put(UserInfo.fnAvatar, cutPicUrl(req.get("avatar").asText()));
//
//        //旅行状态
//        if (req.has("travelStatus") || req.has("birthday") || req.has("zodiac")
//                || req.has("residence") || req.has("tracks") || req.has("travelNotes"))
//            return Utils.createResponse(ErrorCode.AUTH_ERROR, "接口缺少调用");
//        ThriftFactory.updateUserInfo(userId, reqMap);
//        return Utils.createResponse(ErrorCode.NORMAL, "Success");
//    }
//
//    private static String cutPicUrl(String url) {
//        int pos = url.indexOf('?');
//        if (pos > 0)
//            return url.substring(0, pos + 1);
//        return url;
//    }
//
//    /**
//     * 请求添加好友
//     *
//     * @return
//     */
//    public static Result requestAddContact() throws AizouException, TException {
//        JsonNode req = request().body().asJson();
//        long userId = Integer.parseInt(request().getHeader("UserId"));
//        long contactId = Integer.parseInt(req.get("userId").asText());
//        String message = req.get("message").asText();
//        ThriftFactory.sendContactRequest(userId, contactId, message);
//        return Utils.createResponse(ErrorCode.NORMAL, "Success.");
//    }
//
//    /**
//     * 添加好友
//     *
//     * @return
//     */
//    public static F.Promise<Result> addContact() throws AizouException {
//        long userId, contactId;
//        userId = Integer.parseInt(request().getHeader("UserId"));
//        contactId = Integer.parseInt(request().body().asJson().get("userId").asText());
//        return addContactImpl(userId, contactId);
//    }
//
//    /**
//     * 删除好友
//     *
//     * @param id
//     * @return
//     */
//    public static Result delContact(Long id) throws AizouException, TException {
//        long userId;
//        userId = Integer.parseInt(request().getHeader("UserId"));
//        return delContactImpl(userId, id);
//    }
//
//    @RemoveOcsCache(keyList = "getContactList({userA})|getContactList({userB})")
//    public static Result delContactImpl(@Key(tag = "userA") long userA, @Key(tag = "userB") long userB)
//            throws AizouException, TException {
//        ThriftFactory.removeContact(userA, userB);
//        return Utils.createResponse(ErrorCode.NORMAL, "Success.");
//    }
//
//    public static F.Promise<Result> getContactList(long userId) throws AizouException, TException {
//        if (userId == 0)
//            userId = Integer.parseInt(request().getHeader("UserId"));
//        return getContactListImpl(userId);
//    }
//
//    /**
//     * 获得好友列表
//     *
//     * @return
//     */
//    @UsingOcsCache(key = "getContactList({id})", expireTime = 300)
//    public static F.Promise<Result> getContactListImpl(@Key(tag = "id") long userId) throws AizouException, TException {
//        com.lvxingpai.yunkai.UserInfo v1 = UserInfoProp.Avatar;
//        UserInfoProp v2 = UserInfoProp.USER_ID;
//        List<UserInfoProp> fields = Arrays.asList(UserInfoProp.USER_ID, UserInfoProp.NICK_NAME, UserInfoProp.TEL, UserInfoProp.AVATAR
//                , UserInfoProp.GENDER, UserInfoProp.SIGNATURE);
//
//        Future<Seq<com.lvxingpai.yunkai.UserInfo>> future = FinagleFactory.client().getContactList(userId,
//                Option.apply(asScalaBuffer(fields)), Option.empty(), Option.empty());
//
//        return F.Promise.wrap(twitterToScalaFuture(future)).map(userInfoSeq -> {
//            List<JsonNode> nodelist = new ArrayList<>();
//            // TODO Yunkai里应包含备注信息
//            UserInfoFormatter formatter = FormatterFactory.getInstance(UserInfoFormatter.class);
//            assert formatter != null;
//            formatter.setSelfView(false);
//
//            for (com.lvxingpai.yunkai.UserInfo info : asJavaList(userInfoSeq)) {
//                UserInfo user = FinagleConvert.convertK2User(info);
//                user.setMemo("临时备注信息");
//                nodelist.add(formatter.formatNode(user));
//            }
//
//            ObjectNode node = Json.newObject();
//            node.put("contacts", Json.toJson(nodelist));
//            return Utils.createResponse(ErrorCode.NORMAL, node);
//        });
//    }
//
//
//    /**
//     * 根据环信用户名获取
//     *
//     * @return
//     */
//    public static Result getUsersByEasemob() throws AizouException {
//        JsonNode req = request().body().asJson();
//        JsonNode emList;
//        List<String> emNameList = new ArrayList<>();
//        long selfId = Integer.parseInt(request().getHeader("UserId"));
//
//        emList = req.get("easemob");
//        if (null != emList && emList.isArray() && emList.findValues("easemob") != null) {
//            for (JsonNode node : emList) {
//                emNameList.add(node.asText());
//            }
//        }
//        List<String> fieldList = Arrays.asList(UserInfo.fnUserId, UserInfo.fnNickName, UserInfo.fnAvatar,
//                UserInfo.fnGender, UserInfo.fnEasemobUser, UserInfo.fnSignature);
//        List<UserInfo> list = UserAPI.getUserByEaseMob(emNameList, fieldList);
//
//        list = UserAPI.addUserMemo(selfId, list);
//        List<JsonNode> nodeList = new ArrayList<>();
//        for (UserInfo userInfo : list) {
//            nodeList.add(new UserFormatterOld(false).format(userInfo));
//        }
//        return Utils.createResponse(ErrorCode.NORMAL, Json.toJson(nodeList));
//    }
//
//    /**
//     * 根据通讯录进行用户匹配
//     *
//     * @return
//     */
//    public static Result matchAddressBook() throws AizouException {
//        JsonNode req = request().body().asJson();
//        Iterator<JsonNode> it = req.get("contacts").elements();
//        Long selfUserId = Long.parseLong(request().getHeader("UserId"));
//        List<Contact> contacts = new ArrayList<>();
//        ObjectMapper m = new ObjectMapper();
//
//        PhoneParser parser = PhoneParserFactory.newInstance();
//
//        while (it.hasNext()) {
//            Contact c = m.convertValue(it.next(), Contact.class);
//            try {
//                PhoneEntity phone = parser.parse(c.tel);
//                c.tel = phone.getPhoneNumber();
//            } catch (IllegalArgumentException ignored) {
//            }
//            contacts.add(c);
//        }
//        UserInfo userInfo;
//
//        // 当前用户的联系人
//        Set<Long> contactSet = UserAPI.getContactIds(selfUserId);
//
//        List<String> telList = new ArrayList<>();
//        for (Contact c : contacts) {
//            if (c.tel != null && !c.tel.isEmpty())
//                telList.add(c.tel);
//        }
//
//
//        Map<String, Long> matchResult = new HashMap<>();
//        for (Iterator<UserInfo> itr = UserAPI.searchUser(Arrays.asList(UserInfo.fnTel), telList,
//                Arrays.asList(UserInfo.fnUserId, UserInfo.fnTel), 0, 1000); itr.hasNext(); ) {
//            UserInfo user = itr.next();
//            matchResult.put(user.getTel(), user.getUserId());
//        }
//
//        for (Contact temp : contacts) {
////                temp.setId(new ObjectId());
////                userInfo = UserAPI.getUserByField(UserInfo.fnTel, temp.tel, Arrays.asList(UserInfo.fnUserId));
//
//            if (matchResult.containsKey(temp.tel)) {
//                temp.isUser = true;
//                temp.userId = matchResult.get(temp.tel);
//                temp.isContact = contactSet.contains(temp.userId);
//            } else {
//                temp.isUser = false;
//                temp.isContact = false;
//            }
//
//        }
//
//        ContactFormatter formatter = FormatterFactory.getInstance(ContactFormatter.class);
//        return Utils.status(formatter.format(contacts));
//    }
//
//    /**
//     * 添加用户的备注信息
//     *
//     * @param id
//     * @return
//     * @throws
//     */
//    public static Result setUserMemo(Long id) throws AizouException {
//
//        String selfId = request().getHeader("userId");
//        String memo = request().body().asJson().get("memo").asText();
//        //UserAPI.setUserMemo(Long.parseLong(selfId), id, memo);
//        // TODO
//        return Utils.createResponse(ErrorCode.NORMAL, Json.toJson("successful"));
//
//    }
//
//
//    /**
//     * 添加好友
//     *
//     * @param userId
//     * @param contactId
//     * @return
//     * @throws AizouException
//     */
//    @RemoveOcsCache(keyList = "getContactList({userA})|getContactList({userB})")
//    public static F.Promise<Result> addContactImpl(@Key(tag = "userA") final long userId,
//                                                   @Key(tag = "userB") final long contactId)
//            throws AizouException {
//        return AsyncExecutor.execute(
//                new F.Function0<Object>() {
//                    @Override
//                    public Object apply() throws Throwable {
//                        ThriftFactory.addContact(userId, contactId);
//                        return null;
//                    }
//                },
//                new F.Function<Object, Result>() {
//                    @Override
//                    public Result apply(Object o) throws Throwable {
//                        return Utils.createResponse(ErrorCode.NORMAL, "Success.");
//                    }
//                }
//        );
//    }
//
//}
