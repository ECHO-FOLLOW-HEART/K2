package controllers.core;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import exception.ErrorCode;
import exception.TravelPiException;
import models.MorphiaFactory;
import models.misc.Proxy;
import org.mongodb.morphia.query.Query;
import play.libs.Json;
import play.mvc.Result;
import utils.Utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class MiscCtrl {
    /**
     * 获得代理服务器数据
     *
     * @param latency 过滤：延迟低于latency的数据才会被返回
     */
    public static Result getProxies(float latency, int page, int pageSize) {
        try {
            Query<Proxy> query = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.MISC).createQuery(Proxy.class);
            if (latency > 0)
                query.field("latency").lessThan(latency);
            query.field("verified").equal(true);
            query.order("latency").offset(page * pageSize).limit(pageSize);

            List<JsonNode> results = new ArrayList<>();

            final DateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ");
            for (Proxy proxy : query.asList()) {
                ObjectNode val = Json.newObject();
                val.put("host", proxy.host);
                val.put("port", proxy.port);
                val.put("latency", proxy.latency);


                val.put("verifiedTime", fmt.format(proxy.verifiedTime));
                val.put("desc", (proxy.desc != null ? proxy.desc : ""));

                results.add(val);
            }
            return Utils.createResponse(ErrorCode.NORMAL, Json.toJson(results));
        } catch (TravelPiException e) {
            return Utils.createResponse(e.errCode, e.getMessage());
        }
    }
}
