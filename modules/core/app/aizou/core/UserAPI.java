package aizou.core;

import aizou.core.user.ValFormatterFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBObject;
import exception.ErrorCode;
import exception.TravelPiException;
import models.MorphiaFactory;
import models.misc.MiscInfo;
import models.misc.Sequence;
import models.misc.Token;
import models.misc.ValidationCode;
import models.user.Credential;
import models.user.OAuthInfo;
import models.user.UserInfo;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.CriteriaContainerImpl;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;
import play.Configuration;
import play.libs.Json;
import play.mvc.Http;
import utils.Constants;
import utils.FPUtils;
import utils.Utils;
import utils.formatter.taozi.user.SimpleUserFormatter;

import javax.crypto.KeyGenerator;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/**
 * 用户相关API。
 *
 * @author Zephyre
 */
public class UserAPI {

    public static int CMDTYPE_ADD_FRIEND = 2;
    public static int CMDTYPE_DEL_FRIEND = 3;

    public static UserInfo getUserById(ObjectId id) throws TravelPiException {
        Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.USER);
        return ds.createQuery(UserInfo.class).field("_id").equal(id).get();
    }

    public static UserInfo getUserById(String id) throws TravelPiException {
        try {
            return getUserById(new ObjectId(id));
        } catch (IllegalArgumentException e) {
            throw new TravelPiException(ErrorCode.INVALID_ARGUMENT, String.format("Invalid user ID: %s.", id));
        }
    }

    /**
     * 获取用户信息
     *
     * @throws TravelPiException
     */
    public static UserInfo getUserInfo(Integer id) throws TravelPiException {
        return getUserInfo(id, null);
    }

    /**
     * 获取用户信息（添加fieldList限定）
     *
     * @throws TravelPiException
     */
    public static UserInfo getUserInfo(Integer id, List<String> fieldList) throws TravelPiException {
        Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.USER);
        Query<UserInfo> query = ds.createQuery(UserInfo.class).field(UserInfo.fnUserId).equal(id);
        if (fieldList != null && !fieldList.isEmpty()) {
            query.retrievedFields(true, fieldList.toArray(new String[fieldList.size()]));
        }
        return query.get();
    }

    /**
     * 修改用户备注
     *
     * @param selfId
     * @param id
     * @param memo
     * @throws TravelPiException
     */
    public static void setUserMemo(Integer selfId, Integer id, String memo) throws TravelPiException {
        UserInfo userInfo = getUserInfo(selfId);
//        Map<Integer, UserInfo> friends = userInfo.friends;
//        boolean flag = friends.containsKey(id);   //查看是否存在好友

        Map<Integer, String> friendRemark = userInfo.remark;
        friendRemark.put(id, memo);
        Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.USER);
        ds.save(userInfo);

    }

    /**
     * 检查用户是否在黑名单中
     *
     * @param selfId
     * @param id
     */
   /* public static boolean checkBlackList(Integer selfId,Integer id) throws TravelPiException {
        UserInfo userInfo=getUserInfo(selfId);
        Map<Integer,UserInfo> blacklist= userInfo.blackList;
        if (blacklist.containsKey(id)){
            return true;
        } else
            return false;
    }*/

    /**
     * 将用户添加/移除黑名单
     *
     * @param selfId
     * @param list
     * @param operation
     * @throws TravelPiException
     */
    public static void setUserBlacklist(Integer selfId, List<Integer> list, String operation) throws TravelPiException {
        UserInfo userInfo = getUserInfo(selfId);
        Map<Integer, UserInfo> blackList = userInfo.blackList;  //用户的黑名单列表
//        Map<Integer, UserInfo> friends = userInfo.friends;     //用户的朋友圈列表
        switch (operation) {
            case "add":         //用户加入到黑名单
                for (Integer id : list) {
                    if (blackList.containsKey(id)) {         //黑名单中存在用户已经
                        continue;
                    }
                    UserInfo user = getUserInfo(id);
                    blackList.put(id, user);            //添加用户到黑名单
//                    friends.remove(id);                 //将用户从朋友圈中删除
                }
                addEaseMobBlocks(selfId, list);          //向环信中注册
                break;
            case "remove":                                  //用户移除黑名单
                if (list.isEmpty()) {                        //黑名单空的话，抛出异常
                    throw new TravelPiException(ErrorCode.INVALID_ARGUMENT, "INVALID_ARGUMENT");
                } else {
                    for (Integer id : list) {
                        if (!blackList.containsKey(id)) {        //用户不再黑名单中
                            continue;
                        }
                        UserInfo user = getUserInfo(id);
                        blackList.remove(id);                 //添加用户到朋友圈
//                        friends.put(id, user);
                    }
                    for (Integer id : list) {
                        delEaseMobBlocks(selfId, id);
                    }
                    break;
                }
            default:
                throw new TravelPiException(ErrorCode.INVALID_ARGUMENT, "INVALID_ARGUMENT");
        }
        Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.USER);    //更新用户的信息
        ds.save(userInfo);
    }

    /**
     * 获得用户的黑名单列表
     *
     * @param userId
     * @return
     * @throws TravelPiException
     */
    public static List<Integer> getBlackList(Integer userId) throws TravelPiException {
        UserInfo userInfo = getUserInfo(userId);
        Map<Integer, UserInfo> blackList = userInfo.blackList;
        List<Integer> list = new ArrayList<>();
        if (!blackList.isEmpty()) {          //黑名单不为空
            for (Integer i : blackList.keySet()) {
                list.add(i);
            }
        }
        return list;
    }

    /**
     * 根据OAuth信息获得用户信息。
     *
     * @param provider
     * @param oauthId
     * @return
     */
    public static UserInfo getUserByOAuth(String provider, String oauthId) throws TravelPiException {
        Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.USER);
        BasicDBObjectBuilder builder = BasicDBObjectBuilder.start().add("$elemMatch",
                BasicDBObjectBuilder.start().add("provider", provider).add("oauthId", oauthId).get());
        return ds.createQuery(UserInfo.class).filter("oauthList", builder.get()).get();
    }

    /**
     * 根据UDID获得用户信息。
     *
     * @param udid
     * @return
     */
    public static UserInfo getUserByUdid(String udid) throws TravelPiException {
        Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.USER);
        return ds.createQuery(UserInfo.class).field("udid").equal(udid).field("oauthList").notEqual(null).get();
    }

    /**
     * 根据UDID完成用户注册。
     *
     * @param udid
     * @return
     */
    public static UserInfo regByUdid(String udid) throws TravelPiException {
        UserInfo user = getUserByUdid(udid);
        if (user != null)
            return user;

        user = new UserInfo();
        user.nickName = (new ObjectId()).toString();
        user.avatar = "http://default";
        user.oauthList = new ArrayList<>();
        user.udid = udid;

        Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.USER);
        ds.save(user);

        return user;
    }

    /**
     * 根据OAuth信息完成用户注册。
     *
     * @param provider
     * @param oauthId
     * @param extra
     * @return
     * @throws TravelPiException
     */
    public static UserInfo regByOAuth(String provider, String oauthId, DBObject extra, String secToken) throws TravelPiException {
        UserInfo user = getUserByOAuth(provider, oauthId);
        if (user != null) {
            //更新签名
            Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.USER);
//            user.secToken = secToken;
            ds.save(user);

            return user;
        }
        String nickName = null;
        String avatar = null;
        String token = null;
        String udid = null;

        Object tmp = extra.get("nickName");
        if (tmp != null)
            nickName = tmp.toString();
        tmp = extra.get("avatar");
        if (tmp != null)
            avatar = tmp.toString();
        tmp = extra.get("token");
        if (tmp != null)
            token = tmp.toString();
        tmp = extra.get("udid");
        if (tmp != null)
            udid = tmp.toString();

        user = new UserInfo();

        user.nickName = nickName;
        user.avatar = avatar;
        user.udid = udid;
        user.oauthList = new ArrayList<>();
//        user.secToken = secToken;

        OAuthInfo oauthInfo = new OAuthInfo();
        oauthInfo.provider = provider;
        oauthInfo.oauthId = oauthId;
        oauthInfo.nickName = nickName;
        oauthInfo.avatar = avatar;
        oauthInfo.token = token;
        user.oauthList.add(oauthInfo);

        Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.USER);
        ds.save(user);

        return user;
    }

    public static void updateUserInfo(Http.Request req) throws TravelPiException {
        String seq = req.getQueryString("seq");
        String platform = req.getQueryString("platform");
        String appVersion = req.getQueryString("v");
        String uid = req.getQueryString("uid");

        //取得用户信息
        Datastore dsUser = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.USER);
        // 优先按照uid进行lookup
        UserInfo user;
        if (uid != null)
            user = dsUser.createQuery(UserInfo.class).field("_id").equal(new ObjectId(uid)).get();
        else if (seq != null)
            user = dsUser.createQuery(UserInfo.class).field("udid").equal(seq).field("oauthList").equal(null).get();
        else
            user = null;

        if (user == null)
            return;

        //设置更新信息：用户机系统信息、用户App版本、用户设备编号
        UpdateOperations<UserInfo> ops = dsUser.createUpdateOperations(UserInfo.class);
        ops.set("platform", platform);
        ops.set("appVersion", appVersion);
        ops.set("udid", seq);
        ops.set("enabled", true);

        dsUser.updateFirst(dsUser.createQuery(UserInfo.class).field("_id").equal(user.id), ops);
    }

    /**
     * 进行用户权限验证
     *
     * @param uid
     * @param timestamp
     * @param sign
     * @return
     * @throws TravelPiException
     */
    public static boolean authenticate(String uid, String timestamp, String sign) throws TravelPiException {
        return true;
    }

    public static Iterator<UserInfo> searchUser(List<String> fieldDesc, Object value, List<String> fieldList, int page, int pageSize)
            throws TravelPiException {
        Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.USER);
        Query<UserInfo> query = ds.createQuery(UserInfo.class);

        if (fieldDesc == null || fieldDesc.isEmpty())
            throw new TravelPiException(ErrorCode.INVALID_ARGUMENT, "Invalid fields.");

        List<CriteriaContainerImpl> criList = new ArrayList<>();
        for (String fd : fieldDesc)
            criList.add(query.criteria(fd).equal(value));

        query.or(criList.toArray(new CriteriaContainerImpl[criList.size()]));

        if (fieldList != null && !fieldList.isEmpty())
            query.retrievedFields(true, fieldList.toArray(new String[fieldList.size()]));
        query.field("origin").equal(Constants.APP_FLAG_PEACH);

        return query.offset(page * pageSize).limit(pageSize).iterator();
    }


    /**
     * 根据字段获得用户信息。
     *
     * @param fieldDesc 哪些字段为查询目标？
     * @param fieldList 返回结果包含哪些字段？
     */
    public static UserInfo getUserByField(List<String> fieldDesc, String value, List<String> fieldList)
            throws TravelPiException {
        Iterator<UserInfo> itr = searchUser(fieldDesc, value, fieldList, 0, 1);
        if (itr != null && itr.hasNext())
            return itr.next();
        else
            return null;
    }

    /**
     * 根据字段获得用户信息。
     *
     * @param fieldDesc 哪些字段为查询目标？
     */
    public static UserInfo getUserByField(String fieldDesc, String value)
            throws TravelPiException {
        return getUserByField(Arrays.asList(fieldDesc), value, null);
    }

    /**
     * 根据字段获得用户信息。
     *
     * @param fieldDesc 哪些字段为查询目标？
     */
    public static UserInfo getUserByField(String fieldDesc, String value, List<String> fieldFilter)
            throws TravelPiException {
        return getUserByField(Arrays.asList(fieldDesc), value, fieldFilter);
    }

    /**
     * 储存用户信息。
     *
     * @param
     * @return
     */
    public static void saveUserInfo(UserInfo u) throws TravelPiException {
        Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.USER);
        ds.save(u);
    }

    /**
     * 根据手机号码完成用户注册。
     *
     * @param
     * @return
     */
    public static UserInfo regByTel(String tel, Integer countryCode, String pwd) throws TravelPiException {
        UserInfo user = new UserInfo();
        user.id = new ObjectId();
        user.userId = populateUserId();
        user.avatar = "http://default";
        user.oauthList = new ArrayList<>();
        user.tel = tel;
        user.nickName = "桃子_" + user.userId;
        user.gender = "";
        user.dialCode = countryCode;
        user.email = "";
        user.signature = "";
        user.origin = Constants.APP_FLAG_PEACH;
        user.enabled = true;

        // 注册环信
        String[] ret = regEasemob();
        String easemobPwd = ret[1];

        user.easemobUser = ret[0];
        MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.USER).save(user);

        // 注册机密信息
        Credential cre = new Credential();
        cre.id = user.id;
        cre.userId = user.userId;
        cre.salt = Utils.getSalt();
        if (!pwd.equals(""))
            cre.pwdHash = Utils.toSha1Hex(cre.salt + pwd);
        cre.easemobPwd = easemobPwd;
        try {
            cre.secKey = Base64.encodeBase64String(KeyGenerator.getInstance("HmacSHA256").generateKey().getEncoded());
        } catch (NoSuchAlgorithmException e) {
            throw new TravelPiException(ErrorCode.UNKOWN_ERROR, "", e);
        }

        MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.USER).save(cre);

        return user;
    }

    /**
     * 根据微信完成用户注册。
     *
     * @param
     * @return
     */
    public static UserInfo regByWeiXin(UserInfo user) throws TravelPiException {

        // 注册环信
        String[] ret = regEasemob();
        String easemobPwd = ret[1];

        user.easemobUser = ret[0];
        MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.USER).save(user);

        // 注册机密信息
        Credential cre = new Credential();
        cre.id = user.id;
        cre.userId = user.userId;
        cre.salt = Utils.getSalt();
        cre.easemobPwd = easemobPwd;
        try {
            cre.secKey = Base64.encodeBase64String(KeyGenerator.getInstance("HmacSHA256").generateKey().getEncoded());
        } catch (NoSuchAlgorithmException e) {
            throw new TravelPiException(ErrorCode.UNKOWN_ERROR, "", e);
        }
        MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.USER).save(cre);

        return user;
    }

    public static UserInfo oauthToUserInfoForWX(JsonNode json) throws NullPointerException, TravelPiException {
        String nickname = json.get("nickname").asText();
        String headimgurl = json.get("headimgurl").asText();
        UserInfo userInfo = new UserInfo();
        userInfo.id = new ObjectId();
        userInfo.nickName = nickname;
        userInfo.avatar = headimgurl;
        userInfo.gender = json.get("sex").asText().equals("1") ? "M" : "F";
        userInfo.oauthList = new ArrayList<>();
        userInfo.userId = UserAPI.populateUserId();
        userInfo.origin = Constants.APP_FLAG_PEACH;
//        userInfo.secToken = Utils.getSecToken();

        OAuthInfo oauthInfo = new OAuthInfo();
        oauthInfo.provider = "weixin";
        oauthInfo.oauthId = json.get("openid").asText();
        oauthInfo.nickName = nickname;
        oauthInfo.avatar = headimgurl;
        userInfo.oauthList.add(oauthInfo);

        return userInfo;
    }

    public static Integer populateUserId() throws TravelPiException {
        Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.MISC);
        Query<Sequence> query = ds.createQuery(Sequence.class);
        query.field("column").equal(Sequence.USERID);
        UpdateOperations<Sequence> ops = ds.createUpdateOperations(Sequence.class).inc("count");
        Sequence ret = ds.findAndModify(query, ops);
        if (ret == null)
            throw new TravelPiException(ErrorCode.UNKOWN_ERROR, "Unable to generate UserId sequences.");
        return ret.count;
    }

//    /**
//     * 注册密码
//     *
//     * @param u
//     * @param pwd
//     * @return
//     */
//    public static void regCredential(UserInfo u, String pwd) throws TravelPiException {
//        Credential cre = new Credential();
//        cre.id = u.id;
//        cre.userId = u.userId;
//        cre.salt = Utils.getSalt();
//        if (!pwd.equals(""))
//            cre.pwdHash = Utils.toSha1Hex(cre.salt + pwd);
//        MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.USER).save(cre);
//    }

    /**
     * 注册密码和环信
     *
     * @return 返回环信的用户名和密码
     */
    public static String[] regEasemob() throws TravelPiException {
        String easemobPwd;
        try {
            easemobPwd = Base64.encodeBase64String(KeyGenerator.getInstance("HmacSHA256").generateKey().getEncoded());
        } catch (NoSuchAlgorithmException e) {
            throw new TravelPiException(ErrorCode.UNKOWN_ERROR, "", e);
        }

        String easemobName = null;
        boolean flag = false;
        for (int i = 0; i < 5; i++) {
            // 环信注册
            String base = "abcdefghijklmnopqrstuvwxyz0123456789";
            int size = base.length();
            Random random = new Random();
            StringBuilder sb = new StringBuilder();
            for (int j = 0; j < 32; j++)
                sb.append(base.charAt(random.nextInt(size)));
            easemobName = sb.toString();

            try {
                regEasemobReq(easemobName, easemobPwd);
                flag = true;
                break;
            } catch (TravelPiException e) {
                if (e.errCode != ErrorCode.USER_EXIST)
                    throw e;
            }
        }

        if (flag)
            return new String[]{easemobName, easemobPwd};
        else
            throw new TravelPiException(ErrorCode.UNKOWN_ERROR, "");
    }

    /**
     * 判断是否有密码
     *
     * @param
     * @param
     * @return
     */
    public static Credential getPwd(UserInfo u) throws TravelPiException {

        Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.USER);
        Query<Credential> ceQuery = ds.createQuery(Credential.class);
        if (u.userId == null)
            return null;
        Credential cre = ceQuery.field("userId").equal(u.userId).get();
        if (cre == null || cre.pwdHash == null)
            return null;
        return cre;

    }

    /**
     * 获得用户的私密信息。
     *
     * @param userId
     * @return
     * @throws TravelPiException
     */
    public static Credential getCredentialByUserId(Integer userId) throws TravelPiException {
        return getCredentialByUserId(userId, null);
    }

    /**
     * 获得用户的私密信息。
     *
     * @param userId
     * @param fieldList
     * @return
     * @throws TravelPiException
     */
    public static Credential getCredentialByUserId(Integer userId, List<String> fieldList) throws TravelPiException {
        Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.USER);
        Query<Credential> query = ds.createQuery(Credential.class).field("userId").equal(userId);
        if (fieldList != null && !fieldList.isEmpty()) {
            query.retrievedFields(true, fieldList.toArray(new String[fieldList.size()]));
        }
        return query.get();
    }

    /**
     * 重设密码
     *
     * @param u
     * @param pwd
     * @throws TravelPiException
     */
    public static void resetPwd(UserInfo u, String pwd) throws TravelPiException {
        if (pwd == null || pwd.isEmpty() || u == null || u.userId == null)
            throw new TravelPiException(ErrorCode.INVALID_ARGUMENT, "");

        Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.USER);
        Credential cre = ds.createQuery(Credential.class).field(Credential.fnUserId).equal(u.userId).get();
        cre.salt = Utils.getSalt();
        cre.pwdHash = Utils.toSha1Hex(cre.salt + pwd);

        MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.USER).save(cre);
    }

    /**
     * 密码验证
     *
     * @param u
     * @param pwd
     * @return
     */
    public static boolean validCredential(UserInfo u, String pwd) throws TravelPiException {

        Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.USER);
        Query<Credential> ceQuery = ds.createQuery(Credential.class);
        Credential ce = ceQuery.field("userId").equal(u.userId).get();
        return ce != null && ce.pwdHash.equals(Utils.toSha1Hex(ce.salt + pwd));
    }

    /**
     * 发送手机验证码
     *
     * @param countryCode 国家代码
     * @param tel         手机号码
     * @param actionCode  动作代码，表示发送验证码的原因
     * @param expireMs    多少豪秒以后过期
     * @param resendMs    多少毫秒以后可以重新发送验证短信
     */
    public static void sendValCode(int countryCode, String tel, int actionCode, Integer userId, long expireMs, long resendMs)
            throws TravelPiException {
        Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.MISC);
        ValidationCode valCode = ds.createQuery(ValidationCode.class).field("key")
                .equal(ValidationCode.calcKey(countryCode, tel, actionCode)).get();

        if (valCode != null && System.currentTimeMillis() < valCode.resendTime)
            throw new TravelPiException(ErrorCode.SMS_QUOTA_ERROR, "SMS out of quota.");

        ValidationCode oldCode = valCode;
        valCode = ValidationCode.newInstance(countryCode, tel, actionCode, userId, expireMs);
        if (oldCode != null)
            valCode.id = oldCode.id;

        List<String> recipients = new ArrayList<>();
        recipients.add(tel);

        String content = ValFormatterFactory.newInstance(actionCode).format(countryCode, tel, valCode.value, expireMs, null);
        Utils.sendSms(recipients, content);

        valCode.lastSendTime = System.currentTimeMillis();
        valCode.resendTime = valCode.lastSendTime + resendMs;
        ds.save(valCode);
    }

    /**
     * 验证验证码
     *
     * @param countryCode 国家代码
     * @param tel         手机号码
     * @param actionCode  操作码
     * @param valCode     验证码
     * @return 验证码是否有效
     */
    public static boolean checkValidation(int countryCode, String tel, int actionCode, String valCode, Integer userId)
            throws TravelPiException {
        Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.MISC);
        ValidationCode entry = ds.createQuery(ValidationCode.class).field("key")
                .equal(ValidationCode.calcKey(countryCode, tel, actionCode)).get();

        // 如果actionCode == 1或2,即注册或忘记密码,不需要验证userId
        boolean ret = !(entry == null || !entry.value.equals(valCode) || System.currentTimeMillis() > entry.expireTime
                || (isNeedCheckUserId(actionCode) && !entry.userId.equals(userId)));

        // 避免暴力攻击。验证失效次数超过5次，验证码就会失效。
        if (!ret && entry != null) {
            entry.failCnt++;
            if (entry.failCnt > 5)
                entry.expireTime = 0L;
            ds.save(entry);
        }

        return ret;
    }

    /**
     * 获取环信系统的token。如果已经过期，则重新申请一个。
     */
    private static String getEaseMobToken() throws TravelPiException {
        Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.MISC);
        MiscInfo info = ds.createQuery(MiscInfo.class).get();

        String token = info.easemobToken;
        Long tokenExp = info.easemobTokenExpire;
        long cur = System.currentTimeMillis();

        if (token == null || tokenExp == null || cur + 3600 > tokenExp) {
            // 重新获取token
            Configuration config = Configuration.root().getConfig("easemob");
            String orgName = config.getString("org");
            String appName = config.getString("app");
            String clientId = config.getString("client_id");
            String clientSecret = config.getString("client_key");

            ObjectNode data = Json.newObject();
            data.put("grant_type", "client_credentials");
            data.put("client_id", clientId);
            data.put("client_secret", clientSecret);

            String href = String.format("https://a1.easemob.com/%s/%s/token", orgName, appName);
            try {
                URL url = new URL(href);
                URLConnection conn = url.openConnection();
                conn.setDoOutput(true);
                conn.setDoInput(true);
                conn.setRequestProperty("Content-Type", "application/json");
                OutputStreamWriter out = new OutputStreamWriter(conn.getOutputStream());
                out.write(data.toString());
                out.flush();
                out.close();

                InputStream in = conn.getInputStream();
                String body = IOUtils.toString(in, conn.getContentEncoding());

                JsonNode tokenData = Json.parse(body);
                info.easemobToken = tokenData.get("access_token").asText();
                info.easemobUUID = tokenData.get("application").asText();
                info.easemobTokenExpire = System.currentTimeMillis() + tokenData.get("expires_in").asLong() * 1000;
                ds.save(info);
            } catch (java.io.IOException e) {
                throw new TravelPiException(ErrorCode.UNKOWN_ERROR, "Error in retrieving token.");
            }
        }

        return info.easemobToken;
    }

    public static Token valCodetoToken(Integer countryCode, String tel, int actionCode, int userId, long expireMs) throws TravelPiException {
        ValidationCode valCode = ValidationCode.newInstance(countryCode, tel, actionCode, userId, expireMs);
        Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.MISC);
        Token token = Token.newInstance(valCode, expireMs);

        Token uniq = ds.createQuery(Token.class).field("value")
                .equal(token.value).field("userId").equal(token.userId).get();
        if (uniq != null) {
            throw new TravelPiException(ErrorCode.SMS_QUOTA_ERROR, "Token out of quota.");
        }
        ds.save(token);
        return token;
    }

    public static boolean checkToken(String token, int userId, int actionCode) throws TravelPiException {
        Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.MISC);
        Token uniq = ds.createQuery(Token.class).field("value").equal(token).field("used").notEqual(true).get();
        //设置已使用过
        if (uniq != null)
            uniq.used = Boolean.TRUE;
        else
            return false;
        ds.save(uniq);
        boolean ret = !(uniq == null || !uniq.value.equals(token) ||
                !uniq.permissionList.contains(actionCode) || (isNeedCheckUserId(actionCode) && uniq.userId != userId));
        return ret;
    }

    /**
     * 根据actionCode,判断是否要在验证验证码时,验证userID
     *
     * @param actionCode
     * @return
     */
    private static boolean isNeedCheckUserId(int actionCode) {
        return !(actionCode == 1 || actionCode == 2);
    }

    /**
     * 注册环信用户
     *
     * @param userName
     * @param pwd
     */
    private static void regEasemobReq(String userName, String pwd) throws TravelPiException {
        // 重新获取token
        Configuration config = Configuration.root().getConfig("easemob");
        String orgName = config.getString("org");
        String appName = config.getString("app");

        ObjectNode data = Json.newObject();
        data.put("username", userName);
        data.put("password", pwd);

        String href = String.format("https://a1.easemob.com/%s/%s/users", orgName, appName);
        try {
            URL url = new URL(href);
            URLConnection conn = url.openConnection();
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", String.format("Bearer %s", getEaseMobToken()));
            OutputStreamWriter out = new OutputStreamWriter(conn.getOutputStream());
            out.write(data.toString());
            out.flush();
            out.close();

            InputStream in = conn.getInputStream();
            String body = IOUtils.toString(in, conn.getContentEncoding());

            JsonNode tokenData = Json.parse(body);
            if (tokenData.has("error")) {
                if (tokenData.get("error").asText().equals("duplicate_unique_property_exists"))
                    throw new TravelPiException(ErrorCode.USER_EXIST, String.format("Easemob user %s exists.", userName));
                else
                    throw new TravelPiException(ErrorCode.UNKOWN_ERROR, "Error in user registration.");
            }
        } catch (java.io.IOException e) {
            throw new TravelPiException(ErrorCode.UNKOWN_ERROR, "Error in user registration.");
        }
    }

    /**
     * 在环信用户系统中处理用户的好友关系
     *
     * @param userIdA
     * @param userIdB
     */
    public static void modEaseMobContacts(Integer userIdA, Integer userIdB, boolean actionAdd) throws TravelPiException {
        List<String> fieldList = Arrays.asList(UserInfo.fnEasemobUser);
        String userA, userB;
        try {
            userA = UserAPI.getUserInfo(userIdA, fieldList).easemobUser;
            userB = UserAPI.getUserInfo(userIdB, fieldList).easemobUser;
            if (userA == null || userB == null)
                throw new TravelPiException(ErrorCode.UNKOWN_ERROR, "Easemob not regiestered yet.");
        } catch (NullPointerException e) {
            throw new TravelPiException(ErrorCode.USER_NOT_EXIST, "");
        }

        // 重新获取token
        Configuration config = Configuration.root().getConfig("easemob");
        String orgName = config.getString("org");
        String appName = config.getString("app");

        String href = String.format("https://a1.easemob.com/%s/%s/users/%s/contacts/users/%s",
                orgName, appName, userA, userB);
        try {
            URL url = new URL(href);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod(actionAdd ? "POST" : "DELETE");
            conn.setDoInput(true);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", String.format("Bearer %s", getEaseMobToken()));

            InputStream in = conn.getInputStream();
            String body = IOUtils.toString(in, conn.getContentEncoding());

            JsonNode tokenData = Json.parse(body);
            if (tokenData.has("error"))
                throw new TravelPiException(ErrorCode.UNKOWN_ERROR, "Error in user registration.");
        } catch (java.io.IOException e) {
            throw new TravelPiException(ErrorCode.UNKOWN_ERROR, "Error in user registration.");
        }
    }

    /**
     * 在环信用户系统中添加用户的黑名单关系
     */
    public static void addEaseMobBlocks(Integer userIdA, List<Integer> blockIds) throws TravelPiException {
        List<String> fieldList = Arrays.asList(UserInfo.fnEasemobUser);
        String userA;
        if (blockIds == null || blockIds.isEmpty())
            throw new TravelPiException(ErrorCode.INVALID_ARGUMENT, "");

        List<String> blockNames = new ArrayList<>();
        try {
            userA = UserAPI.getUserInfo(userIdA, fieldList).easemobUser;
            if (userA == null)
                throw new TravelPiException(ErrorCode.UNKOWN_ERROR, "Easemob not regiestered yet.");

            for (Integer i : blockIds) {
                String easemobName = UserAPI.getUserInfo(i, fieldList).easemobUser;
                if (easemobName == null)
                    throw new TravelPiException(ErrorCode.UNKOWN_ERROR, "Easemob not regiestered yet.");
                blockNames.add(easemobName);
            }
        } catch (NullPointerException e) {
            throw new TravelPiException(ErrorCode.USER_NOT_EXIST, "");
        }

        ObjectNode data = Json.newObject();
        data.put("usernames", Json.toJson(blockNames));

        // 重新获取token
        Configuration config = Configuration.root().getConfig("easemob");
        String orgName = config.getString("org");
        String appName = config.getString("app");

        String href = String.format("https://a1.easemob.com/%s/%s/users/%s/blocks/users",
                orgName, appName, userA);
        try {
            URL url = new URL(href);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", String.format("Bearer %s", getEaseMobToken()));

            OutputStreamWriter out = new OutputStreamWriter(conn.getOutputStream());
            out.write(data.toString());
            out.flush();
            out.close();

            InputStream in = conn.getInputStream();
            String body = IOUtils.toString(in, conn.getContentEncoding());

            JsonNode tokenData = Json.parse(body);
            if (tokenData.has("error"))
                throw new TravelPiException(ErrorCode.UNKOWN_ERROR, "");
        } catch (java.io.IOException e) {
            throw new TravelPiException(ErrorCode.UNKOWN_ERROR, "");
        }
    }

    /**
     * 在环信用户系统中处理用户的黑名单
     *
     * @param userIdA
     * @param userIdB
     */
    public static void delEaseMobBlocks(Integer userIdA, Integer userIdB) throws TravelPiException {
        List<String> fieldList = Arrays.asList(UserInfo.fnEasemobUser);
        String userA, userB;
        try {
            userA = UserAPI.getUserInfo(userIdA, fieldList).easemobUser;
            userB = UserAPI.getUserInfo(userIdB, fieldList).easemobUser;
        } catch (NullPointerException e) {
            throw new TravelPiException(ErrorCode.USER_NOT_EXIST, "");
        }

        // 重新获取token
        Configuration config = Configuration.root().getConfig("easemob");
        String orgName = config.getString("org");
        String appName = config.getString("app");

        String href = String.format("https://a1.easemob.com/%s/%s/users/%s/blocks/users/%s",
                orgName, appName, userA, userB);
        try {
            URL url = new URL(href);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("DELETE");
            conn.setDoInput(true);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", String.format("Bearer %s", getEaseMobToken()));

            InputStream in = conn.getInputStream();
            String body = IOUtils.toString(in, conn.getContentEncoding());

            JsonNode tokenData = Json.parse(body);
            if (tokenData.has("error"))
                throw new TravelPiException(ErrorCode.UNKOWN_ERROR, "");
        } catch (java.io.IOException e) {
            throw new TravelPiException(ErrorCode.UNKOWN_ERROR, "");
        }
    }

    /**
     * 提出好友申请
     *
     * @param selfId
     * @param otherid
     * @param reason
     */
    public static void sendFriendReq(Integer selfId, Integer otherid, String reason) throws TravelPiException {
        UserInfo userInfo = getUserInfo(selfId);

    }

    /**
     * 添加好友
     *
     * @param selfId
     * @param targetId
     * @throws TravelPiException
     */
    public static void addContact(Integer selfId, Integer targetId) throws TravelPiException {
        if (selfId.equals(targetId))
            return;

        UserInfo selfInfo = getUserInfo(selfId, Arrays.asList(UserInfo.fnContacts, UserInfo.fnNickName,
                UserInfo.fnAvatar, UserInfo.fnGender, UserInfo.fnUserId, UserInfo.fnEasemobUser));  //取得用户实体
        //取得好友的实体
        UserInfo targetInfo = getUserInfo(targetId, Arrays.asList(UserInfo.fnContacts, UserInfo.fnNickName,
                UserInfo.fnAvatar, UserInfo.fnGender, UserInfo.fnUserId, UserInfo.fnEasemobUser));

        if (selfInfo == null || targetInfo == null)
            throw new TravelPiException(ErrorCode.INVALID_ARGUMENT, "Invalid user id.");

        List<UserInfo> selfContacts = selfInfo.friends;
        if (selfContacts == null)
            selfContacts = new ArrayList<>();
        List<UserInfo> targetContacts = targetInfo.friends;
        if (targetContacts == null)
            targetContacts = new ArrayList<>();

        //环信注册
        modEaseMobContacts(selfId, targetId, true);

        //保存
        final Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.USER);

        FPUtils.IFunc func = new FPUtils.IFunc() {

            @Override
            public Object func0() {
                return null;
            }

            @Override
            public Object funcv(Object... val) {
                UpdateOperations<UserInfo> ops = ds.createUpdateOperations(UserInfo.class);

                Integer sid = (Integer) val[0];
                @SuppressWarnings("unchecked")
                List<UserInfo> sc = (List<UserInfo>) val[1];
                UserInfo tinfo = (UserInfo) val[2];

                Query<UserInfo> query = ds.createQuery(UserInfo.class).field(UserInfo.fnUserId).equal(sid);
                if (sc == null || sc.isEmpty()) {
                    ops.set(UserInfo.fnContacts, Arrays.asList(tinfo));
                    ds.updateFirst(query, ops);
                } else {
                    Set<Integer> userIdSet = new HashSet<>();
                    for (UserInfo u : sc)
                        userIdSet.add(u.userId);
                    if (!userIdSet.contains(tinfo.userId)) {
                        ops.add(UserInfo.fnContacts, tinfo);
                        ds.updateFirst(query, ops);
                    }
                }

                return null;
            }
        };

        // 需要互相加对方为好友
        for (Object obj : Arrays.asList(new Object[]{
                new Object[]{selfId, selfContacts, targetInfo},
                new Object[]{targetId, targetContacts, selfInfo}
        })) {
            func.funcv((Object[]) obj);
        }

        // 向加友请求发起的客户端发消息
        unvarnishedTrans(selfInfo, targetInfo, CMDTYPE_ADD_FRIEND);
    }


    /**
     * 删除好友
     *
     * @param selfId
     * @param targetId
     */
    public static void delContact(Integer selfId, Integer targetId) throws TravelPiException {
        if (selfId.equals(targetId))
            return;

        //取得用户实体
        UserInfo selfInfo = getUserInfo(selfId, Arrays.asList(UserInfo.fnContacts, UserInfo.fnUserId, UserInfo.fnEasemobUser));
        UserInfo targetInfo = getUserInfo(targetId, Arrays.asList(UserInfo.fnContacts, UserInfo.fnUserId, UserInfo.fnEasemobUser));
        if (selfInfo == null || targetInfo == null)
            throw new TravelPiException(ErrorCode.INVALID_ARGUMENT, "Invalid user id.");

        //向环信注册
        modEaseMobContacts(selfId, targetId, false);

        final Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.USER);

        FPUtils.IFunc func = new FPUtils.IFunc() {

            @Override
            public Object func0() {
                return null;
            }

            @Override
            public Object funcv(Object... val) {
                UserInfo sinfo = (UserInfo) val[0];
                Integer tid = (Integer) val[1];

                List<UserInfo> contactList = sinfo.friends;
                if (contactList == null || contactList.isEmpty())
                    return null;

                int idx = -1;
                for (int i = 0; i < contactList.size(); i++) {
                    if (contactList.get(i).userId.equals(tid)) {
                        idx = i;
                        break;
                    }
                }
                if (idx != -1) {
                    // 更新数据库
                    contactList.remove(idx);

                    Query<UserInfo> query = ds.createQuery(UserInfo.class).field(UserInfo.fnUserId).equal(sinfo.userId);
                    UpdateOperations<UserInfo> ops = ds.createUpdateOperations(UserInfo.class);
                    ops.set(UserInfo.fnContacts, contactList);
                    ds.updateFirst(query, ops);
                }

                return null;
            }
        };

        // 需要互相删除好友
        for (Object obj : Arrays.asList(new Object[]{
                new Object[]{selfInfo, targetId},
                new Object[]{targetInfo, selfId}
        })) {
            func.funcv((Object[]) obj);
        }

        // 向删友请求发起的客户端发消息
        unvarnishedTrans(selfInfo, targetInfo, CMDTYPE_DEL_FRIEND);
    }


    /**
     * 服务器调用环信接口发送透传消息
     */
    public static void unvarnishedTrans(UserInfo selfInfo, UserInfo targetInfo, int cmdType) throws TravelPiException {
        if (selfInfo.easemobUser == null)
            throw new TravelPiException(ErrorCode.UNKOWN_ERROR, "Easemob not regiestered yet.");
        ObjectNode info = (ObjectNode) new SimpleUserFormatter().format(selfInfo);

        ObjectNode ext = Json.newObject();
        ext.put("CMDType", cmdType);
        ext.put("content", info);

        ObjectNode msg = Json.newObject();
        msg.put("type", "cmd");
        msg.put("msg", "agree to be friends");
        msg.put("action", "tzaction");

        ObjectNode requestBody = Json.newObject();
        List<String> users = new ArrayList<>();
        users.add(targetInfo.easemobUser);


        requestBody.put("target_type", "users");
        requestBody.put("target", Json.toJson(users));
        requestBody.put("msg", msg);
        requestBody.put("ext", ext);
        requestBody.put("from", selfInfo.easemobUser);

        // 重新获取token
        Configuration config = Configuration.root().getConfig("easemob");
        String orgName = config.getString("org");
        String appName = config.getString("app");

        String href = String.format("https://a1.easemob.com/%s/%s/messages",
                orgName, appName);

        try {
            URL url = new URL(href);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", String.format("Bearer %s", getEaseMobToken()));

            OutputStreamWriter out = new OutputStreamWriter(conn.getOutputStream());
            out.write(requestBody.toString());
            out.flush();
            out.close();

            InputStream in = conn.getInputStream();
            String body = IOUtils.toString(in, conn.getContentEncoding());

            JsonNode tokenData = Json.parse(body);
            if (tokenData.has("error"))
                throw new TravelPiException(ErrorCode.UNKOWN_ERROR, "");
        } catch (java.io.IOException e) {
            throw new TravelPiException(ErrorCode.UNKOWN_ERROR, "");
        }
    }

    /**
     * api 获得用户的好友列表
     *
     * @param selfId
     * @return
     * @throws TravelPiException
     */
    public static List<UserInfo> getContactList(Integer selfId) throws TravelPiException {
        List<String> fieldList = Arrays.asList(UserInfo.fnContacts);
        UserInfo userInfo = getUserInfo(selfId, fieldList);
        if (userInfo == null)
            throw new TravelPiException(ErrorCode.INVALID_ARGUMENT, "Invalid UserId.");
        List<UserInfo> friends = userInfo.friends;
        if (friends == null)
            friends = new ArrayList<>();

        return friends;
    }

    /**
     * 排序的字段。
     */
    public enum UserInfoField {
        TEL, NICKNAME, OPENID, USERID, EASEMOB
    }
}
