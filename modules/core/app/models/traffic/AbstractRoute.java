package models.traffic;

import com.fasterxml.jackson.databind.JsonNode;
import com.mongodb.BasicDBObjectBuilder;
import models.AizouBaseEntity;
import models.ITravelPiFormatter;
import models.misc.SimpleRef;
import org.mongodb.morphia.annotations.Embedded;
import play.libs.Json;

import javax.validation.constraints.NotNull;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 交通信息的抽象类。
 *
 * @author Zephyre
 */
public abstract class AbstractRoute extends AizouBaseEntity implements ITravelPiFormatter {
    @NotNull
    @Embedded
    public SimpleRef depStop;

    @NotNull
    @Embedded
    public SimpleRef arrStop;

    @NotNull
    public String code;

    @NotNull
    @Embedded
    public SimpleRef depLoc;

    @NotNull
    @Embedded
    public SimpleRef arrLoc;

    public Integer distance;

    @NotNull
    public Integer timeCost;

    @NotNull
    public Date depTime;

    @NotNull
    public Date arrTime;

    @Override
    public JsonNode toJson() {
        BasicDBObjectBuilder builder = BasicDBObjectBuilder.start().add("_id", getId().toString()).add("code", code);

        for (Map.Entry<String, String> entry : new HashMap<String, String>() {
            {
                put("depLoc", "depLoc");
                put("arrLoc", "arrLoc");
            }
        }.entrySet()) {
            String k = entry.getKey();
            String v = entry.getValue();
            SimpleRef val = null;
            try {
                val = (SimpleRef) AbstractRoute.class.getField(k).get(this);
            } catch (IllegalAccessException | NoSuchFieldException ignored) {
            }
            //PC_Chen:return {} instead of ""
            builder.add(v, val != null ? val.toJson() : new HashMap<>());
        }
        //basic data type fields
        for (String k : new String[]{"distance", "timeCost"}) {//, "jetName", "jetFullName", "depTerm", "arrTerm"}) {
            Object val = null;
            try {
                val = AbstractRoute.class.getField(k).get(this);
            } catch (NoSuchFieldException | IllegalAccessException ignored) {
            }
            if (val != null)
                builder.add(k, val);
        }

        final DateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ");
        for (String k : new String[]{"depTime", "arrTime"}) {
            Date val = null;
            try {
                val = (Date) AbstractRoute.class.getField(k).get(this);
            } catch (NoSuchFieldException | IllegalAccessException ignored) {
            }
            builder.add(k, val != null ? fmt.format(val) : "");
        }

        return Json.toJson(builder.get());
    }

    public void addColumn(BasicDBObjectBuilder builder, Class routesCls, String... keys) {

        //basic data type fields
        for (String k : keys) {
            Object val = null;
            try {
                val = routesCls.getField(k).get(this);
            } catch (NoSuchFieldException | IllegalAccessException ignored) {
            }
            if (val != null) {

                builder.add(k, val);
            }

        }

    }
}
