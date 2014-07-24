package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.QueryBuilder;
import core.PlanAPI;
import core.PlanAPIOld;
import core.PoiAPI;
import exception.ErrorCode;
import exception.TravelPiException;
import models.MorphiaFactory;
import models.morphia.misc.SimpleRef;
import models.morphia.plan.Plan;
import models.morphia.plan.PlanDayEntry;
import models.morphia.plan.PlanItem;
import models.morphia.poi.Hotel;
import models.morphia.poi.ViewSpot;
import models.morphia.traffic.AirRoute;
import models.morphia.traffic.Airport;
import models.morphia.traffic.TrainRoute;
import models.morphia.traffic.TrainStation;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import utils.FPUtils;
import utils.Planner;
import utils.Utils;

import java.net.UnknownHostException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * 路线相关
 *
 * @author Zephyre
 */
public class PlanCtrl extends Controller {


    /**
     * 查询路线的详细信息。
     *
     * @param planId    路线id
     * @param fromLocId 从哪个城市出发
     * @return
     * @throws UnknownHostException
     */
    public static Result getPlanFromTemplates(String planId, String fromLocId, String backLocId, int traffic, int hotel) {
        try {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_YEAR, 3);
            Plan plan = PlanAPI.doPlanner(planId, fromLocId, backLocId, cal);
            JsonNode planJson = plan.toJson();
            fullfill(planJson);


            return Utils.createResponse(ErrorCode.NORMAL, planJson);
        } catch (TravelPiException e) {
            return Utils.createResponse(e.errCode, e.getMessage());
        }
    }

    private static void fullfill(JsonNode planJson) throws TravelPiException {
        // 补全相应信息
        JsonNode details = planJson.get("details");
        if (details != null) {
            boolean isDep = true;
            ObjectNode depItem = null;
            ObjectNode arrItem;
            ObjectNode trafficRoute = null;

            for (JsonNode dayNode : details) {
                if (dayNode == null)
                    continue;
                JsonNode actv = dayNode.get("actv");
                if (actv == null)
                    continue;

                for (JsonNode item : actv) {
                    ObjectNode conItem = (ObjectNode) item;
                    if ("traffic".equals(item.get("type").asText())) {
                        String subType = item.get("subType").asText();

                        if (subType.equals("trainRoute") || subType.equals("airRoute"))
                            trafficRoute = conItem;
                        else {
                            if (isDep) {
                                conItem.put("stopType", "dep");
                                isDep = false;
                                depItem = conItem;
                            } else {
                                conItem.put("stopType", "arr");
                                arrItem = conItem;

                                // 补全交通路线信息
                                if (trafficRoute != null) {
                                    trafficRoute.put("depStop", depItem.get("itemId").asText());
                                    trafficRoute.put("depLoc", depItem.get("locId").asText());
                                    trafficRoute.put("depTime", depItem.get("ts").asText());
                                    trafficRoute.put("arrStop", arrItem.get("itemId").asText());
                                    trafficRoute.put("arrLoc", arrItem.get("locId").asText());
                                    trafficRoute.put("arrTime", arrItem.get("ts").asText());
                                    // TODO 假数据
                                    trafficRoute.put("distance", 1000);
                                }

                                isDep = true;
                                depItem = null;
                                trafficRoute = null;
                            }

                            if ("airport".equals(item.get("subType").asText())) {
                                Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.TRAFFIC);
                                Airport airport = ds.createQuery(Airport.class).field("_id")
                                        .equal(new ObjectId(item.get("itemId").asText())).get();
                                if (airport != null) {
                                    if (airport.addr.coords.lat != null)
                                        conItem.put("lat", airport.addr.coords.lat);
                                    if (airport.addr.coords.lng != null)
                                        conItem.put("lng", airport.addr.coords.lng);
                                    if (airport.addr.coords.blat != null)
                                        conItem.put("blat", airport.addr.coords.blat);
                                    if (airport.addr.coords.blng != null)
                                        conItem.put("blat", airport.addr.coords.blng);
                                }
                            }
                        }
                    } else {
                        String type = item.get("type").asText();
                        if (type != null && (type.equals("vs") || type.equals("hotel") || type.equals("restaurant"))) {
                            // 将POI详情嵌入
                            try {
                                PoiAPI.POIType poiType = null;
                                switch (type) {
                                    case "vs":
                                        poiType = PoiAPI.POIType.VIEW_SPOT;
                                        break;
                                    case "hotel":
                                        poiType = PoiAPI.POIType.HOTEL;
                                        break;
                                    case "restaurant":
                                        poiType = PoiAPI.POIType.RESTAURANT;
                                        break;
                                }
                                if (poiType != null)
                                    conItem.put("details", PoiAPI.getPOIInfo(conItem.get("itemId").asText(),
                                            poiType, true).toJson(2));
                            } catch (TravelPiException ignored) {
                            }
                        }

                    }
                }
            }
        }
    }

    /**
     * 查询路线的详细信息。
     *
     * @param planId    路线id
     * @param fromLocId 从哪个城市出发
     * @return
     * @throws UnknownHostException
     */
    public static Result getPlanFromTemplatesOld(String planId, String fromLocId, String backLocId, int traffic, int hotel) {
        try {
            DBObject plan = PlanAPIOld.getPlanOld(planId);

            DBCollection locCol = Utils.getMongoClient().getDB("geo").getCollection("locality");
            DBObject fromLoc, backLoc;
            try {
                fromLoc = locCol.findOne(QueryBuilder.start("_id").is(new ObjectId(fromLocId)).get());
                if (fromLoc == null)
                    throw new NullPointerException();
                if (backLocId == null || backLocId.isEmpty())
                    backLocId = fromLocId;
                backLoc = locCol.findOne(QueryBuilder.start("_id").is(new ObjectId(backLocId)).get());
                if (backLoc == null)
                    throw new NullPointerException();
                backLocId = backLoc.get("_id").toString();
            } catch (IllegalArgumentException | NullPointerException e) {
                return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, String.format("Invalid plan ID: %s.", planId));
            }

            // 获得基准时间（默认为3天后）
            Calendar calBase = Calendar.getInstance();
            calBase.add(Calendar.DAY_OF_YEAR, 3);
            calBase.set(Calendar.HOUR, 0);
            calBase.set(Calendar.MINUTE, 0);
            calBase.set(Calendar.SECOND, 0);
            calBase.set(Calendar.MILLISECOND, 0);

            DBObject ret1 = Planner.generateUgcPlan(plan, fromLocId, backLocId, calBase);

            return Utils.createResponse(ErrorCode.NORMAL, Utils.bsonToJson(ret1));
        } catch (TravelPiException e) {
            return Utils.createResponse(e.errCode, e.getMessage());
        }

//        ObjectNode ret = Json.newObject();
//        ret.put("_id", plan.get("_id").toString());
//        DBObject loc = (DBObject) plan.get("loc");
//        ret.put("loc", Json.toJson(BasicDBObjectBuilder.start("_id", loc.get("_id").toString())
//                .add("name", loc.get("name").toString()).get()));
//
//        for (String key : new String[]{"target", "title", "tags", "days", "desc", "imageList", "viewCnt"}) {
//            Object tmp = plan.get(key);
//            if (tmp != null)
//                ret.put(key, Json.toJson(tmp));
//        }
//
//        // 获取路线详情
//        BasicDBList detailsList = (BasicDBList) plan.get("details");
//
//
//        // 整个路线详情列表
//        List<JsonNode> detailNodes = new ArrayList<>();
//
//        if (detailsList != null) {
//            int curDay = -1;
//            // 单日路线详情列表（只处理景区）
//            List<JsonNode> detailNodesD = new ArrayList<>();
//
//            // 按照dayIdx和idx的顺序进行排序
//            Collections.sort(detailsList, new Comparator<Object>() {
//                @Override
//                public int compare(Object o1, Object o2) {
//                    int dayIdx1 = (int) ((DBObject) o1).get("dayIdx");
//                    int dayIdx2 = (int) ((DBObject) o2).get("dayIdx");
//                    int idx1 = (int) ((DBObject) o1).get("idx");
//                    int idx2 = (int) ((DBObject) o2).get("idx");
//
//                    int c = dayIdx1 - dayIdx2;
//                    if (c == 0)
//                        c = idx1 - idx2;
//                    return c;
//                }
//            });
//
//            DBCollection vsCol = Utils.getMongoClient().getDB("poi").getCollection("view_spot");
//
//            for (Object aDetailsList : detailsList) {
//                DBObject detailsItem = (DBObject) aDetailsList;
//                DBObject item = (DBObject) detailsItem.get("item");
//                if (!item.get("type").equals("vs"))
//                    continue;
//
//                ObjectNode node = Json.newObject();
//
//                int dayIdx = (int) detailsItem.get("dayIdx");
//                node.put("dayIdx", dayIdx);
//
//                // 景点
//                ObjectNode vsNode = Json.newObject();
//                vsNode.put("_id", item.get("_id").toString());
//                vsNode.put("name", item.get("name").toString());
//                vsNode.put("type", "vs");
//                DBObject vs = vsCol.findOne(QueryBuilder.start("_id").is(item.get("_id")).get());
//                Object tmp = vs.get("tags");
//                if (tmp != null)
//                    vsNode.put("tags", Json.toJson(tmp));
//                tmp = vs.get("intro");
//                if (tmp != null) {
//                    tmp = ((DBObject) tmp).get("desc");
//                    if (tmp != null)
//                        vsNode.put("desc", StringUtils.abbreviate(tmp.toString(), 64));
//                }
//                node.put("item", vsNode);
//
//                DBObject stopLoc = (DBObject) detailsItem.get("loc");
//                ObjectNode locNode = Json.newObject();
//                locNode.put("_id", stopLoc.get("_id").toString());
//                locNode.put("name", stopLoc.get("name").toString());
//                node.put("loc", locNode);
//
//                if (dayIdx != curDay) {
//                    if (!detailNodesD.isEmpty())
//                        detailNodes.add(Json.toJson(detailNodesD));
//                    detailNodesD = new ArrayList<>();
//                }
//                detailNodesD.add(node);
//                curDay = dayIdx;
//            }
//            if (!detailNodesD.isEmpty())
//                detailNodes.add(Json.toJson(detailNodesD));
//        }
//
////        if (traffic != 0) {
////            // TODO 交通的起止时间，需要根据当天的游玩景点而定。
////            // 添加大交通
////            Planner.telomere(detailsList, fromLoc, calBase, true);
////            Planner.telomere(detailsList, backLoc, calBase, false);
////        }
//
//        // 添加每晚住宿
//
//        ret.put("details", Json.toJson(detailNodes));
//
//        return Utils.createResponse(ErrorCode.NORMAL, ret);
    }

    /**
     * 保存用户的路线
     *
     * @param userId
     * @return
     */
    public static Result saveUGCPlan(String userId) throws UnknownHostException, TravelPiException {
//        ObjectNode plan = (ObjectNode) request().body().asJson();
//        DBCollection col = Utils.getMongoClient().getDB("plan").getCollection("plan_info");
//
//        BasicDBObject ugcPlan = new BasicDBObject((java.util.Map) plan);
//
//        String planId;
//        ObjectId planOid, userOid;
//        try {
//            userOid = new ObjectId(userId);
//            DBCollection userCol = Utils.getMongoClient().getDB("user").getCollection("user_info");
//            DBObject user = userCol.findOne(QueryBuilder.start("_id").is(userOid).get(), BasicDBObjectBuilder.start().get());
//            if (user == null)
//                throw new NullPointerException();
//        } catch (IllegalArgumentException | NullPointerException e) {
//            return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, String.format("Invalid user ID: %s.", userId));
//        }
//
//        if (plan.has("_id")) {
//            planId = plan.get("_id").asText();
//            try {
//                planOid = new ObjectId(planId);
//                DBCollection planCol = Utils.getMongoClient().getDB("plan").getCollection("plan_info");
//                DBObject planItem = planCol.findOne(QueryBuilder.start("_id").is(planOid).get(), BasicDBObjectBuilder.start().get());
//                if (planItem == null)
//                    throw new NullPointerException();
//            } catch (IllegalArgumentException | NullPointerException e) {
//                return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, String.format("Invalid plan ID: %s", planId));
//            }
//        } else
//            planOid = ObjectId.get();
//
//
//        return play.mvc.Results.TODO;
        return Utils.createResponse(ErrorCode.NORMAL, "Success");
    }


    /**
     * 路线发现机制
     *
     * @param locId
     * @param sortField
     * @param sort
     * @param tag
     * @param page
     * @param pageSize
     * @return
     */
    public static Result explorePlans(String locId, String poiId, String sortField, String sort, String tag, int page, int pageSize) throws UnknownHostException, TravelPiException {
        List<JsonNode> results = new ArrayList<>();
        for (Iterator<Plan> it = PlanAPI.explore(locId, poiId, sort, tag, page, pageSize, sortField);
             it.hasNext(); )
            results.add(it.next().toJson(false));

        // TODO 预算，以及这里不需要details字段

        return Utils.createResponse(ErrorCode.NORMAL, Json.toJson(results));
    }


    /**
     * 路线发现机制
     *
     * @param locId
     * @param sortField
     * @param sort
     * @param tags
     * @param page
     * @param pageSize
     * @return
     */
    public static Result explorePlansOld(String locId, String poiId, String sortField, String sort, String tags, int page, int pageSize) throws UnknownHostException, TravelPiException {
        List<JsonNode> results = new ArrayList<>();
        for (Object tmp : PlanAPIOld.exploreOld(locId, poiId, sort, tags, page, pageSize, sortField)) {
            DBObject planNode = (DBObject) tmp;
            results.add(PlanAPIOld.getPlanJsonOld(planNode));
        }

        return Utils.createResponse(ErrorCode.NORMAL, Json.toJson(results));
    }


    /**
     * POI映射类
     *
     * @param item
     * @return
     */
    private static PlanItem PoiMapper(JsonNode item) throws TravelPiException {
        String itemId = item.get("itemId").asText();
        String type = item.get("type").asText();
//        String st = item.get("st").asText();
//        String subType = item.get("subType").asText();

        PlanItem planItem = null;
        Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.POI);
        SimpleRef ref;
        switch (type) {
            case "vs":
                ViewSpot vs = ds.createQuery(ViewSpot.class).field("_id").equal(new ObjectId(itemId)).get();
                ref = new SimpleRef();
                ref.id = vs.id;
                ref.zhName = vs.name;
                planItem = new PlanItem();
                planItem.item = ref;
                planItem.loc = vs.addr.loc;
                planItem.type = "vs";
                break;
            case "hotel":
                Hotel hotel = ds.createQuery(Hotel.class).field("_id").equal(new ObjectId(itemId)).get();
                ref = new SimpleRef();
                ref.id = hotel.id;
                ref.zhName = hotel.name;
                planItem = new PlanItem();
                planItem.item = ref;
                planItem.loc = hotel.addr.loc;
                planItem.type = "hotel";
                break;
        }
        return planItem;
    }


    /**
     * 交通映射类
     *
     * @param item
     * @return
     */
    private static PlanItem trafficMapper(JsonNode item) throws TravelPiException {
        String itemId = item.get("itemId").asText();
//        String type = item.get("type").asText();
//        String st = item.get("st").asText();
        String subType = item.get("subType").asText();

//        "type" : "traffic",
//                "ts" : "2014-07-28 22:10:00+0800",
//                "itemId" : "53aaa96b10114e41c5c8d0f3",
//                "st" : "2014-07-28 22:10:00+0800",
//                "subType" : "airport"

        PlanItem planItem = null;
        Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.TRAFFIC);
        SimpleRef ref;
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd hh:mm:ssZ");
        switch (subType) {
            case "airport":
                Airport airport = ds.createQuery(Airport.class).field("_id").equal(new ObjectId(itemId)).get();
                ref = new SimpleRef();
                ref.id = airport.id;
                ref.zhName = airport.zhName;
                planItem = new PlanItem();
                planItem.item = ref;
                planItem.loc = airport.addr.loc;
                planItem.type = "traffic";
                planItem.subType = "airport";
                try {
                    planItem.ts = fmt.parse(item.get("ts").asText());
                } catch (ParseException ignored) {
                }
                break;
            case "trainStation":
                TrainStation trainStation = ds.createQuery(TrainStation.class).field("_id").equal(new ObjectId(itemId)).get();
                ref = new SimpleRef();
                ref.id = trainStation.id;
                ref.zhName = trainStation.zhName;
                planItem = new PlanItem();
                planItem.item = ref;
                planItem.loc = trainStation.addr.loc;
                planItem.type = "traffic";
                planItem.subType = "trainStation";
                try {
                    planItem.ts = fmt.parse(item.get("ts").asText());
                } catch (ParseException ignored) {
                }
                break;
            case "airRoute":
                AirRoute airRoute = ds.createQuery(AirRoute.class).field("_id").equal(new ObjectId(itemId)).get();
                ref = new SimpleRef();
                ref.id = airRoute.id;
                ref.zhName = airRoute.code;
                planItem = new PlanItem();
                planItem.item = ref;
                planItem.type = "traffic";
                planItem.subType = "airRoute";
//                planItem.ts = airRoute.depTime;
                try {
                    planItem.ts = fmt.parse(item.get("ts").asText());
                } catch (ParseException | NullPointerException ignored) {
                }
                break;
            case "trainRoute":
                TrainRoute trainRoute = ds.createQuery(TrainRoute.class).field("_id").equal(new ObjectId(itemId)).get();
                ref = new SimpleRef();
                ref.id = trainRoute.id;
                ref.zhName = trainRoute.code;
                planItem = new PlanItem();
                planItem.item = ref;
                planItem.type = "traffic";
                planItem.subType = "trainRoute";
//                planItem.ts = trainRoute.depTime;
                try {
                    planItem.ts = fmt.parse(item.get("ts").asText());
                } catch (ParseException ignored) {
                }
                break;
        }
        return planItem;
    }


    /**
     * 客户端调用optimizer的时候，上传的是精简后的JSON格式的poi list。需要将其转换为PlanItem列表的形式，并按照天数进行分页。
     *
     * @param rawPlan
     * @return
     */
    private static List<PlanDayEntry> raw2plan(JsonNode rawPlan) throws TravelPiException {
        List<PlanDayEntry> dayEntryList = new ArrayList<>();
        Calendar curCal = Calendar.getInstance();
        curCal.setTimeInMillis(0);
        PlanDayEntry dayEntry = null;

        // 整理原始输入，并排序
        List<JsonNode> rawDetails = new ArrayList<>();
        for (Iterator<JsonNode> itr = rawPlan.elements(); itr.hasNext(); )
            rawDetails.add(itr.next());
        Collections.sort(rawDetails, new Comparator<JsonNode>() {
            @Override
            public int compare(JsonNode o1, JsonNode o2) {
                SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd hh:mm:ssZ");
                try {
                    Date d1 = fmt.parse(o1.get("st").asText());
                    Date d2 = fmt.parse(o2.get("st").asText());
                    if (d1.before(d2))
                        return -1;
                    else if (d1.after(d2))
                        return 1;
                } catch (ParseException ignored) {
                }
                return 0;
            }
        });

        for (JsonNode item : rawDetails) {
            String itemId = item.get("itemId").asText();
            String type = item.get("type").asText();
            String st = item.get("st").asText();

            // 是否需要新一天的行程
            Matcher m = Pattern.compile("^\\d{4}-\\d{2}-\\d{2}").matcher(st);
            SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
            Date sortDate = null;
            if (m.find())
                try {
                    sortDate = fmt.parse(m.group());
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            if (sortDate == null)
                continue;
            if (dayEntry == null || dayEntry.date.getDate() != sortDate.getDate()) {
                dayEntry = new PlanDayEntry();
                dayEntry.date = sortDate;
                dayEntry.actv = new ArrayList<>();
                dayEntryList.add(dayEntry);
            }

            if (type.equals("traffic"))
                dayEntry.actv.add(trafficMapper(item));
            else if (type.equals("vs") || type.equals("hotel"))
                dayEntry.actv.add(PoiMapper(item));
        }
        return dayEntryList;
    }


    /**
     * 对路线进行优化。
     *
     * @param keepOrder
     * @return
     */
    public static Result optimizePlan(int keepOrder) {
        JsonNode rawPlan = request().body().asJson();

        JsonNode details = rawPlan.get("details");
        if (details == null || details.size() == 0)
            return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, "Invalid plan details.");

        ObjectNode ret = Json.newObject();

        List<PlanDayEntry> dayEntryList;
        try {
            dayEntryList = raw2plan(details);
        } catch (TravelPiException e) {
            return Utils.createResponse(e.errCode, e.getMessage());
        }
        List<JsonNode> retDetails = new ArrayList<>();
        for (PlanDayEntry dayEntry : dayEntryList)
            retDetails.add(dayEntry.toJson());
        ret.put("details", Json.toJson(retDetails));
        ret.put("budget", Json.toJson(Arrays.asList(2000, 3000)));

        try {
            fullfill(ret);
        } catch (TravelPiException e) {
            return Utils.createResponse(e.errCode, e.getMessage());
        }

        return Utils.createResponse(ErrorCode.NORMAL, ret);

//        Datastore dsPOI = null;
//        Datastore dsTraffic = null;
//        Datastore dsLoc = null;
//        try {
//            dsPOI = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.POI);
//            dsTraffic = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.TRAFFIC);
//            dsLoc = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.GEO);
//        } catch (TravelPiException ignored) {
//            return Utils.createResponse(ErrorCode.DATABASE_ERROR, "Database error.");
//        }
//
//        Map<Integer, Poi> planPois = new HashMap<>();
//        Map<Integer, PlanItem> planItems = new HashMap<>();
//
//        int i = -1;
//        double lat = 40;
//        double lng = 116;
//        for (Iterator<JsonNode> it = details.iterator(); it.hasNext(); ) {
//            i++;
//
//            JsonNode node = it.next();
//            String type = node.get("type").asText();
//            JsonNode tmp = node.get("subType");
//            String subType = tmp != null ? tmp.asText() : null;
//
//            PlanItem item = new PlanItem();
//            Poi poi = null;
//            if (type.equals("vs")) {
//                ViewSpot vs = dsPOI.createQuery(ViewSpot.class).field("_id").equal(new ObjectId(node.get("itemId").asText())).get();
//                if (vs == null)
//                    continue;
//                SimpleRef ref = new SimpleRef();
//                ref.id = vs.id;
//                ref.zhName = vs.name;
//                item.item = ref;
//                item.loc = vs.addr.loc;
//                item.type = type;
//                poi = new Poi(i + 1, 0, new Point(vs.addr.coords.lng, vs.addr.coords.lat), 240, 4 * 60, 20 * 60);
//            } else if (type.equals("traffic")) {
//                if (subType.equals("trainStation")) {
//
//                }
//            }
//
//            if (poi != null) {
//                planItems.put(i, item);
//                planPois.put(i, poi);
//            }
//        }
//
//        List<Poi> pois1 = new ArrayList<>();
////        pois1.add(new Poi(1, 4, new Point(116.342323434, 39.9061892795), 0,
////                480, 1200));
//        pois1.add(new Poi(2, 0, new Point(116.337649281, 40.4505339258), 240,
//                480, 1200));
//        pois1.add(new Poi(3, 0, new Point(116.391272091, 39.9293099936), 240,
//                480, 1200));
////        pois1.add(new Poi(4, 1, new Point(116.391223123, 39.9254654555), 0, 0,
////                0));
//
//        Map<Integer, List<Poi>> allPois = new HashMap<>();
//
//        List<Poi> poiList = new ArrayList<>(planPois.values());
////        allPois.put(1, new ArrayList<Poi>());
//        allPois.put(2, poiList.subList(0, 2));
//        allPois.put(3, poiList.subList(2, 5));
////        allPois.put(4, poiList.subList(3, poiList.size()));
////        allPois.put(1, new ArrayList<>(pois1));
//        PlanEngine engine = EngineFactory.createPlanEngine(allPois);
//        engine.run();
//        Choice best = engine.getBest();
//
//        scala.collection.Iterator<OptimizedPoi> opItr = best.plan().iterator();
//        List<OptimizedPoi> optimized = new ArrayList<>();
//        while (opItr.hasNext())
//            optimized.add(opItr.next());
//
//
////        List<Poi> pois1 = new ArrayList<>();
////        pois1.add(new Poi(1, 4, new Point(116.342323434, 39.9061892795), 0,
////                480, 1200));
////        pois1.add(new Poi(2, 0, new Point(116.337649281, 40.4505339258), 240,
////                480, 1200));
////        pois1.add(new Poi(3, 0, new Point(116.391272091, 39.9293099936), 240,
////                480, 1200));
////        pois1.add(new Poi(4, 1, new Point(116.391223123, 39.9254654555), 0, 0,
////                0));
////        List<Poi> pois2 = new ArrayList<>();
////        pois2.add(new Poi(5, 0, new Point(116.749835074, 40.6454209626), 240,
////                480, 1200));
////        pois2.add(new Poi(6, 4, new Point(116.403794312, 40.9061892795), 0,
////                480, 1200));
////        Map<Integer, List<Poi>> allPois = new HashMap<>();
////        allPois.put(1, pois1);
//////        allPois.put(2, pois2);
////        PlanEngine engine = EngineFactory.createPlanEngine(allPois);
////        engine.run();
////        Choice best = engine.getBest();
//////        assertEquals(2, best.plan().length());

//        return Utils.createResponse(ErrorCode.NORMAL, rawPlan);
    }
}
