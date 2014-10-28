package controllers.taozi;

import aizou.core.ChatGroupAPI;
import com.fasterxml.jackson.databind.JsonNode;
import exception.ErrorCode;
import exception.TravelPiException;
import play.libs.Json;
import play.mvc.Result;
import utils.Utils;

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
        String ownnerId = request().getHeader("userId");
        JsonNode req = request().body().asJson();
        String groupName = req.get("nickName").asText();
        String desc = req.get("desc").asText();
        boolean isGroupPublic = req.get("isGroupPublic").asBoolean();
        Integer maxUsers = req.get("maxUsers").asInt();

        try {
            String groupId = ChatGroupAPI.createGroupApi(Integer.parseInt(ownnerId), groupName, desc, isGroupPublic, maxUsers);
            return Utils.createResponse(ErrorCode.NORMAL, Json.toJson(groupId));
        } catch (TravelPiException | NumberFormatException e) {
            return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, e.getMessage());
        }
    }

    /**
     * 删除群组
     *
     * @return
     */
    public static Result deleteChatGroup() {
        JsonNode req = request().body().asJson();
        String groupId = req.get("groupId").asText();
        String userId = req.get("userId").asText();
        try {
            String response = ChatGroupAPI.deleteGroupApi(groupId, Integer.parseInt(userId));
            return Utils.createResponse(ErrorCode.NORMAL, Json.toJson(response));
        } catch (TravelPiException e) {
            return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, e.getMessage());
        }
    }

    /**
     * 向群组中添加成员
     */
    public static Result addChatGroupRemember() {
        JsonNode node = request().body().asJson();
        String groupId = node.get("groupId").asText();
        List<Integer> userList = (List<Integer>) node.get("userList").elements();
        try {
            ChatGroupAPI.putUserIntoGroupApi(groupId, userList);
            return Utils.createResponse(ErrorCode.NORMAL, Json.toJson("added successfull"));
        } catch (TravelPiException | NumberFormatException e) {
            return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, Json.toJson(e.getMessage()));
        }
    }

    /**
     * 删除群成员
     *
     * @return
     */
    public static Result deleteRememberFromChatGroup() {
        JsonNode node = request().body().asJson();
        String groupId = node.get("groupId").asText();
        List<Integer> userList = (List<Integer>) node.get("userList").elements();
        try {
            ChatGroupAPI.deleteMemberFromGroupApi(groupId, userList);
            return Utils.createResponse(ErrorCode.NORMAL, Json.toJson("delete successfull"));
        } catch (TravelPiException | NumberFormatException e) {
            return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, Json.toJson(e.getMessage()));
        }
    }

    /**
     * 获取群组详情
     *
     * @return
     */
    public static Result getChatGroupDetail() {
        JsonNode req = request().body().asJson();
        String groupId = req.get("groupId").asText();
        try {
            JsonNode response = ChatGroupAPI.getChatGroupDetailApi(groupId);
            return Utils.createResponse(ErrorCode.NORMAL, response);
        } catch (TravelPiException e) {
            return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, Json.toJson(e.getMessage()));
        }
    }

    public static Result modifyChatGroupDetail() {
        JsonNode req = request().body().asJson();
        String groupId = req.get("groupId").asText();
        String userId = req.get("userId").asText();
        String desc = req.get("desc").asText();
        String groupName = req.get("groupName").asText();
        Boolean isGroupPublic = req.get("isGroupPublic").asBoolean();
        try {
            ChatGroupAPI.modifyChatGroupDetailApi(groupId, userId, isGroupPublic, desc, groupName);
            return Utils.createResponse(ErrorCode.NORMAL, "success");
        } catch (TravelPiException e) {
            return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, Json.toJson(e.getMessage()));
        }
    }
}
