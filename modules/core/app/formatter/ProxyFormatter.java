package formatter;

import models.misc.Proxy;

/**
 * 获得代理服务器信息
 * <p/>
 * Created by zephyre on 10/29/14.
 */
public class ProxyFormatter extends AizouFormatter<Proxy> {

    public ProxyFormatter() {
        registerSerializer(Proxy.class, new ProxySerializer());
        mapper = initObjectMapper(null);
    }
}
