package aizou.core;

import exception.AizouException;
import exception.ErrorCode;
import models.MorphiaFactory;
import models.geo.Locality;
import models.misc.ImageItem;
import models.misc.TravelNote;
import models.plan.Plan;
import models.plan.PlanDayEntry;
import models.plan.PlanItem;
import models.poi.ViewSpot;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;
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

    public static List<TravelNote> solrRequest(String queryString, int page, int pageSize) throws AizouException {
        return solrRequest(queryString, Arrays.asList("authorName", "_to", "title",
                "source", "publishDate", "sourceUrl", "costLower", "costUpper",
                "commentCnt", "viewCnt", "authorAvatar", "contents", "id", "summary"), page, pageSize);
    }

    /**
     * 进行Solr的请求
     *
     * @param queryString
     * @param fields
     * @param page
     * @param pageSize    @return
     */
    public static List<TravelNote> solrRequest(String queryString, List<String> fields, int page, int pageSize) throws AizouException {
        List<TravelNote> results = new ArrayList<>();
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

            query.setQuery(queryString).setStart(page * pageSize).setRows(pageSize);

            for (String f : fields)
                query.addField(f);

            SolrDocumentList docs = server.query(query).getResults();
            Date publishDate;
            for (SolrDocument doc : docs) {
                TravelNote note = new TravelNote();
                note.setId(new ObjectId(doc.get("id").toString()));
                Object tmp;
                note.author = (String) doc.get("authorName");
                note.title = (String) doc.get("title");
                tmp = doc.get("authorAvatar");
                note.avatar = (tmp != null ? (String) tmp : "");
                if (!note.avatar.startsWith("http://"))
                    note.avatar = "http://" + note.avatar;
                tmp = doc.get("favorCnt");
                note.favorCnt = (tmp != null ? ((Long) tmp).intValue() : 0);
                note.contentsList = (List) doc.get("contents");
                note.sourceUrl = (String) doc.get("url");
                note.source = "baidu";
                tmp = doc.get("commentCnt");
                note.commentCnt = (tmp != null ? ((Long) tmp).intValue() : 0);
                tmp = doc.get("viewCnt");
                note.viewCnt = (tmp != null ? ((Long) tmp).intValue() : 0);
                tmp = doc.get("sourceUrl");
                note.sourceUrl = (tmp != null ? (String) tmp : "");
                publishDate = ((Date) doc.get("publishDate"));
                note.publishTime = publishDate == null ? null : publishDate.getTime();

                tmp = doc.get("costUpper");
                note.costUpper = (tmp != null ? Float.parseFloat(String.valueOf(tmp)) : -1);
                tmp = doc.get("costLower");
                note.costLower = (tmp != null ? Float.parseFloat(String.valueOf(tmp)) : -1);

                if (note.contentsList.size() > 1) {
                    StringBuilder sb = new StringBuilder();
                    for (int i = 1; i < note.contentsList.size(); i++) {
                        String c = note.contentsList.get(i);
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
                if (note.contentsList != null)
                    note.content = procContents(note.contentsList);
                else
                    note.content = "";
                results.add(note);
            }
            return results;
        } catch (SolrServerException e) {
            throw new AizouException(ErrorCode.UNKOWN_ERROR, e.getMessage());
        }
    }

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

        StringBuilder sb = new StringBuilder();
        for (String t : targets)
            sb.append(String.format(" title:%s toLoc:%s", t, t));

        for (String t : viewSpots)
            sb.append(String.format(" contentsList:%s", t));

        return solrRequest(sb.toString(), 0, 10);

    }

    /**
     * 通过id获取全部实体
     *
     * @param id
     * @return
     * @throws AizouException
     */
    public static TravelNote getNoteById(ObjectId id) throws AizouException {
        Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.TRAVELNOTE);
        Query<TravelNote> query = ds.createQuery(TravelNote.class).field("_id").equal(id);
        return query.get();
    }


    /**
     * 通过id获取游记
     *
     * @param id
     * @param fields
     * @return
     * @throws AizouException
     */
    public static TravelNote getNoteById(ObjectId id, List<String> fields) throws AizouException {
        Datastore ds = MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.TRAVELNOTE);
        Query<TravelNote> query = ds.createQuery(TravelNote.class).field("_id").equal(id);
        if (fields != null && !fields.isEmpty())
            query.retrievedFields(true, fields.toArray(new String[fields.size()]));
        return query.get();
    }

    /**
     * 返回游记bean
     *
     * @param idList
     * @param fields
     * @return
     * @throws AizouException
     */
    public static List<TravelNote> getNotesById(List<String> idList, List<String> fields) throws AizouException {
        List<TravelNote> noteList = new ArrayList<>();
        ObjectId oid;
        TravelNote travelNote;
        for (String id : idList) {
            oid = new ObjectId(id);
            travelNote = getNoteById(oid, fields);
            noteList.add(travelNote);
        }

        return noteList;
    }

    /**
     * 通过关键字获取游记
     *
     * @param keyword
     * @param page
     * @param pageSize
     * @return
     * @throws SolrServerException
     */
    public static List<TravelNote> searchNotesByWord(String keyword, int page, int pageSize) throws SolrServerException, AizouException {
        List<TravelNote> noteList = new ArrayList<>();

        //solr连接配置
        Configuration config = Configuration.root().getConfig("solr");
        String host = config.getString("host", "localhost");
        Integer port = config.getInt("port", 8983);
        String coreName = config.getString("core", "travelnote");
        String url = String.format("http://%s:%d/solr/%s", host, port, coreName);
        //进行查询，获取游记文档
        SolrServer server = new HttpSolrServer(url);
        SolrQuery query = new SolrQuery();
        query.setQuery(keyword).setStart(page * pageSize).setRows(pageSize);
        SolrDocumentList noteDocs = server.query(query).getResults();
        //TODO 更多游记获取不查询数据库
        Object tmp;
        for (SolrDocument doc : noteDocs) {
            TravelNote note = new TravelNote();
            //获取id
            note.setId(new ObjectId(doc.get("id").toString()));
            //姓名
            tmp = doc.get("authorName");
            note.authorName = (tmp == null ? null : (String) tmp);
            //标题
            tmp = doc.get("title");
            note.title = (tmp == null ? null : (String) tmp);
            //头像
            tmp = doc.get("authorAvatar");
            note.authorAvatar = transImage((String) tmp);
            //摘要
            tmp = doc.get("summary");
            note.summary = (tmp == null ? null : (String) tmp);
            //发表时间
            tmp = doc.get("publishTime");
            note.publishTime = (tmp == null ? null : (long) tmp);
            //出行时间
            tmp = doc.get("travelTime");
            note.travelTime = (tmp == null ? null : (long) tmp);
            //花费上下限
            tmp = doc.get("costUpper");
            note.costUpper = (tmp == null ? null : (float) tmp);
            tmp = doc.get("costLower");
            note.costLower = (tmp == null ? null : (float) tmp);
            //游记封面
            tmp = doc.get("covers");
            note.images = transImages((List) tmp);
            //是否精华帖
            tmp = doc.get("essence");
            note.essence = (tmp == null ? null : (Boolean) tmp);
            //添加来源
            tmp = doc.get("source");
            note.source = (tmp == null ? null : (String) tmp);

            noteList.add(note);
        }

        return noteList;
    }

    private static List<ImageItem> transImages(List<String> list) {
        if (list == null)
            return new ArrayList<>();
        List<ImageItem> result = new ArrayList<>();
        ImageItem imgItem = new ImageItem();
        imgItem.setKey(list.get(0));
        result.add(imgItem);
        return result;
    }

    private static String transImage(String key) {
        if (key == null)
            return "";
        ImageItem imgItem = new ImageItem();
        imgItem.setKey(key);
        return imgItem.getFullUrl();

    }

    /**
     * 通过目的地或者景点的id获取游记
     *
     * @param id
     * @param page
     * @param pageSize
     * @return
     */
    public static List<TravelNote> searchNoteByLocId(String id, int page, int pageSize) throws AizouException, SolrServerException {
        ObjectId oid = new ObjectId(id);
        List<TravelNote> noteList;
        String fields[] = {"zhName", "alias"};
        Locality locality = LocalityAPI.getLocality(oid, Arrays.asList(fields));
        ViewSpot viewSpot = PoiAPI.getVsDetail(oid, Arrays.asList(fields));
        StringBuilder builder = new StringBuilder();
        String zhName;
        List<String> alias;
        if (locality != null) {
            zhName = locality.getZhName();
            alias = locality.getAlias();
            //判断中文名称
            if (!zhName.equals("")) {
                builder.append(zhName);
            }
            //判断别名
            if (!alias.isEmpty()) {
                for (String str : alias) {
                    builder.append(str);
                }
            }
            noteList = searchNotesByWord(builder.toString(), page, pageSize);
            return noteList;
        } else if (viewSpot != null) {
            zhName = viewSpot.getZhName();
            alias = viewSpot.getAlias();
            //判断中文名称
            if (!zhName.equals("")) {
                builder.append(zhName);
            }
            //判断别名
            if (!alias.isEmpty()) {
                for (String str : alias) {
                    builder.append(str);
                }
            }
            noteList = searchNotesByWord(builder.toString(), page, pageSize);
            return noteList;
        } else
            throw new AizouException(ErrorCode.INVALID_ARGUMENT);
    }


    public static List<TravelNote> searchNoteByLoc(List<String> locNames, List<String> vsNames, int page, int pageSize) throws AizouException {

        StringBuilder sb = new StringBuilder();
        if (locNames != null) {
            for (String t : locNames)
                sb.append(String.format(" title:%s toLoc:%s", t, t));
        }
        if (vsNames != null) {
            for (String t : vsNames)
                sb.append(String.format(" contentsList:%s", t));
        }

        return solrRequest(sb.toString(), page, pageSize);
    }

    public static List<TravelNote> searchNoteById(List<String> ids, int pageSize) throws AizouException {
        StringBuilder sb = new StringBuilder();
        if (ids != null) {
            for (String t : ids)
                sb.append(String.format(" id:%s", t));
        }
        return solrRequest(sb.toString(), 0, pageSize);
    }

//    /**
//     * 根據Solr文檔生成遊記對象
//     *
//     * @param docs
//     * @return
//     */
//    private static List<TravelNote> getTravelNotesByDocuments(SolrDocumentList docs) {
//        List<TravelNote> results = new ArrayList<>();
//        StringBuilder sb;
//        TravelNote note;
//        Date publishDate;
//        for (SolrDocument doc : docs) {
//            note = new TravelNote();
//            Object tmp;
//            //note.setId(new ObjectId(doc.get("id").toString()));
//            note.author = (String) doc.get("authorName");
//            note.title = (String) doc.get("title");
//            tmp = doc.get("authorAvatar");
//            note.avatar = (tmp != null ? (String) tmp : "");
//            if (!note.avatar.startsWith("http://"))
//                note.avatar = "http://" + note.avatar;
//            tmp = doc.get("favorCnt");
//            try {
//                note.setId(new ObjectId(doc.get("id").toString()));
//            } catch (IllegalArgumentException e) {
//            }
//            note.favorCnt = (tmp != null ? ((Long) tmp).intValue() : 0);
//            note.contentsList = (List) doc.get("contents");
//            note.contents = note.contentsList.toString();
//            note.sourceUrl = (String) doc.get("url");
//            note.source = (String) doc.get("source");
//            tmp = doc.get("commentCnt");
//            note.commentCnt = (tmp != null ? ((Long) tmp).intValue() : 0);
//            tmp = doc.get("viewCnt");
//            note.viewCnt = (tmp != null ? ((Long) tmp).intValue() : 0);
//            tmp = doc.get("sourceUrl");
//            note.sourceUrl = (tmp != null ? (String) tmp : "");
//            note.contentsList = (List) doc.get("contents");
//            note.source = getSource((String) doc.get("source"));
//            publishDate = (Date) doc.get("publishDate");
//            note.publishTime = publishDate == null ? null : publishDate.getTime();
//            // TODO
//            note.cover = "http://e.hiphotos.baidu.com/lvpics/s%3D800/sign=caab32ee3987e9504617fe6c2039531b/9a504fc2d56285359976ef0c93ef76c6a7ef630c.jpg";
//
//            if (note.contentsList.size() > 1) {
//                sb = new StringBuilder();
//                for (int i = 1; i < note.contentsList.size(); i++) {
//                    String c = note.contentsList.get(i);
//                    if (Pattern.matches("^\\s*http.+", c))
//                        continue;
//                    sb.append(c);
//                    sb.append('\n');
//                    if (sb.length() > 200)
//                        break;
//                }
//                String summary = sb.toString().trim();
//                if (summary.length() > 200)
//                    summary = summary.substring(0, 200) + "……";
//                note.summary = summary;
//            }
//            results.add(note);
//        }
//
//        return results;
//    }

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


//    /**
//     * 通过id获取游记
//     *
//     * @param id
//     * @return
//     * @throws SolrServerException
//     * @throws ParseException
//     */
//    public static List<TravelNote> getTravelNoteDetailApi(String id) throws SolrServerException, ParseException {
//        SolrDocumentList docs;
//        List<TravelNote> results = new ArrayList<>();
//        //配置solr
//        Configuration config = Configuration.root().getConfig("solr");
//        String host = config.getString("host", "http://api.lvxingpai.cn");
//        Integer port = config.getInt("port", 8983);
//        String url = String.format("http://%s:%d/solr", host, port);
//        SolrServer server = new HttpSolrServer(url);
//        SolrQuery query = new SolrQuery();
//
//        String queryString = String.format("id:%s", id);
//        query.setQuery(queryString.trim());
//                /*.addField("id").addField("author").addField("title")
//                .addField("avatar").addField("contentsList").addField("cover").addField("elite")
//                .addField("source").addField("startDate").addField("sourceUrl").addField("toLoc")
//                .addField("viewCnt").addField("commentCnt");*/
//
//        docs = server.query(query).getResults();
//        TravelNote note;
//        for (SolrDocument doc : docs) {
//            Boolean elite = (Boolean) doc.get("elite");
//                /*if (!elite)
//                    continue;*/
//            note = new TravelNote();
//            Object tmp;
//            note.author = (String) doc.get("authorName");
//            note.title = (String) doc.get("title");
//            tmp = doc.get("authorAvatar");
//            note.avatar = (tmp != null ? (String) tmp : "");
//            if (!note.avatar.startsWith("http://"))
//                note.avatar = "http://" + note.avatar;
//            tmp = doc.get("favorCnt");
//            note.favorCnt = (tmp != null ? ((Long) tmp).intValue() : 0);
//            note.contentsList = (List) doc.get("contents");
//            note.contents = procContents(note.contentsList);
//            note.sourceUrl = (String) doc.get("url");
//            note.source = getSource((String) doc.get("source"));
//            tmp = doc.get("commentCnt");
//            note.commentCnt = (tmp != null ? ((Long) tmp).intValue() : 0);
//            tmp = doc.get("viewCnt");
//            note.viewCnt = (tmp != null ? ((Long) tmp).intValue() : 0);
//            tmp = doc.get("sourceUrl");
//            note.sourceUrl = (tmp != null ? (String) tmp : "");
//            tmp = doc.get("costUpper");
//            note.costUpper = (tmp != null ? Float.parseFloat(String.valueOf(tmp)) : -1);
//            tmp = doc.get("costLower");
//            note.costLower = (tmp != null ? Float.parseFloat(String.valueOf(tmp)) : -1);
//            note.publishTime = ((Date) doc.get("publishDate")).getTime();
//            results.add(note);
//        }
//        return results;
//
//    }

    /**
     * 处理游记正文
     *
     * @param contents
     * @return
     */
    public static String procContents(List<String> contents) {
        StringBuilder sb = new StringBuilder();
        sb.append("<div>");
        for (String line : contents) {
            if (line.startsWith("img src")) {
                continue; //不添加表情
            } else if (line.startsWith("http://")) {
                sb.append("<img src=" + line + " />");
            } else
                sb.append("<p> " + line + "</p>");
        }
        sb.append("</div>");
        /*List<String> list = new ArrayList<>();
        list.add(sb.toString().trim());*/
        return sb.toString().trim();
    }

}
