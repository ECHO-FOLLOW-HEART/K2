package core;

import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBObject;
import exception.ErrorCode;
import exception.TravelPiException;
import models.MorphiaFactory;
import models.morphia.user.Credential;
import models.morphia.user.OAuthInfo;
import models.morphia.user.UserInfo;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.UpdateOperations;
import play.mvc.Http;
import utils.Utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * 用户相关API。
 *
 * @author Zephyre
 */
public class UserAPI {

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
     * 根据手机号码获得用户信息。
     *
     * @param tel
     * @return
     */
    public static UserInfo getUserByTel(String tel) throws TravelPiException {
        Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.USER);
        return ds.createQuery(UserInfo.class).field("tel").equal(tel).get();
    }

    /**
     * 根据昵称获得用户信息。
     *
     * @param nickName
     * @return
     */
    public static UserInfo getUserByNickName(String nickName) throws TravelPiException {
        Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.USER);
        return ds.createQuery(UserInfo.class).field("nickName").equal(nickName).get();
    }

    /**
     * 暂存验证码。
     *
     * @param
     * @return
     */
    public static void saveCaptcha(Credential ce) throws TravelPiException {
        Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.USER);
        ds.save(ce);
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

        final DateFormat fmt = new SimpleDateFormat("yyMMddHHmmss");
        UserInfo user = new UserInfo();
        user.id = new ObjectId();
        user.userId = fmt.format(new Date());
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


}
