package aizou.core;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import exception.ErrorCode;
import exception.TravelPiException;
import models.MorphiaFactory;
import models.misc.MiscInfo;
import models.user.ChatGroupInfo;
import models.user.UserInfo;
import org.apache.commons.io.IOUtils;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;
import play.Configuration;
import play.libs.Json;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;


public class ChatGroupAPI {

    /**
     * 连接mongo,存储数据
     */
    public static void saveData(ChatGroupInfo chatGroupInfo) throws TravelPiException {
        Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.USER);
        ds.save(chatGroupInfo);
    }

    /**
     * 移除数据
     */
    public static void deleteData(ChatGroupInfo chatGroupInfo) throws TravelPiException {
        Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.USER);
        ds.delete(chatGroupInfo);
    }


    /**
     * 通过groupId返回群的实体
     */
    public static ChatGroupInfo getChatGroupById(String id) throws TravelPiException {
        Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.USER);
        return ds.createQuery(ChatGroupInfo.class).field("groupId").equal(id).get();

    }

    /**
     * 获取群组信息（添加fieldList限定）
     *
     * @throws TravelPiException
     */
    public static ChatGroupInfo getUserGroupInfo(String id, List<String> list) throws TravelPiException {
        Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.USER);
        Query<ChatGroupInfo> query = ds.createQuery(ChatGroupInfo.class).field("groupId").equal(id);
        if (list != null && !list.isEmpty()) {
            query.retrievedFields(true, list.toArray(new String[list.size()]));
        }
        return query.get();
    }

    /**
     * 发送rest请求,获取response
     */
    public static String setUrlConnection(String href, JsonNode node, Boolean flag, String httpmethod) throws IOException, TravelPiException {
        URL url = new URL(href);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setDoOutput(flag);
        conn.setDoInput(true);
        conn.setRequestMethod(httpmethod);  //调用方法
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Authorization", String.format("Bearer %s", getEaseMobToken()));
        OutputStreamWriter out = new OutputStreamWriter(conn.getOutputStream());
        out.write(node.toString());
        out.flush();
        out.close();

        InputStream in = conn.getInputStream();
        String body = IOUtils.toString(in, conn.getContentEncoding());
        return body;
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
            data.put("clihttps://a1.easemob.com/easemob-demo/chatdemo/tokenent_secret", clientSecret);

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
            } catch (IOException e) {
                throw new TravelPiException(ErrorCode.UNKOWN_ERROR, "Error in retrieving token.");
            }
        }
        return info.easemobToken;
    }

    /**
     * 通过调用环信接口创建一个群组
     *
     * @param ownerId
     * @param groupName
     * @param desc
     * @param isGroupPublic
     * @param maxusers
     * @return
     * @throws TravelPiException
     */

    public static String createGroupApi(Integer ownerId, String groupName, String desc, boolean isGroupPublic, Integer maxusers) throws TravelPiException {
        /**
         * 向环信注册群组
         */
        /*Configuration configuration = Configuration.root().getConfig("easemob");
        String orgName = configuration.getString("org");
        String appName = configuration.getString("app");*/

        UserInfo owner = UserAPI.getUserInfo(ownerId, Arrays.asList(UserInfo.fnNickName, UserInfo.fnAvatar,
                UserInfo.fnSignature, UserInfo.fnGender, UserInfo.fnUserId));

        if (owner == null) {
            throw new TravelPiException(ErrorCode.INVALID_ARGUMENT, "INVALID_ARGUMENT");
        }
        //JsonNode ownerNode = Json.toJson(owner);
        /*ObjectNode data = Json.newObject();
        *//**//**//**//**
         * 请求参数封装
         *//**//**//**//*
        data.put("owner", ownerId);
        data.put("groupName", groupName == null ? owner.nickName : groupName);
        data.put("desc", desc == null ? "" : desc);
        data.put("isGroupPublic", isGroupPublic);
        data.put("maxUsers", maxusers == null ? 50 : maxusers);

        //'https://a1.easemob.com/easemob-demo/4d7e4ba0-dc4a-11e3-90d5-e1ffbaacdaf5/chatgroups'
        String href = String.format("https://a1.easemob.com/%s/%s/chatgroups", orgName, appName);
        String groupId = "";
        try {
            String body = setUrlConnection(href, data, "POST");
            JsonNode dataNode = Json.parse(body);
            groupId = dataNode.get("data").get("groupid").asText();
        } catch (IOException e) {
            throw new TravelPiException(ErrorCode.INVALID_ARGUMENT, "send the request is error");
        } catch (TravelPiException e) {
            throw new TravelPiException(ErrorCode.INVALID_ARGUMENT, "get the token is error");
        }*/

        /**
         * 向mongo中储存数据
         */
        String groupId = "1234";
        ChatGroupInfo chatGroupInfo = new ChatGroupInfo();
        chatGroupInfo.desc = desc;
        chatGroupInfo.groupId = groupId;
        //chatGroupInfo.groupId = groupId;
        chatGroupInfo.groupName = groupName;
        chatGroupInfo.isGroupPublic = isGroupPublic;
        chatGroupInfo.maxUsers = maxusers;
        chatGroupInfo.owner = owner;
        saveData(chatGroupInfo);

        //返回groupId
        return groupId;
    }

    /**
     * 群主删除群
     *
     * @param groupId
     * @param id
     */
    public static String deleteGroupApi(String groupId, Integer id) throws TravelPiException {
        /**
         * 环信删除
         */
        /*Configuration configuration = Configuration.root().getConfig("easemob");
        String orgName = configuration.getString("org");
        String appName = configuration.getString("app");*/

        ChatGroupInfo chatGroupInfo = getUserGroupInfo(groupId, Arrays.asList(ChatGroupInfo.OWNER));
        if (chatGroupInfo == null)
            throw new TravelPiException(ErrorCode.INVALID_ARGUMENT, "INVALID_ARGUMENT");
        int sub = chatGroupInfo.owner.userId - id;
        if (sub == 0) {       //判断id,删除操作只能由群主发起
            //https://a1.easemob.com/easemob-demo/4d7e4ba0-dc4a-11e3-90d5-e1ffbaacdaf5/chatgroups/1411527886490154
            //String href = String.format("https://a1.easemob.com/%s/%s/chargroups/%s", orgName, appName, groupId);
            /*String data = "";
            try {
                String body = setUrlConnection(href, Json.newObject(), false, "DELETE");
                JsonNode dataNode = Json.parse(body);
                data = dataNode.get("data").asText();
            } catch (IOException e) {
                throw new TravelPiException(ErrorCode.INVALID_ARGUMENT, "send the request is error");
            } catch (TravelPiException e) {
                throw new TravelPiException(ErrorCode.INVALID_ARGUMENT, "get the token is error");
            }*/
            /**
             * 本地库中进行删除操作
             */
            deleteData(chatGroupInfo);
            return "success";        //response,删除状态
        } else
            return "only the owner has the right!!";
    }

    /**
     * 添加群成员
     *
     * @param groupId
     * @param userList
     * @throws TravelPiException
     */
    public static void putUserIntoGroupApi(String groupId, List<Integer> userList) throws TravelPiException {
        /**
         * 向环信中添加成员
         */
        /*Configuration configuration = Configuration.root().getConfig("easemob");
        String orgName = configuration.getString("org");
        String appName = configuration.getString("app");
        // https://a1.easemob.com/easemob-demo/chatdemoui/chatgroups/1411816013089/users
        String href = String.format("https://a1.easemob.com/%s/%s/chatgroups/%s/users", orgName, appName, groupId);
        Map<String, List<Integer>> map = new HashMap<>();
        map.put("usernames", userList);
        JsonNode data = Json.toJson(map);
        try {
            setUrlConnection(href, data, "POST");
        } catch (IOException e) {
            throw new TravelPiException(ErrorCode.INVALID_ARGUMENT, "failed");
        } catch (TravelPiException e) {
            throw new TravelPiException(ErrorCode.INVALID_ARGUMENT, "get the token is error");
        }*/

        /**
         * 本地库数据更新
         */
        ChatGroupInfo chatGroupInfo = getUserGroupInfo(groupId, Arrays.asList(ChatGroupInfo.MEMBERS, ChatGroupInfo.GROUPNAME));
        if (chatGroupInfo == null) {
            throw new TravelPiException(ErrorCode.INVALID_ARGUMENT, "INVALID_ARGUMENT");
        }
        List<UserInfo> members = chatGroupInfo.members;
        List<UserInfo> userInfoList = new ArrayList<>();
        for (Integer id : userList) {
            UserInfo userInfo = UserAPI.getUserInfo(id, Arrays.asList(UserInfo.fnNickName,
                    UserInfo.fnGender, UserInfo.fnSignature, UserInfo.fnUserId));
            if (members == null) {
                userInfoList.add(userInfo);
                members = userInfoList;
            } else {
                if (members.contains(userInfo))
                    continue;       //已经包含用户，则不进行添加
                members.add(userInfo);
            }
        }
        //更新库
        Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.USER);
        UpdateOperations<ChatGroupInfo> os = ds.createUpdateOperations(ChatGroupInfo.class);
        //bug
        os.set(ChatGroupInfo.MEMBERS, members);
        ds.update(ds.createQuery(ChatGroupInfo.class).field("groupId").equal(groupId), os);
       /* chatGroupInfo.members=members;
        ds.save(chatGroupInfo);*/
    }

    /**
     * 群组中删除群成员
     *
     * @param groupId
     * @param userList
     */
    public static String deleteMemberFromGroupApi(String groupId, List<Integer> userList) throws TravelPiException {
        /**
         * 环信中进行删除
         */
        /*Configuration configuration = Configuration.root().getConfig("easemob");
        String orgName = configuration.getString("org");
        String appName = configuration.getString("app");*/
        List<UserInfo> members = null;
        if (userList != null) {
            for (Integer id : userList) {
                //String href = String.format("https://a1.easemob.com/%s/%s/chatgroups/%s/users'/%s", orgName, appName, groupId, id);
                //try {
                //setUrlConnection(href, Json.newObject(),false "DELETE");

                /**
                 * 本地库的更新
                 */
                ChatGroupInfo chatGroupInfo = getUserGroupInfo(groupId, Arrays.asList(ChatGroupInfo.MEMBERS));
                members = chatGroupInfo.members;
                UserInfo userInfo = UserAPI.getUserInfo(id);
                if (members != null) {
                    if (members.contains(userInfo))
                        members.remove(userInfo);
                    else
                        continue;
                } else
                    return "群成员空,不能删除";

                //} catch (IOException e) {
                //  throw new TravelPiException(ErrorCode.INVALID_ARGUMENT, "delete failed");
                //}
            }
            Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.USER);
            UpdateOperations<ChatGroupInfo> os = ds.createUpdateOperations(ChatGroupInfo.class);
            os.set("member", members);
            ds.update(ds.createQuery(ChatGroupInfo.class).field("groupId").equal(groupId), os);
            return "删除成功";
        } else
            throw new TravelPiException(ErrorCode.INVALID_ARGUMENT, "INVALID_ARGUMENT");
    }


    /**
     * 获得群组详情
     *
     * @param groupId
     * @return
     * @throws TravelPiException
     */

    public static JsonNode getChatGroupDetailApi(String groupId) throws TravelPiException {
        ChatGroupInfo chatGroupInfo = getChatGroupById(groupId);
        if (chatGroupInfo == null)
            throw new TravelPiException(ErrorCode.INVALID_ARGUMENT, "INVALID_ARGUMENT");
        JsonNode response = chatGroupInfo.toJson();
        return response;
    }

    public static String modifyChatGroupDetailApi(String groupId, String id, boolean isGroupPublic, String groupName, String desc) throws TravelPiException {
        /**
         * 本地库的更新
         */
        ChatGroupInfo chatGroupInfo = getUserGroupInfo(groupId, Arrays.asList(ChatGroupInfo.GROUPNAME, ChatGroupInfo.DESC,
                ChatGroupInfo.ISGROUPPUBLIC, ChatGroupInfo.OWNER));
        if (chatGroupInfo == null) {
            throw new TravelPiException(ErrorCode.INVALID_ARGUMENT, "INVALID_ARGUMENT");
        }
        Boolean flag = ("" + chatGroupInfo.owner.userId).equals(id);
        if (flag) {
            if (desc != null)
                chatGroupInfo.desc = desc;
            if (groupName != null)
                chatGroupInfo.groupName = groupName;
            chatGroupInfo.isGroupPublic = isGroupPublic;

            //bug
            Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.USER);
            UpdateOperations<ChatGroupInfo> os = ds.createUpdateOperations(ChatGroupInfo.class);
            os.set("desc", desc);
            os.set("groupName", groupName);
            os.set("isGrouPublic", isGroupPublic);
            ds.update(ds.createQuery(ChatGroupInfo.class).field("groupId").equal(groupId), os);
            return "修改成功";
        } else
            return "修改失败";         //非群组拥有该权限
    }
}
