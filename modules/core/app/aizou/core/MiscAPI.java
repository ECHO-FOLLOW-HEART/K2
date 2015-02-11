package aizou.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import exception.AizouException;
import formatter.FormatterFactory;
import formatter.taozi.misc.CommentFormatter;
import models.AizouBaseEntity;
import models.AizouBaseItem;
import models.MorphiaFactory;
import models.misc.Column;
import models.misc.Images;
import models.misc.MiscInfo;
import models.poi.Comment;
import models.user.Favorite;
import models.user.UserInfo;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.CriteriaContainerImpl;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;

import java.util.*;
import java.util.regex.Pattern;

/**
 * Created by lxf on 14-11-12.
 */
public class MiscAPI {

    /**
     * 取得旅行专栏图片的url以及跳转链接的url
     *
     * @param type 专栏的类型。现在有两种：recommend和homepage
     * @throws exception.AizouException
     */
    public static List<Column> getColumns(String type, String id) throws AizouException {
        Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.MISC);
        Query<Column> query = ds.createQuery(Column.class);
        if (id.equals("")) {
            query.field(Column.FD_TYPE).equal(type);
            query.retrievedFields(false, Column.FD_CONTENT);
        } else {
            query.field("_id").equal(new ObjectId(id));
        }
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
    public static JsonNode saveComment(Comment comment)
            throws AizouException, ReflectiveOperationException, JsonProcessingException {
        Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.MISC);
        comment.setId(new ObjectId());
        ds.save(comment);

        CommentFormatter formatter = FormatterFactory.getInstance(CommentFormatter.class);
        return formatter.formatNode(comment);
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
    public static List<Comment> displayCommentApi(ObjectId poiId, Double lower, Double upper, long lastUpdate, int page, int pageSize)
            throws AizouException {

        Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.MISC);
        Query<Comment> query = ds.createQuery(Comment.class).field(Comment.FD_ITEM_ID).equal(poiId);
        query = query.order("-" + Comment.FD_PUBLISHTIME);

        if (lastUpdate != 0)
            query.field(Comment.FD_PUBLISHTIME).lessThan(lastUpdate);

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
    public static void isFavorite(AizouBaseEntity item, Long userId) throws AizouException {
        if (userId == null) {
            item.setIsFavorite(false);
            return;
        }
        Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.USER);
        Query<Favorite> query = ds.createQuery(Favorite.class);
        query.field(UserInfo.fnUserId).equal(userId).field(Favorite.fnItemId).equal(item.getId());
        Iterator<Favorite> it = query.iterator();
        if (it.hasNext())
            item.setIsFavorite(true);
    }

    /**
     * 判断是否被收藏
     *
     * @param items
     * @param userId
     * @throws AizouException
     */
    public static void isFavorite(List<? extends AizouBaseEntity> items, Long userId) throws AizouException {
        if (userId == null) {
            return;
        }
        Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.USER);
        Query<Favorite> query = ds.createQuery(Favorite.class);
        query.field(UserInfo.fnUserId).equal(userId);
        List<CriteriaContainerImpl> criList = new ArrayList<>();
        for (AizouBaseEntity temp : items) {
            criList.add(query.criteria(Favorite.fnItemId).equal(temp.getId()));
        }
        query.or(criList.toArray(new CriteriaContainerImpl[criList.size()]));
        List<Favorite> favoriteList = query.asList();

        Map<ObjectId, Favorite> favoriteMap = new HashMap<ObjectId, Favorite>();
        for (Favorite temp : favoriteList) {
            favoriteMap.put(temp.itemId, temp);
        }
        for (AizouBaseEntity temp : items) {
            if (favoriteMap.get(temp.getId()) != null)
                temp.setIsFavorite(true);
        }
    }

    public static List<Images> getLocalityAlbum(ObjectId id, int page, int pageSize) throws AizouException {
        Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.IMAGESTORE);
        Query<Images> query = ds.createQuery(Images.class);

        query.field(Images.FD_ITEMID).equal(id);
        query.offset(page * pageSize).limit(pageSize);
        return query.asList();
    }

    public static Long getLocalityAlbumCount(ObjectId id) throws AizouException {
        Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.IMAGESTORE);
        Query<Images> query = ds.createQuery(Images.class);

        query.field(Images.FD_ITEMID).equal(id);
        return query.countAll();
    }

    public static Map<String, String> getMiscInfos(List<String> keys) throws AizouException {
        Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.MISC);
        Query<MiscInfo> query = ds.createQuery(MiscInfo.class);

        List<CriteriaContainerImpl> criList = new ArrayList<>();
        for (String key : keys) {
            criList.add(query.criteria("key").equal(key));
        }
        query.or(criList.toArray(new CriteriaContainerImpl[criList.size()]));
        List<MiscInfo> miscInfos = query.asList();
        Map<String, String> result = new HashMap<>();
        for (MiscInfo miscInfo : miscInfos) {
            result.put(miscInfo.key, miscInfo.value);
        }
        return result;
    }

}
