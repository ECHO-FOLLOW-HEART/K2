package utils.builder;

import com.fasterxml.jackson.databind.JsonNode;
import com.mongodb.BasicDBObjectBuilder;
import models.morphia.misc.SimpleRef;
import models.morphia.user.UserInfo;
import play.libs.Json;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by topy on 2014/10/13.
 */
public class UserBuilder {

    public static int DETAILS_LEVEL_1 =1;

    public static int DETAILS_LEVEL_2 =2;

    public static JsonNode buildUserInfo(UserInfo u,int level){
        BasicDBObjectBuilder builder = BasicDBObjectBuilder.start();

            builder.add("userId",u.userId).add("nickName", u.nickName).add("avatar", u.avatar).add("gender",u.gender)
                    .add("signature",u.signature).add("tel",u.tel);
        if(level == DETAILS_LEVEL_2){
            builder.add("countryCode", u.countryCode)
                    .add("userId",u.userId).add("email",u.email)
                    .add("friends", UserBuilder.buildUserFriends(u.friends)).add("remark",u.remark);
        }

        return Json.toJson(builder.get());
    }

    public static JsonNode buildUserFriends(Map<Integer,UserInfo> u){
        if(u == null)
            return null;
        BasicDBObjectBuilder builder = BasicDBObjectBuilder.start();
        for (Map.Entry<Integer, UserInfo> entry : u.entrySet()) {
            builder.add(entry.getKey().toString(), UserBuilder.buildUserInfo(entry.getValue(),DETAILS_LEVEL_1));
        }
        return Json.toJson(builder.get());
    }

    public static JsonNode buildRemark(Map<Integer,String> remark){
        if(remark == null)
            return null;
        BasicDBObjectBuilder builder = BasicDBObjectBuilder.start();
        for (Map.Entry<Integer, String> entry : remark.entrySet()) {
            builder.add(entry.getKey().toString(), entry.getValue());
        }
        return Json.toJson(builder.get());
    }

}
