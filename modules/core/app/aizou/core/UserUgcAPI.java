package aizou.core;

import com.fasterxml.jackson.databind.JsonNode;
import database.MorphiaFactory;
import exception.AizouException;
import exception.ErrorCode;
import models.geo.Locality;
import models.misc.Album;
import models.misc.ImageItem;
import models.misc.Track;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.CriteriaContainerImpl;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;

import java.util.*;

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

    /**
     * 取得用户的足迹
     *
     * @param userId
     * @return
     * @throws AizouException
     */
    public static List<Track> getUserTracks(Long userId) throws AizouException {
        Datastore dsUser = MorphiaFactory.datastore();
        Query<Track> query = dsUser.createQuery(Track.class);
        query.field(Track.fnUserId).equal(userId).field(Track.FD_TAOZIENA).equal(true);
        return query.asList();
    }

    /**
     * 取得多个用户的足迹
     *
     * @param uids
     * @return
     * @throws AizouException
     */
    public static List<Track> getUserTracks(List<Long> uids) throws AizouException {
        Datastore dsUser = MorphiaFactory.datastore();

        Query<Track> query = dsUser.createQuery(Track.class);
        query.field(Track.fnUserId).in(uids).field(Track.FD_TAOZIENA).equal(true);
        Collection<String> fieldList = Arrays.asList(Track.fnCountry, Track.fnLocality, Track.fnUserId);
        if (fieldList != null && !fieldList.isEmpty())
            query.retrievedFields(true, fieldList.toArray(new String[fieldList.size()]));
        return query.asList();
    }

    /**
     * 根据足迹的国家和目的地来查询匹配的用户
     *
     * @param countryIds
     * @param locIds
     * @param userIds
     * @return
     * @throws AizouException
     */
    public static List<Track> getExpertUserByTracks(List<ObjectId> countryIds, List<ObjectId> locIds, List<Long> userIds) throws AizouException {
        Datastore dsUser = MorphiaFactory.datastore();
        Query<Track> query = dsUser.createQuery(Track.class);
        // query.field(UserInfo.fnTracks).hasAnyOf(localities).field(UserInfo.fnRoles).hasThisOne(role);
        if (countryIds != null && !countryIds.isEmpty())
            query.field(Track.fnCountry + ".id").in(countryIds);
        if (locIds != null && !locIds.isEmpty())
            query.field(Track.fnLocality + ".id").in(locIds);
        query.field(Track.fnUserId).in(userIds).field(Track.FD_TAOZIENA).equal(true);
        Collection<String> fieldList = Arrays.asList(Track.fnUserId, Track.fnCountry, Track.fnLocality);
        if (fieldList != null && !fieldList.isEmpty())
            query.retrievedFields(true, fieldList.toArray(new String[fieldList.size()]));
        return query.asList();
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

    /**
     * 修改足迹
     *
     * @param userId
     * @param action
     * @param its
     * @throws AizouException
     */
    public static void modifyTracksWithSearch(Long userId, String action, Iterator<JsonNode> its) throws AizouException {
        // 取得需要添加或删除的足迹的LocalityId
        List<ObjectId> allLocalities = getAllObjectIds(its);

        Datastore dsTrack = MorphiaFactory.datastore();
        UpdateOperations<Track> ops = dsTrack.createUpdateOperations(Track.class);
        if (action.equals("add")) {
            List<Track> tracks = fillTracks(userId, allLocalities);
            dsTrack.save(tracks);
        } else if (action.equals("del")) {
            Query<Track> query = dsTrack.createQuery(Track.class);
            query.field(Track.fnUserId).equal(userId).field(Track.fnLocality + ".id").in(allLocalities);
            dsTrack.delete(query);
        } else
            throw new AizouException(ErrorCode.INVALID_ARGUMENT, "Invalid action");
    }

    /**
     * 从Json中取出List对象
     *
     * @param its
     * @return
     */
    public static List<ObjectId> getAllObjectIds(Iterator<JsonNode> its) {
        String oid;
        ObjectId id;
        List<ObjectId> result = new ArrayList<>();
        for (Iterator<JsonNode> iterator = its; iterator.hasNext(); ) {
            oid = (its.next()).asText();
            id = new ObjectId(oid);
            result.add(id);
        }
        return result;
    }

    /**
     * 填充足迹信息
     *
     * @param userId
     * @param ids
     * @return
     */
    public static List<Track> fillTracks(Long userId, List<ObjectId> ids) {

        Datastore ds = MorphiaFactory.datastore();
        Query<Locality> query = ds.createQuery(Locality.class);

        List<CriteriaContainerImpl> criList = new ArrayList<>();
        for (ObjectId tempId : ids) {
            criList.add(query.criteria("_id").equal(tempId));
        }
        query.or(criList.toArray(new CriteriaContainerImpl[criList.size()]));

        List<String> fieldList = Arrays.asList(Locality.FD_ID, Locality.fnLocation, Locality.FD_ZH_NAME, Locality.FD_EN_NAME, Locality.fnImages, Locality.fnCountry);
        if (fieldList != null && !fieldList.isEmpty())
            query.retrievedFields(true, fieldList.toArray(new String[fieldList.size()]));
        List<Locality> localityList = query.asList();
        Track tempTrack;
        List<Track> tracks = new ArrayList<>();
        for (Locality locality : localityList) {
            tempTrack = new Track();
            tempTrack.setId(new ObjectId());
            tempTrack.setLocality(locality);
            tempTrack.setCountry(locality.getCountry());
            tempTrack.setUserId(userId);
            tempTrack.setTaoziEna(true);
            // 唯一标识
            tempTrack.setItemId();
            tracks.add(tempTrack);

        }
        return tracks;
    }
}
