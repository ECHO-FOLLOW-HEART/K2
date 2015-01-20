package formatter;

import com.fasterxml.jackson.databind.JsonSerializer;
import models.misc.Proxy;

import java.util.HashMap;
import java.util.Map;

/**
 * 获得代理服务器信息
 * <p/>
 * Created by zephyre on 10/29/14.
 */
public class ProxyFormatter extends AizouFormatter<Proxy> {

    public ProxyFormatter() {
        Map<Class<? extends Proxy>, JsonSerializer<Proxy>> serializerMap = new HashMap<>();

        serializerMap.put(Proxy.class, new ProxySerializer());
        mapper = initObjectMapper(null, serializerMap);
    }
}
