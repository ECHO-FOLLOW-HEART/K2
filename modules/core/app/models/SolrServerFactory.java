package models;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import play.Configuration;

import java.util.Hashtable;

/**
 * 生成Solr连接池的工厂类
 *
 * Created by zephyre on 2/1/15.
 */
public class SolrServerFactory {
    private static Hashtable<String, SolrServer> solrPool = new Hashtable<>();

    public static SolrServer getSolrInstance(String coreName) {
        if (!solrPool.containsKey(coreName)) {
            synchronized (SolrServerFactory.class) {
                if (!solrPool.containsKey(coreName)) {
                    //solr连接配置
                    Configuration config = Configuration.root().getConfig("solr");
                    String host = config.getString("host", "localhost");
                    Integer port = config.getInt("port", 8983);
                    String url = String.format("http://%s:%d/solr/%s", host, port, config.getString(coreName));
                    //进行查询，获取游记文档
                    SolrServer server = new HttpSolrServer(url);

                    solrPool.put(coreName, server);
                }
            }
        }

        return solrPool.get(coreName);
    }
}
