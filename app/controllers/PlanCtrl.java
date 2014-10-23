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
import models.morphia.misc.Share;
import models.morphia.misc.SimpleRef;
import models.morphia.plan.*;
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
import play.Configuration;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import utils.*;

import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;


/**
 * 路线相关
 *
 * @author Zephyre
 */
public class PlanCtrl extends Controller {


    public static int WEB_REQUEST_FLAG = 1;
    public static String UGCPLAN = "UgcPlan";
    public static String SHAREPLAN = "SharePlan";

    /**
     * 查询路线的详细信息。
     *
     * @param planId    路线id
     * @param fromLocId 从哪个城市出发
     * @return
     * @throws UnknownHostException
     */
    public static Result getPlanFromTemplates(String planId, String fromLocId, String backLocId, int webFlag, String uid) {
        try {
            Http.Request req = request();
            if (fromLocId.equals("")) {
                Plan plan = PlanAPI.getPlan(planId, false);
                if (plan == null)
                    throw new TravelPiException(ErrorCode.INVALID_OBJECTID, String.format("Invalid plan ID: %s.", planId.toString()));
                return Utils.createResponse(ErrorCode.NORMAL, plan.toJson());
            }
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_YEAR, 3);

            UgcPlan plan = PlanAPI.doPlanner(planId, fromLocId, backLocId, cal, req);

            Configuration config = Configuration.root();
            Map budget = (Map) config.getObject("budget");
            Double trafficBudgetDefault = 0d;
            int stayBudgetDefault = 0;
            try {
                if (budget != null) {
                    trafficBudgetDefault = Double.valueOf(budget.get("trafficBudgetDefault").toString());
                    stayBudgetDefault = Integer.valueOf(budget.get("stayBudgetDefault").toString());
                }
            } catch (ClassCastException e) {
                trafficBudgetDefault = 0d;
            }

            //TODO 临时添加住宿预算，交通预算
            Double trafficBudgetT = 0d;
            List<SimpleRef> targets = plan.targets;
            if (null != targets && targets.size() > 0) {
                trafficBudgetT = Bache.getTrafficBudget(fromLocId, plan.targets.get(0).id.toString());
            } else {
                trafficBudgetT = trafficBudgetDefault;
            }
            plan.trafficBudget = Integer.valueOf((int) trafficBudgetT.doubleValue());
            plan.stayBudget = plan.days * stayBudgetDefault;

            buildBudget(plan);

            JsonNode planJson = plan.toJson();
            fullfill(planJson);


            if (DataFilter.isAppRequest(req))
                return Utils.createResponse(ErrorCode.NORMAL, DataFilter.appDescFilter(DataFilter.appJsonFilter(planJson, req, Constants.SMALL_PIC), req));
            else
                return Utils.createResponse(ErrorCode.NORMAL, updatePlanByNode(planJson, uid).toJson());
        } catch (TravelPiException e) {
            return Utils.createResponse(e.errCode, e.getMessage());
        }
    }

    private static UgcPlan updatePlanByNode(JsonNode data, String uid) throws TravelPiException {
        String ugcPlanId = data.get("_id").asText();
        String templateId = data.get("templateId").asText();
        String title = data.get("title").asText();
        //String uid = data.has("uid") ? data.get("uid").asText() : "";

        DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat timeFmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ");

        Plan plan = PlanAPI.getPlan(templateId, false);
        if (plan == null)
            throw new TravelPiException(ErrorCode.INVALID_OBJECTID, String.format("Invalid plan ID: %s.", templateId));
        UgcPlan ugcPlan = new UgcPlan(plan);

        PlanDayEntry planDayEntry = null;
        PlanItem planItem = null;
        List<PlanItem> planItemList = null;
        SimpleRef pItem = null;
        SimpleRef pLoc = null;
        List<PlanDayEntry> planDayEntryList = new ArrayList<PlanDayEntry>();

        JsonNode jsList = data.get("details");
        JsonNode tempDay = null;
        int dayIndex = 0;
        Date startDate = null;
        Date endDate = null;
        for (int i = 0; i < jsList.size(); i++) {
            tempDay = jsList.get(i);
            planDayEntry = new PlanDayEntry();
            if (!tempDay.has("actv"))
                continue;
            JsonNode actv = tempDay.get("actv");
            JsonNode date = tempDay.get("date");

            //设置Date
            try {
                planDayEntry.date = timeFmt.parse(date.asText());
            } catch (ParseException e) {
                throw new TravelPiException(ErrorCode.INVALID_ARGUMENT, e.getMessage(), e);
            }
            if (dayIndex == 0) {
                startDate = planDayEntry.date;
            } else if (dayIndex == jsList.size() - 1) {
                endDate = planDayEntry.date;
            }
            dayIndex++;
            planItemList = new ArrayList<PlanItem>();

            for (JsonNode item : actv) {
                try {
                    //新建PlanItem
                    planItem = new PlanItem();
                    pItem = new SimpleRef();
                    pLoc = new SimpleRef();
                    planItem.ts = planDayEntry.date;
                    if (item.has("ts") && (!item.get("ts").asText().equals(""))) {
                        planItem.ts = timeFmt.parse(item.get("ts").asText());
                    } else {
                        planItem.ts = planDayEntry.date;
                    }
                    if (item.has("itemId")) {
                        pItem.id = new ObjectId(item.get("itemId").asText());
                        pItem.zhName = item.get("itemName").asText();
                        planItem.item = pItem;
                    }
                    if (item.has("locId")) {
                        pLoc.id = new ObjectId(item.get("locId").asText());
                        pLoc.zhName = item.get("locName").asText();
                        planItem.loc = pLoc;
                    }

                    String subTypeStr = item.get("subType").asText();
                    if (subTypeStr.equals("airport") || subTypeStr.equals("trainStaion")) {
                        planItem.stopType = item.get("stopType").asText();
                        if (item.has("lat") && item.has("lng")) {
                            planItem.lat = Double.parseDouble(item.get("lat").asText());
                            planItem.lng = Double.parseDouble(item.get("lng").asText());
                        }
                    }
                    if (subTypeStr.equals("airRoute") || subTypeStr.equals("trainRoute")) {
                        if (item.has("depStop") && item.has("depLoc")) {
                            planItem.depLoc = new ObjectId(item.get("depLoc").asText());
                            planItem.depStop = new ObjectId(item.get("depStop").asText());
                        }
                        if (item.has("arrLoc") && item.has("arrStop")) {
                            planItem.arrLoc = new ObjectId(item.get("arrLoc").asText());
                            planItem.arrStop = new ObjectId(item.get("arrStop").asText());
                        }
                        if (item.has("depTime") && item.has("arrTime")
                                && (!item.get("depTime").asText().equals(""))
                                && (!item.get("arrTime").asText().equals(""))) {
                            planItem.depTime = timeFmt.parse(item.get("depTime").asText());
                            planItem.arrTime = timeFmt.parse(item.get("arrTime").asText());
                        }
                        if (item.has("distance")) {
                            planItem.distance = item.get("distance").asText();
                        }
                    }
                    planItem.type = item.get("type").asText();
                    planItem.subType = item.get("subType").asText();

                    planItemList.add(planItem);
                } catch (ParseException e) {
                    throw new TravelPiException(ErrorCode.INVALID_ARGUMENT, e.getMessage(), e);
                }
            }
            planDayEntry.actv = planItemList;
            planDayEntryList.add(planDayEntry);
        }
        ugcPlan.details = planDayEntryList;
        ugcPlan.stayBudget = Integer.parseInt(data.get("stayBudget").asText());
        ugcPlan.viewBudget = Integer.parseInt(data.get("viewBudget").asText());
        ugcPlan.trafficBudget = Integer.parseInt(data.get("trafficBudget").asText());
        //设置UGC路线ID
        ugcPlan.id = new ObjectId(ugcPlanId);
        ugcPlan.templateId = new ObjectId(templateId);
        ugcPlan.startDate = startDate;
        ugcPlan.endDate = endDate;
        ugcPlan.title = title;
        //uid为空，中间态数据；否则为保存态数据。中间态数据会被定期清理。
        if (!uid.equals("")) {
            ugcPlan.uid = new ObjectId(uid);
            ugcPlan.persisted = true;

        } else {
            ugcPlan.persisted = false;
        }
        ugcPlan.updateTime = (new Date()).getTime();
        ugcPlan.enabled = true;
        ugcPlan.isFromWeb = true;

        PlanAPI.saveUGCPlan(ugcPlan);
        return ugcPlan;

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
                                if (airport != null && airport.addr != null && airport.addr.coords != null) {
                                    if (airport.addr.coords.lat != null)
                                        conItem.put("lat", airport.addr.coords.lat);
                                    if (airport.addr.coords.lng != null)
                                        conItem.put("lng", airport.addr.coords.lng);
                                    if (airport.addr.coords.blat != null)
                                        conItem.put("blat", airport.addr.coords.blat);
                                    if (airport.addr.coords.blng != null)
                                        conItem.put("blat", airport.addr.coords.blng);
                                }
                            } else if ("trainStation".equals(item.get("subType").asText())) {
                                Datastore dsTrain = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.TRAFFIC);
                                TrainStation trainStation = dsTrain.createQuery(TrainStation.class).field("_id")
                                        .equal(new ObjectId(item.get("itemId").asText())).get();
                                if (trainStation != null && trainStation.addr != null && trainStation.addr.coords != null) {
                                    if (trainStation.addr.coords.lat != null)
                                        conItem.put("lat", trainStation.addr.coords.lat);
                                    if (trainStation.addr.coords.lng != null)
                                        conItem.put("lng", trainStation.addr.coords.lng);
                                    if (trainStation.addr.coords.blat != null)
                                        conItem.put("blat", trainStation.addr.coords.blat);
                                    if (trainStation.addr.coords.blng != null)
                                        conItem.put("blat", trainStation.addr.coords.blng);
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
        JsonNode action = data.get("action");
        String updateField = null;
        String ugcPlanId = null;

        try {
            String actionFlag = action.asText();
            ugcPlanId = data.get("_id") == null ? null : data.get("_id").asText();
            ObjectId oid = ugcPlanId == null ? new ObjectId() : new ObjectId(ugcPlanId);
            //只更新标题
            if (actionFlag.equals("updateTitle")) {
                updateField = data.get("title").asText();
                PlanAPI.updateUGCPlanByFiled(oid, "title", updateField);
            }
            //只更新用户ID：web用
            if (actionFlag.equals("updateUid")) {
                updateField = data.get("uid").asText();
                PlanAPI.updateUGCPlanByFiled(oid, "uid", updateField);
            }
            //更新路线
            if (actionFlag.equals("upsert")) {
                updateUGCPlan(data, UGCPLAN);
            }
        } catch (NullPointerException | IllegalAccessException | NoSuchFieldException | ParseException | InstantiationException e) {
            return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, e.getMessage());

        } catch (TravelPiException e) {
            return Utils.createResponse(e.errCode, e.getMessage());
        }

        return Utils.createResponse(ErrorCode.NORMAL, "Success");
    }

    /**
     * @param data
     * @param saveToTable UGC路线表，分享路线表
     * @throws TravelPiException
     * @throws ParseException
     * @throws IllegalAccessException
     * @throws NoSuchFieldException
     * @throws InstantiationException
     */
    private static void updateUGCPlan(JsonNode data, String saveToTable) throws TravelPiException, ParseException, IllegalAccessException, NoSuchFieldException, InstantiationException {
        String ugcPlanId = data.get("_id").asText();
        String templateId = data.get("templateId").asText();
        String title = data.get("title").asText();
        String uid = data.has("uid") ? data.get("uid").asText() : "";
        String startDateStr = data.get("startDate").asText();
        String endDateStr = data.get("endDate").asText();
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, 3);
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat timeFmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ");

        JsonNode details = data.get("details");
        JsonNode trafficInfo = data.get("traffic");

        Date startDate = format.parse(startDateStr);
        Date endDate = format.parse(endDateStr);
        Calendar startCal = Calendar.getInstance();
        startCal.setTime(startDate);
        Calendar endCal = Calendar.getInstance();
        endCal.setTime(endDate);
        //根据模板路线获取UGC路线
        Plan plan = PlanAPI.getPlan(templateId, false);
        UgcPlan ugcPlan = new UgcPlan(plan);
        if (plan == null)
            throw new TravelPiException(ErrorCode.INVALID_OBJECTID, String.format("Invalid plan ID: %s.", templateId));

        //补全信息
        List<PlanDayEntry> dayEntryList = raw2plan(details, trafficInfo, startCal, endCal, false);
        List<JsonNode> retDetails = new ArrayList<>();
        for (PlanDayEntry dayEntry : dayEntryList) retDetails.add(dayEntry.toJson());
        ObjectNode ret = Json.newObject();
        ret.put("details", Json.toJson(retDetails));

        PlanDayEntry planDayEntry = null;
        PlanItem planItem = null;
        List<PlanItem> planItemList = null;
        SimpleRef pItem = null;
        SimpleRef pLoc = null;
        List<PlanDayEntry> planDayEntryList = new ArrayList<PlanDayEntry>();

        //补全details信息
        fullfill(ret);

        JsonNode jsList = ret.get("details");
        JsonNode tempDay = null;
        for (int i = 0; i < jsList.size(); i++) {
            tempDay = jsList.get(i);
            planDayEntry = new PlanDayEntry();
            if (!tempDay.has("actv"))
                continue;
            JsonNode actv = tempDay.get("actv");
            JsonNode date = tempDay.get("date");
            //设置Date
            planDayEntry.date = timeFmt.parse(date.asText());
            planItemList = new ArrayList<PlanItem>();

            for (JsonNode item : actv) {
                //新建PlanItem
                planItem = new PlanItem();
                pItem = new SimpleRef();
                pLoc = new SimpleRef();
                planItem.ts = planDayEntry.date;
                if (item.has("ts")) {
                    planItem.ts = timeFmt.parse(item.get("ts").asText());
                } else {
                    planItem.ts = planDayEntry.date;
                }
                if (item.has("itemId")) {
                    pItem.id = new ObjectId(item.get("itemId").asText());
                    pItem.zhName = item.get("itemName").asText();
                    planItem.item = pItem;
                }
                if (item.has("locId")) {
                    pLoc.id = new ObjectId(item.get("locId").asText());
                    pLoc.zhName = item.get("locName").asText();
                    planItem.loc = pLoc;
                }

                String subTypeStr = item.get("subType").asText();
                if (subTypeStr.equals("airport") || subTypeStr.equals("trainStaion")) {
                    planItem.stopType = item.get("stopType").asText();
                    if (item.has("lat") && item.has("lng")) {
                        planItem.lat = Double.parseDouble(item.get("lat").asText());
                        planItem.lng = Double.parseDouble(item.get("lng").asText());
                    }
                }
                if (subTypeStr.equals("airRoute") || subTypeStr.equals("trainRoute")) {
                    if (item.has("depStop") && item.has("depLoc")) {
                        planItem.depLoc = new ObjectId(item.get("depLoc").asText());
                        planItem.depStop = new ObjectId(item.get("depStop").asText());
                    }
                    if (item.has("arrLoc") && item.has("arrStop")) {
                        planItem.arrLoc = new ObjectId(item.get("arrLoc").asText());
                        planItem.arrStop = new ObjectId(item.get("arrStop").asText());
                    }
                    if (item.has("depTime") && item.has("arrTime")) {
                        planItem.depTime = timeFmt.parse(item.get("depTime").asText());
                        planItem.arrTime = timeFmt.parse(item.get("arrTime").asText());
                    }
                    if (item.has("distance")) {
                        planItem.distance = item.get("distance").asText();
                    }
                }
                planItem.type = item.get("type").asText();
                planItem.subType = item.get("subType").asText();

                planItemList.add(planItem);
            }
            planDayEntry.actv = planItemList;
            planDayEntryList.add(planDayEntry);
        }
        ugcPlan.details = planDayEntryList;

        ugcPlan.stayBudget = Integer.parseInt(data.get("stayBudget").asText());
        ugcPlan.viewBudget = Integer.parseInt(data.get("viewBudget").asText());
        ugcPlan.trafficBudget = Integer.parseInt(data.get("trafficBudget").asText());
        //设置UGC路线ID
        ugcPlan.id = new ObjectId(ugcPlanId);
        ugcPlan.startDate = startDate;
        ugcPlan.endDate = endDate;
        ugcPlan.title = title;
        //分享接口
        if (!uid.equals("")) {
            ugcPlan.uid = new ObjectId(uid);
        }
        ugcPlan.updateTime = (new Date()).getTime();
        ugcPlan.enabled = true;

        if (saveToTable.equals(SHAREPLAN)) {
            SharePlan sharePlan = new SharePlan(ugcPlan);

            PlanAPI.saveSharePlan(sharePlan);
        } else {
            PlanAPI.saveUGCPlan(ugcPlan);
        }

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
    public static Result explorePlans(String fromLoc, String locId, String poiId, String sortField, String sort, String tag, int minDays, int maxDays, int page, int pageSize) throws UnknownHostException, TravelPiException {
        List<JsonNode> results = new ArrayList<>();

        if (locId != null && !locId.isEmpty()) {
            // fix一个bug。POI的id在不经意之间变动过，所以只能做重定向处理。
            List<String> locIdList = Arrays.asList("53aa9b3610114e3fdc2fa5b9", "53aa9b3510114e3fdc2fa3e2",
                    "53aa9b4110114e3fdc2fb266", "53aa9b5010114e3fdc2fbcdf", "53aa9b3310114e3fdc2fa014");
            if (locIdList.contains(locId)) {
                poiId = locId;
                locId = null;
            }
        }

        if (poiId != null && !poiId.isEmpty()) {
            // fix一个bug。POI的id在不经意之间变动过，所以只能做重定向处理。
            Map<String, String> poiIdMapping = new HashMap<>();

            poiIdMapping.put("53aa9b4510114e3fdc2fb55e", "53f30e3e10114e377228a77f");
            poiIdMapping.put("53aa9f5610114e3fdc30255a", "53f31a1210114e3b228c50e7");
            poiIdMapping.put("53aa9b5310114e3fdc2fbe4c", "53f3117910114e377228b06c");
            poiIdMapping.put("53aa9cb410114e3fdc2fd635", "53f312ee10114e377228b5b9");
            poiIdMapping.put("53aa9b4110114e3fdc2fb266", "53f30d2710114e377228a48a");
            poiIdMapping.put("53aa9b7a10114e3fdc2fce57", "53f3105e10114e3779f93379");
            poiIdMapping.put("53aa9b5710114e3fdc2fc07b", "53f3121710114e377228b29d");
            poiIdMapping.put("53aa9caf10114e3fdc2fd4f2", "53f3110c10114e377228aefe");
            poiIdMapping.put("53aa9b5a10114e3fdc2fc218", "53f3128810114e377228b439");
            poiIdMapping.put("53aa9b6510114e3fdc2fc6b6", "53f30d8810114e3779f92bda");
            poiIdMapping.put("53aa9b3410114e3fdc2fa2e7", "53f30e7f10114e376de5b757");
            poiIdMapping.put("53aa9b5010114e3fdc2fbcdf", "53f311b610114e376de5c076");

            poiIdMapping.put("53aa9b3610114e3fdc2fa5b9", "53f30f8b10114e376de5ba26");
            poiIdMapping.put("53aa9b4f10114e3fdc2fbc40", "53f310d210114e377228ae60");
            poiIdMapping.put("53aa9b7310114e3fdc2fcbd1", "53f30f6c10114e3779f930f5");
            poiIdMapping.put("53aa9b6410114e3fdc2fc655", "53f30d6510114e3779f92b78");
            poiIdMapping.put("53aa9b3410114e3fdc2fa286", "53f30e5b10114e376de5b6f6");
            poiIdMapping.put("53aa9b6510114e3fdc2fc6e0", "53f30d9710114e3779f92c04");
            poiIdMapping.put("53aa9b3510114e3fdc2fa3e2", "53f30edd10114e376de5b851");
            poiIdMapping.put("53aa9b4b10114e3fdc2fba02", "53f30ffb10114e377228ac23");
            poiIdMapping.put("53aa9b4b10114e3fdc2fba04", "53f3116610114e377228b026");
            poiIdMapping.put("53aa9b5510114e3fdc2fbf78", "53f311ce10114e377228b199");
            poiIdMapping.put("53aa9b4510114e3fdc2fb587", "53f30e4e10114e377228a7a8");
            poiIdMapping.put("53aa9f3010114e3fdc302043", "53f330fc10114e3bb32d1181");
            poiIdMapping.put("53aa9b4910114e3fdc2fb867", "53f30f5f10114e377228aa87");


            poiIdMapping.put("53aa9b3210114e3fdc2f9e53", "53f30cd110114e376de5b2c3");
            poiIdMapping.put("53aa9b3910114e3fdc2faa5b", "53f3113d10114e376de5bec9");
            poiIdMapping.put("53aa9b5b10114e3fdc2fc25a", "53f3129a10114e377228b47b");
            poiIdMapping.put("53aa9b3910114e3fdc2faa50", "53f3113910114e376de5bebe");
            poiIdMapping.put("53aa9dbf10114e3fdc2ff1cf", "53f3109510114e3779f9340a");
            poiIdMapping.put("53aa9b5d10114e3fdc2fc360", "53f312e210114e377228b582");
            poiIdMapping.put("53aa9b3310114e3fdc2f9ff6", "53f30d6a10114e376de5b467");
            poiIdMapping.put("53aa9b4910114e3fdc2fb84e", "53f30f5610114e377228aa6e");
            poiIdMapping.put("53aa9f0210114e3fdc3019ba", "53f3127b10114e376de5c339");
            poiIdMapping.put("53aa9b5e10114e3fdc2fc374", "53f312e710114e377228b596");
            poiIdMapping.put("53aa9b4810114e3fdc2fb75d", "53f324f310114e3d0d3a21b4");
            poiIdMapping.put("53aa9b5410114e3fdc2fbed9", "53f311a110114e377228b0fa");
            poiIdMapping.put("53aa9b3310114e3fdc2fa014", "53f310c010114e376de5bd60");
            poiIdMapping.put("53aa9b3610114e3fdc2fa5b6", "53f30f8a10114e376de5ba23");
            poiIdMapping.put("53aa9b3610114e3fdc2fa5b7", "53f30f8b10114e376de5ba24");
            poiIdMapping.put("53aa9a6410114e3fd4783553", "53f310ff10114e3779f93527");
            poiIdMapping.put("53aa9a6410114e3fd478355b", "53f317d610114e3b1d2e6f06");
            poiIdMapping.put("53aa9a6410114e3fd4783576", "53f3172710114e3b12f69d3a");

            if (poiIdMapping.containsKey(poiId))
                poiId = poiIdMapping.get(poiId);
        }

        Double trafficBudget = Bache.getTrafficBudget(fromLoc, locId);
        //取得预算常量
        Configuration config = Configuration.root();
        Map budget = (Map) config.getObject("budget");
        int stayBudgetDefault = 0;
        try {
            if (budget != null) {
                stayBudgetDefault = Integer.valueOf(budget.get("stayBudgetDefault").toString());
            }
        } catch (ClassCastException e) {
            stayBudgetDefault = 0;
        }

        for (Iterator<Plan> it = PlanAPI.explore(locId, poiId, sort, tag, minDays, maxDays, page, pageSize, sortField);
             it.hasNext(); ) {
            //加入交通预算,住宿预算
            if (null != fromLoc && !fromLoc.trim().equals("")) {
                try {
                    results.add(addTrafficBudget(it, trafficBudget, stayBudgetDefault));
                } catch (ClassCastException e) {
                    return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, e.getMessage());
                }
            } else {
                results.add(it.next().toJson(false));
            }
        }
        return Utils.createResponse(ErrorCode.NORMAL, DataFilter.appJsonFilter(Json.toJson(results), request(), Constants.SMALL_PIC));
    }

    /**
     * 路线中加入交通预算和食宿预算
     *
     * @param it
     * @param trafficBudg
     * @param stayBudgetDefault
     * @return
     * @throws ClassCastException
     */
    private static JsonNode addTrafficBudget(Iterator<Plan> it, Double trafficBudg, int stayBudgetDefault) throws ClassCastException {
        Plan plan = it.next();
        if (null != plan) {
            plan.trafficBudget = Integer.valueOf((int) trafficBudg.doubleValue());
            plan.stayBudget = plan.days == null ? 0 : plan.days * stayBudgetDefault;
        }
        return plan.toJson(false);
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
        String st = item.get("st").asText();
        SimpleDateFormat timeFmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ");
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
                try {
                    planItem.ts = timeFmt.parse(st);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
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
                try {
                    planItem.ts = timeFmt.parse(st);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
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
        //12小时制：hh:mm:ss  24小时制：HH:mm:ss
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ");
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
                planItem.transfer = item.get("transfer") == null ? "" : item.get("transfer").asText();
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
                planItem.transfer = item.get("transfer") == null ? "" : item.get("transfer").asText();
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
                planItem.transfer = item.get("transfer") == null ? "" : item.get("transfer").asText();
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
                planItem.transfer = item.get("transfer") == null ? "" : item.get("transfer").asText();
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
        if (item.type.equals("vs") || item.type.equals("traffic")) {
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
        } else if (item.type.equals("hotel")) {
            SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
            for (int i = 0; i < plan.size(); i++) {
                curCal = Calendar.getInstance();
                curCal.setTime(plan.get(i).date);
                if (curCal.get(Calendar.DAY_OF_YEAR) == itemCal.get(Calendar.DAY_OF_YEAR)) {
                    plan.get(i).actv.add(item);
                }
            }
        }
        return plan;
    }


    /**
     * 客户端调用optimizer的时候，上传的是精简后的JSON格式的poi list。需要将其转换为PlanItem列表的形式，并按照天数进行分页。
     *
     * @param rawDetails
     * @return
     */
    private static List<PlanDayEntry> raw2plan(JsonNode rawDetails, JsonNode trafficInfo,
                                               Calendar startCal, Calendar endCal, boolean needOptimize) throws TravelPiException {
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
            if (PlanUtils.isTransferTraffic(item)) {
                needOptimize = false;
            }

            String subType = item.get("subType").asText();
            PlanItem trafficItem = trafficMapper(item);
            if (trafficItem.ts == null)
                trafficItem.ts = lastTs;
            else
                lastTs = trafficItem.ts;

            isAway = PlanUtils.isFromTraffic(item);
            (isAway ? awayTraffic : backTraffic).add(trafficItem);

//            if (subType.equals("airport") || subType.equals("trainStation")) {
//                arrived = !arrived;
//                if (arrived)
//                    isAway = !isAway;
//            }
        }

        // 需要考虑两端的大交通的时间是否需要shift
        if (!awayTraffic.isEmpty() && needOptimize) {
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
        if (!backTraffic.isEmpty() && needOptimize) {
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
            dayEntryList = raw2plan(details, trafficInfo, startCal, endCal, true);
        } catch (TravelPiException e) {
            return Utils.createResponse(e.errCode, e.getMessage());
        }

        PlanAPI.addHotels(dayEntryList);

        if (optLevel == 2)
            PlanAPI.pseudoOptimize(dayEntryList);

        List<JsonNode> retDetails = new ArrayList<>();
        for (PlanDayEntry dayEntry : dayEntryList) retDetails.add(dayEntry.toJson());
        ObjectNode ret = Json.newObject();
        ret.put("details", Json.toJson(retDetails));
        try {
            fullfill(ret);
        } catch (TravelPiException e) {
            return Utils.createResponse(e.errCode, e.getMessage());
        }

        Http.Request req = request();
        if (!DataFilter.isAppRequest(req)) {
            //WEB分支
            String uid = rawPlan.get("uid") == null ? "" : rawPlan.get("uid").asText();
            ObjectNode planNode = (ObjectNode) rawPlan;
            planNode.put("details", Json.toJson(ret.get("details")));
            try {
                updatePlanByNode(Json.toJson(planNode), uid);
                return Utils.createResponse(ErrorCode.NORMAL, "Success");
            } catch (TravelPiException e) {
                return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, e.getMessage());
            }
        } else
            return Utils.createResponse(ErrorCode.NORMAL, DataFilter.appJsonFilter(ret, req, Constants.SMALL_PIC));
    }

    /**
     * 取得某用户的路线
     *
     * @param userId
     * @param page
     * @param pageSize
     * @return
     */
    public static Result getUGCPlans(String userId, String ugcPlanId, String updateTime, int page, int pageSize) {

        try {
            //根据ID取得UGC路线
            if (!ugcPlanId.equals("")) {
                UgcPlan ugcPlan = PlanAPI.getPlanById(ugcPlanId);
                //根据ID取用户路线时，先判断时间戳，是否需要更新
                //如果不需要更新，只返回一个标识，以节省流量
                long updateTimeInDB = ugcPlan.updateTime;
                if (!updateTime.equals("")) {
                    Long appTimeStamp = Long.parseLong(updateTime);
                    if (appTimeStamp.longValue() == updateTimeInDB) {
                        return Utils.createResponse(ErrorCode.DONOTNEED_UPDATE, "DO NOT NEED UPDATE");
                    }
                }
                //取详细信息
                JsonNode planJson = ugcPlan.toJson(true);
                planJson = DataFilter.appJsonFilter(planJson, request(), Constants.BIG_PIC);
                return Utils.createResponse(ErrorCode.NORMAL, DataFilter.appJsonFilter(planJson, request(), Constants.BIG_PIC));
            }
            //根据用户ID取得UGC路线列表
            if (!userId.equals("")) {
                List<JsonNode> results = new ArrayList<JsonNode>();
                for (Iterator<UgcPlan> it = PlanAPI.getPlanByUser(userId, page, pageSize); it.hasNext(); ) {
                    //取粗略信息
                    results.add(it.next().toJson(false));
                }
                return Utils.createResponse(ErrorCode.NORMAL, DataFilter.appJsonFilter(Json.toJson(results), request(), Constants.SMALL_PIC));
            }
            return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, "Error:INVALID ARGUMENT ");
        } catch (ClassCastException ec) {
            return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, ec.getMessage());
        } catch (TravelPiException e) {
            return Utils.createResponse(e.errCode, e.getMessage());
        }
    }

    /**
     * 取得分享的路线
     *
     * @param planId
     * @return
     */
    public static Result getSharePlans(String planId) {

        try {
            //根据ID取得分享的路线
            if (!planId.equals("")) {
                SharePlan sharePlan = PlanAPI.getSharePlanById(planId);

                //取详细信息
                JsonNode planJson = sharePlan.toJson(true);
                return Utils.createResponse(ErrorCode.NORMAL, planJson);
            }

            return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, "Error:INVALID ARGUMENT ");
        } catch (TravelPiException e) {
            return Utils.createResponse(e.errCode, e.getMessage());
        }
    }

    public static Result deleteUGCPlans(String ugcPlanId) {
        try {
            PlanAPI.deleteUGCPlan(ugcPlanId);
            return Utils.createResponse(ErrorCode.NORMAL, "Success");
        } catch (TravelPiException e) {
            return Utils.createResponse(e.errCode, e.getMessage());
        }
    }

    /**
     * 分享
     *
     * @return
     */
    public static Result shareUGCPlan() {
        try {

            JsonNode data = request().body().asJson();

            Configuration config = Configuration.root();
            Map shareConf = (Map) config.getObject("share");

            String domain = shareConf.get("domain").toString();
            String url = shareConf.get("url").toString();
            StringBuffer uri = new StringBuffer(10);
            uri.append("http://");
            uri.append(domain);
            uri.append("/");
            uri.append(url);
            uri.append("?");
            uri.append("id=");
            uri.append(data.get("_id").asText());

            updateUGCPlan(data, SHAREPLAN);

            Share share = new Share();
            List<String> urlList = new ArrayList<String>();
            urlList.add(uri.toString());
            share.urls = urlList;
            return Utils.createResponse(ErrorCode.NORMAL, DataFilter.appJsonFilter(share.toJson(), request(), Constants.SMALL_PIC));
        } catch (ClassCastException | NullPointerException ec) {
            return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, ec.getMessage());
        } catch (TravelPiException | NoSuchFieldException | InstantiationException | ParseException | IllegalAccessException e) {
            return Utils.createResponse(ErrorCode.INVALID_ARGUMENT, e.getMessage());
        }
    }
}
