package controllers.app;

import aizou.core.ChatGroupAPI;
import com.fasterxml.jackson.databind.JsonNode;
import com.twitter.finagle.Thrift;
import controllers.thrift.ThriftFactory;
import play.mvc.Controller;
import play.mvc.Result;
import utils.Utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by topy on 2015/6/15.
 */
public class GroupCtrl extends Controller {

//
//    public static Result createGroup() {
//        String ownnerId = request().getHeader("UserId");
//        JsonNode req = request().body().asJson();
//        String groupName = Utils.getReqValue(req, "name", ownnerId + "的群组");
//        String desc = Utils.getReqValue(req, "desc", "这是一个群组");
//        String avatar = Utils.getReqValue(req, "avatar", "");
//
//        Iterator<JsonNode> iterable = req.get("participants").iterator();
//        List<Long> userList = new ArrayList<>();
//        while (iterable.hasNext()) {
//            Long id = iterable.next().asLong();
//            userList.add(id);
//        }
//
////        ThriftFactory.createChatGroup(ownnerId,groupName,userList);
////        GroupCtrl.createGroup();
////        String groupId = ChatGroupAPI.createGroupApi(Long.parseLong(ownnerId), groupName, desc, isGroupPublic, maxUsers);
//        return null;
//    }


}
