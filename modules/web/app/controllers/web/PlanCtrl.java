package controllers.web;

import aizou.core.PlanAPI;
import aizou.core.PoiAPI;
import aizou.core.TrafficAPI;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import exception.ErrorCode;
import exception.TravelPiException;
import models.MorphiaFactory;
import models.geo.Locality;
import models.misc.Share;
import models.misc.SimpleRef;
import models.plan.*;
import models.poi.AbstractPOI;
import models.poi.Hotel;
import models.poi.Restaurant;
import models.poi.ViewSpot;
import models.traffic.AirRoute;
import models.traffic.Airport;
import models.traffic.TrainRoute;
import models.traffic.TrainStation;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;
import play.Configuration;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import utils.*;
import utils.formatter.travelpi.plan.SimpleUgcPlanFormatter;

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

    public static String UGCPLAN = "UgcPlan";
    public static String SHAREPLAN = "SharePlan";

    /**
     * 查询路线的详细信息。
     *
     * @param planId    路线id
     * @param fromLocId 从哪个城市出发
     * @return
     * @throws java.net.UnknownHostException
     */
    public static Result getPlanFromTemplates(String planId, String fromLocId, String backLocId, int webFlag, String uid) {
        try {
            Http.Request req = request();
            if (fromLocId.equals("")) {
                Plan plan = PlanAPI.getPlan(planId, false);
                if (plan == null)
                    throw new TravelPiException(ErrorCode.INVALID_ARGUMENT, String.format("Invalid plan ID: %s.", planId.toString()));
                return Utils.createResponse(ErrorCode.NORMAL, plan.toJson());
            }
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_YEAR, 3);

            // 处理fromLoc/backLoc的映射
            Map<String, Object> mapConf = Configuration.root().getConfig("locMapping").asMap();
            Object tmp = mapConf.get(fromLocId);
            if (tmp != null)
                fromLocId = tmp.toString();
            tmp = mapConf.get(backLocId);
            if (tmp != null)
                backLocId = tmp.toString();

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

            Double trafficBudgetT;
            List<SimpleRef> targets = plan.getTargets();
            if (null != targets && targets.size() > 0) {
                trafficBudgetT = getTrafficBudget(fromLocId, plan.getTargets().get(0).id.toString());
            } else {
                trafficBudgetT = trafficBudgetDefault;
            }
            plan.setTrafficBudget((int) trafficBudgetT.doubleValue());
            plan.setStayBudget(plan.getDays() * stayBudgetDefault);

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

    /**
     * 获得交通预算
     *
     * @return
     */
    private static Double getTrafficBudget(String depId, String arrId) throws TravelPiException {
        ObjectId depOid, arrOid;
        try {
            depOid = new ObjectId(depId);
            arrOid = new ObjectId(arrId);
        } catch (IllegalArgumentException e) {
            throw new TravelPiException(ErrorCode.INVALID_ARGUMENT, "Invalid locality ID.");
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

            // TODO
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


    private static UgcPlan updatePlanByNode(JsonNode data, String uid) throws TravelPiException {
        String ugcPlanId = data.get("_id").asText();
        String templateId = data.get("templateId").asText();
        String title = data.get("title").asText();
        //String uid = data.has("uid") ? data.get("uid").asText() : "";

        DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat timeFmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ");

        Plan plan = PlanAPI.getPlan(templateId, false);
        if (plan == null)
            throw new TravelPiException(ErrorCode.INVALID_ARGUMENT, String.format("Invalid plan ID: %s.", templateId));
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
                    if (subTypeStr.equals("airport") || subTypeStr.equals("trainStation")) {
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
        ugcPlan.setDetails(planDayEntryList);
        ugcPlan.setStayBudget(Integer.parseInt(data.get("stayBudget").asText()));
        ugcPlan.setViewBudget(Integer.parseInt(data.get("viewBudget").asText()));
        ugcPlan.setTrafficBudget(Integer.parseInt(data.get("trafficBudget").asText()));
        //设置UGC路线ID
        ugcPlan.setId(new ObjectId(ugcPlanId));
        ugcPlan.setTemplateId(new ObjectId(templateId));
        ugcPlan.setStartDate(startDate);
        ugcPlan.setEndDate(endDate);
        ugcPlan.setTitle(title);
        //uid为空，中间态数据；否则为保存态数据。中间态数据会被定期清理。
        if (!uid.equals("")) {
            ugcPlan.setUid(new ObjectId(uid));
            ugcPlan.setPersisted(true);

        } else {
            ugcPlan.setPersisted(false);
        }
        ugcPlan.setUpdateTime((new Date()).getTime());
        ugcPlan.setEnabled(true);
        ugcPlan.setIsFromWeb(true);

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
        List<PlanDayEntry> details = plan.getDetails();
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
        plan.setBudget(budget);

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
     * 保存用户的路线
     *
     * @return
     */
    public static Result saveUGCPlan() {
        ObjectNode data = (ObjectNode) request().body().asJson();
        JsonNode action = data.get("action");
        String updateField = null;
        String ugcPlanId = null;

        String abc = request().getQueryString("uid1");
        String uid = request().getQueryString("uid");
        if (!data.has("uid") && uid != null)
            data.put("uid", uid);

        try {
            String actionFlag = action.asText();
            ugcPlanId = data.get("_id") == null ? null : data.get("_id").asText();
            ObjectId oid = ugcPlanId == null ? new ObjectId() : new ObjectId(ugcPlanId);
            //只更新标题
            if (actionFlag.equals("updateTitle")) {

                updateField = data.get("title").asText();
                //记录日志
                LogUtils.info(Plan.class, "UpdateTitle:" + updateField);
                LogUtils.info(Plan.class, request());

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
     * @throws exception.TravelPiException
     * @throws java.text.ParseException
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
            throw new TravelPiException(ErrorCode.INVALID_ARGUMENT, String.format("Invalid plan ID: %s.", templateId));

        //补全信息
        List<PlanDayEntry> dayEntryList = raw2plan(details, trafficInfo, startCal, endCal, false);
        List<JsonNode> retDetails = new ArrayList<>();
        for (PlanDayEntry dayEntry : dayEntryList)
            retDetails.add(dayEntry.toJson());
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
                if (subTypeStr.equals("airport") || subTypeStr.equals("trainStation")) {
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
        ugcPlan.setDetails(planDayEntryList);

        ugcPlan.setStayBudget(Integer.parseInt(data.get("stayBudget").asText()));
        ugcPlan.setViewBudget(Integer.parseInt(data.get("viewBudget").asText()));
        ugcPlan.setTrafficBudget(Integer.parseInt(data.get("trafficBudget").asText()));
        //设置UGC路线ID
        ugcPlan.setId(new ObjectId(ugcPlanId));
        ugcPlan.setStartDate(startDate);
        ugcPlan.setEndDate(endDate);
        ugcPlan.setTitle(title);
        //分享接口
        if (!uid.equals("")) {
            ugcPlan.setUid(new ObjectId(uid));
        }
        ugcPlan.setUpdateTime((new Date()).getTime());
        ugcPlan.setEnabled(true);

        if (saveToTable.equals(SHAREPLAN)) {
            SharePlan sharePlan = new SharePlan(ugcPlan);

            PlanAPI.saveSharePlan(sharePlan);
        } else {
            PlanAPI.saveUGCPlan(ugcPlan);
        }

    }


    /**
     * 路线发现机制，相对旅行派接口有改动
     *
     * @param locId
     * @param sortField
     * @param sort
     * @param tag
     * @param page
     * @param pageSize
     * @return
     */
    public static Result explorePlans(String fromLoc, String locId, String poiId, String sortField, String sort, String tag, int minDays, int maxDays, int page, int pageSize) {
        List<JsonNode> results = new ArrayList<>();

        // 处理fromLoc/backLoc的映射
        Map<String, Object> mapConf = Configuration.root().getConfig("locMapping").asMap();
        Object tmp = mapConf.get(fromLoc);
        if (tmp != null)
            fromLoc = tmp.toString();

        try {
            Double trafficBudget = 0.0;
            if (fromLoc != null && !fromLoc.isEmpty() && locId != null && !locId.isEmpty())
                trafficBudget = getTrafficBudget(fromLoc, locId);

            int stayBudgetDefault = 0;
            try {
                //取得预算常量
                Map<String, Object> budget = Configuration.root().getConfig("budget").asMap();
                if (budget != null)
                    stayBudgetDefault = Integer.valueOf(budget.get("stayBudgetDefault").toString());
            } catch (ClassCastException ignored) {
            }


            List<Plan> planList = new ArrayList<>();
            for (Iterator<Plan> it = PlanAPI.explore(locId, poiId, sort, tag, minDays, maxDays, page,
                    pageSize, sortField); it.hasNext(); )
                planList.add(it.next());

            // 有tag的排在前面
            Collections.sort(planList, new Comparator<Plan>() {
                @Override
                public int compare(Plan o1, Plan o2) {
                    if (o1.getManualPriority() == null)
                        o1.setManualPriority(0);
                    if (o2.getManualPriority() == null)
                        o2.setManualPriority(0);

                    if (o1.getManualPriority() > o2.getManualPriority())
                        return 1;
                    else if (o1.getManualPriority() < o2.getManualPriority())
                        return -1;
                    else {
                        int tag1 = o1.getLxpTag() != null ? o1.getLxpTag().size() : 0;
                        int tag2 = o2.getLxpTag() != null ? o2.getLxpTag().size() : 0;
                        return tag2 - tag1;
                    }
                }
            });

            for (Plan plan : planList) {
                //加入交通预算,住宿预算
                if (null != fromLoc && !fromLoc.trim().equals("")) {
                    addTrafficBudget(plan, trafficBudget, stayBudgetDefault);
                    results.add(plan.toJson(false));
                } else {
                    results.add(plan.toJson(false));
                }
            }

            return Utils.createResponse(ErrorCode.NORMAL, DataFilter.appJsonFilter(Json.toJson(results), request(), Constants.SMALL_PIC));
        } catch (TravelPiException e) {
            return Utils.createResponse(e.errCode, e.getMessage());
        }

    }

    /**
     * 路线中加入交通预算和食宿预算
     *
     * @param plan
     * @param trafficBudg
     * @param stayBudgetDefault
     * @return
     * @throws ClassCastException
     */
    private static void addTrafficBudget(Plan plan, Double trafficBudg, int stayBudgetDefault) {
        if (null != plan) {
            plan.setTrafficBudget((int) trafficBudg.doubleValue());
            plan.setStayBudget(plan.getDays() == null ? 0 : plan.getDays() * stayBudgetDefault);
        }
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

        PlanItem planItem = null;
        Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.POI);
        SimpleRef ref, locRef;
        switch (type) {
            case "vs":
                ViewSpot vs = ds.createQuery(ViewSpot.class).field("_id").equal(new ObjectId(itemId)).get();
                ref = new SimpleRef();
                ref.id = vs.getId();
                ref.zhName = vs.name;
                planItem = new PlanItem();
                planItem.item = ref;
                locRef = new SimpleRef();
                locRef.setEnName(vs.getLocality().getEnName());
                locRef.setZhName(vs.getLocality().getZhName());
                locRef.setId(vs.getLocality().getId());
                planItem.loc = locRef;
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
                ref.id = hotel.getId();
                ref.zhName = hotel.name;
                planItem = new PlanItem();
                planItem.item = ref;
                locRef = new SimpleRef();
                locRef.setEnName(hotel.getLocality().getEnName());
                locRef.setZhName(hotel.getLocality().getZhName());
                locRef.setId(hotel.getLocality().getId());
                planItem.loc = locRef;
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
                ref.id = trainStation.getId();
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
                ref.id = airRoute.getId();
                ref.zhName = airRoute.code;
                planItem = new PlanItem();
                planItem.item = ref;
                planItem.type = "traffic";
                planItem.subType = "airRoute";
                planItem.transfer = item.get("transfer") == null ? "" : item.get("transfer").asText();
                try {
                    planItem.ts = fmt.parse(item.get("ts").asText());
                } catch (ParseException | NullPointerException ignored) {
                }
                break;
            case "trainRoute":
                TrainRoute trainRoute = ds.createQuery(TrainRoute.class).field("_id").equal(new ObjectId(itemId)).get();
                ref = new SimpleRef();
                ref.id = trainRoute.getId();
                ref.zhName = trainRoute.code;
                planItem = new PlanItem();
                planItem.item = ref;
                planItem.type = "traffic";
                planItem.subType = "trainRoute";
                planItem.transfer = item.get("transfer") == null ? "" : item.get("transfer").asText();
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
    public static Result getUGCPlans(String userId, String ugcPlanId, int page, int pageSize) {
        try {
            //根据ID取得UGC路线
            if (!ugcPlanId.equals("")) {
                UgcPlan ugcPlan = PlanAPI.getPlanById(ugcPlanId);

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

    public static Result deleteUGCPlans(String ugcPlanId) {
        try {
            PlanAPI.deleteUGCPlan(ugcPlanId);
            return Utils.createResponse(ErrorCode.NORMAL, "Success");
        } catch (TravelPiException e) {
            return Utils.createResponse(e.errCode, e.getMessage());
        }
    }
}
