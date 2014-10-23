package core;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBObject;
import core.user.ValFormatterFactory;
import exception.ErrorCode;
import exception.TravelPiException;
import models.MorphiaFactory;
import models.morphia.misc.MiscInfo;
import models.morphia.misc.Sequence;
import models.morphia.misc.Token;
import models.morphia.misc.ValidationCode;
import models.morphia.user.Credential;
import models.morphia.user.OAuthInfo;
import models.morphia.user.UserInfo;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;
import play.Configuration;
import play.libs.Json;
import play.mvc.Http;
import utils.Utils;

import javax.crypto.KeyGenerator;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * 用户相关API。
 *
 * @author Zephyre
 */
public class UserAPI {

    /**
     * 排序的字段。
     */
    public enum UserInfoField {
        TEL, NICKNAME, OPENID, USERID
    }

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

    public static UserInfo getUserByUserId(Integer id) throws TravelPiException {
        Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.USER);
        return ds.createQuery(UserInfo.class).field("userId").equal(id).get();
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
        UserInfo userInfo = getUserByUserId(selfId);
        Map<Integer, UserInfo> friends = userInfo.friends;
        boolean flag = friends.containsKey(id);   //查看是否存在好友
        if (flag) {
            Map<Integer, String> friendRemark = userInfo.remark;
            friendRemark.put(id, memo);
            Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.USER);
            ds.save(userInfo);
        } else
            throw new TravelPiException(ErrorCode.INVALID_ARGUMENT, "INVALID_ARGUMENT");

    }

    /**
     * 检查用户是否在黑名单中
     *
     * @param selfId
     * @param id
     */
    public static boolean checkBlackList(Integer selfId, Integer id) throws TravelPiException {
        UserInfo userInfo = getUserByUserId(selfId);
        Map<Integer, UserInfo> blacklist = userInfo.blackList;
        if (blacklist.containsKey(id)) {
            return true;
        } else
            return false;
    }

    /**
     * 将用户添加/移除黑名单
     *
     * @param selfId
     * @param list
     * @param operation
     * @throws TravelPiException
     */
    public static void setUserBlacklist(Integer selfId, List<Integer> list, String operation) throws TravelPiException {
        UserInfo userInfo = getUserByUserId(selfId);
        Map<Integer, UserInfo> blackList = userInfo.blackList;  //用户的黑名单列表
        Map<Integer, UserInfo> friends = userInfo.friends;     //用户的朋友圈列表
        switch (operation) {
            case "add":         //用户加入到黑名单
                for (Integer id : list) {
                    if (blackList.containsKey(id)) {         //黑名单中存在用户已经
                        continue;
                    }
                    UserInfo user = getUserByUserId(id);
                    blackList.put(id, user);            //添加用户到黑名单
                    friends.remove(id);                 //将用户从朋友圈中删除
                }
                break;
            case "remove":                                  //用户移除黑名单
                if (list.isEmpty()) {                        //黑名单空的话，抛出异常
                    throw new TravelPiException(ErrorCode.INVALID_ARGUMENT, "INVALID_ARGUMENT");
                } else {
                    for (Integer id : list) {
                        if (!blackList.containsKey(id)) {        //用户不再黑名单中
                            continue;
                        }
                        UserInfo user = getUserByUserId(id);
                        blackList.remove(id);                 //添加用户到朋友圈
                        friends.put(id, user);
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
        UserInfo userInfo = getUserByUserId(userId);
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
            user.secToken = secToken;
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
        user.secToken = secToken;

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
        if (uid == null || uid.isEmpty() || timestamp == null || timestamp.isEmpty() || sign == null || sign.isEmpty())
            return false;

        try {
            UserInfo userInfo = UserAPI.getUserById(uid);
            if (userInfo == null || userInfo.secToken == null)
                return false;
            String serverSign = Utils.toSha1Hex(timestamp + userInfo.secToken);
            if (!sign.equals(serverSign))
                return false;
        } catch (NullPointerException e) {
            return false;
        }
        return true;
    }

    /**
     * 根据字段获得用户信息。
     *
     * @param
     * @return
     */
    public static UserInfo getUserByField(UserInfoField field, String value) throws TravelPiException {
        Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.USER);
        String stKey;
        UserInfo userInfo = null;
        switch (field) {
            case TEL:
                stKey = "tel";
                userInfo = ds.createQuery(UserInfo.class).field(stKey).equal(value).get();
                break;
            case NICKNAME:
                stKey = "nickName";
                userInfo = ds.createQuery(UserInfo.class).field(stKey).equal(value).get();
                break;
            case OPENID:
                stKey = "oauthList.oauthId";
                userInfo = ds.createQuery(UserInfo.class).field(stKey).equal(value).get();
                break;

        }
        return userInfo;
    }

    /**
     * 根据字段获得用户信息。
     *
     * @param
     * @return
     */
    public static UserInfo getUserByField(UserInfoField field, int value) throws TravelPiException {
        Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.USER);
        String stKey ;
        UserInfo userInfo = null;
        switch (field) {
            case USERID:
                stKey = "userId";
                userInfo = ds.createQuery(UserInfo.class).field(stKey).equal(value).get();
                break;

        }
        return userInfo;
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
        user.userId = getUserId();
        user.avatar = "http://default";
        user.oauthList = new ArrayList<>();
        user.tel = tel;
        user.nickName = "桃子_" + user.userId;
        user.gender = "F";
        user.countryCode = countryCode;
        user.email = "";
        user.secToken = Utils.getSecToken();
        user.signature = "";
        user.origin = "peach-telUser";
        user.enabled = true;

        // 注册私密信息
        regCredential(user, pwd);

        MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.USER).save(user);

        return user;
    }

    public static Integer getUserId() throws TravelPiException {
        Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.MISC);
        Query<Sequence> query = ds.createQuery(Sequence.class);
        query.field("column").equals(Sequence.USERID);
        Integer uid = query.get().count;
        //DBObject newDocument = new BasicDBObject();
        //newDocument.put("$inc", new BasicDBObject().append("count", 1));
        UpdateOperations<Sequence> ops = ds.createUpdateOperations(Sequence.class).inc("count");
        ds.findAndModify(query, ops);
        return uid;
    }

    /**
     * 密码加密
     *
     * @param u
     * @param pwd
     * @return
     */
    public static void regCredential(UserInfo u, String pwd) throws TravelPiException {
        Credential cre = new Credential();
        cre.id = u.id;
        cre.userId = u.userId;
        cre.salt = Utils.getSalt();
        if (!pwd.equals(""))
            cre.pwdHash = Utils.toSha1Hex(cre.salt + pwd);

        // 环信注册
        String base = "abcdefghijklmnopqrstuvwxyz0123456789";
        int size = base.length();
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 32; i++)
            sb.append(base.charAt(random.nextInt(size)));
        cre.easemobUser = sb.toString();
        String passwd = null;
        try {
            passwd = Base64.encodeBase64String(KeyGenerator.getInstance("HmacSHA256").generateKey().getEncoded());
        } catch (NoSuchAlgorithmException ignored) {
        }
        assert passwd != null;
        cre.easemobPwd = passwd;

        regEaseMob(cre.easemobUser, cre.easemobPwd);

        MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.USER).save(cre);
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
     * 重设密码
     *
     * @param u
     * @param pwd
     * @throws TravelPiException
     */
    public static void resetPwd(UserInfo u, String pwd) throws TravelPiException {

        Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.USER);
        Query<Credential> ceQuery = ds.createQuery(Credential.class);
        Credential cre = ceQuery.field("userId").equal(u.userId).get();
        cre.salt = Utils.getSalt();
        cre.pwdHash = Utils.toSha1Hex(cre.salt + pwd);
        ds.save(cre);
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
        if (ce == null)
            return false;
        else if (ce.pwdHash.equals(Utils.toSha1Hex(ce.salt + pwd)))
            return true;
        else
            return false;
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
    public static boolean checkValidation(int countryCode, String tel, int actionCode, String valCode, int userId)
            throws TravelPiException {
        Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.MISC);
        ValidationCode entry = ds.createQuery(ValidationCode.class).field("key")
                .equal(ValidationCode.calcKey(countryCode, tel, actionCode)).get();

        // 如果actionCode == 1或2,是注册,不需要验证userId
        boolean ret = !(entry == null || !entry.value.equals(valCode) || System.currentTimeMillis() > entry.expireTime
                || (isNeedCheckUserId(actionCode) && entry.userId != userId));

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
        Token uniq = ds.createQuery(Token.class).field("value").equal(token).field("used").equal(false).get();
        //设置已使用过
        uniq.used = false;
        ds.save(uniq);
        boolean ret = !(uniq == null || !uniq.value.equals(token) || System.currentTimeMillis() > uniq.expireTime ||
                !uniq.permissionList.contains(actionCode)||(isNeedCheckUserId(actionCode)&& uniq.userId != userId));
        return ret;
    }

    /**
     * 根据actionCode,
     *
     * @param actionCode
     * @return
     */
    private static boolean isNeedCheckUserId(int actionCode){
        if(actionCode == 1 || actionCode == 2){
            return false;
        }
        return true;
    }
    /**
     * 注册环信用户
     *
     * @param userName
     * @param pwd
     */
    private static void regEaseMob(String userName, String pwd) throws TravelPiException {
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
            if (tokenData.has("error"))
                throw new TravelPiException(ErrorCode.UNKOWN_ERROR, "Error in user registration.");
        } catch (java.io.IOException e) {
            throw new TravelPiException(ErrorCode.UNKOWN_ERROR, "Error in user registration.");
        }
    }
}
