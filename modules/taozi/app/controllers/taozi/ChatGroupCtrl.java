package controllers.taozi;

import aizou.core.ChatGroupAPI;
import com.fasterxml.jackson.databind.JsonNode;
import exception.AizouException;
import exception.ErrorCode;
import play.libs.Json;
import play.mvc.Result;
import utils.Utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static play.mvc.Controller.request;

/**
 * Created by lxf on 14-10-27.
 */
public class ChatGroupCtrl {

    /**
     * 创建群组
     *
     * @return
     */
    public static Result createChatGroup() {
        try {
            String ownnerId = request().getHeader("UserId");
            JsonNode req = request().body().asJson();
            String groupName = req.get("nickName").asText();        //可能为空
            String desc = req.get("desc").asText();
            boolean isGroupPublic = req.get("isGroupPublic").asBoolean();
            Integer maxUsers = req.get("maxUsers").asInt();
            String groupId = ChatGroupAPI.createGroupApi(Integer.parseInt(ownnerId), groupName, desc, isGroupPublic, maxUsers);
            return Utils.createResponse(ErrorCode.NORMAL, Json.toJson(groupId));
        } catch (AizouException | NumberFormatException | NullPointerException e) {
            return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, e.getMessage());
        }
    }

    /**
     * 删除群组
     *
     * @return
     */
    public static Result deleteChatGroup() {
        try {
            JsonNode req = request().body().asJson();
            String groupId = req.get("groupId").asText();
            String userId = req.get("userId").asText();
            String response = ChatGroupAPI.deleteGroupApi(groupId, Integer.parseInt(userId));
            return Utils.createResponse(ErrorCode.NORMAL, Json.toJson(response));
        } catch (AizouException | NullPointerException e) {
            return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, e.getMessage());
        }
    }

    /**
     * 向群组中添加成员
     */
    public static Result addChatGroupRemember() {
        try {
            JsonNode node = request().body().asJson();
            String groupId = node.get("groupId").asText();
            Iterator<JsonNode> iterable = node.get("userList").iterator();
            List<Integer> userList = new ArrayList<>();
            while (iterable.hasNext()) {
                Integer id = iterable.next().asInt();
                userList.add(id);
            }
            ChatGroupAPI.putUserIntoGroupApi(groupId, userList);
            return Utils.createResponse(ErrorCode.NORMAL, Json.toJson("added successfull"));
        } catch (AizouException | NumberFormatException | NullPointerException e) {
            return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, Json.toJson(e.getMessage()));
        }
    }

    /**
     * 删除群成员
     *
     * @return
     */
    public static Result deleteRememberFromChatGroup() {
        try {
            JsonNode node = request().body().asJson();
            String groupId = node.get("groupId").asText();
            Iterator<JsonNode> iterator = node.get("userList").iterator();
            List<Integer> userList = new ArrayList<>();
            while (iterator.hasNext()) {
                Integer id = iterator.next().asInt();
                userList.add(id);
            }
            ChatGroupAPI.deleteMemberFromGroupApi(groupId, userList);
            return Utils.createResponse(ErrorCode.NORMAL, Json.toJson("delete successfull"));
        } catch (AizouException | NumberFormatException e) {
            return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, Json.toJson(e.getMessage()));
        }
    }

    /**
     * 获取群组详情
     *
     * @return
     */
    public static Result getChatGroupDetail() {
        try {
            JsonNode req = request().body().asJson();
            String groupId = req.get("groupId").asText();
            JsonNode response = ChatGroupAPI.getChatGroupDetailApi(groupId);
            return Utils.createResponse(ErrorCode.NORMAL, response);
        } catch (AizouException e) {
            return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, Json.toJson(e.getMessage()));
        }
    }

    /**
     * 修改群组详情
     *
     * @return
     */
    public static Result modifyChatGroupDetail() {
        try {
            JsonNode req = request().body().asJson();
            String groupId = req.get("groupId").asText();
            String userId = req.get("userId").asText();
            String desc = req.get("desc").asText();
            String groupName = req.get("groupName").asText();
            Boolean isGroupPublic = req.get("isGroupPublic").asBoolean();
            ChatGroupAPI.modifyChatGroupDetailApi(groupId, userId, isGroupPublic, desc, groupName);
            return Utils.createResponse(ErrorCode.NORMAL, "success");
        } catch (AizouException e) {
            return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, Json.toJson(e.getMessage()));
        }
    }
}
