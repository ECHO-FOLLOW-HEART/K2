package aizou.core;

import com.fasterxml.jackson.databind.JsonNode;
import database.MorphiaFactory;
import exception.AizouException;
import exception.ErrorCode;
import models.AizouBaseEntity;
import models.geo.Locality;
import models.misc.Album;
import models.misc.ImageItem;
import models.user.UserInfo;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Created by topy on 2015/7/4.
 */
public class UserUgcAPI {


    /**
     * 取得用户相册的图片
     *
     * @param userId
     * @return
     * @throws AizouException
     */
    public static List<Album> getUserAlbums(Long userId) throws AizouException {
        Datastore dsUser = MorphiaFactory.datastore();
        Query<Album> query = dsUser.createQuery(Album.class);
        query.field(Album.FD_USERID).equal(userId).field(Album.FD_TAOZIENA).equal(true);
        return query.asList();
    }

    /**
     * 删除用户图片
     *
     * @param userId
     * @param picId
     * @throws AizouException
     */
    public static void deleteUserAlbums(Long userId, Object picId) throws AizouException {
        Datastore dsUser = MorphiaFactory.datastore();
        Query<Album> query = dsUser.createQuery(Album.class);
        query.field(Album.FD_USERID).equal(userId).field(Album.FD_ID).equal(picId).field(Album.FD_TAOZIENA).equal(true);

        UpdateOperations<Album> ops = dsUser.createUpdateOperations(Album.class);
        ops.set(Album.FD_TAOZIENA, false);
        dsUser.updateFirst(query, ops);
    }

    public static List<UserInfo> getExpertUserByTracks(List<ObjectId> ids, String role, Collection<String> fieldList) throws AizouException {
        Datastore dsUser = MorphiaFactory.datastore();

        Query<UserInfo> query = dsUser.createQuery(UserInfo.class);
        List<Locality> localities = new ArrayList<>();
        Locality locality;
        for (ObjectId id : ids) {
            locality = new Locality();
            locality.setId(id);
            localities.add(locality);
        }
        query.field(UserInfo.fnTracks).hasAnyOf(localities).field(UserInfo.fnRoles).hasThisOne(role);
        if (fieldList != null && !fieldList.isEmpty())
            query.retrievedFields(true, fieldList.toArray(new String[fieldList.size()]));
        return query.asList();

    }

    /**
     * 修改足迹信息
     *
     * @param userId
     * @param action
     * @param its
     * @throws AizouException
     */
    public static void modifyTracks(Long userId, String action, Iterator<JsonNode> its) throws AizouException {

        Datastore dsUser = MorphiaFactory.datastore();
        Query<UserInfo> query = dsUser.createQuery(UserInfo.class);
        query.field(Album.FD_USERID).equal(userId);

        UpdateOperations<UserInfo> ops = dsUser.createUpdateOperations(UserInfo.class);
        if (action.equals("add"))
            ops.addAll(UserInfo.fnTracks, strListToObjectIdList(its, Locality.class), false);
        else if (action.equals("del"))
            ops.removeAll(UserInfo.fnTracks, strListToObjectIdList(its, Locality.class));
        else
            throw new AizouException(ErrorCode.INVALID_ARGUMENT, "Invalid action");
        dsUser.updateFirst(query, ops);
    }


    public static <T extends AizouBaseEntity> List<T> strListToObjectIdList(Iterator<JsonNode> it, Class<T> cls) {
        List<T> result = new ArrayList<>();
        T entity;
        String oid;
        ObjectId id;
        try {
            Constructor constructor = cls.getConstructor();
            for (Iterator<JsonNode> iterator = it; iterator.hasNext(); ) {
                oid = (it.next()).asText();
                id = new ObjectId(oid);
                entity = (T) constructor.newInstance();
                entity.setId(id);
                result.add(entity);
            }
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            return null;
        }
        return result;
    }

    /**
     * 添加一张用户上传的图片
     *
     * @param userId
     * @param imageItem
     * @throws AizouException
     */
    public static void addUserAlbum(Long userId, ImageItem imageItem, String id) throws AizouException {
        Datastore dsUser = MorphiaFactory.datastore();

        Album entity = new Album();
        entity.setId(new ObjectId(id));
        entity.setcTime(System.currentTimeMillis());
        entity.setImage(imageItem);
        entity.setUserId(userId);
        entity.setTaoziEna(true);
        dsUser.save(entity);
    }
}
