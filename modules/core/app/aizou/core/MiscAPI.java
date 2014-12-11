package aizou.core;

import exception.AizouException;
import models.MorphiaFactory;
import models.geo.Locality;
import models.misc.Column;
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
     * 通过关键词对城市进行搜索。
     *
     * @param keyword  搜索关键词。
     * @param prefix   是否为前缀搜索？
     * @param page     分页偏移量。
     * @param pageSize 页面大小。
     */
    public static List<Locality> searchLocalities(String keyword, boolean prefix, ObjectId countryId, int page, int pageSize)
            throws AizouException {
        Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.GEO);
        Query<Locality> query = ds.createQuery(Locality.class);
        if (keyword != null && !keyword.isEmpty())
            query.filter("zhName", Pattern.compile(prefix ? "^" + keyword : keyword));
        if (countryId != null)
            query.field(String.format("%s.%s", Locality.fnCountry, SimpleRef.simpID)).equal(countryId);
        return query.order(String.format("-%s, %s", Locality.fnHotness, Locality.fnRating))
                .offset(page * pageSize).limit(pageSize).asList();
    }

}
