package utils.builder;

import com.fasterxml.jackson.databind.JsonNode;
import com.mongodb.BasicDBObjectBuilder;
import core.UserAPI;
import exception.TravelPiException;
import models.morphia.user.Credential;
import models.morphia.user.UserInfo;
import play.libs.Json;

import java.util.Map;

/**
 * Created by topy on 2014/10/13.
 */
public class UserBuilder {

    public static int DETAILS_LEVEL_1 = 1;

    public static int DETAILS_LEVEL_2 = 2;

    public static int DETAILS_LEVEL_3 = 3;

    public static JsonNode buildUserInfo(UserInfo u, int level) throws TravelPiException {
        BasicDBObjectBuilder builder = BasicDBObjectBuilder.start();

        builder.add("userId", u.userId == null ? "" : u.userId).add("nickName", u.nickName == null ? "" : u.nickName)
                .add("avatar", u.avatar == null ? "" : u.avatar).add("gender", u.gender == null ? "" : u.gender)
                .add("signature", u.signature == null ? "" : u.signature).add("tel", u.tel == null ? "" : u.tel)
                .add("secToken", u.secToken == null ? "" : u.secToken);
        Credential ce = UserAPI.getPwd(u);
//         if(ce != null){
//             builder.add("hasPwd",ce == null?false:(ce.pwdHash == null?true:false));
//         }
        if (level == DETAILS_LEVEL_2 && ce != null)
            builder.add("easemobPwd", ce.easemobPwd == null ? "" : ce.easemobPwd)
                    .add("easemobUser", ce.easemobUser == null ? "" : ce.easemobUser);
        if (level == DETAILS_LEVEL_3) {
            JsonNode friends = UserBuilder.buildUserFriends(u.friends);
            JsonNode remark = UserBuilder.buildRemark(u.remark);
            builder.add("countryCode", u.countryCode == null ? "" : u.countryCode)
                    .add("email", u.email == null ? "" : u.email)
                    .add("friends", friends == null ? "" : friends).add("remark", remark == null ? "" : remark);
        }

        return Json.toJson(builder.get());
    }

    public static JsonNode buildUserFriends(Map<Integer, UserInfo> u) throws TravelPiException {
        if (u == null)
            return null;
        BasicDBObjectBuilder builder = BasicDBObjectBuilder.start();
        for (Map.Entry<Integer, UserInfo> entry : u.entrySet()) {
            builder.add(entry.getKey().toString(), UserBuilder.buildUserInfo(entry.getValue(), DETAILS_LEVEL_1));
        }
        return Json.toJson(builder.get());
    }

    public static JsonNode buildRemark(Map<Integer, String> remark) {
        if (remark == null)
            return null;
        BasicDBObjectBuilder builder = BasicDBObjectBuilder.start();
        for (Map.Entry<Integer, String> entry : remark.entrySet()) {
            builder.add(entry.getKey().toString(), entry.getValue());
        }
        return Json.toJson(builder.get());
    }

}
