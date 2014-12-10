package aizou.core;

import exception.AizouException;
import exception.ErrorCode;
import models.misc.TravelNote;
import models.plan.Plan;
import models.plan.PlanDayEntry;
import models.plan.PlanItem;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.bson.types.ObjectId;
import play.Configuration;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;

/**
 * 游记攻略
 *
 * @author Zephyre
 */
public class TravelNoteAPI {

    public static List<TravelNote> searchNoteByPlan(ObjectId planId) throws AizouException {
        Plan plan = PlanAPI.getPlan(planId, false);
        if (plan == null)
            plan = PlanAPI.getPlan(planId, true);
        if (plan == null)
            throw new AizouException(ErrorCode.INVALID_ARGUMENT, String.format("INVALID OBJECT ID: %s", planId.toString()));

        Map<String, String> tMap = new HashMap<>();
        List<String> viewSpots = new ArrayList<>();
        List<PlanDayEntry> details = plan.getDetails();
        if (details == null)
            details = new ArrayList<>();

        for (PlanDayEntry entry : details) {
            List<PlanItem> actv = entry.actv;
            if (actv == null)
                actv = new ArrayList<>();

            for (PlanItem item : actv) {
                if (item.type == null || !item.type.equals("vs"))
                    continue;

                viewSpots.add(item.item.zhName);
                String fullName = item.loc.zhName;
                if (!tMap.containsKey(fullName)) {
                    String shortName = LocalityAPI.getLocality(item.loc.id).getZhName();
//                    String shortName = LocalityAPI.getLocality(item.loc.id).shortName;
                    tMap.put(fullName, shortName);
                }
            }
        }
        Collection<String> targets = tMap.values();

        List<TravelNote> results = new ArrayList<>();

        SolrDocumentList docs;
        try {
            Configuration config = Configuration.root().getConfig("solr");
            String host = config.getString("host", "localhost");
            Integer port = config.getInt("port", 8983);
            String url = String.format("http://%s:%d/solr", host, port);
            /*
            HttpSolrServer is thread-safe and if you are using the following constructor,
            you *MUST* re-use the same instance for all requests.  If instances are created on
            the fly, it can cause a connection leak. The recommended practice is to keep a
            static instance of HttpSolrServer per solr server url and share it for all requests.
            See https://issues.apache.org/jira/browse/SOLR-861 for more details
            */
            SolrServer server = new HttpSolrServer(url);
            SolrQuery query = new SolrQuery();

            StringBuilder sb = new StringBuilder();
            for (String t : targets)
                sb.append(String.format(" title:%s toLoc:%s", t, t));

            for (String t : viewSpots)
                sb.append(String.format(" contents:%s", t));

            query.setQuery(sb.toString().trim()).addField("authorName").addField("_to").addField("title").addField("contents")
                    .addField("sourceUrl").addField("commentCnt").addField("viewCnt").addField("authorAvatar");

            docs = server.query(query).getResults();

            for (SolrDocument doc : docs) {
                TravelNote note = new TravelNote();
                Object tmp;
                note.authorName = (String) doc.get("authorName");
                note.title = (String) doc.get("title");
                tmp = doc.get("authorAvatar");
                note.authorAvatar = (tmp != null ? (String) tmp : "");
                if (!note.authorAvatar.startsWith("http://"))
                    note.authorAvatar = "http://" + note.authorAvatar;
                tmp = doc.get("favorCnt");
                note.favorCnt = (tmp != null ? ((Long) tmp).intValue() : 0);
                note.contents = (List) doc.get("contents");
                note.sourceUrl = (String) doc.get("url");
                note.source = "baidu";
                tmp = doc.get("commentCnt");
                note.commentCnt = (tmp != null ? ((Long) tmp).intValue() : 0);
                tmp = doc.get("viewCnt");
                note.viewCnt = (tmp != null ? ((Long) tmp).intValue() : 0);
                note.publishDate = new Date();
                tmp = doc.get("sourceUrl");
                note.sourceUrl = (tmp != null ? (String) tmp : "");

                if (note.contents.size() > 1) {
                    sb = new StringBuilder();
                    for (int i = 1; i < note.contents.size(); i++) {
                        String c = note.contents.get(i);
                        if (Pattern.matches("^\\s*http.+", c))
                            continue;
                        sb.append(c);
                        sb.append('\n');
                        if (sb.length() > 200)
                            break;
                    }
                    String summary = sb.toString().trim();
                    if (summary.length() > 200)
                        summary = summary.substring(0, 200) + "……";
                    note.summary = summary;
                }
                results.add(note);
            }

            return results;

        } catch (SolrServerException e) {
            throw new AizouException(ErrorCode.UNKOWN_ERROR, e.getMessage());
        }
    }

    public static List<TravelNote> searchNoteByLoc(List<String> lcoNames, List<String> vsNames, int page, int pageSize) throws AizouException {

        SolrDocumentList docs;
        try {
            Configuration config = Configuration.root().getConfig("solr");
            String host = config.getString("host", "localhost");
            Integer port = config.getInt("port", 8983);
            String url = String.format("http://%s:%d/solr", host, port);
            /*
            HttpSolrServer is thread-safe and if you are using the following constructor,
            you *MUST* re-use the same instance for all requests.  If instances are created on
            the fly, it can cause a connection leak. The recommended practice is to keep a
            static instance of HttpSolrServer per solr server url and share it for all requests.
            See https://issues.apache.org/jira/browse/SOLR-861 for more details
            */
            SolrServer server = new HttpSolrServer(url);
            SolrQuery query = new SolrQuery();
            StringBuilder sb = new StringBuilder();
            if (lcoNames != null) {
                for (String t : lcoNames)
                    sb.append(String.format(" title:%s toLoc:%s", t, t));
            }
            if (vsNames != null) {
                for (String t : vsNames)
                    sb.append(String.format(" contents:%s", t));
            }
            query.setQuery(sb.toString().trim()).addField("authorName").addField("_to").addField("title").addField("source")
                    .addField("sourceUrl").addField("commentCnt").addField("viewCnt").addField("authorAvatar").addField("contents").addField("id");
            query.setStart(page);
            query.setRows(pageSize);

            docs = server.query(query).getResults();
            return getTravelNotesByDocuments(docs);

        } catch (SolrServerException e) {
            throw new AizouException(ErrorCode.UNKOWN_ERROR, e.getMessage());
        }
    }

    public static List<TravelNote> searchNoteById(List<String> ids, int pageSize) throws AizouException {

        List<TravelNote> results = new ArrayList<>();
        SolrDocumentList docs;
        try {
            Configuration config = Configuration.root().getConfig("solr");
            String host = config.getString("host", "localhost");
            Integer port = config.getInt("port", 8983);
            String url = String.format("http://%s:%d/solr", host, port);
            /*
            HttpSolrServer is thread-safe and if you are using the following constructor,
            you *MUST* re-use the same instance for all requests.  If instances are created on
            the fly, it can cause a connection leak. The recommended practice is to keep a
            static instance of HttpSolrServer per solr server url and share it for all requests.
            See https://issues.apache.org/jira/browse/SOLR-861 for more details
            */
            SolrServer server = new HttpSolrServer(url);
            SolrQuery query = new SolrQuery();
            StringBuilder sb = new StringBuilder();
            if (ids != null) {
                for (String t : ids)
                    sb.append(String.format(" id:%s", t));
            }
            query.setQuery(sb.toString().trim()).addField("authorName").addField("_to").addField("title")
                    .addField("sourceUrl").addField("commentCnt").addField("viewCnt").addField("authorAvatar").addField("contents").addField("id");
            query.setRows(pageSize);
            docs = server.query(query).getResults();
            return getTravelNotesByDocuments(docs);

        } catch (SolrServerException e) {
            throw new AizouException(ErrorCode.UNKOWN_ERROR, e.getMessage());
        }
    }

    /**
     * 根據Solr文檔生成遊記對象
     *
     * @param docs
     * @return
     */
    private static List<TravelNote> getTravelNotesByDocuments(SolrDocumentList docs) {
        List<TravelNote> results = new ArrayList<>();
        StringBuilder sb;
        TravelNote note;
        for (SolrDocument doc : docs) {
            note = new TravelNote();
            Object tmp;
            note.setId(new ObjectId(doc.get("id").toString()));
            note.authorName = (String) doc.get("authorName");
            note.title = (String) doc.get("title");
            tmp = doc.get("authorAvatar");
            note.authorAvatar = (tmp != null ? (String) tmp : "");
            if (!note.authorAvatar.startsWith("http://"))
                note.authorAvatar = "http://" + note.authorAvatar;
            tmp = doc.get("favorCnt");
            try {
                note.setId(new ObjectId(doc.get("id").toString()));
            } catch (IllegalArgumentException e) {
            }
            note.favorCnt = (tmp != null ? ((Long) tmp).intValue() : 0);
            note.contents = (List) doc.get("contents");
            note.sourceUrl = (String) doc.get("url");
            note.source = (String) doc.get("source");
            tmp = doc.get("commentCnt");
            note.commentCnt = (tmp != null ? ((Long) tmp).intValue() : 0);
            tmp = doc.get("viewCnt");
            note.viewCnt = (tmp != null ? ((Long) tmp).intValue() : 0);
            note.publishDate = new Date();
            tmp = doc.get("sourceUrl");
            note.sourceUrl = (tmp != null ? (String) tmp : "");
            note.contents = (List) doc.get("contents");
            note.source = getSource((String) doc.get("source"));
            note.publishDate = (Date) doc.get("publishDate");
            // TODO
            note.cover = "http://e.hiphotos.baidu.com/lvpics/s%3D800/sign=caab32ee3987e9504617fe6c2039531b/9a504fc2d56285359976ef0c93ef76c6a7ef630c.jpg";

            if (note.contents.size() > 1) {
                sb = new StringBuilder();
                for (int i = 1; i < note.contents.size(); i++) {
                    String c = note.contents.get(i);
                    if (Pattern.matches("^\\s*http.+", c))
                        continue;
                    sb.append(c);
                    sb.append('\n');
                    if (sb.length() > 200)
                        break;
                }
                String summary = sb.toString().trim();
                if (summary.length() > 200)
                    summary = summary.substring(0, 200) + "……";
                note.summary = summary;
            }
            results.add(note);
        }

        return results;
    }

    /**
     * 游记来源
     *
     * @param source
     * @return
     */
    public static String getSource(String source) {
        Map<String, String> map = new HashMap<>();
        map.put("chanyouji", "禅游记");
        map.put("baidu", "百度");
        map.put("mafengwo", "蚂蜂窝");
        if (map.containsKey(source))
            return map.get(source);
        else
            return "";
    }

    /**
     * 转换日期格式
     *
     * @param date
     * @return
     * @throws ParseException
     */
    public static String dateFormat(Date date) throws ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        return dateFormat.format(date);
    }


    /**
     * 通过id获取游记
     *
     * @param id
     * @return
     * @throws SolrServerException
     * @throws ParseException
     */
    public static List<TravelNote> getTravelNoteDetailApi(String id) throws SolrServerException, ParseException {
        SolrDocumentList docs;
        List<TravelNote> results = new ArrayList<>();
        //配置solr
        Configuration config = Configuration.root().getConfig("solr");
        String host = config.getString("host", "http://api.lvxingpai.cn");
        Integer port = config.getInt("port", 8983);
        String url = String.format("http://%s:%d/solr", host, port);
        SolrServer server = new HttpSolrServer(url);
        SolrQuery query = new SolrQuery();

        String queryString = String.format("id:%s", id);
        query.setQuery(queryString.trim());
                /*.addField("id").addField("authorName").addField("title")
                .addField("authorAvatar").addField("contents").addField("cover").addField("elite")
                .addField("source").addField("startDate").addField("sourceUrl").addField("toLoc")
                .addField("viewCnt").addField("commentCnt");*/

        docs = server.query(query).getResults();

        for (SolrDocument doc : docs) {
            Boolean elite = (Boolean) doc.get("elite");
                /*if (!elite)
                    continue;*/
            TravelNote note = new TravelNote();
            Object tmp;
            note.authorName = (String) doc.get("authorName");
            note.title = (String) doc.get("title");
            tmp = doc.get("authorAvatar");
            note.authorAvatar = (tmp != null ? (String) tmp : "");
            if (!note.authorAvatar.startsWith("http://"))
                note.authorAvatar = "http://" + note.authorAvatar;
            tmp = doc.get("favorCnt");
            note.favorCnt = (tmp != null ? ((Long) tmp).intValue() : 0);
            note.contents = procContents((List) doc.get("contents"));
            note.sourceUrl = (String) doc.get("url");
            note.source = getSource((String) doc.get("source"));
            tmp = doc.get("commentCnt");
            note.commentCnt = (tmp != null ? ((Long) tmp).intValue() : 0);
            tmp = doc.get("viewCnt");
            note.viewCnt = (tmp != null ? ((Long) tmp).intValue() : 0);
            tmp = doc.get("sourceUrl");
            note.sourceUrl = (tmp != null ? (String) tmp : "");
            tmp = doc.get("costUpper");
            note.costUpper = (tmp != null ? Float.parseFloat(String.valueOf(tmp)) : -1);
            tmp = doc.get("costLower");
            note.costLower = (tmp != null ? Float.parseFloat(String.valueOf(tmp)) : -1);
            note.publishDate = (Date) doc.get("publishDate");
            results.add(note);
        }
        return results;

    }

    /**
     * 处理游记正文
     *
     * @param contents
     * @return
     */
    public static List<String> procContents(List<String> contents) {
        StringBuilder sb = new StringBuilder();
        sb.append("<div>");
        for (String line : contents) {
            if (line.startsWith("img src")) {
                line = line.replaceAll("\\\\", "");
                sb.append("<" + line + ">");
            } else if (line.startsWith("http://")) {
                sb.append("<img src=" + line + " >");
            } else
                sb.append("<p> " + line + "</p>");
        }
        sb.append("</div>");
        List<String> list = new ArrayList<>();
        list.add(sb.toString().trim());
        return list;
    }


}
