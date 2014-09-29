package models.morphia.plan;

import com.fasterxml.jackson.databind.JsonNode;
import com.mongodb.BasicDBObjectBuilder;
import core.PoiAPI;
import exception.TravelPiException;
import models.ITravelPiFormatter;
import models.morphia.misc.SimpleRef;
import models.morphia.poi.Hotel;
import models.morphia.poi.ViewSpot;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Transient;
import play.libs.Json;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * 路线规划中的基本单元。
 *
 * @author Zephyre
 */
@Embedded
public class PlanItem implements ITravelPiFormatter {
    @Embedded
    public SimpleRef item;

    @Embedded
    public SimpleRef loc;

    public Integer idx;

    public String type;

    public String subType;

    public Date ts;

    public String transfer;

    @Transient
    public Object extra;

    public double lat;
    public double lng;
    public String stopType;
    public ObjectId depStop;
    public ObjectId depLoc;
    public Date depTime;
    public ObjectId arrStop;
    public ObjectId arrLoc;
    public Date arrTime;
    public String distance;


    @Override
    public JsonNode toJson() {
        BasicDBObjectBuilder builder = BasicDBObjectBuilder.start();

        JsonNode itemNode = item.toJson();
        builder.add("itemId", itemNode.get("_id"));
        builder.add("itemName", itemNode.get("name"));
        if (loc != null) {
            JsonNode locNode = loc.toJson();
            builder.add("locId", locNode.get("_id"));
            builder.add("locName", locNode.get("name"));
        }
        builder.add("type", type != null ? type : "");
        builder.add("subType", subType != null ? subType : "");
        if (type != null && type.equals("traffic")) {
            builder.add("transfer", transfer != null ? transfer : "");
        }

        final DateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ");
        if (subType != null && (subType.equals("airport") || subType.equals("trainStaion"))) {
            if (lat != 0 && lng != 0) {
                builder.add("lat", lat);
                builder.add("lng", lng);
            }
            builder.add("stopType", stopType);
        }
        if (subType != null && (subType.equals("airRoute") || subType.equals("trainRoute"))) {
            builder.add("depStop", depStop == null ? depStop : depStop.toString());
            builder.add("depLoc", depLoc == null ? depLoc : depLoc.toString());
            builder.add("depTime", depTime == null ? "" : fmt.format(depTime));
            builder.add("arrStop", arrStop == null ? arrStop : arrStop.toString());
            builder.add("arrLoc", arrLoc == null ? arrLoc : arrLoc.toString());
            builder.add("arrTime", arrTime == null ? "" : fmt.format(arrTime));
            builder.add("distance", distance);
        }

        if (ts != null) {

            TimeZone tz = TimeZone.getTimeZone("Asia/Shanghai");
            fmt.setTimeZone(tz);
            builder.add("ts", fmt.format(ts));
        } else
            builder.add("ts", "");


        if (type != null && type.equals("vs")) {
            // 将景点详情嵌入
            try {
                ViewSpot vs = (ViewSpot) PoiAPI.getPOIInfo(item.id, PoiAPI.POIType.VIEW_SPOT, true);
                if (null != vs)
                    builder.add("details", PoiAPI.getPOIInfo(item.id, PoiAPI.POIType.VIEW_SPOT, true).toJson(3));
            } catch (TravelPiException ignored) {
            }
        }
        if (type != null && type.equals("hotel")) {
            // 将酒店详情嵌入
            try {
                Hotel ht = (Hotel) PoiAPI.getPOIInfo(item.id, PoiAPI.POIType.HOTEL, true);
                if (null != ht)
                    builder.add("details", PoiAPI.getPOIInfo(item.id, PoiAPI.POIType.HOTEL, true).toJson(3));
            } catch (TravelPiException ignored) {
            }
        }

        return Json.toJson(builder.get());
    }
}
