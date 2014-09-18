package controllers;

import exception.ErrorCode;
import exception.TravelPiException;
import models.MorphiaFactory;
import models.morphia.geo.Locality;
import models.morphia.plan.Plan;
import models.morphia.plan.PlanDayEntry;
import models.morphia.plan.PlanItem;
import models.morphia.poi.ViewSpot;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;
import play.Configuration;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import utils.Utils;

import java.util.*;

/**
 * Created by topy on 2014/8/3.
 */
public class Bache extends Controller {

    /**
     * 计算旅行计划模板的预算，并入库
     *
     * @return
     */
    public static Result getPlanBudget(String depId, String arrId) {
        Double VIEWPOINT_DEFAULT_PRICE = 0d;

        //取得景点预算常量
        Configuration config = Configuration.root();
        Map budget = (Map) config.getObject("budget");
        try {
            if (budget != null) {
                VIEWPOINT_DEFAULT_PRICE = Double.valueOf(budget.get("viewpointBudgetDefault").toString());
            }
        } catch (ClassCastException e) {
            return Utils.createResponse(ErrorCode.INVALID_CONFARG, Json.newObject());
        }

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
            List<Double> vsPriceList = new ArrayList<Double>(5000);
            //得到景点Id-景点价格Map
            // Map<ObjectId, Double> iD_Price_Map = getVsPriceById();
            Map<ObjectId, Double> iD_Price_Map = new HashMap<ObjectId, Double>();
            Double tempPrice = 0d;
            Double totalPrice = 0d;
            int days = 0;
            query.field("enabled").equal(Boolean.TRUE);
            for (Iterator<Plan> it = query.iterator(); it.hasNext(); ) {
                tempPlan = (Plan) it.next();
                tempDetails = (List<PlanDayEntry>) tempPlan.details;
                days = tempPlan.days;
                actvs = new ArrayList<PlanItem>(10);
                totalPrice = 0d;
                if (null != tempDetails) {
                    // 遍历details
                    for (PlanDayEntry entry : tempDetails) {
                        actvs = entry.actv;
                        // 遍历Acts
                        for (PlanItem item : actvs) {
                            if (item.type.equals("vs")) {
                                //得到景点Id
                                tempObjectId = item.item.id;
                                if (iD_Price_Map.get(tempObjectId) == null) {
                                    iD_Price_Map.putAll(getVsPriceById(tempObjectId));
                                }
                                tempPrice = iD_Price_Map.get(tempObjectId);
                                if (null != tempPrice) {
                                    totalPrice = totalPrice + tempPrice;
                                } else {
                                    totalPrice = totalPrice + VIEWPOINT_DEFAULT_PRICE;
                                }
                            }
                        }
                    }
                }
                ops = ds.createUpdateOperations(Plan.class);
                ops.set("viewBudget", totalPrice);
                ds.update(query, ops, true);
            }
        } catch (TravelPiException e) {
            return Utils.createResponse(ErrorCode.DATABASE_ERROR, Json.newObject());
        }
        return Utils.createResponse(ErrorCode.NORMAL, Json.newObject());
    }

    /**
     * 获得交通预算
     *
     * @return
     */
    public static Double getTrafficBudget(String depId, String arrId) throws TravelPiException {

        //取得交通预算常量
        Configuration config = Configuration.root();
        Map mongo = (Map) config.getObject("budget");
        Double trafficBudget = 0d;
        Double trafficRatio = 0d;
        try {
            if (mongo != null) {
                trafficBudget = Double.valueOf(mongo.get("trafficBudgetDefault").toString());
                trafficRatio = Double.valueOf(mongo.get("trafficBudgetRatio").toString());
            }
        } catch (ClassCastException e) {
            trafficBudget = 0d;
            trafficRatio = 0d;
        }
        // 根据里程数与预算比率，计算得出交通预算
        if (null != depId && (!depId.trim().equals(""))
                && null != arrId && (!arrId.trim().equals(""))) {
            ObjectId depOid = new ObjectId(depId);
            ObjectId arrOid = new ObjectId(arrId);
            Locality depLoc = null;
            Locality arrLoc = null;
            int kmMount = 0;
            Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.GEO);
            Query<Locality> query = ds.createQuery(Locality.class);
            Query<Locality> query1 = ds.createQuery(Locality.class);
            depLoc = query.field("_id").equal(depOid).get();
            arrLoc = query1.field("_id").equal(arrOid).get();
            if (null != depLoc && null != arrLoc) {
                kmMount = Utils.getDistatce(depLoc.coords.lat, arrLoc.coords.lat, depLoc.coords.lng, arrLoc.coords.lng);
            }
            trafficBudget = kmMount * trafficRatio;
        }
        return trafficBudget;
    }


    private static Map<ObjectId, Double> getVsPriceById(ObjectId id) throws TravelPiException {

        Map<ObjectId, Double> mapPrice = new HashMap<ObjectId, Double>(5000);

        Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.POI);
        Query<ViewSpot> query = ds.createQuery(ViewSpot.class);
        query.field("_id").equal(id);
        ViewSpot viewSpotTemp = null;
        for (Iterator<ViewSpot> it = query.iterator(); it.hasNext(); ) {
            viewSpotTemp = (ViewSpot) it.next();
            mapPrice.put(viewSpotTemp.id, viewSpotTemp.price);
        }
        return mapPrice;
    }

    public static Result updateLocalityProvCap() {

        List<String> capList = Arrays.asList(cap);
        try {
            Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.GEO);
            Query<Locality> query = ds.createQuery(Locality.class);
            query.field("zhName").hasAnyOf(capList).field("level").equal(2);
            Locality locality = null;
            UpdateOperations<Locality> ops = null;
            Datastore updateDs = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.PLAN);
            for (Iterator<Locality> it = query.iterator(); it.hasNext(); ) {
                locality = (Locality) it.next();
                locality.provCap = true;
                ds.save(locality);
            }
        } catch (TravelPiException e) {
            return Utils.createResponse(e.errCode, e.getMessage());
        }
        return Utils.createResponse(ErrorCode.NORMAL, "Success");
    }


    public static String[] cap = new String[]{"北京市", "天津市", "上海市", "重庆市", "哈尔滨市",
            "长春市", "沈阳市", "呼和浩特市", "石家庄市", "乌鲁木齐市", "兰州市", "西宁市",
            "西安市", "银川市", "郑州市", "济南市", "太原市", "合肥市", "武汉市", "长沙市",
            "南京市", "成都市", "贵阳市", "昆明市", "南宁市", "拉萨市", "杭州市", "南昌市",
            "广州市", "福州市", "台北市", "海口市"};

}
