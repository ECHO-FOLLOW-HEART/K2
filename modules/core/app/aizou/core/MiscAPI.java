package aizou.core;

import exception.AizouException;
import models.MorphiaFactory;
import models.geo.Locality;
import models.misc.PageFirst;
import models.misc.SimpleRef;
import models.poi.Comment;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by lxf on 14-11-12.
 */
public class MiscAPI {

    /**
     * 取得旅行专栏图片的url以及跳转链接的url
     *
     * @return
     * @throws exception.AizouException
     */
    public static List<PageFirst> getColumns() throws AizouException {
         Datastore ds=MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.MISC);
         Query<PageFirst> query=ds.createQuery(PageFirst.class);
         return query.asList();

    }

    public static void saveColumns(PageFirst pageFirst) throws AizouException {
        Datastore ds=MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.MISC);
        ds.save(pageFirst);
    }

    /**
     * 储存评论信息
     *
     * @param comment
     * @throws exception.AizouException
     */
    public static void saveComment(Comment comment) throws AizouException {
        Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.MISC);
        ds.save(comment);
    }

    /**
     * 更新对景点的评论
     *
     * @param poiId
     * @param commentDetails
     * @throws exception.AizouException
     */
    public static void updateComment(String poiId, String commentDetails) throws AizouException {
        Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.MISC);
        UpdateOperations<Comment> uo = ds.createUpdateOperations(Comment.class);
        uo.set("commentDetails", commentDetails);
        ds.update(ds.createQuery(Comment.class).field("poiId").equal(poiId), uo);
    }

    /**
     * 通过poiId取得评论
     *
     * @param poiId
     * @param page
     * @param pageSize
     * @return
     * @throws exception.AizouException
     */
    public static List<Comment> displayCommentApi(ObjectId poiId, Double lower,Double upper, int page, int pageSize)
            throws AizouException {

        Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.MISC);
        Query<Comment> query = ds.createQuery(Comment.class).field("poiId").equal(poiId);
        query = query.order(Comment.FD_TIME);

        /*if (goodComment) {
            query = query.filter("score >=", 0.7).filter("score <", 1.0);
            return query.offset(page * pageSize).limit(pageSize).asList();
        }
        if (midComment) {
            query = query.filter("score >=", 0.3).filter("score <", 0.7);
            return query.offset(page * pageSize).limit(pageSize).asList();
        }
        if (midComment) {
            query = query.filter("score <", 0.3);
            return query.offset(page * pageSize).limit(pageSize).asList();
        }*/
        return query.filter(Comment.FD_RATING +" >=",lower).filter(Comment.FD_RATING +" <",upper).offset(page * pageSize).limit(page).asList();

    }


}
