package controllers.taozi;

import exception.AizouException;
import exception.ErrorCode;
import models.MorphiaFactory;
import models.misc.MiscInfo;
import models.user.UserInfo;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;
import play.mvc.Controller;
import play.mvc.Result;
import utils.Constants;
import utils.Utils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by topy on 2014/10/27.
 */
public class BacheCtrl extends Controller {

    /**
     * 添加封面故事
     */
    public static Result addHomeImage() {
        try {
            Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.MISC);

            MiscInfo miscInfo = new MiscInfo();
            miscInfo.appHomeImage = "http://lxp-assets.qiniudn.com/app/cover-20140905";
            Map<String, String> map = new HashMap<>();
            map.put("title", "桃子");
            map.put("content", "<p>小明和小波浪幸福地坐在一起。</p>");
            map.put("contentType", "html");
            miscInfo.coverStory = map;
            miscInfo.application = Constants.APP_FLAG_TAOZI;
            ds.save(miscInfo);
        } catch (AizouException e) {
            return Utils.createResponse(e.getErrCode(), e.getMessage());
        }

        return Utils.createResponse(ErrorCode.NORMAL, "Success");
    }

    public static Result changeOrign() {
        try {
            Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.USER);
            Query<UserInfo> query = ds.createQuery(UserInfo.class);
            query.field("userId").notEqual(null);
            for (UserInfo us : query.asList()) {
                us.setAlias(Arrays.asList(us.getNickName().toLowerCase()));
                us.setRoles(Arrays.asList(UserInfo.fnRoles_Common));

                ds.save(us);
            }
        } catch (AizouException e) {
            return Utils.createResponse(e.getErrCode(), e.getMessage());
        }
        return Utils.createResponse(ErrorCode.NORMAL, "Success");
    }

}
