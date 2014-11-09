package aizou.core;

import exception.TravelPiException;
import models.MorphiaFactory;
import models.guide.Guide;
import models.poi.Dinning;
import models.poi.Shopping;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by topy on 2014/11/5.
 */
public class GuideAPI {


    /**
     * 通过id返回Guide详情
     *
     * @param id
     * @param list
     * @return
     * @throws TravelPiException
     */
    public static Guide getGuideInfo(ObjectId id, List<String> list) throws TravelPiException {
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
     * @throws TravelPiException
     */
    public static void saveGuideTitle(ObjectId id, String title) throws TravelPiException {
        Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.GUIDE);
        UpdateOperations<Guide> uo = ds.createUpdateOperations(Guide.class);
        uo.set("title", title);
        ds.update(ds.createQuery(Guide.class).field("_id").equal(id), uo);
    }

    /**
     * 判断是添加还是更新
     */
    public static Shopping confirmShoppingOpration(String id,String tyepId) throws TravelPiException {
        if (tyepId==null||tyepId.equals("")){   //用户第一次提交数据
            Shopping shopping=new Shopping();
            shopping.id=new ObjectId();
            return shopping;
        }
        Guide guide=getGuideInfo(new ObjectId(id),Arrays.asList(Guide.FNDINNING,Guide.FNSHOPPING));
        List<Shopping> shoppingList=guide.shopping;
        if (shoppingList.isEmpty()){
            Shopping shopping=new Shopping();
            shopping.id=new ObjectId();
            return shopping;
        }
        else{
            for (Shopping shopping:shoppingList){
                if (shopping.id.equals(new ObjectId(tyepId))){
                    return shopping;
                }
            }
            Shopping shopping=new Shopping();
            shopping.id=new ObjectId();
            return shopping;
        }

    }

    public static Dinning confirmDinningOpration(String id,String tyepId) throws TravelPiException {
        if (tyepId==null||tyepId.equals("")){   //用户第一次提交数据
            Dinning dinning=new Dinning();
            dinning.id=new ObjectId();
            return dinning;
        }
        Guide guide=getGuideInfo(new ObjectId(id),Arrays.asList(Guide.FNDINNING,Guide.FNSHOPPING));
        List<Dinning> dinningList=guide.dinning;
        if (dinningList.isEmpty()){
            Dinning dinning=new Dinning();
            dinning.id=new ObjectId();
            return dinning;
        }
        else{
            for (Dinning dinning:dinningList){
                if (dinning.id.equals(new ObjectId(tyepId))){
                    return dinning;
                }
            }
            Dinning dinning=new Dinning();
            dinning.id=new ObjectId();
            return dinning;
        }

    }
    /**
     * 保存购物信息
     *
     * @param id
     * @param shop
     * @throws TravelPiException
     */
    public static void savaGuideShopping(ObjectId id, Shopping shop) throws TravelPiException {
        Guide guide = getGuideInfo(id, Arrays.asList(Guide.FNSHOPPING));
        List<Shopping> shopping = guide.shopping;
        if (shopping == null) {
            List<Shopping> shoppingList = new ArrayList<>();
            shoppingList.add(shop);
            shopping = shoppingList;
        } else {
            shopping.add(shop);
        }
        Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.GUIDE);
        UpdateOperations<Guide> uo = ds.createUpdateOperations(Guide.class);
        uo.set("shopping", shopping);
        ds.update(ds.createQuery(Guide.class).field("_id").equal(id), uo);
    }

    /**
     * 保存用户的美食攻略
     *
     * @param id
     * @param din
     * @throws TravelPiException
     */
    public static void savaGuideDinning(ObjectId id, Dinning din) throws TravelPiException {
        Guide guide = getGuideInfo(id, Arrays.asList(Guide.FNDINNING));
        List<Dinning> dinning = guide.dinning;
        if (dinning == null) {
            List<Dinning> dinningList = new ArrayList<>();
            dinningList.add(din);
            dinning = dinningList;
        } else {
            dinning.add(din);
        }
        Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.GUIDE);
        UpdateOperations<Guide> uo = ds.createUpdateOperations(Guide.class);
        uo.set("dinning", dinning);
        ds.update(ds.createQuery(Guide.class).field("_id").equal(id), uo);
    }
}
