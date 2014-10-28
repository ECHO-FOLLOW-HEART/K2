package models.user;

/**
 * Created by lxf on 14-10-25.
 */

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.BasicDBObjectBuilder;
import models.ITravelPiFormatter;
import models.TravelPiBaseItem;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Transient;
import play.data.validation.Constraints;
import play.libs.Json;

import java.util.ArrayList;
import java.util.List;

public class ChatGroupInfo extends TravelPiBaseItem implements ITravelPiFormatter {
    @Transient
    public static final String OWNER="owner";
    @Transient
    public static final String MEMBERS="members";
    @Transient
    public static final String GROUPNAME="groupName";
    @Transient
    public static final String DESC="desc";
    @Transient
    public static final String ISGROUPPUBLIC="isGroupPublic";

    @Id
    public ObjectId id;

    /**
     * 群的id
     */
    @Constraints.Required
    public String groupId;

    /**
     * 群主
     */
    @Constraints.Required
    @Embedded
    public UserInfo owner;

    /**
     * 群成员
     */
    public List<UserInfo> members;

    /**
     * 群名称
     */
    public String groupName;

    /**
     * 群描述
     */
    public String desc;

    /**
     * 群是否为公开群
     */
    public Boolean isGroupPublic;

    /**
     * 群组的最大成员数
     */
    public Integer maxUsers;


    /**
     * 转换成json
     *
     * @return
     */
    @Override
    public JsonNode toJson() {
        BasicDBObjectBuilder builder = BasicDBObjectBuilder.start();
        builder.add("ownerId", owner.userId).add("ownerName", owner.nickName).add("desc", desc)
                .add("groupName", groupName).add("isGroupPublic", isGroupPublic);
        List<JsonNode> list=new ArrayList<>();
        for (UserInfo userInfo:members){
            ObjectNode node=Json.newObject();
            node.put("userId",userInfo.userId);
            node.put("userName",userInfo.nickName);
            list.add(node);
        }
        builder.add("members",list);
        return Json.toJson(builder.get());
    }
}
