package aizou.core;

import exception.AizouException;
import models.MorphiaFactory;
import models.misc.Answer;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;
import play.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * 问答api
 * Created by lxf on 15-1-26.
 */
public class QaAPI {

    /**
     * 通过source获取bean
     *
     * @param source
     * @return
     * @throws exception.AizouException
     */
    public static Answer getNoteById(String source) throws AizouException {
        Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.MISC);
        Query<Answer> query = ds.createQuery(Answer.class).field("qId").equal(source);
        return query.get();
    }

    /**
     * 返回游记bean
     *
     * @return
     * @throws AizouException
     */
    public static List<Answer> getNotesById(List<String> sourceList) throws AizouException {
        List<Answer> qaList = new ArrayList<>();
        Answer qa;
        for (String source : sourceList) {
            qa = getNoteById(source);
            qaList.add(qa);
        }

        return qaList;
    }

    public static List<String> searchQaApi(String queryword, int page, int pageSize) throws SolrServerException {
        List<String> sourceList = new ArrayList<>();

        //solr连接配置
        Configuration config = Configuration.root().getConfig("solr");
        String host = config.getString("host", "localhost");
        Integer port = config.getInt("port", 8983);
        String coreName = config.getString("qaCore", "qaCore");
        String url = String.format("http://%s:%d/solr/%s", host, port, coreName);
        //进行查询，获取游记文档
        SolrServer server = new HttpSolrServer(url);
        SolrQuery query = new SolrQuery();
        String queryString = String.format("title:%s", queryword);
        query.setQuery(queryString).setStart(page * pageSize).setRows(pageSize);
        SolrDocumentList qaDocs = server.query(query).getResults();

        Object tmp;
        for (SolrDocument doc : qaDocs) {
//            Qa qa = new Qa();
//            //获取id
//            qa.setId(new ObjectId(doc.get("id").toString()));
//            //姓名
//            tmp = doc.get("authorName");
//            qa.authorName = (tmp == null ? null : (String) tmp);
//            //标题
//            tmp = doc.get("title");
//            qa.title = (tmp == null ? null : (String) tmp);
//            //头像
//            tmp = doc.get("authorAvatar");
//            qa.authorAvatar = (tmp == null ? null : (String) tmp);
//            //发表时间
//            tmp = doc.get("publishTime");
//            qa.publishTime = (tmp == null ? null : (long) tmp);
//            //是否精华帖
//            tmp = doc.get("essence");
//            qa.essence = (tmp == null ? null : (Boolean) tmp);
            //获取来源
            tmp = doc.get("source");
//          qa.source = (tmp == null ? null : (String) tmp);
            String source = (tmp == null ? null : (String) tmp);

            sourceList.add(source);
        }

        return sourceList;
    }

}
