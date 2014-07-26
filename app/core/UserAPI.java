package core;

import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBObject;
import exception.ErrorCode;
import exception.TravelPiException;
import models.MorphiaFactory;
import models.morphia.user.OAuthLoginInfo;
import models.morphia.user.UserInfo;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;

import java.util.ArrayList;

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
        return ds.createQuery(UserInfo.class).field("udid").equal(udid).get();
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
    public static UserInfo regByOAuth(String provider, String oauthId, DBObject extra) throws TravelPiException {
        UserInfo user = getUserByOAuth(provider, oauthId);
        if (user != null)
            return user;

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

        OAuthLoginInfo oauthInfo = new OAuthLoginInfo();
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
}
