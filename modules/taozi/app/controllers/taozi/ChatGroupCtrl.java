package controllers.taozi;

import aizou.core.UserAPI;
import aizou.core.UserGroupApI;
import com.fasterxml.jackson.databind.JsonNode;
import exception.ErrorCode;
import exception.TravelPiException;
import models.user.UserInfo;
import play.libs.Json;
import play.mvc.Result;
import utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
            int groupId = UserGroupApI.createGroup(Integer.parseInt(ownnerId), groupName, desc, isGroupPublic, maxUsers);
            return Utils.createResponse(ErrorCode.NORMAL, Json.toJson(groupId));
        } catch (TravelPiException | NumberFormatException e) {
            return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, Json.toJson("failed"));
        }
    }

    /**
     * 向群组中添加成员
     */
    public static Result addRemember() {
        JsonNode node = request().body().asJson();
        String groupId = node.get("groupId").asText();
        List<Integer> remember = (List<Integer>) node.get("userList").elements();
        Map<Integer,UserInfo> map= new HashMap<>();
        //bug
        try {
            for (Integer id : remember) {
                UserInfo user = UserAPI.getUserByUserId(id);
                map.put(id,user);
            }
            UserGroupApI.putMemberIntoGroup(Integer.parseInt(groupId), map);
            return Utils.createResponse(ErrorCode.NORMAL, Json.toJson("added successfull"));
        } catch (TravelPiException | NumberFormatException e) {
            return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, Json.toJson("added failed"));
        }
    }

    /**
     *删除群成员
     * @return
     */
    public static Result deleteRemember() {
        JsonNode node = request().body().asJson();
        String groupId = node.get("groupId").asText();
        List<Integer> userList = (List<Integer>) node.get("userList").elements();
        //bug
        try {
            UserGroupApI.deleteRememberFromGup(Integer.parseInt(groupId), userList);
            return Utils.createResponse(ErrorCode.NORMAL, Json.toJson("delete successfull"));
        } catch (TravelPiException | NumberFormatException e) {
            return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, Json.toJson("delete failed"));
        }
    }
}
