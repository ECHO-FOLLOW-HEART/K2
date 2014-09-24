package controllers;

import exception.ErrorCode;
import exception.TravelPiException;
import models.MorphiaFactory;
import models.morphia.geo.Locality;
import models.morphia.misc.Recommendation;
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


    /**
     * 生成
     *
     * @param id
     * @return
     * @throws TravelPiException
     */
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

    /**
     * 设置省会城市
     *
     * @return
     */
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


    public static void addRecommend() {
        try {
            Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.MISC);
            ds.save();
            Query<Recommendation> query = ds.createQuery(Recommendation.class);
            query.field("");

        } catch (TravelPiException e) {
            Utils.createResponse(e.errCode, e.getMessage());
        }
    }


    public static Result getLocalities() {
        List<String> capList = Arrays.asList(cap);
        Datastore ds = null;
        try {
            ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.GEO);
            Query<Locality> query = ds.createQuery(Locality.class);
            query.field("zhName").hasAnyOf(capList).field("level").equal(2).field("enabled").equal(Boolean.TRUE);
            List<Recommendation> recommendList = new ArrayList<Recommendation>();
            Recommendation rec;
            int index = 1;
            for (Locality locality : query.asList()) {
                rec = new Recommendation();
                rec.hotCity = index;
                index++;
                rec.imageList = locality.imageList;
                rec.id = locality.id;
                rec.name = locality.zhName;
                rec.enabled = true;
                recommendList.add(rec);
            }
            Datastore update = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.MISC);
            update.save(recommendList);


        } catch (TravelPiException e) {
            return Utils.createResponse(e.errCode, e.getMessage());
        }

        return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, "Success");
    }

    public static String[] vsList = new String[]{"火石寨", "黄梁梦吕仙祠", "景洪曼听公园", "中国竹艺城", "神木臭柏自然保护区",
            "寒山寺", "罗锅箐―大羊场"};

    public static Result getViewSpot() {
        List<String> capList = Arrays.asList(vsList);
        Datastore ds = null;
        try {
            ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.POI);
            Query<ViewSpot> query = ds.createQuery(ViewSpot.class);
            query.field("name").hasAnyOf(capList).field("enabled").equal(Boolean.TRUE);
            List<Recommendation> recommendList = new ArrayList<Recommendation>();
            Recommendation rec;
            int index = 1;
            for (ViewSpot vs : query.asList()) {
                rec = new Recommendation();
                rec.hotVs = index;
                index++;
                rec.imageList = vs.imageList;
                rec.images = vs.images;
                rec.id = vs.id;
                rec.name = vs.name;
                rec.enabled = true;
                recommendList.add(rec);
            }
            Datastore update = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.MISC);
            update.save(recommendList);


        } catch (TravelPiException e) {
            return Utils.createResponse(e.errCode, e.getMessage());
        }

        return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, "Success");
    }

    public static String[] plListNew = new String[]{"高句丽云峰湖之旅", "桂林激情之旅", "别样武汉走透透", "老上海徒步路线六", "苏杭天堂自由行"};
    public static String[] plListEdit = new String[]{"穿梭石头古堡间", "桂林激情之旅", "穿越历史之行", "古村风情", "古国森林胜景"};
    public static String[] plListMust = new String[]{"神农架新奇之旅", "神农之上", "神农之上千奇百怪", "桂林激情之旅"};
    public static String[] plListPopular = new String[]{"张家界全景之旅", "张家界休闲游", "桂林激情之旅", "张家界自然氧吧之旅"};
    public static String AVATAR = "http://q.qlogo.cn/qqapp/1101717903/F4CE6A45B977464B9EB28EA856024170/100";

    public static Result getRecPlans(int plType) {
        List<String> capList = Arrays.asList(plListNew);
        Class cls = Plan.class;
        switch (plType) {
            case 1:
                capList = Arrays.asList(plListNew);
                break;
            case 2:
                capList = Arrays.asList(plListEdit);
                break;
            case 3:
                capList = Arrays.asList(plListMust);
                break;
            case 4:
                capList = Arrays.asList(plListPopular);
                break;
        }
        Datastore ds = null;
        try {
            ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.PLAN);
            Query<Plan> query = ds.createQuery(Plan.class);
            query.field("title").hasAnyOf(capList).field("enabled").equal(Boolean.TRUE);
            List<Recommendation> recommendList = new ArrayList<Recommendation>();
            Recommendation rec;
            int index = 1;
            for (Plan pl : query.asList()) {
                rec = new Recommendation();
                switch (plType) {
                    case 1:
                        rec.newItemWeight = index;
                        break;
                    case 2:
                        rec.editorWeight = index;
                        break;
                    case 3:
                        rec.mustGoWeight = index;
                        break;
                    case 4:
                        rec.popularityWeight = index;
                        break;
                }
                index++;
                rec.imageList = pl.imageList;
                rec.images = pl.images;
                rec.id = pl.id;
                rec.name = pl.title;
                rec.editorNickName = "小王";
                rec.editorAvatar = AVATAR;
                rec.enabled = true;
                recommendList.add(rec);
            }
            Datastore update = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.MISC);
            update.save(recommendList);


        } catch (TravelPiException e) {
            return Utils.createResponse(e.errCode, e.getMessage());
        }

        return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, "Success");
    }


}
