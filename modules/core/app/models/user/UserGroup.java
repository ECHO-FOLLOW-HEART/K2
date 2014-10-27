package models.user;

/**
 * Created by lxf on 14-10-25.
 */
import com.fasterxml.jackson.databind.JsonNode;
import com.mongodb.BasicDBObjectBuilder;
import models.ITravelPiFormatter;
import models.TravelPiBaseItem;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Id;
import play.data.validation.Constraints;
import play.libs.Json;

import java.util.Map;

public class UserGroup extends  TravelPiBaseItem implements ITravelPiFormatter{
    @Id
    public ObjectId id;

    /**
     * 群的id
     */
    @Constraints.Required
    public Integer groupId;
    /**
     *群主
     */

    @Constraints.Required
    @Embedded
    public UserInfo ownner;

    /**
     * 群成员
     */
    public Map<Integer,UserInfo> remember;

    /**
     *群名称
     */
    public String groupName;

    /**
     * 群描述
     */
    public String desc;

    /**
     * 群是否为公开群
     */
    public boolean isGroupPublic;

    /**
     *群组的最大成员数
     */
    public int maxUsers;


    /**
     *转换成json
     * @return
     */
    @Override
    public JsonNode toJson() {
        BasicDBObjectBuilder builder=BasicDBObjectBuilder.start();
        builder.add("_id",id).add("groupId",groupId).add("groupOwner",ownner).add("groupMember",remember)
               .add("groupName",groupName).add("isGroupPublic",isGroupPublic).add("maxuses",maxUsers);
        return Json.toJson(builder.get());
    }
}
