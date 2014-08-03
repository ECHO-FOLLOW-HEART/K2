package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import core.PlanAPI;
import core.TrafficAPI;
import exception.ErrorCode;
import exception.TravelPiException;
import models.MorphiaFactory;
import models.morphia.geo.Coords;
import models.morphia.geo.Locality;
import models.morphia.plan.Plan;
import models.morphia.plan.PlanDayEntry;
import models.morphia.plan.PlanItem;
import models.morphia.poi.ViewSpot;
import models.morphia.traffic.TrainRoute;
import models.morphia.traffic.TrainRouteIterator;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import utils.Utils;

import java.util.*;

/**
 * Created by topy on 2014/8/3.
 */
public class Bache extends Controller {

    private static final int VIEWPOINT_MOUNT = 5000;

    private static final int STAY_DEFAULT_PRICE = 200;

    private static final Double VIEWPOINT_DEFAULT_PRICE = 30d;
    /**
     * 获得。
     *
     * @return
     */
    public static Result getPlanBudget(String depId,String arrId){
        int trafficBudget = getTrafficBudget(depId, arrId);
        //int trafficBudget = 0;

        try {
            Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.PLAN);
            Query<Plan> query = ds.createQuery(Plan.class);
            UpdateOperations<Plan> update = ds.createUpdateOperations(Plan.class);

            UpdateOperations<Plan> ops = ds.createUpdateOperations(Plan.class);

            Query<Plan> queryStay = ds.createQuery(Plan.class);
            UpdateOperations<Plan> opsStay = ds.createUpdateOperations(Plan.class);

            ObjectId tempObjectId = null;
            Plan tempPlan = null;
            List<PlanDayEntry> tempDetails = null;
            List<PlanItem> actvs = null;
            List<Double> vsPriceList = new ArrayList<Double>(VIEWPOINT_MOUNT);
            //得到景点Id-景点价格Map
            Map<ObjectId,Double> iD_Price_Map = getVsPriceById();
            Double tempPrice = 0d;
            Double totalPrice = 0d;
            int days = 0;

            for (Iterator<Plan> it = query.iterator(); it.hasNext(); ) {
                tempPlan =(Plan)it.next();
                tempDetails = (List<PlanDayEntry>) tempPlan.details;
                days = tempPlan.days;
                actvs = new ArrayList<PlanItem>(10);
                totalPrice = 0d;
                if(null!=tempDetails) {

                    // 遍历details
                    for (PlanDayEntry entry : tempDetails) {
                        actvs = entry.actv;
                        // 遍历Acts
                        for (PlanItem item : actvs) {
                            if (item.type.equals("vs")) {
                                //得到景点Id
                                tempObjectId = item.item.id;
                                tempPrice = iD_Price_Map.get(tempObjectId);
                                if(null!= tempPrice){
                                    totalPrice = totalPrice+tempPrice;
                                }else{
                                    totalPrice = totalPrice + VIEWPOINT_DEFAULT_PRICE;
                                }
                            }
                        }
                    }
                }
                ops = ds.createUpdateOperations(Plan.class);
                ops.set("viewBudget",totalPrice);
                ops.set("stayBudget",days*STAY_DEFAULT_PRICE);
                ops.set("trafficBudget",trafficBudget);
                ds.update(query, ops, true);
            }

        } catch (TravelPiException e) {
        }
        return Utils.createResponse(ErrorCode.NORMAL, Json.newObject());
    }

    /**
     * 获得。
     *
     * @return
     */
    public static int getTrafficBudget(String depId,String arrId) {

        int trafficBudget = 1;
        int trafficRatio = 10;

        if (null != depId && (!depId.trim().equals(""))
                && null != arrId && (!arrId.trim().equals(""))) try {
            ObjectId depOid = new ObjectId(depId);
            ObjectId arrOid = new ObjectId(arrId);
            Locality depLoc = null;
            Locality arrLoc = null;
            int kmMount = 0;
            Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.GEO);
            Query<Locality> query = ds.createQuery(Locality.class);
            if(null!=depLoc && null!= arrLoc){
                depLoc = query.field("_id").equal(depOid).get();
                arrLoc = query.field("_id").equal(arrOid).get();

            kmMount =  Utils.getDistatce(depLoc.coords.lat, arrLoc.coords.lat, depLoc.coords.lng, arrLoc.coords.lng);
            }

            trafficBudget = kmMount / trafficRatio;

        } catch (TravelPiException e) {
        }
        return trafficBudget;
    }


    private static Map<ObjectId,Double> getVsPriceById(){

        Map<ObjectId,Double> mapPrice = new HashMap<ObjectId,Double>(VIEWPOINT_MOUNT);

        try{

            Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.POI);
            Query<ViewSpot> query = ds.createQuery(ViewSpot.class);
            ViewSpot viewSpotTemp = null;
            for (Iterator<ViewSpot> it = query.iterator(); it.hasNext(); ) {
                viewSpotTemp =(ViewSpot)it.next();
                mapPrice.put(viewSpotTemp.id, viewSpotTemp.price);
            }
        }  catch (TravelPiException e) {
        }
        return mapPrice;
    }

}
