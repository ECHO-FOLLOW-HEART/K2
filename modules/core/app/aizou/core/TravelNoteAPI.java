package aizou.core;

import exception.ErrorCode;
import exception.TravelPiException;
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

import java.util.*;
import java.util.regex.Pattern;

/**
 * 游记攻略
 *
 * @author Zephyre
 */
public class TravelNoteAPI {

    public static List<TravelNote> searchNoteByPlan(ObjectId planId) throws TravelPiException {
        Plan plan = PlanAPI.getPlan(planId, false);
        if (plan == null)
            plan = PlanAPI.getPlan(planId, true);
        if (plan == null)
            throw new TravelPiException(ErrorCode.INVALID_ARGUMENT, String.format("INVALID OBJECT ID: %s", planId.toString()));

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
            throw new TravelPiException(ErrorCode.UNKOWN_ERROR, e.getMessage());
        }
    }

    public static List<TravelNote> searchNoteByLoc(List<String> lcoNames, List<String> vsNames, int pageSize) throws TravelPiException {

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
            if (lcoNames != null) {
                for (String t : lcoNames)
                    sb.append(String.format(" title:%s toLoc:%s", t, t));
            }

            if (vsNames != null) {
                for (String t : vsNames)
                    sb.append(String.format(" contents:%s", t));
            }

            query.setQuery(sb.toString().trim()).addField("authorName").addField("_to").addField("title")
                    .addField("sourceUrl").addField("commentCnt").addField("viewCnt").addField("authorAvatar").addField("contents");
            query.setRows(pageSize);

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
            throw new TravelPiException(ErrorCode.UNKOWN_ERROR, e.getMessage());
        }
    }

}
