package aizou.core;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import exception.AizouException;
import exception.ErrorCode;
import database.MorphiaFactory;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class ChatGroupAPI {

    /**
     * 存储数据
     *
     * @param chatGroupInfo
     * @throws exception.AizouException
     */
    public static void saveData(ChatGroupInfo chatGroupInfo) throws AizouException {
        Datastore ds = MorphiaFactory.datastore();
        ds.save(chatGroupInfo);
    }

    /**
     * 移除数据
     *
     * @param chatGroupInfo
     * @throws exception.AizouException
     */
    public static void deleteData(ChatGroupInfo chatGroupInfo) throws AizouException {
        Datastore ds = MorphiaFactory.datastore();
        ds.delete(chatGroupInfo);
    }


    /**
     * 通过groupId返回群的实体
     *
     * @param id
     * @return
     * @throws exception.AizouException
     */
    public static ChatGroupInfo getChatGroupById(String id) throws AizouException {
        Datastore ds = MorphiaFactory.datastore();
        return ds.createQuery(ChatGroupInfo.class).field("groupId").equal(id).get();

    }

    /**
     * 获取群组信息（添加fieldList限定）
     *
     * @param id
     * @param list
     * @return
     * @throws exception.AizouException
     */
    public static ChatGroupInfo getUserGroupInfo(String id, List<String> list) throws AizouException {
        Datastore ds = MorphiaFactory.datastore();
        Query<ChatGroupInfo> query = ds.createQuery(ChatGroupInfo.class).field("groupId").equal(id);
        if (list != null && !list.isEmpty()) {
            query.retrievedFields(true, list.toArray(new String[list.size()]));
        }
        return query.get();
    }

    /**
     * 连接环信
     *
     * @param href
     * @param node
     * @param flag
     * @param httpmethod
     * @return
     * @throws java.io.IOException
     * @throws exception.AizouException
     */
    public static String setUrlConnection(String href, JsonNode node, Boolean flag, String httpmethod) throws IOException, AizouException {
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
     * 获取环信系统的token,如果已经过期，则重新申请一个
     *
     * @return
     * @throws exception.AizouException
     */
    private static String getEaseMobToken() throws AizouException {
        Datastore ds = MorphiaFactory.datastore();
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
                throw new AizouException(ErrorCode.UNKOWN_ERROR, "Error in retrieving token.");
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
     * @throws exception.AizouException
     */

    public static String createGroupApi(Long ownerId, String groupName, String desc, boolean isGroupPublic, Integer maxusers) throws AizouException {
        /**
         * 向环信注册群组
         */
        /*Configuration configuration = Configuration.root().getConfig("easemob");
        String orgName = configuration.getString("org");
        String appName = configuration.getString("app");*/

        UserInfo owner = UserAPI.getUserInfo(ownerId, Arrays.asList(UserInfo.fnNickName, UserInfo.fnAvatar,
                UserInfo.fnSignature, UserInfo.fnGender, UserInfo.fnUserId));

        if (owner == null) {
            throw new AizouException(ErrorCode.INVALID_ARGUMENT, "INVALID_ARGUMENT");
        }
        /*JsonNode ownerNode = Json.toJson(owner);
        ObjectNode data = Json.newObject();*/
        //参数封装
        /*data.put("owner", ownerId);
        data.put("groupName", groupName == null ? owner.nickName : groupName);
        data.put("desc", desc == null ? "" : desc);
        data.put("isGroupPublic", isGroupPublic);
        data.put("maxUsers", maxusers == null ? 50 : maxusers);

        //'https://a1.easemob.com/easemob-demo/4d7e4ba0-dc4a-11e3-90d5-e1ffbaacdaf5/chatgroups'
        String href = String.format("https://a1.easemob.com/%s/%s/chatgroups", orgName, appName);
        String groupId = "";
        try {
            String body = setUrlConnection(href, data,true "POST");
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
     * 删除群
     *
     * @param groupId
     * @param id
     */
    public static String deleteGroupApi(String groupId, Long id) throws AizouException {
        /**
         * 环信删除
         */
        /*Configuration configuration = Configuration.root().getConfig("easemob");
        String orgName = configuration.getString("org");
        String appName = configuration.getString("app");*/

        ChatGroupInfo chatGroupInfo = getUserGroupInfo(groupId, Arrays.asList(ChatGroupInfo.OWNER));
        //判断是否为空
        if (chatGroupInfo == null)
            throw new AizouException(ErrorCode.INVALID_ARGUMENT, "INVALID_ARGUMENT");
        long sub = chatGroupInfo.owner.getUserId() - id;
        if (sub == 0) {       //判断id,删除操作只能由群主发起
            /*String href = String.format("https://a1.easemob.com/%s/%s/chargroups/%s", orgName, appName, groupId);
            String data = "";
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
     * @throws exception.AizouException
     */
    public static void putUserIntoGroupApi(String groupId, List<Long> userList) throws AizouException {
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
            setUrlConnection(href, data,true,"POST");
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
            throw new AizouException(ErrorCode.INVALID_ARGUMENT, "INVALID_ARGUMENT");
        }
        List<UserInfo> members = chatGroupInfo.members;
        List<UserInfo> userInfoList = new ArrayList<>();
        Boolean flag = false;
        for (Long id : userList) {
            UserInfo userInfo = UserAPI.getUserInfo(id, Arrays.asList(UserInfo.fnNickName,
                    UserInfo.fnGender, UserInfo.fnSignature, UserInfo.fnUserId, UserInfo.fnAvatar));
            if (members == null) {
                userInfoList.add(userInfo);
                members = userInfoList;
            } else {
                for (UserInfo tmpUserInfo : members) {
                    if ((tmpUserInfo.getUserId() - userInfo.getUserId() == 0)) {
                        flag = true;
                        break;
                    }
                }
                if (flag == true)
                    throw new AizouException(ErrorCode.USER_EXIST, "USER_EXIST");
                else
                    members.add(userInfo);
            }
        }
        //更新库
        Datastore ds = MorphiaFactory.datastore();
        UpdateOperations<ChatGroupInfo> os = ds.createUpdateOperations(ChatGroupInfo.class);
        os.set(ChatGroupInfo.MEMBERS, members);
        ds.update(ds.createQuery(ChatGroupInfo.class).field("groupId").equal(groupId), os);
    }

    /**
     * 群组中删除群成员
     *
     * @param groupId
     * @param userList
     */
    public static void deleteMemberFromGroupApi(String groupId, List<Long> userList) throws AizouException {
        /**
         * 环信中进行删除
         */
        /*Configuration configuration = Configuration.root().getConfig("easemob");
        String orgName = configuration.getString("org");
        String appName = configuration.getString("app");*/
        List<UserInfo> members = null;
        if (userList != null) {
            for (Long id : userList) {
                /*String href = String.format("https://a1.easemob.com/%s/%s/chatgroups/%s/users'/%s", orgName, appName, groupId, id);
                try {
                    setUrlConnection(href, Json.newObject(), false, "DELETE");*/
                /**
                 * 本地库的更新
                 */
                ChatGroupInfo chatGroupInfo = getUserGroupInfo(groupId, Arrays.asList(ChatGroupInfo.MEMBERS));
                if (chatGroupInfo == null)
                    throw new AizouException(ErrorCode.INVALID_ARGUMENT, "INVALID_ARGUMENT");
                members = chatGroupInfo.members;
                UserInfo userInfo = UserAPI.getUserInfo(id, Arrays.asList(UserInfo.fnAvatar, UserInfo.fnGender, UserInfo.fnNickName, UserInfo.fnSignature, UserInfo.fnUserId));
                if (members != null) {
                    for (int i = 0; i < members.size() && members != null; i++) {
                        UserInfo tmpuserInfo = members.get(i);
                        if (tmpuserInfo != null) {
                            if ((tmpuserInfo.getUserId() - userInfo.getUserId()) == 0) {
                                members.remove(tmpuserInfo);
                            } else
                                throw new AizouException(ErrorCode.USER_NOT_EXIST, "user not exists");
                        }
                    }
                } else
                    throw new AizouException(ErrorCode.DATA_NOT_EXIST, "group members do not exist");
            }
            /*}catch(IOException e){
                throw new TravelPiException(ErrorCode.INVALID_ARGUMENT, "delete failed");
            }*/
        } else
            throw new AizouException(ErrorCode.INVALID_ARGUMENT, "delete failed");
        //更新库
        Datastore ds = MorphiaFactory.datastore();
        UpdateOperations<ChatGroupInfo> os = ds.createUpdateOperations(ChatGroupInfo.class);
        os.set(ChatGroupInfo.MEMBERS, members);
        ds.update(ds.createQuery(ChatGroupInfo.class).field("groupId").equal(groupId), os);
    }


    /**
     * 获取群组详情
     *
     * @param groupId
     * @return
     * @throws exception.AizouException
     */

    public static JsonNode getChatGroupDetailApi(String groupId) throws AizouException {
        ChatGroupInfo chatGroupInfo = getChatGroupById(groupId);
        if (chatGroupInfo == null)
            throw new AizouException(ErrorCode.INVALID_ARGUMENT, "INVALID_ARGUMENT");
        // TODO 需要实现
//        JsonNode response = chatGroupInfo.toJson();
        return Json.newObject();
    }

    /**
     * 修改群组详情
     *
     * @param groupId
     * @param id
     * @param isGroupPublic
     * @param groupName
     * @param desc
     * @return
     * @throws exception.AizouException
     */
    public static String modifyChatGroupDetailApi(String groupId, String id, boolean isGroupPublic, String groupName, String desc) throws AizouException {
        /**
         * 本地库的更新
         */
        ChatGroupInfo chatGroupInfo = getUserGroupInfo(groupId, Arrays.asList(ChatGroupInfo.GROUPNAME, ChatGroupInfo.DESC,
                ChatGroupInfo.ISGROUPPUBLIC, ChatGroupInfo.OWNER));
        if (chatGroupInfo == null) {
            throw new AizouException(ErrorCode.INVALID_ARGUMENT, "INVALID_ARGUMENT");
        }
        Boolean flag = ("" + chatGroupInfo.owner.getUserId()).equals(id);
        if (flag) {
            if (desc != null)
                chatGroupInfo.desc = desc;
            if (groupName != null)
                chatGroupInfo.groupName = groupName;
            chatGroupInfo.isGroupPublic = isGroupPublic;

            //bug
            Datastore ds = MorphiaFactory.datastore();
            UpdateOperations<ChatGroupInfo> os = ds.createUpdateOperations(ChatGroupInfo.class);
            os.set("desc", desc);
            os.set("groupName", groupName);
            os.set("isGroupPublic", isGroupPublic);
            ds.update(ds.createQuery(ChatGroupInfo.class).field("groupId").equal(groupId), os);
            return "修改成功";
        } else
            return "修改失败";         //非群组拥有该权限
    }
}