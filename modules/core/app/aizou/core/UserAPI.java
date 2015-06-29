package aizou.core;

import aizou.core.user.ValFormatterFactory;
import aspectj.Key;
import aspectj.UsingOcsCache;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBObject;
import asynchronous.AsyncExecutor;
import exception.AizouException;
import exception.ErrorCode;
import formatter.taozi.user.UserFormatterOld;
import models.AizouBaseEntity;
import com.lvxingpai.k2.core.MorphiaFactory;
import models.geo.Locality;
import models.misc.*;
import models.user.Credential;
import models.user.OAuthInfo;
import models.user.Relationship;
import models.user.UserInfo;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.CriteriaContainerImpl;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;
import play.Configuration;
import play.libs.F;
import play.libs.Json;
import utils.Constants;
import utils.Utils;

import javax.crypto.KeyGenerator;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/**
 * 用户相关API。
 *
 * @author Zephyre
 */import utils.LogUtils;

public class UserAPI {

    public static int CMDTYPE_REQUEST_FRIEND = 1;
    public static int CMDTYPE_ADD_FRIEND = 2;
    public static int CMDTYPE_DEL_FRIEND = 3;

    public static UserInfo getUserById(ObjectId id) throws AizouException {
        Datastore ds = MorphiaFactory.datastore();
        return ds.createQuery(UserInfo.class).field("_id").equal(id).get();
    }

    public static UserInfo getUserById(String id) throws AizouException {
        try {
            return getUserById(new ObjectId(id));
        } catch (IllegalArgumentException e) {
            throw new AizouException(ErrorCode.INVALID_ARGUMENT, String.format("Invalid user ID: %s.", id));
        }
    }

    /**
     * 获取用户信息
     *
     * @throws exception.AizouException
     */
    public static UserInfo getUserInfo(Long id) throws AizouException {
        return getUserInfo(id, null);
    }

    /**
     * 获得用户的ObjectId
     * <p>
     * 该方法的主要用途是：提供一个UserId，检查该用户是否存在。以此作为和用户相关的接口的预先验证。
     *
     * @param id
     * @return
     * @throws AizouException
     */
    @UsingOcsCache(key = "getUserOid|{id}", expireTime = 86400)
    public static String getUserOid(@Key(tag = "id") Long id) throws AizouException {
        UserInfo info = getUserInfo(id, Arrays.asList("_id"));
        if (info != null)
            return info.getId().toString();
        else
            return null;
    }

    /**
     * 获取用户信息（添加fieldList限定）
     *
     * @throws exception.AizouException
     */
    public static UserInfo getUserInfo(Long id, Collection<String> fieldList) throws AizouException {
        Datastore ds = MorphiaFactory.datastore();
        Query<UserInfo> query = ds.createQuery(UserInfo.class).field(UserInfo.fnUserId).equal(id);
        if (fieldList != null && !fieldList.isEmpty())
            query.retrievedFields(true, fieldList.toArray(new String[fieldList.size()]));

        return query.get();
    }

    /**
     * 修改用户备注
     *
     * @param
     * @param
     * @param memo
     * @throws
     */
    public static void setUserMemo(Long selfId, Long targetId, String memo) throws AizouException {

        Datastore ds = MorphiaFactory.datastore();
        Query<Relationship> query = ds.createQuery(Relationship.class);
        String memoTarget;
        Long userA;
        Long userB;
        if (selfId > targetId) {
            userA = targetId;
            userB = selfId;
            memoTarget = Relationship.FD_MemoA;
        } else {
            userA = selfId;
            userB = targetId;
            memoTarget = Relationship.FD_MemoB;
        }
        query.field("userA").equal(userA).field("userB").equal(userB);
        UpdateOperations<Relationship> ops = ds.createUpdateOperations(Relationship.class);
        ops.set(memoTarget, memo);
        ds.update(query, ops);
    }

    public static List<UserInfo> addUserMemo(Long selfId, List<UserInfo> friends) throws AizouException {
        List<UserInfo> result = new ArrayList<>();
        Long friendId;
        // UserId比自己大的用户列表
        List<Long> bigUserIds = new ArrayList<>();
        List<UserInfo> bigUsers = new ArrayList<>();
        // UserId比自己小的用户列表
        List<Long> smallUserIds = new ArrayList<>();
        List<UserInfo> smallUsers = new ArrayList<>();
        for (UserInfo userInfo : friends) {
            friendId = userInfo.getUserId();
            if (selfId < friendId) {
                bigUserIds.add(userInfo.getUserId());
                bigUsers.add(userInfo);
            } else {
                smallUserIds.add(userInfo.getUserId());
                smallUsers.add(userInfo);
            }
        }

        Datastore ds = MorphiaFactory.datastore();
        Query<Relationship> query;
        if (bigUserIds != null && !bigUserIds.isEmpty()) {
            query = ds.createQuery(Relationship.class);
            query.field(Relationship.FD_UserA).equal(selfId).field(Relationship.FD_UserB).in(bigUserIds);
            List<Relationship> bigUserRs = query.asList();
            // 比我的userId大的用户，备注取memoB
            setMomeByDifUser(bigUserRs, bigUsers, Relationship.FD_MemoB);
        }

        if (smallUserIds != null && !smallUserIds.isEmpty()) {
            query = ds.createQuery(Relationship.class);
            query.field(Relationship.FD_UserB).equal(selfId).field(Relationship.FD_UserA).in(smallUserIds);
            List<Relationship> smallUserRs = query.asList();
            // 比我的userId小的用户，备注取memoA
            setMomeByDifUser(smallUserRs, smallUsers, Relationship.FD_MemoA);
        }

        result.addAll(bigUsers);
        result.addAll(smallUsers);
        return result;
    }

    private static void setMomeByDifUser(List<Relationship> rs, List<UserInfo> friends, String momeTarget) {
        Map<Long, String> userMemo = new HashMap<>();
        for (Relationship temp : rs) {
            if (momeTarget.equals(Relationship.FD_MemoB))
                userMemo.put(temp.getUserB(), temp.getMemoB());
            else if (momeTarget.equals(Relationship.FD_MemoA))
                userMemo.put(temp.getUserA(), temp.getMemoA());
        }
        Long userId;
        for (UserInfo user : friends) {
            userId = user.getUserId();
            if (userMemo.get(userId) != null)
                user.setMemo(userMemo.get(userId));
        }
    }


//
//    /**
//     * 检查用户是否在黑名单中
//     *
//     * @param selfId
//     * @param id
//     */
//   /* public static boolean checkBlackList(Integer selfId,Integer id) throws TravelPiException {
//        UserInfo userInfo=getUserInfo(selfId);
//        Map<Integer,UserInfo> blacklist= userInfo.blackList;
//        if (blacklist.containsKey(id)){
//            return true;
//        } else
//            return false;
//    }*/
//
//    /**
//     * 将用户添加/移除黑名单
//     *
//     * @param selfId
//     * @param list
//     * @param operation
//     * @throws TravelPiException
//     */
//    public static void setUserBlacklist(Integer selfId, List<Integer> list, String operation) throws TravelPiException {
//        UserInfo userInfo = getUserInfo(selfId);
//        Map<Integer, UserInfo> blackList = userInfo.blackList;  //用户的黑名单列表
////        Map<Integer, UserInfo> friends = userInfo.friends;     //用户的朋友圈列表
//        switch (operation) {
//            case "add":         //用户加入到黑名单
//                for (Integer id : list) {
//                    if (blackList.containsKey(id)) {         //黑名单中存在用户已经
//                        continue;
//                    }
//                    UserInfo user = getUserInfo(id);
//                    blackList.put(id, user);            //添加用户到黑名单
////                    friends.remove(id);                 //将用户从朋友圈中删除
//                }
//                addEaseMobBlocks(selfId, list);          //向环信中注册
//                break;
//            case "remove":                                  //用户移除黑名单
//                if (list.isEmpty()) {                        //黑名单空的话，抛出异常
//                    throw new TravelPiException(ErrorCode.INVALID_ARGUMENT, "INVALID_ARGUMENT");
//                } else {
//                    for (Integer id : list) {
//                        if (!blackList.containsKey(id)) {        //用户不再黑名单中
//                            continue;
//                        }
//                        UserInfo user = getUserInfo(id);
//                        blackList.remove(id);                 //添加用户到朋友圈
////                        friends.put(id, user);
//                    }
//                    for (Integer id : list) {
//                        delEaseMobBlocks(selfId, id);
//                    }
//                    break;
//                }
//            default:
//                throw new TravelPiException(ErrorCode.INVALID_ARGUMENT, "INVALID_ARGUMENT");
//        }
//        Datastore ds = MorphiaFactory.datastore();    //更新用户的信息
//        ds.save(userInfo);
//    }
//
//    /**
//     * 获得用户的黑名单列表
//     *
//     * @param userId
//     * @return
//     * @throws TravelPiException
//     */
//    public static List<Integer> getBlackList(Integer userId) throws TravelPiException {
//        UserInfo userInfo = getUserInfo(userId);
//        Map<Integer, UserInfo> blackList = userInfo.blackList;
//        List<Integer> list = new ArrayList<>();
//        if (!blackList.isEmpty()) {          //黑名单不为空
//            for (Integer i : blackList.keySet()) {
//                list.add(i);
//            }
//        }
//        return list;
//    }

    /**
     * 根据OAuth信息获得用户信息。
     *
     * @param provider
     * @param oauthId
     * @return
     */
    public static UserInfo getUserByOAuth(String provider, String oauthId) throws AizouException {
        Datastore ds = MorphiaFactory.datastore();
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
    public static UserInfo getUserByUdid(String udid) throws AizouException {
        Datastore ds = MorphiaFactory.datastore();
        return ds.createQuery(UserInfo.class).field("udid").equal(udid).field("oauthList").notEqual(null).get();
    }

    /**
     * 根据UDID完成用户注册。
     *
     * @param udid
     * @return
     */
    public static UserInfo regByUdid(String udid) throws AizouException {
        UserInfo user = getUserByUdid(udid);
        if (user != null)
            return user;

        user = UserInfo.newInstance(populateUserId());
        user.setNickName((new ObjectId()).toString());
        user.setAvatar("http://default");

        Datastore ds = MorphiaFactory.datastore();
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
     * @throws exception.AizouException
     */
    public static UserInfo regByOAuth(String provider, String oauthId, DBObject extra, String secToken) throws AizouException {
        UserInfo user = getUserByOAuth(provider, oauthId);
        if (user != null) {
            //更新签名
            Datastore ds = MorphiaFactory.datastore();
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

        user = UserInfo.newInstance(populateUserId());

        user.setNickName(nickName);
        user.setAvatar(avatar);

        OAuthInfo oauthInfo = new OAuthInfo();
        oauthInfo.setProvider(provider);
        oauthInfo.setOauthId(oauthId);
        oauthInfo.setNickName(nickName);
        oauthInfo.setAvatar(avatar);
        oauthInfo.setToken(token);
        List<OAuthInfo> oauthList = user.getOauthList();
        if (oauthList == null)
            oauthList = new ArrayList<>();
        // TODO 此处需要检查。假设原来的oauthList已经有微信了，现在如果再调用一次，是否会append一个新的微信账号进去？
        oauthList.add(oauthInfo);
        user.setOauthList(oauthList);

        Datastore ds = MorphiaFactory.datastore();
        ds.save(user);

        return user;
    }

//    public static void updateUserInfo(Http.Request req) throws AizouException {
//        String seq = req.getQueryString("seq");
//        String platform = req.getQueryString("platform");
//        String appVersion = req.getQueryString("v");
//        String uid = req.getQueryString("uid");
//
//        //取得用户信息
//        Datastore dsUser = MorphiaFactory.datastore();
//        // 优先按照uid进行lookup
//        UserInfo user;
//        if (uid != null)
//            user = dsUser.createQuery(UserInfo.class).field("_id").equal(new ObjectId(uid)).get();
//        else if (seq != null)
//            user = dsUser.createQuery(UserInfo.class).field("udid").equal(seq).field("oauthList").equal(null).get();
//        else
//            user = null;
//
//        if (user == null)
//            return;
//
//        //设置更新信息：用户机系统信息、用户App版本、用户设备编号
//        UpdateOperations<UserInfo> ops = dsUser.createUpdateOperations(UserInfo.class);
//        ops.set("platform", platform);
//        ops.set("appVersion", appVersion);
//        ops.set("udid", seq);
//        ops.set("enabled", true);
//
//        dsUser.updateFirst(dsUser.createQuery(UserInfo.class).field("_id").equal(user.getId()), ops);
//    }

    public static void updateUserInfo(Map<String, Object> reqMap) throws AizouException {

        //取得用户信息
        Datastore dsUser = MorphiaFactory.datastore();
        UpdateOperations<UserInfo> ops = dsUser.createUpdateOperations(UserInfo.class);
        for (Map.Entry<String, Object> entry : reqMap.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (key.equals(UserInfo.fnTracks))
                value = strListToObjectIdList((Iterator<JsonNode>) value, Locality.class);
            else if (key.equals(UserInfo.fnTravelNotes))
                value = strListToObjectIdList((Iterator<JsonNode>) value, TravelNote.class);
            ops.set(key, value);
        }
        dsUser.updateFirst(dsUser.createQuery(UserInfo.class).field(UserInfo.fnUserId).equal(reqMap.get(UserInfo.fnUserId)), ops);
    }

    private static <T extends AizouBaseEntity> List<T> strListToObjectIdList(Iterator<JsonNode> it, Class<T> cls) {
        List<T> result = new ArrayList<>();
        T entity;
        String oid;
        ObjectId id;
        try {
            Constructor constructor = cls.getConstructor();
            for (Iterator<JsonNode> iterator = it; iterator.hasNext(); ) {
                oid = (it.next()).asText();
                id = new ObjectId(oid);
                entity = (T) constructor.newInstance();
                entity.setId(id);
                result.add(entity);
            }
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            return null;
        }
        return result;
    }

    /**
     * 进行用户权限验证
     *
     * @param uid
     * @param timestamp
     * @param sign
     * @return
     * @throws exception.AizouException
     */
    public static boolean authenticate(String uid, String timestamp, String sign) throws AizouException {
        return true;
    }

    public static Iterator<UserInfo> searchUser(Collection<String> fieldDesc, Collection<?> valueList,
                                                Collection<String> fieldList,
                                                int page, int pageSize)
            throws AizouException {
        Datastore ds = MorphiaFactory.datastore();

        if (fieldDesc == null || fieldDesc.isEmpty())
            throw new AizouException(ErrorCode.INVALID_ARGUMENT, "Invalid fields.");
        List<CriteriaContainerImpl> criList = new ArrayList<>();
        for (String fd : fieldDesc) {
            // 为了达到忽略大小写的效果，用户名用别名搜索
            if (fd.equals(UserInfo.fnNickName)) {
                fd = UserInfo.fnAlias;
                valueList = phraseUserAlias(valueList);
            }
            if (fd.equals(UserInfo.fnUserId) && Utils.isNumeric(valueList))
                valueList = phraseUserIdType(valueList);
            if (valueList.size() == 1) {
                criList.add(ds.createQuery(UserInfo.class).criteria(fd).equal(valueList.iterator().next()));
            } else if (valueList.size() > 1)
                criList.add(ds.createQuery(UserInfo.class).criteria(fd).hasAnyOf(valueList));
        }
        Query<UserInfo> query = ds.createQuery(UserInfo.class);
        query.or(criList.toArray(new CriteriaContainerImpl[criList.size()]));

        if (fieldList != null && !fieldList.isEmpty())
            query.retrievedFields(true, fieldList.toArray(new String[fieldList.size()]));
        query.field("origin").equal(Constants.APP_FLAG_TAOZI);
        query.order(String.format("-%s, %s", UserInfo.fnLevel, UserInfo.fnUserId));
        return query.offset(page * pageSize).limit(pageSize).iterator();
    }

    private static List<Long> phraseUserIdType(Collection<?> list) {
        List<Long> result = new ArrayList<>();
        for (Object temp : list) {
            result.add(Long.valueOf(temp.toString()));
        }
        return result;
    }

    private static List<String> phraseUserAlias(Collection<?> list) {
        List<String> result = new ArrayList<>();
        for (Object temp : list) {
            result.add(temp.toString().toLowerCase());
        }
        return result;
    }

    /**
     * 根据字段获得用户信息。
     *
     * @param fieldDesc 哪些字段为查询目标？
     * @param fieldList 返回结果包含哪些字段？
     */
    public static UserInfo getUserByField(List<String> fieldDesc, Object value, List<String> fieldList)
            throws AizouException {
        Iterator<UserInfo> itr = searchUser(fieldDesc, Arrays.asList(value), fieldList, 0, 1);
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
    public static UserInfo getUserByField(String fieldDesc, Object value, List<String> fieldList)
            throws AizouException {
        return getUserByField(Arrays.asList(fieldDesc), value, fieldList);
    }

    /**
     * 根据字段获得用户信息。
     *
     * @param fieldDesc 哪些字段为查询目标？
     */
    public static UserInfo getUserByField(String fieldDesc, Object value)
            throws AizouException {
        return getUserByField(Arrays.asList(fieldDesc), value, null);
    }


    /**
     * 储存用户信息。
     *
     * @param
     * @return
     */
    public static void saveUserInfo(UserInfo u) throws AizouException {
        Datastore ds = MorphiaFactory.datastore();
        ds.save(u);
    }

    /**
     * 根据手机号码完成用户注册。
     *
     * @param
     * @return
     */
    public static UserInfo regByTel(String tel, Integer dialCode, String pwd) throws AizouException {
        UserInfo user = UserInfo.newInstance(populateUserId());
        user.setTel(tel);
        user.setDialCode(dialCode);
        user.setOrigin(Constants.APP_FLAG_TAOZI);

        // 注册环信
        String[] ret = regEasemob();
        user.setEasemobUser(ret[0]);
        MorphiaFactory.datastore().save(user);

        // 注册机密信息
        String easemobPwd = ret[1];
        Credential cre = new Credential();
        cre.setId(user.getId());
        cre.setUserId(user.getUserId());
        cre.setSalt(Utils.getSalt());
        if (!pwd.equals(""))
            cre.setPwdHash(Utils.toSha1Hex(cre.getSalt() + pwd));
        cre.setEasemobPwd(easemobPwd);
        try {
            cre.setSecKey(Base64.encodeBase64String(KeyGenerator.getInstance("HmacSHA256").generateKey().getEncoded()));
        } catch (NoSuchAlgorithmException e) {
            throw new AizouException(ErrorCode.UNKOWN_ERROR, "", e);
        }

        MorphiaFactory.datastore().save(cre);

        return user;
    }

    /**
     * 根据微信完成用户注册。
     *
     * @param
     * @return
     */
    public static UserInfo regByWeiXin(UserInfo user) throws AizouException {

        // 注册环信
        String[] ret = regEasemob();
        String easemobPwd = ret[1];

        user.setEasemobUser(ret[0]);
        user.setOrigin(Constants.APP_FLAG_TAOZI);
        MorphiaFactory.datastore().save(user);

        // 注册机密信息
        Credential cre = new Credential();
        cre.setId(user.getId());
        cre.setUserId(user.getUserId());
        cre.setSalt(Utils.getSalt());
        cre.setEasemobPwd(easemobPwd);
        try {
            cre.setSecKey(Base64.encodeBase64String(KeyGenerator.getInstance("HmacSHA256").generateKey().getEncoded()));
        } catch (NoSuchAlgorithmException e) {
            throw new AizouException(ErrorCode.UNKOWN_ERROR, "", e);
        }
        MorphiaFactory.datastore().save(cre);

        return user;
    }

    public static UserInfo oauthToUserInfoForWX(JsonNode json) throws AizouException {
        String nickname = json.get("nickname").asText();
        String headimgurl = json.get("headimgurl").asText();
        UserInfo userInfo = UserInfo.newInstance(populateUserId());
        userInfo.setNickName(nickname);
        userInfo.setAvatar(headimgurl);
        userInfo.setGender(json.get("sex").asText().equals("1") ? "M" : "F");
        userInfo.setOrigin(Constants.APP_FLAG_TAOZI);

        OAuthInfo oauthInfo = new OAuthInfo();
        oauthInfo.setProvider("weixin");
        oauthInfo.setOauthId(json.get("openid").asText());
        oauthInfo.setNickName(nickname);
        oauthInfo.setAvatar(headimgurl);
        List<OAuthInfo> oauthList = userInfo.getOauthList();
        if (oauthList == null)
            oauthList = new ArrayList<>();
        // TODO 直接append，会不会相同来源的多个Oauth账号重复在一起？
        oauthList.add(oauthInfo);
        userInfo.setOauthList(oauthList);

        return userInfo;
    }

    public static Long populateUserId() throws AizouException {
        Datastore ds = MorphiaFactory.datastore();
        Query<Sequence> query = ds.createQuery(Sequence.class);
        query.field("column").equal(Sequence.USERID);
        UpdateOperations<Sequence> ops = ds.createUpdateOperations(Sequence.class).inc("count");
        Sequence ret = ds.findAndModify(query, ops);
        if (ret == null)
            throw new AizouException(ErrorCode.UNKOWN_ERROR, "Unable to generate UserId sequences.");
        return ret.count;
    }

    /**
     * 注册密码和环信
     *
     * @return 返回环信的用户名和密码
     */
    public static String[] regEasemob() throws AizouException {
        String easemobPwd;
        try {
            easemobPwd = Base64.encodeBase64String(KeyGenerator.getInstance("HmacSHA256").generateKey().getEncoded());
        } catch (NoSuchAlgorithmException e) {
            throw new AizouException(ErrorCode.UNKOWN_ERROR, "", e);
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
            } catch (AizouException e) {
                if (e.getErrCode() != ErrorCode.USER_EXIST)
                    throw e;
            }
        }

        if (flag)
            return new String[]{easemobName, easemobPwd};
        else
            throw new AizouException(ErrorCode.UNKOWN_ERROR, "");
    }

    /**
     * 判断是否有密码
     *
     * @param
     * @param
     * @return
     */
    public static Credential getPwd(UserInfo u) throws AizouException {

        Datastore ds = MorphiaFactory.datastore();
        Query<Credential> ceQuery = ds.createQuery(Credential.class);
        if (u.getUserId() == null)
            return null;
        Credential cre = ceQuery.field("userId").equal(u.getUserId()).get();
        if (cre == null || cre.getPwdHash() == null)
            return null;
        return cre;

    }

    /**
     * 获得用户的私密信息。
     *
     * @param userId
     * @return
     * @throws exception.AizouException
     */
    public static Credential getCredentialByUserId(Long userId) throws AizouException {
        return getCredentialByUserId(userId, null);
    }

    /**
     * 获得用户的私密信息。
     *
     * @param userId
     * @param fieldList
     * @return
     * @throws exception.AizouException
     */
    public static Credential getCredentialByUserId(Long userId, List<String> fieldList) throws AizouException {
        Datastore ds = MorphiaFactory.datastore();
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
     * @throws exception.AizouException
     */
    public static void resetPwd(UserInfo u, String pwd) throws AizouException {
        if (pwd == null || pwd.isEmpty() || u == null || u.getUserId() == null)
            throw new AizouException(ErrorCode.INVALID_ARGUMENT, "");

        Datastore ds = MorphiaFactory.datastore();
        Credential cre = ds.createQuery(Credential.class).field(Credential.fnUserId).equal(u.getUserId()).get();
        cre.setSalt(Utils.getSalt());
        cre.setPwdHash(Utils.toSha1Hex(cre.getSalt() + pwd));

        MorphiaFactory.datastore().save(cre);
    }

    /**
     * 密码验证
     *
     * @param u
     * @param pwd
     * @return
     */
    public static boolean validCredential(UserInfo u, String pwd) throws AizouException {
        Datastore ds = MorphiaFactory.datastore();
        Query<Credential> ceQuery = ds.createQuery(Credential.class);
        Credential ce = ceQuery.field("userId").equal(u.getUserId()).get();
        return ce != null && ce.getPwdHash().equals(Utils.toSha1Hex(ce.getSalt() + pwd));
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
    public static Long sendValCode(int countryCode, String tel, int actionCode, Integer userId, long expireMs, long resendMs)
            throws AizouException {
        Datastore ds = MorphiaFactory.datastore();
        ValidationCode valCode = ds.createQuery(ValidationCode.class).field("key")
                .equal(ValidationCode.calcKey(countryCode, tel, actionCode)).get();

        // 如果当前时间小于设置的时间间隔，就返回
        if (valCode != null && System.currentTimeMillis() < valCode.resendTime)
            return (valCode.resendTime - System.currentTimeMillis()) / 1000;

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
        return resendMs / 1000;
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
            throws AizouException {
        Datastore ds = MorphiaFactory.datastore();
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
    private static String getEaseMobToken() throws AizouException {
        Datastore ds = MorphiaFactory.datastore();
        MiscInfo info = ds.createQuery(MiscInfo.class).field("key").equal(MiscInfo.FD_TAOZI_HUANXIN_INFO).get();

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
                OutputStreamWriter out = new OutputStreamWriter(conn.getOutputStream(), "UTF-8");
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
                throw new AizouException(ErrorCode.UNKOWN_ERROR, "Error in retrieving token.");
            }
        }

        return info.easemobToken;
    }

    public static Token valCodetoToken(Integer countryCode, String tel, int actionCode, int userId, long expireMs) throws AizouException {
        ValidationCode valCode = ValidationCode.newInstance(countryCode, tel, actionCode, userId, expireMs);
        Datastore ds = MorphiaFactory.datastore();
        Token token = Token.newInstance(valCode, expireMs);

        Token uniq = ds.createQuery(Token.class).field("value")
                .equal(token.value).field("userId").equal(token.userId).get();
        if (uniq != null) {
            throw new AizouException(ErrorCode.SMS_QUOTA_ERROR, "Token out of quota.");
        }
        ds.save(token);
        return token;
    }

    public static boolean checkToken(String token, int userId, int actionCode) throws AizouException {
        Datastore ds = MorphiaFactory.datastore();
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
    private static void regEasemobReq(String userName, String pwd) throws AizouException {
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
            OutputStreamWriter out = new OutputStreamWriter(conn.getOutputStream(), "UTF-8");
            out.write(data.toString());
            out.flush();
            out.close();

            InputStream in = conn.getInputStream();
            String body = IOUtils.toString(in, conn.getContentEncoding());

            JsonNode tokenData = Json.parse(body);
            if (tokenData.has("error")) {
                if (tokenData.get("error").asText().equals("duplicate_unique_property_exists"))
                    throw new AizouException(ErrorCode.USER_EXIST, String.format("Easemob user %s exists.", userName));
                else
                    throw new AizouException(ErrorCode.UNKOWN_ERROR, "Error in user registration.");
            }
        } catch (java.io.IOException e) {
            throw new AizouException(ErrorCode.UNKOWN_ERROR, "Error in user registration.");
        }
    }

    /**
     * 在环信用户系统中处理用户的好友关系
     *
     * @param userIdA
     * @param userIdB
     */
    public static void modEaseMobContacts(Long userIdA, Long userIdB, boolean actionAdd) throws AizouException {
        List<String> fieldList = Arrays.asList(UserInfo.fnEasemobUser);
        String userA, userB;
        try {
            userA = UserAPI.getUserInfo(userIdA, fieldList).getEasemobUser();
            userB = UserAPI.getUserInfo(userIdB, fieldList).getEasemobUser();
            if (userA == null || userB == null)
                throw new AizouException(ErrorCode.UNKOWN_ERROR, "Easemob not regiestered yet.");
        } catch (NullPointerException e) {
            throw new AizouException(ErrorCode.USER_NOT_EXIST, "");
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
                throw new AizouException(ErrorCode.UNKOWN_ERROR, "Error in user registration.");
        } catch (java.io.IOException e) {
            throw new AizouException(ErrorCode.UNKOWN_ERROR, "Error in user registration.");
        }
    }

    /**
     * 在环信用户系统中添加用户的黑名单关系
     */
    public static void addEaseMobBlocks(Long userIdA, List<Long> blockIds) throws AizouException {
        List<String> fieldList = Arrays.asList(UserInfo.fnEasemobUser);
        String userA;
        if (blockIds == null || blockIds.isEmpty())
            throw new AizouException(ErrorCode.INVALID_ARGUMENT, "");

        List<String> blockNames = new ArrayList<>();
        try {
            userA = UserAPI.getUserInfo(userIdA, fieldList).getEasemobUser();
            if (userA == null)
                throw new AizouException(ErrorCode.UNKOWN_ERROR, "Easemob not regiestered yet.");

            for (Long i : blockIds) {
                String easemobName = UserAPI.getUserInfo(i, fieldList).getEasemobUser();
                if (easemobName == null)
                    throw new AizouException(ErrorCode.UNKOWN_ERROR, "Easemob not regiestered yet.");
                blockNames.add(easemobName);
            }
        } catch (NullPointerException e) {
            throw new AizouException(ErrorCode.USER_NOT_EXIST, "");
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

            OutputStreamWriter out = new OutputStreamWriter(conn.getOutputStream(), "UTF-8");
            out.write(data.toString());
            out.flush();
            out.close();

            InputStream in = conn.getInputStream();
            String body = IOUtils.toString(in, conn.getContentEncoding());

            JsonNode tokenData = Json.parse(body);
            if (tokenData.has("error"))
                throw new AizouException(ErrorCode.UNKOWN_ERROR, "");
        } catch (java.io.IOException e) {
            throw new AizouException(ErrorCode.UNKOWN_ERROR, "");
        }
    }

    /**
     * 在环信用户系统中处理用户的黑名单
     *
     * @param userIdA
     * @param userIdB
     */
    public static void delEaseMobBlocks(Long userIdA, Long userIdB) throws AizouException {
        List<String> fieldList = Arrays.asList(UserInfo.fnEasemobUser);
        String userA, userB;
        try {
            userA = UserAPI.getUserInfo(userIdA, fieldList).getEasemobUser();
            userB = UserAPI.getUserInfo(userIdB, fieldList).getEasemobUser();
        } catch (NullPointerException e) {
            throw new AizouException(ErrorCode.USER_NOT_EXIST, "");
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
                throw new AizouException(ErrorCode.UNKOWN_ERROR, "");
        } catch (java.io.IOException e) {
            throw new AizouException(ErrorCode.UNKOWN_ERROR, "");
        }
    }

    /**
     * 提出好友申请
     *
     * @param selfId
     * @param targetId
     * @throws exception.AizouException
     */
    public static void requestAddContact(Long selfId, Long targetId, String message) throws AizouException {
        if (selfId.equals(targetId))
            return;

        //取得用户信息实体
        UserInfo selfInfo = getUserInfo(selfId, Arrays.asList(UserInfo.fnContacts, UserInfo.fnNickName,
                UserInfo.fnAvatar, UserInfo.fnGender, UserInfo.fnUserId, UserInfo.fnEasemobUser));  //取得用户实体
        //取得好友信息实体
        UserInfo targetInfo = getUserInfo(targetId, Arrays.asList(UserInfo.fnContacts, UserInfo.fnNickName,
                UserInfo.fnAvatar, UserInfo.fnGender, UserInfo.fnUserId, UserInfo.fnEasemobUser));

        if (selfInfo == null || targetInfo == null)
            throw new AizouException(ErrorCode.INVALID_ARGUMENT, "Invalid user id.");

        // 向被加好友的客户端发消息
        unvarnishedTrans(selfInfo, targetInfo, CMDTYPE_REQUEST_FRIEND, message);
    }

    /**
     * 添加好友
     *
     * @param selfId
     * @param targetId
     * @throws exception.AizouException
     */
    public static void addContact(final Long selfId, final Long targetId) throws AizouException {
        if (selfId.equals(targetId))
            return;
        //取得自己的实体
        final UserInfo selfInfo = getUserInfo(selfId, Arrays.asList(UserInfo.fnNickName,
                UserInfo.fnAvatar, UserInfo.fnGender, UserInfo.fnUserId, UserInfo.fnEasemobUser, UserInfo.fnSignature));  //取得用户实体
        //取得对方的实体
        final UserInfo targetInfo = getUserInfo(targetId, Arrays.asList(UserInfo.fnNickName,
                UserInfo.fnAvatar, UserInfo.fnGender, UserInfo.fnUserId, UserInfo.fnEasemobUser, UserInfo.fnSignature));

        if (selfInfo == null || targetInfo == null)
            throw new AizouException(ErrorCode.INVALID_ARGUMENT, "Invalid user id.");

        List<F.Promise<Object>> promiseList2 = new ArrayList<>();

//        //环信注册
//        promiseList2.add(F.Promise.promise(new F.Function0<Object>() {
//            @Override
//            public Object apply() throws Throwable {
//                modEaseMobContacts(selfId, targetId, true);
//                return null;
//            }
//        }));
//
//        // 互相删除环信黑名单
//        promiseList2.add(F.Promise.promise(new F.Function0<Object>() {
//            @Override
//            public Object apply() throws Throwable {
//                delEaseMobBlocks(selfId, targetId);
//                return null;
//            }
//        }));
//
//        promiseList2.add(F.Promise.promise(new F.Function0<Object>() {
//            @Override
//            public Object apply() throws Throwable {
//                delEaseMobBlocks(targetId, selfId);
//                return null;
//            }
//        }));
//
//        //在关系表添加好友
//        promiseList2.add(F.Promise.promise(new F.Function0<Object>() {
//            @Override
//            public Object apply() throws Throwable {
//                addFriends(selfId, targetId);
//                return null;
//            }
//        }));
//
//        // 向加友请求发起的客户端发消息
//        promiseList2.add(F.Promise.promise(new F.Function0<Object>() {
//            @Override
//            public Object apply() throws Throwable {
//                unvarnishedTrans(selfInfo, targetInfo, CMDTYPE_ADD_FRIEND, null);
//                return null;
//            }
//        }));
        promiseList2.addAll(
                AsyncExecutor.creatPromises(
                        new F.Function0<Object>() {
                            @Override
                            public Object apply() throws Throwable {
                                modEaseMobContacts(selfId, targetId, true);
                                return null;
                            }
                        },
                        new F.Function0<Object>() {
                            @Override
                            public Object apply() throws Throwable {
                                delEaseMobBlocks(selfId, targetId);
                                return null;
                            }
                        },
                        new F.Function0<Object>() {
                            @Override
                            public Object apply() throws Throwable {
                                delEaseMobBlocks(targetId, selfId);
                                return null;
                            }
                        },
                        new F.Function0<Object>() {
                            @Override
                            public Object apply() throws Throwable {
                                addFriends(selfId, targetId);
                                return null;
                            }
                        },
                        new F.Function0<Object>() {
                            @Override
                            public Object apply() throws Throwable {
                                unvarnishedTrans(selfInfo, targetInfo, CMDTYPE_ADD_FRIEND, null);
                                return null;
                            }
                        }
                )
        );

        // 默认超时时间：10秒
        F.Promise.sequence(promiseList2).get(10000);
    }


    /**
     * 删除好友
     *
     * @param selfId
     * @param targetId
     */
    public static void delContact(final Long selfId, final Long targetId) throws AizouException {
        if (selfId.equals(targetId))
            return;

        //取得用户实体
        final UserInfo selfInfo = getUserInfo(selfId, Arrays.asList(UserInfo.fnContacts, UserInfo.fnUserId, UserInfo.fnEasemobUser));
        final UserInfo targetInfo = getUserInfo(targetId, Arrays.asList(UserInfo.fnContacts, UserInfo.fnUserId, UserInfo.fnEasemobUser));
        if (selfInfo == null || targetInfo == null)
            throw new AizouException(ErrorCode.INVALID_ARGUMENT, "Invalid user id.");

        List<F.Promise<Object>> promiseList2 = new ArrayList<>();

//        //向环信注册
//        promiseList2.add(F.Promise.promise(new F.Function0<Object>() {
//            @Override
//            public Object apply() throws Throwable {
//                modEaseMobContacts(selfId, targetId, false);
//                return null;
//            }
//        }));
//
//        //需要互添加环信黑名单，防止继续发送消息
//        promiseList2.add(F.Promise.promise(new F.Function0<Object>() {
//            @Override
//            public Object apply() throws Throwable {
//                addEaseMobBlocks(selfId, Arrays.asList(targetId));
//                return null;
//            }
//        }));
//
//        promiseList2.add(F.Promise.promise(new F.Function0<Object>() {
//            @Override
//            public Object apply() throws Throwable {
//                addEaseMobBlocks(targetId, Arrays.asList(selfId));
//                return null;
//            }
//        }));
//
//        // 关系表删除好友
//        promiseList2.add(F.Promise.promise(new F.Function0<Object>() {
//            @Override
//            public Object apply() throws Throwable {
//                delFriends(selfId, targetId);
//                return null;
//            }
//        }));
//
//        // 向删友请求发起的客户端发消息
//        promiseList2.add(F.Promise.promise(new F.Function0<Object>() {
//            @Override
//            public Object apply() throws Throwable {
//                unvarnishedTrans(selfInfo, targetInfo, CMDTYPE_DEL_FRIEND, null);
//                return null;
//            }
//        }));

        promiseList2.addAll(
                AsyncExecutor.creatPromises(
                        new F.Function0<Object>() {
                            @Override
                            public Object apply() throws Throwable {
                                //向环信注册
                                modEaseMobContacts(selfId, targetId, false);
                                return null;
                            }
                        },
                        new F.Function0<Object>() {
                            @Override
                            public Object apply() throws Throwable {
                                //需要互添加环信黑名单，防止继续发送消息
                                addEaseMobBlocks(selfId, Arrays.asList(targetId));
                                return null;
                            }
                        },
                        new F.Function0<Object>() {
                            @Override
                            public Object apply() throws Throwable {
                                //需要互添加环信黑名单，防止继续发送消息
                                addEaseMobBlocks(targetId, Arrays.asList(selfId));
                                return null;
                            }
                        },
                        new F.Function0<Object>() {
                            @Override
                            public Object apply() throws Throwable {
                                // 关系表删除好友
                                delFriends(selfId, targetId);
                                return null;
                            }
                        },
                        new F.Function0<Object>() {
                            @Override
                            public Object apply() throws Throwable {
                                // 向删友请求发起的客户端发消息
                                unvarnishedTrans(selfInfo, targetInfo, CMDTYPE_DEL_FRIEND, null);
                                return null;
                            }
                        }
                )
        );

        // 默认超时时间：10秒
        F.Promise.sequence(promiseList2).get(10000);
    }


    /**
     * 服务器调用环信接口发送透传消息
     */
    public static void unvarnishedTrans(UserInfo selfInfo, UserInfo targetInfo, int cmdType, String message) throws AizouException {
        if (selfInfo.getEasemobUser() == null)
            throw new AizouException(ErrorCode.UNKOWN_ERROR, "Easemob not regiestered yet.");
        ObjectNode info = (ObjectNode) new UserFormatterOld(false).format(selfInfo);
        // 添加邀请信息
        info.put("attachMsg", message);
        ObjectNode ext = Json.newObject();
        ext.put("CMDType", cmdType);

        ext.put("content", info.toString());

        ObjectNode msg = Json.newObject();
        msg.put("type", "cmd");
        msg.put("action", "tzaction");
//        if (message != null)
//            msg.put("msg", message);
//        else
//            msg.put("msg", "");

        ObjectNode requestBody = Json.newObject();
        List<String> users = new ArrayList<>();
        users.add(targetInfo.getEasemobUser());

        requestBody.put("target_type", "users");

        requestBody.put("target", Json.toJson(users));
        requestBody.put("msg", msg);
        requestBody.put("ext", ext);
        requestBody.put("from", selfInfo.getEasemobUser());

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

            OutputStreamWriter out = new OutputStreamWriter(conn.getOutputStream(), "UTF-8");
            out.write(requestBody.toString());
            LogUtils.info(UserAPI.class, requestBody.toString());
            out.flush();
            out.close();

            InputStream in = conn.getInputStream();
            String body = IOUtils.toString(in, conn.getContentEncoding());

            JsonNode tokenData = Json.parse(body);
            LogUtils.info(UserAPI.class, body.toString());
            if (tokenData.has("error"))
                throw new AizouException(ErrorCode.UNKOWN_ERROR, "");
        } catch (java.io.IOException e) {
            throw new AizouException(ErrorCode.UNKOWN_ERROR, "");
        }
    }

    /**
     * api 获得用户的好友列表
     *
     * @param selfId
     * @return
     * @throws exception.AizouException
     */
    public static List<UserInfo> getContactList(Long selfId) throws AizouException {
        // 从关系表中取得好友关系列表
        Set<Long> contactSet = getContactIds(selfId);

        // 取得好友信息
        Datastore ds = MorphiaFactory.datastore();
        Query<UserInfo> queryFriends = ds.createQuery(UserInfo.class);
        List<CriteriaContainerImpl> criList = new ArrayList<>();
        for (Long tempId : contactSet) {
            criList.add(queryFriends.criteria("userId").equal(tempId));
        }
        queryFriends.or(criList.toArray(new CriteriaContainerImpl[criList.size()]));

        return queryFriends.asList();
    }

    /**
     * api 获得用户的好友列表
     *
     * @param selfId
     * @return
     * @throws exception.AizouException
     */
    public static Set<Long> getContactIds(Long selfId) throws AizouException {
        UserInfo userInfo = getUserInfo(selfId, null);
        if (userInfo == null)
            throw new AizouException(ErrorCode.INVALID_ARGUMENT, "Invalid UserId.");

        // 从关系表中取得好友关系列表
        Datastore ds = MorphiaFactory.datastore();
        Query<Relationship> query = ds.createQuery(Relationship.class);

        query.or(query.criteria("userA").equal(selfId), query.criteria("userB").equal(selfId));
        List<Relationship> relations = query.asList();
        if (relations.isEmpty())
            return new HashSet<>();

        // 从好友关系列表中提取好友ID
        Set<Long> contactSet = new HashSet<>();
        for (Relationship relationship : relations) {
            if (!relationship.getUserA().equals(selfId))
                contactSet.add(relationship.getUserA());
            else
                contactSet.add(relationship.getUserB());
        }

        return contactSet;
    }

    public static List<UserInfo> getUserByEaseMob(List<String> users, List<String> fieldList) throws AizouException {
        try {
            Datastore ds = MorphiaFactory.datastore();
            Query<UserInfo> query = ds.createQuery(UserInfo.class);
            List<CriteriaContainerImpl> criList = new ArrayList<>();
            for (String name : users) {
                criList.add(query.criteria("easemobUser").equal(name));
            }
            query.or(criList.toArray(new CriteriaContainerImpl[criList.size()]));

            if (fieldList != null && !fieldList.isEmpty())
                query.retrievedFields(true, fieldList.toArray(new String[fieldList.size()]));

            return query.asList();
        } catch (IllegalArgumentException e) {
            throw new AizouException(ErrorCode.INVALID_ARGUMENT, "Error easeMob users.");
        }
    }

    public static void resetAvater(Long userId, String avater) throws AizouException {
        Datastore dsUser = MorphiaFactory.datastore();
        UpdateOperations<UserInfo> ops = dsUser.createUpdateOperations(UserInfo.class);
        ops.set("avatar", avater);
        dsUser.updateFirst(dsUser.createQuery(UserInfo.class).field("userId").equal(userId), ops);
    }

    /**
     * 关系表添加好友
     *
     * @param selfId
     * @param targetId
     * @throws AizouException
     */
    public static void addFriends(Long selfId, Long targetId) throws AizouException {
        Datastore ds = MorphiaFactory.datastore();

        Query<Relationship> query = ds.createQuery(Relationship.class);
        // 较小的userId设为userA
        Long userA = selfId > targetId ? targetId : selfId;
        // 较大的userId设为userB
        Long userB = selfId > targetId ? selfId : targetId;

        query.field("userA").equal(userA).field("userB").equal(userB);
        // 如果不存在好友关系，则添加好友
        if (!query.iterator().hasNext()) {
            Relationship relationship = new Relationship();
            relationship.setId(new ObjectId());
            relationship.setUserA(userA);
            relationship.setUserB(userB);
            ds.save(relationship);
        }

    }

    /**
     * 关系表删除好友
     *
     * @param selfId
     * @param targetId
     * @throws AizouException
     */
    public static void delFriends(Long selfId, Long targetId) throws AizouException {
        Datastore ds = MorphiaFactory.datastore();

        Query<Relationship> query = ds.createQuery(Relationship.class);
        // 较小的userId
        Long userA = selfId > targetId ? targetId : selfId;
        // 较大的userId
        Long userB = selfId > targetId ? selfId : targetId;

        query.field("userA").equal(userA).field("userB").equal(userB);
        // 如果存在好友关系，则删除好友关系
        if (query.iterator().hasNext())
            ds.delete(query);
    }

    /**
     * 填充用户信息
     */
    public static void fillUserInfo(UserInfo userInfo) throws AizouException {
        List<Locality> tracks = userInfo.getTracks();
        if (tracks != null) {
            List<ObjectId> tracksIds = new ArrayList<>();
            for (Locality track : tracks)
                tracksIds.add(track.getId());
            List<Locality> completeTracks = LocalityAPI.getLocalityList(tracksIds, Arrays.asList(Locality.FD_ID,
                    Locality.FD_ZH_NAME, Locality.fnLocation, Locality.fnCountry), 0, utils.Constants.MAX_COUNT);

            userInfo.setTracks(completeTracks);
        }

        List<TravelNote> travelNoteList = userInfo.getTravelNotes();
        if (travelNoteList != null) {
            List<ObjectId> travelNotesIds = new ArrayList<>();
            for (TravelNote travelNote : travelNoteList)
                travelNotesIds.add(travelNote.getId());
            List<TravelNote> completeTravelNotes = TravelNoteAPI.getNotesByIdList(travelNotesIds, Arrays.asList(TravelNote.FD_ID,
                    TravelNote.fnTitle, TravelNote.fnImages, TravelNote.fnSummary));
            userInfo.setTravelNotes(completeTravelNotes);
        }
    }

    /**
     * 添加一张用户上传的图片
     *
     * @param userId
     * @param imageItem
     * @throws AizouException
     */
    public static void addUserAlbum(Long userId, ImageItem imageItem, String id) throws AizouException {
        Datastore dsUser = MorphiaFactory.datastore();

        Album entity = new Album();
        entity.setId(new ObjectId(id));
        entity.setcTime(System.currentTimeMillis());
        entity.setImage(imageItem);
        entity.setUserId(userId);
        entity.setTaoziEna(true);
        dsUser.save(entity);
    }

    /**
     * 取得用户相册的图片
     *
     * @param userId
     * @return
     * @throws AizouException
     */
    public static List<Album> getUserAlbums(Long userId) throws AizouException {
        Datastore dsUser = MorphiaFactory.datastore();
        Query<Album> query = dsUser.createQuery(Album.class);
        query.field(Album.FD_USERID).equal(userId).field(Album.FD_TAOZIENA).equal(true);
        return query.asList();
    }

    /**
     * 删除用户图片
     *
     * @param userId
     * @param picId
     * @throws AizouException
     */
    public static void deleteUserAlbums(Long userId, Object picId) throws AizouException {
        Datastore dsUser = MorphiaFactory.datastore();
        Query<Album> query = dsUser.createQuery(Album.class);
        query.field(Album.FD_USERID).equal(userId).field(Album.FD_ID).equal(picId).field(Album.FD_TAOZIENA).equal(true);

        UpdateOperations<Album> ops = dsUser.createUpdateOperations(Album.class);
        ops.set(Album.FD_TAOZIENA, false);
        dsUser.updateFirst(query, ops);
    }

    public static List<UserInfo> getExpertUserByTracks(List<ObjectId> ids, String role, Collection<String> fieldList) throws AizouException {
        Datastore dsUser = MorphiaFactory.datastore();

        Query<UserInfo> query = dsUser.createQuery(UserInfo.class);
        List<Locality> localities = new ArrayList<>();
        Locality locality;
        for (ObjectId id : ids) {
            locality = new Locality();
            locality.setId(id);
            localities.add(locality);
        }
        query.field(UserInfo.fnTracks).hasAnyOf(localities).field(UserInfo.fnRoles).hasThisOne(role);
        if (fieldList != null && !fieldList.isEmpty())
            query.retrievedFields(true, fieldList.toArray(new String[fieldList.size()]));
        return query.asList();

    }

    /**
     * 修改足迹信息
     *
     * @param userId
     * @param action
     * @param its
     * @throws AizouException
     */
    public static void modifyTracks(Long userId, String action, Iterator<JsonNode> its) throws AizouException {

        Datastore dsUser = MorphiaFactory.datastore();
        Query<UserInfo> query = dsUser.createQuery(UserInfo.class);
        query.field(Album.FD_USERID).equal(userId);

        UpdateOperations<UserInfo> ops = dsUser.createUpdateOperations(UserInfo.class);
        if (action.equals("add"))
            ops.addAll(UserInfo.fnTracks, strListToObjectIdList(its, Locality.class), false);
        else if (action.equals("del"))
            ops.removeAll(UserInfo.fnTracks, strListToObjectIdList(its, Locality.class));
        else
            throw new AizouException(ErrorCode.INVALID_ARGUMENT, "Invalid action");
        dsUser.updateFirst(query, ops);
    }

    /**
     * 服务器调用环信接口发送透传消息
     */
    public static void sendMessageToUser(String selfEasemob, UserInfo targetInfo, String message) throws AizouException {
        if (selfEasemob == null)
            throw new AizouException(ErrorCode.UNKOWN_ERROR, "Easemob not regiestered yet.");
//        ObjectNode info = (ObjectNode) new UserFormatterOld(false).format(selfInfo);
//        // 添加邀请信息
//        info.put("attachMsg", message);
//        ObjectNode ext = Json.newObject();
//        ext.put("CMDType", cmdType);
//
//        ext.put("content", info.toString());

        ObjectNode msg = Json.newObject();
        msg.put("type", "txt");
        msg.put("msg", message);

        ObjectNode requestBody = Json.newObject();
        List<String> users = new ArrayList<>();
        users.add(targetInfo.getEasemobUser());

        requestBody.put("target_type", "users");

        requestBody.put("target", Json.toJson(users));
        requestBody.put("msg", msg);
        //requestBody.put("ext", ext);
        requestBody.put("from", selfEasemob);

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

            OutputStreamWriter out = new OutputStreamWriter(conn.getOutputStream(), "UTF-8");
            out.write(requestBody.toString());
            LogUtils.info(UserAPI.class, requestBody.toString());
            out.flush();
            out.close();

            InputStream in = conn.getInputStream();
            String body = IOUtils.toString(in, conn.getContentEncoding());

            JsonNode tokenData = Json.parse(body);
            LogUtils.info(UserAPI.class, body.toString());
            if (tokenData.has("error"))
                throw new AizouException(ErrorCode.UNKOWN_ERROR, "");
        } catch (java.io.IOException e) {
            throw new AizouException(ErrorCode.UNKOWN_ERROR, "");
        }
    }
    /**
     * 应用图片为头像
     *
     * @param userId
     * @param url
     * @throws AizouException
     */
//    public static void setAlbumsToAvatar(Long userId, String url) throws AizouException {
//        Datastore ds = MorphiaFactory.datastore();
//        Query<UserInfo> query = ds.createQuery(UserInfo.class);
//        query.field(UserInfo.fnUserId).equal(userId);
//
//        UpdateOperations<UserInfo> ops = ds.createUpdateOperations(UserInfo.class);
//        ops.set(UserInfo.fnAvatar, url);
//        ds.update(query, ops);
//
//    }


}
