package aizou.core;

import exception.AizouException;
import exception.ErrorCode;
import models.AizouBaseEntity;
import models.MorphiaFactory;
import models.geo.Locality;
import models.guide.AbstractGuide;
import models.guide.Guide;
import models.guide.GuideTemplate;
import models.guide.ItinerItem;
import models.poi.*;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.CriteriaContainerImpl;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;
import utils.Constants;
import utils.LogUtils;

import java.util.*;

/**
 * Created by topy on 2014/11/5.
 */
public class GuideAPI {

    /**
     * 根据ID取得攻略
     *
     * @return
     * @throws exception.AizouException
     */
    public static Guide getGuideByDestination(List<ObjectId> ids, Integer userId) throws AizouException {
        Query<GuideTemplate> query = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.GUIDE)
                .createQuery(GuideTemplate.class);
        List<CriteriaContainerImpl> criList = new ArrayList<>();
        for (ObjectId id : ids) {
            criList.add(query.criteria("locId").equal(id));
        }
        query.or(criList.toArray(new CriteriaContainerImpl[criList.size()]));
        List<GuideTemplate> guideTemplates = query.asList();

        Query<Locality> queryDes = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.GEO)
                .createQuery(Locality.class);
        List<String> fieldList = new ArrayList<>();
        Collections.addAll(fieldList, "_id", "zhName", "enName");
        queryDes.retrievedFields(true, fieldList.toArray(new String[fieldList.size()]));
        List<CriteriaContainerImpl> criListDes = new ArrayList<>();
        for (ObjectId id : ids) {
            criListDes.add(queryDes.criteria("_id").equal(id));
        }
        queryDes.or(criListDes.toArray(new CriteriaContainerImpl[criListDes.size()]));

        List<Locality> destinations = queryDes.asList();

        // 根据选择的目的地顺序排序
        guideTemplates = sortGuideTemplates(guideTemplates, ids);
        Guide result = constituteUgcGuide(guideTemplates, destinations, userId);
        // 保存攻略时，置为可用
        result.setTaoziEna(true);
        //创建时即保存
        MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.GUIDE).save(result);
        return result;

    }

    /**
     * 取得一个空的攻略
     *
     * @return
     * @throws exception.AizouException
     */
    public static Guide getEmptyGuide(List<ObjectId> ids, Integer userId) throws AizouException {

        Query<Locality> queryDes = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.GEO)
                .createQuery(Locality.class);
        List<String> fieldList = new ArrayList<>();
        Collections.addAll(fieldList, "_id", "zhName", "enName");
        queryDes.retrievedFields(true, fieldList.toArray(new String[fieldList.size()]));
        List<CriteriaContainerImpl> criListDes = new ArrayList<>();
        for (ObjectId id : ids) {
            criListDes.add(queryDes.criteria("_id").equal(id));
        }
        queryDes.or(criListDes.toArray(new CriteriaContainerImpl[criListDes.size()]));

        List<Locality> destinations = queryDes.asList();
        Guide ugcGuide = new Guide();
        ugcGuide.userId = userId;
        ugcGuide.localities = destinations;
        ugcGuide.updateTime = System.currentTimeMillis();
        ugcGuide.itinerary = new ArrayList<>();
        ugcGuide.shopping = new ArrayList<>();
        ugcGuide.restaurant = new ArrayList<>();
        ugcGuide.itineraryDays = 0;
        if (destinations != null && destinations.get(0) != null)
            ugcGuide.images = destinations.get(0).getImages();
        ugcGuide.title = getUgcGuideTitle(destinations);
        // 保存攻略时，置为可用
        ugcGuide.setTaoziEna(true);
        //创建时即保存
        MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.GUIDE).save(ugcGuide);
        return ugcGuide;

    }

    /**
     * 取得用户攻略的标题
     *
     * @param destinations
     * @return
     */
    private static String getUgcGuideTitle(List<Locality> destinations) {
        StringBuffer titlesBuffer = new StringBuffer();
        for (Locality locality : destinations)
            titlesBuffer.append(locality.getZhName()).append(Constants.SYMBOL_NEWTON);
        String titleStr = titlesBuffer.toString();
        titleStr = titleStr.substring(0, titleStr.length() - 1);
        titleStr = "我的" + titleStr + "旅行计划";
        return titleStr;
    }

    /**
     * 根据选择的目的地顺序排序
     *
     * @param guideTemplates
     * @param ids
     * @return
     */
    private static List<GuideTemplate> sortGuideTemplates(List<GuideTemplate> guideTemplates, List<ObjectId> ids) {

        List<GuideTemplate> result = new ArrayList<>();
        Map<ObjectId, GuideTemplate> map = new HashMap<>();
        for (GuideTemplate guideTemplate : guideTemplates) {
            map.put(guideTemplate.locId, guideTemplate);
        }

        GuideTemplate temp;
        for (ObjectId id : ids) {
            temp = map.get(id);
            if (temp != null)
                result.add(temp);
        }
        return result;
    }

    /**
     * 合并模板攻略
     *
     * @param guideTemplates
     * @param destinations
     * @param userId
     * @return
     */
    private static Guide constituteUgcGuide(List<GuideTemplate> guideTemplates, List<Locality> destinations, Integer userId) {
        Guide ugcGuide = new Guide();
        ugcGuide.setId(new ObjectId());
        ugcGuide.userId = userId;
        ugcGuide.localities = destinations;
        ugcGuide.updateTime = System.currentTimeMillis();

        // 生成攻略标题
        ugcGuide.title = getUgcGuideTitle(destinations);

        //如果模板为空，组成空攻略
        if (guideTemplates == null || guideTemplates.size() == 0) {
            ugcGuide.itinerary = new ArrayList<>();
            ugcGuide.shopping = new ArrayList<>();
            ugcGuide.restaurant = new ArrayList<>();
            ugcGuide.itineraryDays = 0;
            ugcGuide.images = destinations.get(0).getImages();
            return ugcGuide;
        }
        boolean isMultipleDestination = false;
        int acrossLocalityDay = 0;
        int itineraryDaysCnt = 0;
        List<ObjectId> locIds = new ArrayList<>();

        List<ItinerItem> itineraries = new ArrayList<>();
        List<Shopping> shoppingList = new ArrayList<>();
        List<Restaurant> restaurants = new ArrayList<>();

        for (GuideTemplate temp : guideTemplates) {
            if (temp == null)
                continue;
            locIds.add(temp.getId());
            //行程单多目的地时，dayIndex要在前一个目的地的dayIndex基础上累加
            acrossLocalityDay = itineraryDaysCnt;
            if (temp.itinerary != null && temp.itinerary.size() > 0) {
                for (ItinerItem it : temp.itinerary) {
                    //
                    it.dayIndex = it.dayIndex + acrossLocalityDay + (isMultipleDestination ? 1 : 0);
                    if (itineraryDaysCnt < it.dayIndex)
                        itineraryDaysCnt = it.dayIndex;

                }
                itineraries.addAll(temp.itinerary);
            }
            if (temp.shopping != null && temp.shopping.size() > 0) {
                shoppingList.addAll(temp.shopping);
            }

            if (temp.restaurant != null && temp.restaurant.size() > 0) {
                restaurants.addAll(temp.restaurant);
            }
            isMultipleDestination = true;
        }

        ugcGuide.itinerary = itineraries;
        ugcGuide.shopping = shoppingList;
        ugcGuide.restaurant = restaurants;
        ugcGuide.itineraryDays = itineraryDaysCnt + 1;
        //取第一个目的地的图片
        if (guideTemplates != null && guideTemplates.get(0) != null)
            ugcGuide.images = guideTemplates.get(0).images;
        return ugcGuide;

    }

    /**
     * 根据ID取得攻略
     *
     * @param id
     * @return
     * @throws exception.AizouException
     */
    public static Guide getGuideById(ObjectId id, List<String> fieldList) throws AizouException {
        Query<Guide> query = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.GUIDE)
                .createQuery(Guide.class);
        if (fieldList != null && !fieldList.isEmpty())
            query.retrievedFields(true, fieldList.toArray(new String[fieldList.size()]));
        query.field("_id").equal(id);
        return query.get();
    }

    /**
     * 根据ID删除攻略
     *
     * @param id
     * @throws exception.AizouException
     */
    public static void deleteGuideById(ObjectId id) throws AizouException {

        Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.GUIDE);
        UpdateOperations<Guide> update = ds.createUpdateOperations(Guide.class);
        Query<Guide> query = ds.createQuery(Guide.class);
        query.field("_id").equal(id);
        update.set(AizouBaseEntity.FD_TAOZIENA, false);
        ds.update(query, update);

    }

    /**
     * 根据用户ID取得攻略列表
     *
     * @param uid
     * @return
     * @throws exception.AizouException
     */
    public static List<Guide> getGuideByUser(Integer uid, List<String> fieldList, int page, int pageSize) throws AizouException {
        Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.GUIDE);
        Query<Guide> query = ds.createQuery(Guide.class);
        query.field(Guide.fnUserId).equal(uid).field(AizouBaseEntity.FD_TAOZIENA).equal(true);
        if (fieldList != null && !fieldList.isEmpty())
            query.retrievedFields(true, fieldList.toArray(new String[fieldList.size()]));
        query.offset(page * pageSize).limit(pageSize);
        query.order("-updateTime");
        return query.asList();
    }

    /**
     * 更新行程单
     *
     * @param guideId
     * @param guide
     * @throws exception.AizouException
     */
    public static void updateGuide(ObjectId guideId, Guide guide, Integer userId) throws AizouException {
        Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.GUIDE);
        Query<Guide> query = ds.createQuery(Guide.class).field("id").equal(guideId).field("userId").equal(userId);
        if (query.iterator().hasNext()) {
            UpdateOperations<Guide> update = ds.createUpdateOperations(Guide.class);
            if (guide.itinerary != null) {
                fillPOIType(guide.itinerary);
                update.set(AbstractGuide.fnItinerary, guide.itinerary);
            }
            if (guide.itineraryDays != null)
                update.set(Guide.fnItineraryDays, guide.itineraryDays);
            if (guide.shopping != null)
                update.set(AbstractGuide.fnShopping, guide.shopping);
            if (guide.restaurant != null)
                update.set(AbstractGuide.fnRestaurant, guide.restaurant);
            if (guide.images != null)
                update.set(AbstractGuide.fnImages, guide.images);
            if (guide.title != null)
                update.set(Guide.fnTitle, guide.title);
            update.set(Guide.fnUpdateTime, System.currentTimeMillis());

            ds.update(query, update);
        } else
            throw new AizouException(ErrorCode.INVALID_ARGUMENT, String.format("User %s has no guide which id is %s.", userId, guideId));

    }

    /**
     * 判断并添加poi类型
     *
     * @param itinerary
     */
    private static void fillPOIType(List<ItinerItem> itinerary) {
        AbstractPOI poi;
        for (ItinerItem item : itinerary) {
            poi = item.poi;
            if (poi instanceof ViewSpot)
                item.poi.type = "vs";
            else if (poi instanceof Restaurant)
                item.poi.type = "restaurant";
            else if (poi instanceof Shopping)
                item.poi.type = "shopping";
            else if (poi instanceof Hotel)
                item.poi.type = "hotel";
        }
    }

    /**
     * 通过id返回Guide详情
     *
     * @param id
     * @param list
     * @return
     * @throws exception.AizouException
     */
    public static Guide getGuideInfo(ObjectId id, List<String> list) throws AizouException {
        Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.GUIDE);
        Query<Guide> query = ds.createQuery(Guide.class).field("_id").equal(id);
        if (list != null && !list.isEmpty()) {
            query.retrievedFields(true, list.toArray(new String[list.size()]));
        }
        return query.get();
    }

    /**
     * 保存攻略标题
     *
     * @param id
     * @param title
     * @throws exception.AizouException
     */
    public static void saveGuideTitle(ObjectId id, String title) throws AizouException {
        Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.GUIDE);
        UpdateOperations<Guide> uo = ds.createUpdateOperations(Guide.class);
        uo.set(Guide.fnTitle, title);
        uo.set(Guide.fnUpdateTime, System.currentTimeMillis());
        ds.update(ds.createQuery(Guide.class).field("_id").equal(id), uo);
    }

    /**
     * 保存攻略
     *
     * @throws exception.AizouException
     */
    public static void saveGuideByUser(Guide guide, Integer userId) throws AizouException {
        Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.GUIDE);
        guide.setId(new ObjectId());
        guide.userId = userId;
        guide.updateTime = System.currentTimeMillis();
        ds.save(guide);
    }

    /**
     * 获得目的地的攻略信息
     *
     * @param id
     * @return
     * @throws exception.AizouException
     */
    public static Locality getLocalityGuideInfo(ObjectId id) throws AizouException {
        Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.GEO);
        Query<Locality> query = ds.createQuery(Locality.class);
        query.field("id").equal(id);
        query.retrievedFields(true, Locality.fnDinningIntro, Locality.fnShoppingIntro);
        return query.get();
    }

    /**
     * @return
     */
    public static Guide fillGuideInfo(Guide guide) throws AizouException {
        if (guide == null)
            return new Guide();
        AbstractPOI poi;
        String type;
        List<ItinerItem> itinerary = guide.itinerary;
        List<ItinerItem> newItinerary = new ArrayList<>();
        List<Shopping> shopping = guide.shopping;
        List<Restaurant> restaurant = guide.restaurant;

        ObjectId tempId;
        List<ObjectId> vsIdList = new ArrayList<>();
        List<ObjectId> hotelIdList = new ArrayList<>();
        List<ObjectId> restaurantIdList = new ArrayList<>();
        List<ObjectId> shoppingIdList = new ArrayList<>();

        // 按照类型取得行程单中POI的ID
        if (itinerary != null && itinerary.size() > 0) {
            for (ItinerItem temp : itinerary) {
                type = temp.poi.type;
                tempId = temp.poi.getId();
                if (type == null)
                    continue;
                switch (type) {
                    case "vs":
                        vsIdList.add(tempId);
                        break;
                    case "hotel":
                        hotelIdList.add(tempId);
                        break;
                    case "restaurant":
                        restaurantIdList.add(tempId);
                        break;
                    case "shopping":
                        shoppingIdList.add(tempId);
                        break;
                    default:
                        throw new AizouException(ErrorCode.INVALID_ARGUMENT, String.format("Invalid POI type: %s.", type));
                }

            }
        }

        // 按类型查询POI，并放入Map中
        List<ViewSpot> vsTempList = (List<ViewSpot>) PoiAPI.getPOIInfoList(vsIdList, "vs", null, Constants.ZERO_COUNT, Constants.MAX_COUNT);
        List<Restaurant> resTempList = (List<Restaurant>) PoiAPI.getPOIInfoList(restaurantIdList, "restaurant", null, Constants.ZERO_COUNT, Constants.MAX_COUNT);
        List<Shopping> shopTempList = (List<Shopping>) PoiAPI.getPOIInfoList(shoppingIdList, "shopping", null, Constants.ZERO_COUNT, Constants.MAX_COUNT);
        List<Hotel> hotelTempList = (List<Hotel>) PoiAPI.getPOIInfoList(hotelIdList, "hotel", null, Constants.ZERO_COUNT, Constants.MAX_COUNT);

        // 查询行程单中所有poi的评论
        List<ObjectId> poiIdList = new ArrayList<>();
        poiIdList.addAll(vsIdList);
        poiIdList.addAll(hotelIdList);
        poiIdList.addAll(restaurantIdList);
        poiIdList.addAll(shoppingIdList);
        List<Comment> commentsEntities = PoiAPI.getPOICommentByList(poiIdList, 0, 1);
        transformCommetnListToMap(commentsEntities, vsTempList, resTempList, shopTempList, hotelTempList);

        //取得行程单中的ID-Entity Map
        Map<ObjectId, ViewSpot> vsMap = (Map<ObjectId, ViewSpot>) transformPoiListToMap(vsTempList);
        Map<ObjectId, Restaurant> restaurantMap = (Map<ObjectId, Restaurant>) transformPoiListToMap(resTempList);
        Map<ObjectId, Shopping> shoppingMap = (Map<ObjectId, Shopping>) transformPoiListToMap(shopTempList);
        Map<ObjectId, Hotel> hotelMap = (Map<ObjectId, Hotel>) transformPoiListToMap(hotelTempList);

        // 填充行程单中的内容
        if (itinerary != null && itinerary.size() > 0) {
            for (ItinerItem temp : itinerary) {
                type = temp.poi.type;
                if (type == null)
                    continue;
                switch (type) {
                    case "vs":
                        poi = vsMap.get(temp.poi.getId());
                        break;
                    case "hotel":
                        poi = hotelMap.get(temp.poi.getId());
                        break;
                    case "restaurant":
                        poi = restaurantMap.get(temp.poi.getId());
                        break;
                    case "shopping":
                        poi = shoppingMap.get(temp.poi.getId());
                        break;
                    default:
                        throw new AizouException(ErrorCode.INVALID_ARGUMENT, String.format("Invalid POI type: %s.", type));
                }
                if (poi == null) {
                    LogUtils.info(GuideAPI.class, String.format("POI is not exist.Id: %s, Type: %s.", temp.poi.getId().toString(), type));
                    continue;
                } else {
                    temp.poi = poi;
                    if (poi.images != null && (!poi.images.isEmpty())) {
                        temp.poi.images = Arrays.asList(poi.images.get(0));
                    }
                    newItinerary.add(temp);
                }
            }
            guide.itinerary = newItinerary;
        } else
            guide.itinerary = new ArrayList<>();

        List<ObjectId> ids;
        // 填充购物单中的内容
        if (shopping != null && shopping.size() > 0) {
            ids = new ArrayList();
            for (Shopping temp : shopping) {
                ids.add(temp.getId());
            }
            List<Shopping> shop = (List<Shopping>) PoiAPI.getPOIInfoList(ids, "shopping", null, Constants.ZERO_COUNT, Constants.MAX_COUNT);

            List<Comment> commentsEntitiesSh = PoiAPI.getPOICommentByList(ids, 0, 1);
            transformCommetnListToMap(commentsEntitiesSh, shop);

            Map<ObjectId, Shopping> shopMap = new HashMap<>();
            for (Shopping temp : shop) {
                shopMap.put(temp.getId(), temp);
            }
            List<Shopping> newShop = new ArrayList();
            Shopping sTemp;
            for (Shopping temp : guide.shopping) {
                sTemp = shopMap.get(temp.getId());
                if (sTemp.images != null && (!sTemp.images.isEmpty())) {
                    sTemp.images = Arrays.asList(sTemp.images.get(0));
                }
                if (sTemp != null) {
                    newShop.add(sTemp);
                }
            }
            guide.shopping = newShop;
        } else
            guide.shopping = new ArrayList<>();

        // 填充美食单中的内容
        if (restaurant != null && restaurant.size() > 0) {
            ids = new ArrayList();
            for (Restaurant temp : restaurant) {
                ids.add(temp.getId());
            }
            List<Restaurant> res = (List<Restaurant>) PoiAPI.getPOIInfoList(ids, "restaurant", null, Constants.ZERO_COUNT, Constants.MAX_COUNT);

            List<Comment> commentsEntitiesSh = PoiAPI.getPOICommentByList(ids, 0, 1);
            transformCommetnListToMap(commentsEntitiesSh, res);

            Map<ObjectId, Restaurant> resMap = new HashMap<>();
            for (Restaurant temp : res) {
                resMap.put(temp.getId(), temp);
            }
            List<Restaurant> newRes = new ArrayList();
            Restaurant rTemp;
            for (Restaurant temp : guide.restaurant) {
                rTemp = resMap.get(temp.getId());
                if (rTemp.images != null && (!rTemp.images.isEmpty())) {
                    rTemp.images = Arrays.asList(rTemp.images.get(0));
                }
                if (rTemp != null) {
                    newRes.add(rTemp);
                }
            }
            guide.restaurant = newRes;
        } else
            guide.restaurant = new ArrayList<>();
        return guide;
    }

    private static Map<ObjectId, ? extends AbstractPOI> transformPoiListToMap(List<? extends AbstractPOI> list) {
        Map<ObjectId, AbstractPOI> result = new HashMap<>();
        if (list == null || list.isEmpty())
            return result;
        for (AbstractPOI temp : list) {
            result.put(temp.getId(), temp);
        }
        return result;
    }

    private static void transformCommetnListToMap(List<Comment> list, List<? extends AbstractPOI>... poiList) {
        Map<ObjectId, List<Comment>> result = new HashMap<>();
        List<Comment> value;

        for (Comment temp : list) {
            value = result.get(temp.getItemId());
            if (value == null)
                value = new ArrayList();
            value.add(temp);
            result.put(temp.getItemId(), value);
        }
        List<Comment> comments;
        for (int i = 0; i < poiList.length; i++) {
            for (AbstractPOI poi : poiList[i]) {
                comments = poi.getComments();
                if (comments == null)
                    comments = new ArrayList();
                if (result.get(poi.getId()) != null)
                    comments.addAll(result.get(poi.getId()));
                poi.setComments(comments);
            }
        }
    }

}
