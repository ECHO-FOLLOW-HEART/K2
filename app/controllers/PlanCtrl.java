package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.QueryBuilder;
import core.PlanAPI;
import core.PlanAPIOld;
import core.PoiAPI;
import core.TrafficAPI;
import exception.ErrorCode;
import exception.TravelPiException;
import models.MorphiaFactory;
import models.morphia.misc.SimpleRef;
import models.morphia.plan.Plan;
import models.morphia.plan.PlanDayEntry;
import models.morphia.plan.PlanItem;
import models.morphia.poi.AbstractPOI;
import models.morphia.poi.Hotel;
import models.morphia.poi.Restaurant;
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
import utils.Planner;
import utils.Utils;

import java.net.UnknownHostException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;


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
            buildBudget(plan);
            JsonNode planJson = plan.toJson();
            fullfill(planJson);


            return Utils.createResponse(ErrorCode.NORMAL, planJson);
        } catch (TravelPiException e) {
            return Utils.createResponse(e.errCode, e.getMessage());
        }
    }

    /**
     * 计算预算数据。
     *
     * @param plan
     * @return
     */
    private static Plan buildBudget(Plan plan) {
        List<Integer> budget = Arrays.asList(0, 0);
//        if (budget == null || budget.isEmpty())

        // 扫描plan，获得价格信息
        List<PlanDayEntry> details = plan.details;
        if (details == null || details.isEmpty())
            return plan;
        for (PlanDayEntry dayEntry : details) {
            List<PlanItem> actv = dayEntry.actv;
            if (actv == null || actv.isEmpty())
                continue;

            for (PlanItem item : actv) {
                if (item.type.equals("traffic")) {
                    if (item.subType.equals("airRoute")) {
                        Calendar cal = Calendar.getInstance();
                        cal.setTime(dayEntry.date);
                        try {
                            AirRoute airRoute = TrafficAPI.getAirRouteByCode(item.item.zhName, cal);
                            double price = (airRoute.price.price != null ? airRoute.price.price : 0);
                            double surcharge = (airRoute.price.surcharge != null ? airRoute.price.surcharge : 0);
                            double tax = (airRoute.price.tax != null ? airRoute.price.tax : 0);
                            double cost = price + surcharge + tax;
                            budget.set(0, (int) (cost + budget.get(0)));
                            budget.set(1, (int) (cost + budget.get(1)));
                        } catch (TravelPiException ignored) {
                        }
                    }

                } else if (Arrays.asList("vs", "hotel", "restaurant").contains(item.type)) {
                    Class<? extends AbstractPOI> poiClass = null;
                    switch (item.type) {
                        case "vs":
                            poiClass = ViewSpot.class;
                            break;
                        case "hotel":
                            poiClass = Hotel.class;
                            break;
                        case "restaurant":
                            poiClass = Restaurant.class;
                            break;
                    }
                    if (poiClass != null) {
                        try {
                            // TODO 需要改成通过调用PoiAPI来获得相应的价格数据。
                            Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.POI);
                            AbstractPOI ret = ds.createQuery(poiClass).field("_id").equal(item.item.id).get();
                            if (ret == null)
                                continue;

                            if (ret.price == null || ret.price <= 0) {
                                budget.set(0, 50 + budget.get(0));
                                budget.set(1, 120 + budget.get(1));
                            } else {
                                budget.set(0, (int) (ret.price + budget.get(0)));
                                budget.set(1, (int) (ret.price + budget.get(1)));
                            }
                        } catch (TravelPiException ignored) {
                        }
                    }
                }
            }
        }

        budget.set(0, budget.get(0) / 100 * 100);
        budget.set(1, budget.get(1) / 100 * 100);
        plan.budget = budget;
        return plan;
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
     * @return
     */
    public static Result saveUGCPlan() {
        JsonNode data = request().body().asJson();
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
    private static PlanItem poiMapper(JsonNode item) throws TravelPiException {
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
        String subType = item.get("subType").asText();

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
                } catch (ParseException | NullPointerException ignored) {
                }
                break;
        }
        return planItem;
    }


    /**
     * 将item追加到plan尾部。如有必要，会自动创建相应的PlanDayEntry对象。
     *
     * @param plan
     * @param item
     * @param itemCal
     * @return
     */
    private static List<PlanDayEntry> appendPlanItem(List<PlanDayEntry> plan, PlanItem item, Calendar itemCal) {
        if (plan == null)
            plan = new ArrayList<>();

        if (itemCal == null) {
            itemCal = Calendar.getInstance();
            itemCal.setTime(item.ts);
        }

        if (plan.isEmpty())
            plan.add(new PlanDayEntry(itemCal));

        PlanDayEntry dayEntry;
        Calendar curCal;
        while (true) {
            dayEntry = plan.get(plan.size() - 1);
            curCal = Calendar.getInstance();
            curCal.setTime(dayEntry.date);

            if (curCal.get(Calendar.DAY_OF_YEAR) == itemCal.get(Calendar.DAY_OF_YEAR))
                break;
            if (curCal.after(itemCal))
                // 避免死循环。通常，itemCal一定会位于curCal之后。
                return plan;

            // 新生成一个PlanDayEntry
            curCal.add(Calendar.DAY_OF_YEAR, 1);
            dayEntry = new PlanDayEntry(curCal);
            plan.add(dayEntry);
        }

        // 此时的dayEntry，就是item应该插入的地方。
        dayEntry.actv.add(item);

        return plan;
    }


    /**
     * 客户端调用optimizer的时候，上传的是精简后的JSON格式的poi list。需要将其转换为PlanItem列表的形式，并按照天数进行分页。
     *
     * @param rawDetails
     * @return
     */
    private static List<PlanDayEntry> raw2plan(JsonNode rawDetails, JsonNode trafficInfo,
                                               Calendar startCal, Calendar endCal) throws TravelPiException {
        // 获得两端大交通的信息
        List<PlanItem> awayTraffic = new ArrayList<>();
        List<PlanItem> backTraffic = new ArrayList<>();
        boolean isAway = true;
        boolean arrived = true;
        Date lastTs = null;
        for (JsonNode item : rawDetails) {
            String type = item.get("type").asText();
            if (!type.equals("traffic"))
                continue;

            String subType = item.get("subType").asText();
            PlanItem trafficItem = trafficMapper(item);
            if (trafficItem.ts == null)
                trafficItem.ts = lastTs;
            else
                lastTs = trafficItem.ts;

            (isAway ? awayTraffic : backTraffic).add(trafficItem);

            if (subType.equals("airport") || subType.equals("trainStation")) {
                arrived = !arrived;
                if (arrived)
                    isAway = !isAway;
            }
        }

        // 需要考虑两端的大交通的时间是否需要shift
        if (!awayTraffic.isEmpty()) {
            Calendar depCal = Calendar.getInstance();
            depCal.setTime(awayTraffic.get(0).ts);
            Calendar arrCal = Calendar.getInstance();
            arrCal.setTime(awayTraffic.get(2).ts);


            // 如果到达时间过晚，晚于critCal，则需要调整
            Calendar critCal = Calendar.getInstance();
            critCal.setTimeInMillis(startCal.getTimeInMillis());
            critCal.set(Calendar.HOUR_OF_DAY, 13);
            critCal.set(Calendar.MINUTE, 0);
            critCal.set(Calendar.SECOND, 0);
            critCal.set(Calendar.MILLISECOND, 0);

            boolean modified = false;
            while (arrCal.after(critCal)) {
                depCal.add(Calendar.DAY_OF_YEAR, -1);
                arrCal.add(Calendar.DAY_OF_YEAR, -1);
                modified = true;
            }
            // arrCal也不能太早。如果太早，需要向后移动
            int dt = (int) ((critCal.getTimeInMillis() - arrCal.getTimeInMillis()) / (1000l * 3600 * 24));
            if (dt > 0) {
                depCal.add(Calendar.DAY_OF_YEAR, dt);
                arrCal.add(Calendar.DAY_OF_YEAR, dt);
                modified = true;
            }

            if (modified) {
                awayTraffic.get(0).ts = depCal.getTime();
                awayTraffic.get(1).ts = depCal.getTime();
                awayTraffic.get(2).ts = arrCal.getTime();
            }
        }
        if (!backTraffic.isEmpty()) {
            Calendar depCal = Calendar.getInstance();
            depCal.setTime(backTraffic.get(0).ts);
            Calendar arrCal = Calendar.getInstance();
            arrCal.setTime(backTraffic.get(2).ts);

            // 如果出发时间过早，早于critCal，则需要调整
            Calendar critCal = Calendar.getInstance();
            critCal.setTimeInMillis(endCal.getTimeInMillis());
            critCal.set(Calendar.HOUR_OF_DAY, 12);
            critCal.set(Calendar.MINUTE, 0);
            critCal.set(Calendar.SECOND, 0);
            critCal.set(Calendar.MILLISECOND, 0);

            boolean modified = false;
            while (depCal.before(critCal)) {
                depCal.add(Calendar.DAY_OF_YEAR, 1);
                arrCal.add(Calendar.DAY_OF_YEAR, 1);
                modified = true;
            }
            // depCal也不能太晚。如果太晚，需要向前移动。
            int dt = (int) ((arrCal.getTimeInMillis() - critCal.getTimeInMillis()) / (1000l * 3600 * 24));
            if (dt > 0) {
                depCal.add(Calendar.DAY_OF_YEAR, -dt);
                arrCal.add(Calendar.DAY_OF_YEAR, -dt);
                modified = true;
            }
            if (modified) {
                backTraffic.get(0).ts = depCal.getTime();
                backTraffic.get(1).ts = depCal.getTime();
                backTraffic.get(2).ts = arrCal.getTime();
            }
        }

        List<PlanDayEntry> entryList = null;
        for (PlanItem item : awayTraffic) entryList = appendPlanItem(entryList, item, null);

        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
        for (JsonNode item : rawDetails) {
            String type = item.get("type").asText();
            try {
                Calendar cal = Calendar.getInstance();
                cal.setTime(fmt.parse(item.get("st").asText()));
                if (type.equals("vs") || type.equals("hotel"))
                    entryList = appendPlanItem(entryList, poiMapper(item), cal);
            } catch (ParseException ignored) {
            }
        }

        for (PlanItem item : backTraffic) entryList = appendPlanItem(entryList, item, null);

        return entryList;
    }


    /**
     * 对路线进行优化。
     *
     * @return
     */
    public static Result optimizePlan() {
        JsonNode rawPlan = request().body().asJson();

        // 优化级别。
        int optLevel = 1;
        JsonNode tmp = rawPlan.get("optLevel");
        if (tmp != null)
            optLevel = tmp.asInt(1);

        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");

        JsonNode details, trafficInfo;
        Date startDate, endDate;
        try {
            details = rawPlan.get("details");
            trafficInfo = rawPlan.get("traffic");
            startDate = fmt.parse(rawPlan.get("startDate").asText());
            endDate = fmt.parse(rawPlan.get("endDate").asText());
        } catch (ParseException e) {
            return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, "Invalid plan.");
        }

        Calendar startCal = Calendar.getInstance();
        startCal.setTime(startDate);
        Calendar endCal = Calendar.getInstance();
        endCal.setTime(endDate);

        List<PlanDayEntry> dayEntryList;
        try {
            dayEntryList = raw2plan(details, trafficInfo, startCal, endCal);
        } catch (TravelPiException e) {
            return Utils.createResponse(e.errCode, e.getMessage());
        }
        PlanAPI.addHotels(dayEntryList);

        List<JsonNode> retDetails = new ArrayList<>();
        for (PlanDayEntry dayEntry : dayEntryList) retDetails.add(dayEntry.toJson());
        ObjectNode ret = Json.newObject();
        ret.put("details", Json.toJson(retDetails));
//        ret.put("budget", Json.toJson(Arrays.asList(2000, 3000)));

        try {
            fullfill(ret);
        } catch (TravelPiException e) {
            return Utils.createResponse(e.errCode, e.getMessage());
        }

        return Utils.createResponse(ErrorCode.NORMAL, ret);
    }
}
