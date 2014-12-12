package aizou.core;

import com.fasterxml.jackson.databind.JsonNode;
import exception.AizouException;
import formatter.taozi.misc.CommentFormatter;
import models.AizouBaseEntity;
import models.MorphiaFactory;
import models.geo.Locality;
import models.misc.Column;
import models.misc.TravelNote;
import models.poi.AbstractPOI;
import models.poi.Comment;
import models.user.Favorite;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;

import java.util.Iterator;
import java.util.List;

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
    public static List<Column> getColumns() throws AizouException {
        Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.MISC);
        Query<Column> query = ds.createQuery(Column.class);
        return query.asList();

    }

    public static void saveColumns(Column pageFirst) throws AizouException {
        Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.MISC);
        ds.save(pageFirst);
    }

    /**
     * 储存评论信息
     *
     * @param comment
     * @throws exception.AizouException
     */
    public static JsonNode saveComment(Comment comment) throws AizouException {
        Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.MISC);
        comment.setId(new ObjectId());
        ds.save(comment);

        CommentFormatter formatter = new CommentFormatter();
        return formatter.format(comment);
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
     * @param lastUpdate
     * @param pageSize
     * @return
     * @throws exception.AizouException
     */
    public static List<Comment> displayCommentApi(ObjectId poiId, Double lower, Double upper, long lastUpdate, int pageSize)
            throws AizouException {

        Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.MISC);
        Query<Comment> query = ds.createQuery(Comment.class).field(Comment.FD_ITEM_ID).equal(poiId);
        query = query.order("-" + Comment.FD_CTIME);

        if (lastUpdate != 0)
            query.field(Comment.FD_CTIME).lessThan(lastUpdate);

//        query.field(Comment.FD_RATING).greaterThanOrEq(lower).field(Comment.FD_RATING).lessThanOrEq(upper);

        query.limit(pageSize);

        return query.asList();
    }

    /**
     * 判断是否被收藏
     *
     * @param item
     * @param userId
     * @throws AizouException
     */
    public static void isFavorite(AizouBaseEntity item, Integer userId) throws AizouException {
        if(userId == null) {
            item.setIsFavorite(false);
            return;
        }
        Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.USER);
        Query<Favorite> query = ds.createQuery(Favorite.class);
        query.field("userId").equal(userId).field(Favorite.fnItemId).equal(item.getId());
        Iterator<Favorite> it = query.iterator();
        if (it.hasNext())
            item.setIsFavorite(true);
    }

}
