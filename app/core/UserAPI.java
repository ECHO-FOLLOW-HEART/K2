package core;

import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBObject;
import core.user.ValFormatterFactory;
import exception.ErrorCode;
import exception.TravelPiException;
import models.MorphiaFactory;
import models.morphia.misc.MiscInfo;
import models.morphia.misc.Sequence;
import models.morphia.plan.Plan;
import models.morphia.traffic.TrainRoute;
import models.morphia.misc.ValidationCode;

import models.morphia.user.Credential;
import models.morphia.user.OAuthInfo;
import models.morphia.user.UserInfo;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;
import play.mvc.Http;
import utils.DataConvert.UserConvert;
import utils.Utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
        TEL, NICKNAME, OPENID
    }

    public static UserInfo getUserById(ObjectId id) throws TravelPiException {
        Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.USER);
        return ds.createQuery(UserInfo.class).field("_id").equal(id).get();
    }

    public static UserInfo getUserById(String id) throws TravelPiException {
        try {
            return getUserById(new ObjectId(id));
        } catch (IllegalArgumentException e) {
            throw new TravelPiException(ErrorCode.INVALID_OBJECTID, String.format("Invalid user ID: %s.", id));
        }
    }

    public static UserInfo getUserByUserId(String id) throws TravelPiException {
        Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.USER);
        return ds.createQuery(UserInfo.class).field("userId").equal(id).get();
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
    public static UserInfo getUserByField(UserInfoField field,String value) throws TravelPiException {
        String stKey = null;
        switch (field) {
            case TEL:
                stKey = "tel";
                break;
            case NICKNAME:
                stKey = "nickName";
                break;
            case OPENID:
                stKey = "oauthList.oauthId";
                break;
        }
        Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.USER);
        return ds.createQuery(UserInfo.class).field(stKey).equal(value).get();
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
    public static UserInfo regByTel(String tel) throws TravelPiException {

        UserInfo user = new UserInfo();
        user.id = new ObjectId();
        user.userId = getUserId();
        user.nickName = (new ObjectId()).toString();
        user.avatar = "http://default";
        user.oauthList = new ArrayList<>();
        user.tel = tel;
        user.gender = "F";
        user.countryCode = 86;
        user.email = "";
        user.signature = "";
        user.origin = "peach";
        user.enabled = true;

        Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.USER);
        ds.save(user);
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
        ds.findAndModify(query,ops);
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
        cre.pwdHash = Utils.toSha1Hex(cre.salt + pwd);

        Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.USER);
        ds.save(cre);
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
    public static void sendValCode(int countryCode, String tel, int actionCode, long expireMs, long resendMs)
            throws TravelPiException {
        Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.MISC);
        ValidationCode valCode = ds.createQuery(ValidationCode.class).field("key")
                .equal(ValidationCode.calcKey(countryCode, tel, actionCode)).get();

        if (valCode != null && System.currentTimeMillis() < valCode.resendTime)
            throw new TravelPiException(ErrorCode.SMS_QUOTA_ERROR, "SMS out of quota.");

        ValidationCode oldCode = valCode;
        valCode = ValidationCode.newInstance(countryCode, tel, actionCode, expireMs);
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
    public static boolean checkValidation(int countryCode, String tel, int actionCode, String valCode)
            throws TravelPiException {
        Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.MISC);
        ValidationCode entry = ds.createQuery(ValidationCode.class).field("key")
                .equal(ValidationCode.calcKey(countryCode, tel, actionCode)).get();
        return !(entry == null || !entry.value.equals(valCode) || System.currentTimeMillis() > entry.expireTime);
    }
}
