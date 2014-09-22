package core;

import exception.ErrorCode;
import exception.TravelPiException;
import models.morphia.misc.TravelNote;
import models.morphia.plan.Plan;
import models.morphia.plan.PlanDayEntry;
import models.morphia.plan.PlanItem;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.bson.types.ObjectId;

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
            throw new TravelPiException(ErrorCode.INVALID_OBJECTID, String.format("INVALID OBJECT ID: %s", planId.toString()));

        Map<String, String> tMap = new HashMap<>();
        List<String> viewSpots = new ArrayList<>();
        List<PlanDayEntry> details = plan.details;
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
                    String shortName = LocalityAPI.getLocality(item.loc.id).shortName;
                    tMap.put(fullName, shortName);
                }
            }
        }
        Collection<String> targets = tMap.values();

        List<TravelNote> results = new ArrayList<>();

        SolrDocumentList docs;
        try {
            String url = "http://121.201.7.184:8983/solr";
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
                sb.append(String.format(" title:%s _to:%s", t, t));

            for (String t : viewSpots)
                sb.append(String.format(" contents:%s", t));

            query.setQuery(sb.toString().trim()).addField("authorName").addField("_to").addField("title").addField("contents")
                    .addField("url").addField("commentCnt").addField("viewCnt");

            docs = server.query(query).getResults();

            for (SolrDocument doc : docs) {
                TravelNote note = new TravelNote();
                note.authorName = (String) doc.get("authorName");
                note.title = ((List<String>) (doc.get("title"))).get(0);
                note.contents = (List<String>) doc.get("contents");
                note.sourceUrl = (String) doc.get("url");
                note.source = "baidu";
                note.commentCnt = (Integer) doc.get("commentCnt");
                note.viewCnt = (Integer) doc.get("viewCnt");

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
