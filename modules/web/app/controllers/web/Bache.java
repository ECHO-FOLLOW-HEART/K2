package controllers.web;

import exception.AizouException;
import exception.ErrorCode;
import models.MorphiaFactory;
import models.geo.Locality;
import models.misc.Recommendation;
import models.plan.AbstractPlan;
import models.plan.Plan;
import models.poi.Comment;
import models.user.UserInfo;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.CriteriaContainerImpl;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;
import play.Configuration;
import play.mvc.Controller;
import play.mvc.Result;
import utils.Utils;

import java.util.*;

/**
 * Created by topy on 2014/8/3.
 */
public class Bache extends Controller {

    public static String[] cap = new String[]{"北京市", "天津市", "上海市", "重庆市", "哈尔滨市",
            "长春市", "沈阳市", "呼和浩特市", "石家庄市", "乌鲁木齐市", "兰州市", "西宁市",
            "西安市", "银川市", "郑州市", "济南市", "太原市", "合肥市", "武汉市", "长沙市",
            "南京市", "成都市", "贵阳市", "昆明市", "南宁市", "拉萨市", "杭州市", "南昌市",
            "广州市", "福州市", "台北市", "海口市"};
    public static String[] vsList = new String[]{"火石寨", "黄梁梦吕仙祠", "景洪曼听公园", "中国竹艺城", "神木臭柏自然保护区",
            "寒山寺", "罗锅箐―大羊场", "景洪曼听公园", "大连星海国际会展中心", "兴光朝鲜族民族村", "梅城故城址", "布托湖", "朗豪坊商场", "高岭山", "蒲花暗河景区", "石象寺"};
    public static String[] plListNew = new String[]{"高句丽云峰湖之旅", "桂林激情之旅", "别样武汉走透透", "老上海徒步路线六", "苏杭天堂自由行"};
    public static String[] plListEdit = new String[]{"厦门", "三亚", "海口", "哈尔滨", "大连", "杭州", "苏州", "平遥", "陕西"};
    public static String[] plListMust = new String[]{"北京", "上海", "广州", "深圳", "成都", "桂林", "天津", "西藏"};
    public static String[] plListPopular = new String[]{"张家界", "福建", "新疆", "山东", "成都", "太原"};
    public static String[] EDITOR_AVATAR = new String[]{"http://q.qlogo.cn/qqapp/1101717903/F4CE6A45B977464B9EB28EA856024170/100",
            "http://tp1.sinaimg.cn/1449136544/180/5700214805/0", "http://tp2.sinaimg.cn/1988161053/180/5649844519/1",
            "http://tp2.sinaimg.cn/1350968733/180/5622387392/1"};

    public static Result addRecPlans(int plType) {
        List<String> capList = new ArrayList<>();

        Class cls = Plan.class;
        int manualIndex = 1;
        switch (plType) {
            case 1:
                manualIndex = 1;
                capList = Arrays.asList(plListNew);
                break;
            case 2:
                manualIndex = 2;
                capList = Arrays.asList(plListEdit);
                break;
            case 3:
                manualIndex = 3;
                capList = Arrays.asList(plListMust);
                break;
            case 4:
                manualIndex = 4;
                capList = Arrays.asList(plListPopular);
                break;
        }
        Datastore ds;
        try {

            ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.PLAN);

            List<ObjectId> cityIds = getHotCities(capList);

            List<UserInfo> userInfos = getAvatars(0,60);

            Query<Plan> querySrc = ds.createQuery(Plan.class);
            querySrc.field("desc").notEqual(null).field("desc").notEqual("")
                    .field("days").greaterThan(3)
                    .field("images").notEqual(null).field("enabled").equal(Boolean.TRUE);

            List<CriteriaContainerImpl> criList = new ArrayList<>();
            for (ObjectId tempId : cityIds) {
                criList.add(querySrc.criteria(String.format("%s.id", AbstractPlan.FD_TARGETS)).hasThisOne(tempId));
            }
            querySrc.or(criList.toArray(new CriteriaContainerImpl[criList.size()]));

            querySrc.order("-forkedCnt").limit(6);

            List<Plan> titleList = querySrc.asList();
            List<Recommendation> recommendList = new ArrayList<>();
            Recommendation rec;
            int index = 1;
            for (Plan pl : titleList) {
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
                int number = new Random().nextInt(3) + 1;
                index++;
                rec.imageList = null;
                rec.images = pl.getImages();
                rec.setId(pl.getId());
                rec.name = pl.getTitle();
                rec.editorNickName = userInfos.get(index).getNickName();
//                rec.editorAvatar = String.format("http://images.taozilvxing.com/%s", userInfos.get(index).getAvatar());
                rec.editorAvatar = userInfos.get(index).getAvatar();
                rec.desc = pl.getDesc();
                rec.editorDate = new Date();
                rec.planViews = pl.getForkedCnt();
                rec.setEnabled(true);
                recommendList.add(rec);
            }
            Datastore update = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.MISC);
            update.save(recommendList);
        } catch (AizouException e) {
            return Utils.createResponse(e.getErrCode(), e.getMessage());
        }
        return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, "Success");
    }

    public static List<ObjectId> getHotCities(List<String> cityNames) throws AizouException {
        Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.GEO);
        Query<Locality> query = ds.createQuery(Locality.class);
        List<CriteriaContainerImpl> criList = new ArrayList<>();
        for (String city : cityNames) {
            criList.add(query.criteria("zhName").equal(city));
        }
        query.or(criList.toArray(new CriteriaContainerImpl[criList.size()]));
        List<Locality> localities = query.asList();
        List<ObjectId> result = new ArrayList();
        for (Locality locality : localities)
            result.add(locality.getId());
        return result;
    }

    public static List<UserInfo> getAvatars(int page, int pageSize) throws AizouException {
        Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.MISC);
        Query<Comment> query = ds.createQuery(Comment.class);
//        List<CriteriaContainerImpl> criList = new ArrayList<>();
//        for (String city : cityNames) {
//            criList.add(query.criteria("zhName").equal(city));
//        }
//        query.or(criList.toArray(new CriteriaContainerImpl[criList.size()]));
        query.field("authorAvatar").notEqual(null).field("authorName").notEqual(null);
        query.offset(page * pageSize).limit(pageSize);
        List<Comment> comments = query.asList();
        List<UserInfo> result = new ArrayList();
        UserInfo tempInfo;
        for (Comment comment : comments) {
            tempInfo = new UserInfo();
            tempInfo.setAvatar(comment.getAuthorAvatar());
            tempInfo.setNickName(comment.getAuthorName());
            result.add(tempInfo);
        }
        return result;
    }
//    /**
//     * Need
//     * 计算旅行计划模板的预算，并入库
//     *
//     * @return
//     */
//    public static Result getPlanBudget(String depId, String arrId) {
//        Double VIEWPOINT_DEFAULT_PRICE = 0d;
//
//        //取得景点预算常量
//        Configuration config = Configuration.root();
//        Map budget = (Map) config.getObject("budget");
//        try {
//            if (budget != null) {
//                VIEWPOINT_DEFAULT_PRICE = Double.valueOf(budget.get("viewpointBudgetDefault").toString());
//            }
//        } catch (ClassCastException e) {
//            return Utils.createResponse(ErrorCode.INVALID_CONFARG, Json.newObject());
//        }
//
//        try {
//            Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.PLAN);
//            Query<Plan> query = ds.createQuery(Plan.class);
//            UpdateOperations<Plan> update = ds.createUpdateOperations(Plan.class);
//
//            UpdateOperations<Plan> ops = ds.createUpdateOperations(Plan.class);
//
//            Query<Plan> queryStay = ds.createQuery(Plan.class);
//            UpdateOperations<Plan> opsStay = ds.createUpdateOperations(Plan.class);
//
//            ObjectId tempObjectId = null;
//            Plan tempPlan = null;
//            List<PlanDayEntry> tempDetails = null;
//            List<PlanItem> actvs = null;
//            List<Double> vsPriceList = new ArrayList<Double>(5000);
//            //得到景点Id-景点价格Map
//            // Map<ObjectId, Double> iD_Price_Map = getVsPriceById();
//            Map<ObjectId, Double> iD_Price_Map = new HashMap<ObjectId, Double>();
//            Double tempPrice = 0d;
//            Double totalPrice = 0d;
//            int days = 0;
//            query.field("enabled").equal(Boolean.TRUE);
//            for (Iterator<Plan> it = query.iterator(); it.hasNext(); ) {
//                tempPlan = (Plan) it.next();
//                tempDetails = (List<PlanDayEntry>) tempPlan.details;
//                days = tempPlan.days;
//                actvs = new ArrayList<PlanItem>(10);
//                totalPrice = 0d;
//                if (null != tempDetails) {
//                    // 遍历details
//                    for (PlanDayEntry entry : tempDetails) {
//                        actvs = entry.actv;
//                        // 遍历Acts
//                        for (PlanItem item : actvs) {
//                            if (item.type.equals("vs")) {
//                                //得到景点Id
//                                tempObjectId = item.item.id;
//                                if (iD_Price_Map.get(tempObjectId) == null) {
//                                    iD_Price_Map.putAll(getVsPriceById(tempObjectId));
//                                }
//                                tempPrice = iD_Price_Map.get(tempObjectId);
//                                if (null != tempPrice) {
//                                    totalPrice = totalPrice + tempPrice;
//                                } else {
//                                    totalPrice = totalPrice + VIEWPOINT_DEFAULT_PRICE;
//                                }
//                            }
//                        }
//                    }
//                }
//                ops = ds.createUpdateOperations(Plan.class);
//                ops.set("viewBudget", totalPrice);
//                ds.update(query, ops, true);
//            }
//        } catch (TravelPiException e) {
//            return Utils.createResponse(ErrorCode.DATABASE_ERROR, Json.newObject());
//        }
//        return Utils.createResponse(ErrorCode.NORMAL, Json.newObject());
//    }

    /**
     * 获得交通预算
     *
     * @return
     */
    public static Double getTrafficBudget(String depId, String arrId) throws AizouException {
        ObjectId depOid, arrOid;
        try {
            depOid = new ObjectId(depId);
            arrOid = new ObjectId(arrId);
        } catch (IllegalArgumentException e) {
            throw new AizouException(ErrorCode.INVALID_ARGUMENT, "Invalid locality ID.");
        }

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
            Locality depLoc;
            Locality arrLoc;
            int kmMount = 0;
            Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.GEO);
            Query<Locality> query = ds.createQuery(Locality.class);
            Query<Locality> query1 = ds.createQuery(Locality.class);
            depLoc = query.field("_id").equal(depOid).get();
            arrLoc = query1.field("_id").equal(arrOid).get();

            if (null != depLoc && null != arrLoc && depLoc.getLocation() != null && arrLoc.getLocation() != null) {
                kmMount = Utils.getDistatce(depLoc.getLocation().getCoordinates()[1],
                        arrLoc.getLocation().getCoordinates()[1],
                        depLoc.getLocation().getCoordinates()[0],
                        arrLoc.getLocation().getCoordinates()[0]);
            }
            trafficBudget = kmMount * trafficRatio;
        }
        return trafficBudget;
    }

//    /**
//     * 生成景点价格
//     *
//     * @param id
//     * @return
//     * @throws TravelPiException
//     */
//    private static Map<ObjectId, Double> getVsPriceById(ObjectId id) throws TravelPiException {
//
//        Map<ObjectId, Double> mapPrice = new HashMap<ObjectId, Double>(5000);
//
//        Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.POI);
//        Query<ViewSpot> query = ds.createQuery(ViewSpot.class);
//        query.field("_id").equal(id);
//        ViewSpot viewSpotTemp = null;
//        for (Iterator<ViewSpot> it = query.iterator(); it.hasNext(); ) {
//            viewSpotTemp = (ViewSpot) it.next();
//            mapPrice.put(viewSpotTemp.id, viewSpotTemp.price);
//        }
//        return mapPrice;
//    }
//
//    /**
//     * 设置省会城市
//     *
//     * @return
//     */
//    public static Result updateLocalityProvCap() {
//
//        List<String> capList = Arrays.asList(cap);
//        try {
//            Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.GEO);
//            Query<Locality> query = ds.createQuery(Locality.class);
//            query.field("zhName").hasAnyOf(capList).field("level").equal(2);
//            Locality locality = null;
//            for (Iterator<Locality> it = query.iterator(); it.hasNext(); ) {
//                locality = (Locality) it.next();
//                locality.provCap = true;
//                ds.save(locality);
//            }
//        } catch (TravelPiException e) {
//            return Utils.createResponse(e.getErrCode(), e.getMessage());
//        }
//        return Utils.createResponse(ErrorCode.NORMAL, "Success");
//    }
//
//    /**
//     * 添加推荐城市
//     *
//     * @return
//     */
//    public static Result getLocalities() {
//        List<String> capList = Arrays.asList(cap);
//        Datastore ds = null;
//        try {
//            ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.GEO);
//            Query<Locality> query = ds.createQuery(Locality.class);
//            query.field("zhName").hasAnyOf(capList).field("level").equal(2).field("enabled").equal(Boolean.TRUE);
//            List<Recommendation> recommendList = new ArrayList<Recommendation>();
//            Recommendation rec;
//            int index = 1;
//            Description descp = null;
//            for (Locality locality : query.asList()) {
//                rec = new Recommendation();
//                rec.hotCity = index;
//                index++;
//                rec.imageList = locality.imageList;
//                rec.id = locality.id;
//                rec.name = locality.zhName;
//                descp = new Description();
//                descp.desc = locality.desc;
//                rec.description = descp;
//                rec.enabled = true;
//                recommendList.add(rec);
//            }
//            Datastore update = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.MISC);
//            update.save(recommendList);
//
//
//        } catch (TravelPiException e) {
//            return Utils.createResponse(e.getErrCode(), e.getMessage());
//        }
//
//        return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, "Success");
//    }
//
//    /**
//     * 添加推荐景点
//     *
//     * @return
//     */
//    public static Result getViewSpot() {
//        List<String> capList = Arrays.asList(vsList);
//        Datastore ds = null;
//        try {
//            ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.POI);
//            Query<ViewSpot> query = ds.createQuery(ViewSpot.class);
//            query.field("name").hasAnyOf(capList).field("enabled").equal(Boolean.TRUE);
//            List<Recommendation> recommendList = new ArrayList<Recommendation>();
//            Recommendation rec;
//            int index = 1;
//            for (ViewSpot vs : query.asList()) {
//                rec = new Recommendation();
//                rec.hotVs = index;
//                index++;
//                rec.imageList = vs.imageList;
//                rec.images = vs.images;
//                rec.id = vs.id;
//                rec.name = vs.name;
//                rec.description = vs.description;
//                rec.enabled = true;
//                recommendList.add(rec);
//            }
//            Datastore update = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.MISC);
//            update.save(recommendList);
//
//
//        } catch (TravelPiException e) {
//            return Utils.createResponse(e.getErrCode(), e.getMessage());
//        }
//
//        return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, "Success");
//    }
//

//    /**
//     * 添加推荐计划
//     *
//     * @param plType
//     * @return
//     */
//    public static Result getRecPlans(int plType) {
//        List<String> capList = Arrays.asList(plListNew);
//
//        Class cls = Plan.class;
//        int manualIndex = 1;
//        switch (plType) {
//            case 1:
//                manualIndex = 1;
//                capList = Arrays.asList(plListNew);
//                break;
//            case 2:
//                manualIndex = 2;
//                capList = Arrays.asList(plListEdit);
//                break;
//            case 3:
//                manualIndex = 3;
//                capList = Arrays.asList(plListMust);
//                break;
//            case 4:
//                manualIndex = 4;
//                capList = Arrays.asList(plListPopular);
//                break;
//        }
//        Datastore ds = null;
//        try {
//
//            ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.PLAN);
//
//
//            Query<Plan> querySrc = ds.createQuery(Plan.class);
//            querySrc.field("manualPriority").equal(manualIndex).field("desc").notEqual(null)
//                    .field("images").notEqual(null).field("enabled").equal(Boolean.TRUE);
//
//            List<Plan> titleList = querySrc.asList();
//            List<Recommendation> recommendList = new ArrayList<Recommendation>();
//            Recommendation rec;
//            int index = 1;
//            for (Plan pl : titleList) {
//                rec = new Recommendation();
//                switch (plType) {
//                    case 1:
//                        rec.newItemWeight = index;
//                        break;
//                    case 2:
//                        rec.editorWeight = index;
//                        break;
//                    case 3:
//                        rec.mustGoWeight = index;
//                        break;
//                    case 4:
//                        rec.popularityWeight = index;
//                        break;
//                }
//                int number = new Random().nextInt(3) + 1;
//                index++;
//                rec.imageList = null;
//                rec.images = pl.getImages();
//                rec.setId(pl.getId());
//                rec.name = pl.getTitle();
//                rec.editorNickName = EDITOR_NICKNAME[number];
//                rec.editorAvatar = EDITOR_AVATAR[number];
//                rec.description = pl.getDescription();
//                rec.editorDate = new Date();
//                rec.planViews = 1000 + new Random().nextInt(1000);
//                rec.setEnabled(true);
//                recommendList.add(rec);
//            }
//            Datastore update = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.MISC);
//
//            update.save(recommendList);
//
//
//        } catch (AizouException e) {
//            return Utils.createResponse(e.getErrCode(), e.getMessage());
//        }
//
//        return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, "Success");
//    }

    public static Result changeId(int plType) {
        String field = null;
        switch (plType) {
            case 1:
                field = "mustGoWeight";
                break;
            case 2:
                field = "popularityWeight";
                break;
            case 3:
                field = "newItemWeight";
                break;
            case 4:
                field = "editorWeight";
                break;
        }
        Datastore ds = null;
        Datastore planDs = null;

        Plan newPlan;
        try {
            ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.MISC);
            UpdateOperations<Recommendation> update = ds.createUpdateOperations(Recommendation.class);
            Query<Recommendation> queryRec = ds.createQuery(Recommendation.class);
            List<Recommendation> recs = queryRec.field(field).notEqual(null).asList();

            planDs = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.PLAN);
            Query<Plan> querySrc = planDs.createQuery(Plan.class);

            for (Recommendation rec : recs) {
                newPlan = querySrc.field("title").equal(rec.name).get();
                if (newPlan == null) {
                    System.out.println(rec.name);
                    continue;
                }
                ds.delete(rec);
                //update.set("_id",newPlan.getId());
            }
        } catch (AizouException e) {
            return Utils.createResponse(e.getErrCode(), e.getMessage());
        }

        return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, "Success");
    }
//
//    /**
//     * 在路线中标识路线所属的省，支持根据省会查询接口
//     *
//     * @return
//     */
//    public static Result addProToPlan() {
//
//
//        try {
//            Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.PLAN);
//            Query<Plan> querySrc = ds.createQuery(Plan.class);
//
//            Plan plan;
//            List<SimpleRef> sims;
//            Locality cap;
//            SimpleRef capRef;
//            HashMap<String, SimpleRef> capMap;
//            for (Iterator it = querySrc.iterator(); it.hasNext(); ) {
//                capMap = new HashMap<>();
//                plan = (Plan) it.next();
//                sims = plan.targets;
//                if (null == sims)
//                    continue;
//                for (SimpleRef refs : sims) {
//                    cap = findCap(refs.id);
//                    if (cap == null)
//                        continue;
//                    capRef = new SimpleRef();
//                    capRef.id = cap.id;
//                    capRef.zhName = cap.zhName;
//                    capMap.put(capRef.id.toString(), capRef);
//                }
//                for (SimpleRef capTemp : capMap.values()) {
//                    sims.add(capTemp);
//                }
//                plan.targets = sims;
//                ds.save(plan);
//            }
//
//        } catch (TravelPiException e) {
//            return Utils.createResponse(e.getErrCode(), e.getMessage());
//        }
//
//        return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, "Success");
//
//    }
//
//    /**
//     * 查找一个城市所在省的省会
//     *
//     * @param oid
//     * @return
//     */
//    private static Locality findCap(ObjectId oid) {
//
//        Datastore ds;
//        try {
//            ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.GEO);
//
//            Query<Locality> querySrc = ds.createQuery(Locality.class);
//
//            Locality locality = querySrc.field("_id").equal(oid).get();
//            if (locality.level == 1) {
//                return locality;
//            } else {
//                ObjectId superId = locality.superAdm.id;
//                querySrc = ds.createQuery(Locality.class);
//                Locality sLocality = querySrc.field("_id").equal(superId).get();
//                if (sLocality.level == 1) {
//                    return sLocality;
//                } else {
//                    ObjectId sSuperId = sLocality.superAdm.id;
//                    querySrc = ds.createQuery(Locality.class);
//                    Locality sSLocality = querySrc.field("_id").equal(sSuperId).get();
//
//                    if (sSLocality.level == 1) {
//                        return sSLocality;
//                    } else {
//                        return null;
//                    }
//                }
//            }
//        } catch (TravelPiException e) {
//            return null;
//        }
//    }
//
//    /**
//     * 设置自增序列，用于记录用户自增ID
//     */
//    public static Result addUserIdSequence() {
//        try {
//            Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.MISC);
//            Sequence info = new Sequence();
//            info.column = Sequence.USERID;
//            info.count = 100000;
//            ds.save(info);
//        } catch (TravelPiException e) {
//            return Utils.createResponse(e.getErrCode(), e.getMessage());
//        }
//
//        return Utils.createResponse(ErrorCode.NORMAL, "Success");
//    }

}
