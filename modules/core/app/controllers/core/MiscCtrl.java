package controllers.core;

import com.fasterxml.jackson.databind.JsonNode;
import exception.ErrorCode;
import exception.TravelPiException;
import models.MorphiaFactory;
import models.misc.Proxy;
import org.mongodb.morphia.query.Query;
import play.libs.Json;
import play.mvc.Result;
import utils.Utils;
import utils.formatter.ProxyFormatter;

import java.util.ArrayList;
import java.util.List;

public class MiscCtrl {
    /**
     * 获得代理服务器数据
     *
     * @param latency 过滤：延迟低于latency的数据才会被返回
     */
    public static Result getProxies(String verifier, float latency, int page, int pageSize) {
        try {
            Query<Proxy> query = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.MISC).createQuery(Proxy.class);

            if (verifier == null || verifier.isEmpty())
                verifier = "baidu";

            query.field(String.format("verified.%s", verifier)).equal(true);
            if (latency > 0)
                query.field(String.format("latency.%s", verifier)).lessThan(latency);

            query.order(String.format("latency.%s", verifier)).offset(page * pageSize).limit(pageSize);

            List<JsonNode> results = new ArrayList<>();

            for (Proxy proxy : query) {
                results.add(new ProxyFormatter().format(proxy));
            }
            return Utils.createResponse(ErrorCode.NORMAL, Json.toJson(results));
        } catch (TravelPiException e) {
            return Utils.createResponse(e.errCode, e.getMessage());
        }
    }
}
