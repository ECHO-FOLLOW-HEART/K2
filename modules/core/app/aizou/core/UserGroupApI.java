package aizou.core;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import exception.ErrorCode;
import exception.TravelPiException;
import models.MorphiaFactory;
import models.user.UserGroup;
import models.user.UserInfo;
import org.apache.commons.io.IOUtils;
import org.mongodb.morphia.Datastore;
import play.Configuration;
import play.libs.Json;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by lxf on 14-10-25.
 */
public class UserGroupApI {

    /**
     * 连接mongo,存储数据
     */
    public static void saveData(UserGroup userGroup) throws TravelPiException {
        Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.USER);
        ds.save(userGroup);
    }

    /**
     * 通过groupId返回群的实体
     */
    public static UserGroup getChatGroupById(Integer id) throws TravelPiException {
        Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.USER);
        return ds.createQuery(UserGroup.class).field("groupId").equal(id).get();

    }

    /**
     *
     */
    public static String setUrlConnection(String url) {
        String href = String.format("https://a1.easemob.com/%s/%s/users", orgName, appName);
        try {
            URL url = new URL(href);
            URLConnection conn = url.openConnection();
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", String.format("Bearer %s", UserAPI.getEaseMobToken()));
            OutputStreamWriter out = new OutputStreamWriter(conn.getOutputStream());
            out.write(data.toString());
            out.flush();
            out.close();

            InputStream in = conn.getInputStream();
            String body = IOUtils.toString(in, conn.getContentEncoding());
        }
        /**
         * 通过调用环信接口创建一个群组
         * @param ownnerId
         * @param groupName
         * @param desc
         * @param isGroupPublic
         * @param maxusers
         * @return
         * @throws TravelPiException
         */

    public static String createGroup(Integer ownnerId, String groupName, String desc, boolean isGroupPublic, Integer maxusers) throws TravelPiException {
        Configuration configuration = Configuration.root().getConfig("easemob");
        String orgName = configuration.getString("org");
        String appName = configuration.getString("app");

        UserInfo ownner = UserAPI.getUserByUserId(ownnerId);  //拿到群主信息
        JsonNode ownnerNode = Json.toJson(ownner);
        ObjectNode data = Json.newObject();
        data.put("ownner", ownnerNode);
        data.put("groupName", groupName == null ? ownner.nickName : groupName);
        data.put("desc", desc == null ? "" : desc);
        data.put("isGroupPublic", isGroupPublic);
        data.put("maxUsers", maxusers == null ? 50 : maxusers);

        //'https://a1.easemob.com/easemob-demo/4d7e4ba0-dc4a-11e3-90d5-e1ffbaacdaf5/chatgroups'
        String href = String.format("https://a1.easemob.com/%s/%s/users", orgName, appName);
        try {
            URL url = new URL(href);
            URLConnection conn = url.openConnection();
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", String.format("Bearer %s", UserAPI.getEaseMobToken()));
            OutputStreamWriter out = new OutputStreamWriter(conn.getOutputStream());
            out.write(data.toString());
            out.flush();
            out.close();

            InputStream in = conn.getInputStream();
            String body = IOUtils.toString(in, conn.getContentEncoding());
            JsonNode dataNode = Json.parse(body);
            String groupId = dataNode.get("data").get("groupid").asText();
            return groupId;
        } catch (IOException e) {
            throw new TravelPiException(ErrorCode.INVALID_ARGUMENT, "error in create chatgroup");
        }

    }

    /**
     * 添加群成员
     *
     * @param groupId
     * @param users
     * @throws TravelPiException
     */
    public static void putMemberIntoGroup(Integer groupId, Map<Integer, UserInfo> users) throws TravelPiException {
        UserGroup userGroup = getChatGroupById(groupId);
        Map<Integer, UserInfo> remember = userGroup.remember;
        Set<Integer> keysId = users.keySet();
        for (Integer id : keysId) {
            remember.put(id, users.get(id));
        }
        saveData(userGroup);
    }

    /**
     * 群组中删除用户
     *
     * @param groupId
     * @param userList
     */
    public static void deleteRememberFromGup(Integer groupId, List<Integer> userList) throws TravelPiException {
        UserGroup userGroup = getChatGroupById(groupId);
        Map<Integer, UserInfo> remember = userGroup.remember;
        for (Integer id : userList) {
            remember.remove(id);
        }
        saveData(userGroup);
    }
}
