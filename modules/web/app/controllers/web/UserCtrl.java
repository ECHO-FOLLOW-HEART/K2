package controllers.web;

import aizou.core.UserAPI;
import com.fasterxml.jackson.databind.JsonNode;
import com.mongodb.*;
import exception.ErrorCode;
import exception.TravelPiException;
import models.user.UserInfo;
import org.bson.types.ObjectId;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import utils.Utils;

import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * 用户相关的Controller。
 *
 * @author Zephyre
 */
public class UserCtrl extends Controller {

    /**
     * 第三方登录
     *
     * @return
     */
    public static Result oauthLogin() {
        JsonNode req = request().body().asJson();
        String provider = req.get("provider").asText();
        String oauthId = req.get("oauthId").asText();
        String secToken = Utils.getSecToken();

        BasicDBObjectBuilder builder = BasicDBObjectBuilder.start();
        for (String k : new String[]{"nickName", "avatar", "token", "udid"}) {
            try {
                builder.add(k, req.get(k).asText());
            } catch (NullPointerException ignored) {
            }
        }
        try {
            UserInfo user = UserAPI.regByOAuth(provider, oauthId, builder.get(), secToken);
            return Utils.createResponse(ErrorCode.NORMAL, user.toJson());
        } catch (TravelPiException e) {
            return Utils.createResponse(e.errCode, e.getMessage());
        }
    }

}
