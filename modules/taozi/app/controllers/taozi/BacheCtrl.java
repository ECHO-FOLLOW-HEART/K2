package controllers.taozi;

import aizou.core.LocalityAPI;
import exception.ErrorCode;
import exception.TravelPiException;
import models.MorphiaFactory;
import models.geo.Locality;
import models.misc.MiscInfo;
import models.misc.Recom;
import models.misc.RecomType;
import models.misc.SimpleRef;
import models.user.UserInfo;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;
import play.mvc.Controller;
import play.mvc.Result;
import utils.Constants;
import utils.Utils;

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
            miscInfo.application = Constants.APP_FLAG_PEACH;
            ds.save(miscInfo);
        } catch (TravelPiException e) {
            return Utils.createResponse(e.errCode, e.getMessage());
        }

        return Utils.createResponse(ErrorCode.NORMAL, "Success");
    }

    public static Result changeOrign() {
        try {
            Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.USER);
            Query<UserInfo> query = ds.createQuery(UserInfo.class);
            query.field("userId").notEqual(null);
            for (UserInfo us : query.asList()) {
                us.setOrigin(Constants.APP_FLAG_PEACH);
                ds.save(us);
            }
        } catch (TravelPiException e) {
            return Utils.createResponse(e.errCode, e.getMessage());
        }
        return Utils.createResponse(ErrorCode.NORMAL, "Success");
    }

    public static Result createRecom() {
        Datastore ds = null;
        SimpleRef ref;
        Locality temp;
        try {
            ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.MISC);
            Query<Recom> query = ds.createQuery(Recom.class);
            RecomType type1 = new RecomType();
            type1.id = new ObjectId();
            type1.name = "人气城市";
            RecomType type2 = new RecomType();
            type2.id = new ObjectId();
            type2.name = "热门城市";

            Recom recom1 = new Recom();
            recom1.id = new ObjectId("5463d11b10114e2215b7e518");
            recom1.type = type1;
            recom1.linkType = 1;
            recom1.desc = "城南是一个风景如画...";
            recom1.linkUrl = "";
            recom1.cover = "";
            recom1.zhName = "城南";
            recom1.enName = "Chengnan";
            recom1.weight = 1;
            recom1.enabled = true;


            Recom recom2 = new Recom();
            recom2.id = new ObjectId("5463d11b10114e2211a9cf0e");
            recom2.type = type1;
            recom2.linkType = 1;
            recom2.desc = "邦博通是一个风景如画...";
            recom2.linkUrl = "";
            recom2.cover = "";
            recom2.zhName = "邦博通";
            recom2.enName = "bang bua thong";
            recom2.weight = 2;
            recom2.enabled = true;

            Recom recom3 = new Recom();
            recom3.id = new ObjectId("5463d11b10114e2215b7e519");
            recom3.type = type1;
            recom3.linkType = 1;
            recom3.desc = "邦博通是一个风景如画...";
            recom3.linkUrl = "";
            recom3.cover = "";
            recom3.zhName = "东海";
            recom3.enName = "bang bua thong";
            recom3.weight = 2;
            recom3.enabled = true;

            /**
             *
             */
            Recom recom4 = new Recom();
            recom4.id = new ObjectId("5463d11b10114e2215b7e666");
            recom4.type = type2;
            recom4.linkType = 1;
            recom4.desc = "城南是一个风景如画...";
            recom4.linkUrl = "";
            recom4.cover = "";
            recom4.zhName = "城南";
            recom4.enName = "Chengnan";
            recom4.weight = 1;
            recom4.enabled = true;

            Recom recom5 = new Recom();
            recom5.id = new ObjectId("5463d11b10114e2215b7e667");
            recom5.type = type2;
            recom5.linkType = 1;
            recom5.desc = "邦内那隆是一个风景如画...";
            recom5.linkUrl = "";
            recom5.cover = "";
            recom5.zhName = "邦内那隆";
            recom5.enName = "ban chuan";
            recom5.weight = 1;
            recom5.enabled = true;

            Recom recom6 = new Recom();
            recom6.id = new ObjectId("5463d11b10114e2215b7e668");
            recom6.type = type2;
            recom6.linkType = 1;
            recom6.desc = "昌宁郡是一个风景如画...";
            recom6.linkUrl = "";
            recom6.cover = "";
            recom6.zhName = "昌宁郡";
            recom6.enName = "changnyeong";
            recom6.weight = 1;
            recom6.enabled = true;

            Recom recom7 = new Recom();
            recom7.id = new ObjectId("5463d11b10114e2215b7e669");
            recom7.type = type2;
            recom7.linkType = 1;
            recom7.desc = "邦卡是一个风景如画...";
            recom7.linkUrl = "";
            recom7.cover = "";
            recom7.zhName = "邦卡";
            recom7.enName = "Bang Khla District";
            recom7.weight = 1;
            recom7.enabled = true;

            Recom recom8 = new Recom();
            recom8.id = new ObjectId("5463d11b10114e2215b7e670");
            recom8.type = type2;
            recom8.linkType = 1;
            recom8.desc = "城南是一个风景如画...";
            recom8.linkUrl = "";
            recom8.cover = "";
            recom8.zhName = "城南";
            recom8.enName = "Chengnan";
            recom8.weight = 1;
            recom8.enabled = true;

            Recom recom9 = new Recom();
            recom9.id = new ObjectId("5463d11b10114e2215b7e671");
            recom9.type = type2;
            recom9.linkType = 1;
            recom9.desc = "Cheongju-si是一个风景如画...";
            recom9.linkUrl = "";
            recom9.cover = "";
            recom9.zhName = "清州";
            recom9.enName = "Cheongju-si";
            recom9.weight = 1;
            recom9.enabled = true;

            ds.save(recom1);
            ds.save(recom2);
            ds.save(recom3);
            ds.save(recom4);
            ds.save(recom5);
            ds.save(recom6);
            ds.save(recom7);
            ds.save(recom8);
            ds.save(recom9);
        } catch (TravelPiException e) {
            e.printStackTrace();
        }

        return Utils.createResponse(ErrorCode.NORMAL, "Success");
    }
}
