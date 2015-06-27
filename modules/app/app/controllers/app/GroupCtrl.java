package controllers.app;

import aizou.core.ChatGroupAPI;
import com.fasterxml.jackson.databind.JsonNode;
import com.twitter.finagle.Thrift;
import controllers.thrift.ThriftFactory;
import exception.ErrorCode;
import formatter.FormatterFactory;
import formatter.taozi.group.ChatGroupFormatter;
import formatter.taozi.guide.SimpleGuideFormatter;
import formatter.taozi.user.UserInfoFormatter;
import formatter.taozi.user.UserInfoSimpleFormatter;
import models.group.ChatGroup;
import models.user.UserInfo;
import org.apache.thrift.TException;
import org.bson.types.ObjectId;
import play.mvc.Controller;
import play.mvc.Result;
import utils.Utils;

import java.util.*;

/**
 * Created by topy on 2015/6/15.
 */
public class GroupCtrl extends Controller {


    /**
     * 创建群组
     *
     * @return
     */
    public static Result createGroup() {
        String ownnerId = request().getHeader("UserId");
        JsonNode req = request().body().asJson();
        String groupName = Utils.getReqValue(req, "name", ownnerId + "的讨论组");
        String desc = Utils.getReqValue(req, "desc", "这是一个群组");
        String avatar = Utils.getReqValue(req, "avatar", "");

        Iterator<JsonNode> iterable = req.get("participants").iterator();
        List<Long> userList = new ArrayList<>();
        while (iterable.hasNext()) {
            Long id = iterable.next().asLong();
            userList.add(id);
        }
        Map<String, String> fieldMap = new HashMap<>();
        fieldMap.put(ChatGroup.FD_NAME, groupName);
        fieldMap.put(ChatGroup.FD_DESC, desc);
        fieldMap.put(ChatGroup.FD_AVATAR, avatar);
        try {
            ThriftFactory.createChatGroup(Long.valueOf(ownnerId), userList, fieldMap);
        } catch (TException e) {
            return Utils.createResponse(ErrorCode.DATABASE_ERROR, "群组创建失败");
        }
        return Utils.createResponse(ErrorCode.NORMAL, "Success.");
    }

    /**
     * 修改群组
     *
     * @param gId
     * @return
     */
    public static Result modifyGroup(Long gId) {
        String ownnerId = request().getHeader("UserId");
        JsonNode req = request().body().asJson();
        Map<String, String> fieldMap = new HashMap<>();
        if (req.has("name"))
            fieldMap.put(ChatGroup.FD_NAME, req.get("name").asText());
        if (req.has("desc"))
            fieldMap.put(ChatGroup.FD_DESC, req.get("desc").asText());
        if (req.has("avatar"))
            fieldMap.put(ChatGroup.FD_AVATAR, req.get("avatar").asText());
        ChatGroup chatGroup = null;
        try {
            chatGroup = ThriftFactory.updateChatGroup(gId, fieldMap);
            chatGroup.setId(new ObjectId());
        } catch (TException e) {
            return Utils.createResponse(ErrorCode.DATABASE_ERROR, "群组修改失败");
        }
        ChatGroupFormatter chatGroupFormatter = FormatterFactory.getInstance(ChatGroupFormatter.class);
        return Utils.createResponse(ErrorCode.NORMAL, chatGroupFormatter.formatNode(chatGroup));
    }

    /**
     * 获得群组信息
     *
     * @param gId
     * @return
     */
    public static Result getGroup(Long gId) {
        ChatGroup chatGroup = null;
        try {
            chatGroup = ThriftFactory.getChatGroup(gId);
        } catch (TException e) {
            return Utils.createResponse(ErrorCode.DATABASE_ERROR, "获得群组信息失败");
        }
        ChatGroupFormatter chatGroupFormatter = FormatterFactory.getInstance(ChatGroupFormatter.class);
        return Utils.createResponse(ErrorCode.NORMAL, chatGroupFormatter.formatNode(chatGroup));
    }

    /**
     * 操作群组
     *
     * @param gId
     * @return
     */
    public static Result opGroup(Long gId) {

        JsonNode req = request().body().asJson();
        String action = req.get("action").asText();

        Iterator<JsonNode> iterable = req.get("participants").iterator();
        List<Long> userList = new ArrayList<>();
        while (iterable.hasNext()) {
            Long id = iterable.next().asLong();
            userList.add(id);
        }
        try {
            if (action.equals("addMembers"))
                ThriftFactory.addChatGroupMembers(gId, userList);
            else if (action.equals("delMembers"))
                ThriftFactory.removeChatGroupMembers(gId, userList);
        } catch (TException e) {
            return Utils.createResponse(ErrorCode.DATABASE_ERROR, "操作失败");
        }
        return Utils.createResponse(ErrorCode.NORMAL, "Success!");
    }

    public static Result getGroupUsers(Long gId) {
        List<UserInfo> users = null;
        try {
            users = ThriftFactory.getChatGroupMembers(gId);
        } catch (TException e) {
            return Utils.createResponse(ErrorCode.DATABASE_ERROR, "获得群组信息失败");
        }
        UserInfoSimpleFormatter formatter = FormatterFactory.getInstance(UserInfoSimpleFormatter.class);
        return Utils.createResponse(ErrorCode.NORMAL, formatter.formatNode(users));
    }

    /**
     * 取得用户的群组
     *
     * @param uid
     * @param page
     * @param pageSize
     * @return
     */
    public static Result getUserGroup(Long uid, int page, int pageSize) {
        List<ChatGroup> chatGroup = ThriftFactory.getUserChatGroups(uid, page, pageSize);
        ChatGroupFormatter chatGroupFormatter = FormatterFactory.getInstance(ChatGroupFormatter.class);
        if (chatGroup == null)
            chatGroup = new ArrayList<ChatGroup>();
        return Utils.createResponse(ErrorCode.NORMAL, chatGroupFormatter.formatNode(chatGroup));
    }


}
