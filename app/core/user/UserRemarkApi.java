package core.user;

import exception.TravelPiException;
import models.MorphiaFactory;
import models.morphia.user.UserInfo;
import org.mongodb.morphia.Datastore;
import play.mvc.Http;

import java.util.Map;

/**
 * Created by lxf on 14-10-22.
 */
public class UserRemarkApi {
    public static UserInfo getUserByUserId(Integer id) throws TravelPiException {
        Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.USER);
        return ds.createQuery(UserInfo.class).field("userId").equal(id).get();
    }

    public static UserInfo setUserRemark(Http.Request req,Integer id,String memo) throws TravelPiException {
        String uid=req.getHeader("uid");
        UserInfo userInfo=getUserByUserId(Integer.parseInt(uid));
        Map<Integer, UserInfo> friends=userInfo.friends;
        boolean flag=friends.containsKey(id);   //查看是否存在好友
        if (flag){
            Map<Integer,String> friendRemark=userInfo.remark;
            String remark=friendRemark.get(id);
            if (!memo.equals(remark)){            //比较备注信息
                 friendRemark.put(id,memo);
            }
            else
                return userInfo;
        }
        else
            return  userInfo;
        Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.USER);
        ds.save(userInfo);               //更新用户信息
        return userInfo;
    }
}
