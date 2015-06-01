package controllers.app;

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
    public static Result createChatGroup() throws AizouException {
        String ownnerId = request().getHeader("UserId");
        JsonNode req = request().body().asJson();
        String groupName = req.get("nickName").asText();        //可能为空
        String desc = req.get("desc").asText();
        boolean isGroupPublic = req.get("isGroupPublic").asBoolean();
        Integer maxUsers = req.get("maxUsers").asInt();
        String groupId = ChatGroupAPI.createGroupApi(Long.parseLong(ownnerId), groupName, desc, isGroupPublic, maxUsers);
        return Utils.createResponse(ErrorCode.NORMAL, Json.toJson(groupId));
    }

    /**
     * 删除群组
     *
     * @return
     */
    public static Result deleteChatGroup() throws AizouException {
        JsonNode req = request().body().asJson();
        String groupId = req.get("groupId").asText();
        String userId = req.get("userId").asText();
        String response = ChatGroupAPI.deleteGroupApi(groupId, Long.parseLong(userId));
        return Utils.createResponse(ErrorCode.NORMAL, Json.toJson(response));
    }

    /**
     * 向群组中添加成员
     */
    public static Result addChatGroupRemember() throws AizouException {
        JsonNode node = request().body().asJson();
        String groupId = node.get("groupId").asText();
        Iterator<JsonNode> iterable = node.get("userList").iterator();
        List<Long> userList = new ArrayList<>();
        while (iterable.hasNext()) {
            Long id = iterable.next().asLong();
            userList.add(id);
        }
        ChatGroupAPI.putUserIntoGroupApi(groupId, userList);
        return Utils.createResponse(ErrorCode.NORMAL, Json.toJson("added successfull"));
    }

    /**
     * 删除群成员
     *
     * @return
     */
    public static Result deleteRememberFromChatGroup() throws AizouException {
        JsonNode node = request().body().asJson();
        String groupId = node.get("groupId").asText();
        Iterator<JsonNode> iterator = node.get("userList").iterator();
        List<Long> userList = new ArrayList<>();
        while (iterator.hasNext()) {
            Long id = iterator.next().asLong();
            userList.add(id);
        }
        ChatGroupAPI.deleteMemberFromGroupApi(groupId, userList);
        return Utils.createResponse(ErrorCode.NORMAL, Json.toJson("delete successfull"));
    }

    /**
     * 获取群组详情
     *
     * @return
     */
    public static Result getChatGroupDetail() throws AizouException {
        JsonNode req = request().body().asJson();
        String groupId = req.get("groupId").asText();
        JsonNode response = ChatGroupAPI.getChatGroupDetailApi(groupId);
        return Utils.createResponse(ErrorCode.NORMAL, response);
    }

    /**
     * 修改群组详情
     *
     * @return
     */
    public static Result modifyChatGroupDetail() throws AizouException {
        JsonNode req = request().body().asJson();
        String groupId = req.get("groupId").asText();
        String userId = req.get("userId").asText();
        String desc = req.get("desc").asText();
        String groupName = req.get("groupName").asText();
        Boolean isGroupPublic = req.get("isGroupPublic").asBoolean();
        ChatGroupAPI.modifyChatGroupDetailApi(groupId, userId, isGroupPublic, desc, groupName);
        return Utils.createResponse(ErrorCode.NORMAL, "success");
    }
}
