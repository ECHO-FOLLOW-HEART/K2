package controllers.core;

import exception.AizouException;
import exception.ErrorCode;
import formatter.FormatterFactory;
import formatter.ProxyFormatter;
import com.lvxingpai.k2.core.MorphiaFactory;
import models.misc.Proxy;
import org.mongodb.morphia.query.Query;
import play.mvc.Result;
import utils.Utils;

import java.util.Date;

public class MiscCtrl {
    /**
     * 获得代理服务器数据
     *
     * @param latency  过滤：延迟低于latency的数据才会被返回
     * @param recently 过滤：表示只返回最近XX小时的数据
     */
    public static Result getProxies(String verifier, float latency, int recently, int page, int pageSize)
            throws IllegalAccessException, InstantiationException, AizouException {
        Query<Proxy> query = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.MISC).createQuery(Proxy.class);

        if (verifier == null || verifier.isEmpty())
            verifier = "baidu";

        query.field(String.format("verified.%s", verifier)).equal(true);
        if (latency > 0)
            query.field(String.format("latency.%s", verifier)).lessThan(latency);
        if (recently > 0) {
            Date date = new Date();
            date.setTime(System.currentTimeMillis() - recently * 3600 * 1000L);
            query.field("verifiedTime").greaterThanOrEq(date);
        }

        query.order(String.format("latency.%s", verifier)).offset(page * pageSize).limit(pageSize);

        ProxyFormatter formatter = FormatterFactory.getInstance(ProxyFormatter.class);

        return Utils.status(ErrorCode.NORMAL, formatter.format(query.asList()));
    }
}
