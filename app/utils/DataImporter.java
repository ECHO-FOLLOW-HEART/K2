package utils;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.Expr;
import com.fasterxml.jackson.databind.JsonNode;
import models.geos.Locality;
import models.geos.Country;
import models.traffic.train.TrainStation;
import org.apache.commons.lang3.StringUtils;
import play.libs.Json;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 导入数据。
 *
 * @author Zephyre
 */
public class DataImporter {
    private int port;
    private String db;
    private String password;
    private String user;
    private String hostName;

    public DataImporter(String hostName, int port, String user, String password, String db) {
        this.hostName = hostName;
        this.port = port;
        this.user = user;
        this.password = password;
        this.db = db;
    }

    /**
     * 遍历城市区划树
     *
     * @param code
     * @param conn
     * @return
     * @throws ClassNotFoundException
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws SQLException
     */
    private Locality importNode(String code, Connection conn) throws ClassNotFoundException, IllegalAccessException, InstantiationException, SQLException {
        Statement statement = conn.createStatement();

        String selectSql = String.format("SELECT * FROM vxp_area_code WHERE CODE='%s'", code);
        ResultSet res = statement.executeQuery(selectSql);
        if (!res.next())
            return null;

        // 获得父节点
        List<Integer> nodeList = new ArrayList<>();
        for (String pathNode : StringUtils.split(res.getString("PATH"), '-')) {
            try {
                int nodeCode = Integer.parseInt(StringUtils.trim(pathNode));
                if (nodeCode != 0)
                    nodeList.add(nodeCode);
            } catch (NumberFormatException ignored) {
            }
        }

        int intCode = Integer.parseInt(code);
        if (intCode != 0 && !nodeList.contains(intCode))
            nodeList.add(intCode);

        Locality parent = null;
        if (nodeList.size() == 0)
            return null;
        else if (nodeList.size() > 1) {
            // 确认父节点入库
            long parentNode = (long) (nodeList.get(nodeList.size() - 2));
            parent = Locality.finder.byId(parentNode);
            if (parent == null)
                parent = importNode(String.valueOf(parentNode), conn);
            if (parent == null)
                return null;
        }

        long cityId = (long) (nodeList.get(nodeList.size() - 1));
        Locality locality = Locality.finder.byId(cityId);
        if (locality == null) {
            locality = new Locality();
            locality.id = cityId;
            locality.level = nodeList.size();
            locality.country = Country.finder.byId("CN");
            locality.zhLocalityName = res.getString("NAME");
            locality.localLocalityName = res.getString("NAME");
            locality.supLocality = parent;
            locality.save();
        }
        return locality;
    }

    public JsonNode importGeoSite(int start, int count) {
        try {
            Class.forName("com.mysql.jdbc.Driver").newInstance(); //MYSQL驱动
            String connStr = String.format("jdbc:mysql://%s:%d/%s", this.hostName, this.port, this.db);
            Connection conn = DriverManager.getConnection(connStr, this.user, this.password);

            Statement statement = conn.createStatement();

            String selectSql = String.format("SELECT CODE FROM vxp_area_code LIMIT %d, %d", start, count);
            ResultSet res = statement.executeQuery(selectSql);
            int cnt = 0;
            while (res.next()) {
                String code = res.getString("CODE");
                Locality locality = importNode(code, conn);
                if (locality != null)
                    cnt++;
            }

            // 一些清理工作
            for (Locality locality : Locality.finder.where().disjunction().add(Expr.like("zhLocalityName", "%市辖区%"))
                    .add(Expr.eq("zhLocalityName", "县"))
                    .add(Expr.eq("zhLocalityName", "%直辖%")).findList()) {
                Locality parent = locality.supLocality;
                if (parent == null)
                    continue;
                for (Locality siblings : locality.chiLocalityList) {
                    siblings.supLocality = parent;
                    siblings.level = parent.level + 1;
                    siblings.save();
                }
                locality.delete();
            }

            final int finalCnt = cnt;
            return Json.toJson(new HashMap<String, Object>() {
                {
                    put("code", 0);
                    put("total", finalCnt);
                }
            });
        } catch (Exception e) {
            final Exception finalE = e;
            Ebean.rollbackTransaction();
            return Json.toJson(new HashMap<String, Object>() {
                {
                    put("code", -1);
                    put("msg", "MySQL error: " + finalE.getMessage());
                }
            });
        }
    }

    /**
     * 导入火车站
     * @param start
     * @param count
     * @return
     */
    public JsonNode importTrainSite(int start, int count) {
        try {
            Class.forName("com.mysql.jdbc.Driver").newInstance(); //MYSQL驱动
            String connStr = String.format("jdbc:mysql://%s:%d/%s", this.hostName, this.port, this.db);
            Connection conn = DriverManager.getConnection(connStr, this.user, this.password);

            Statement statement = conn.createStatement();
            String selectSql = String.format("SELECT id, city, pinyin, shortpy, area_code FROM vxp_train_city LIMIT %d, %d", start, count);
            ResultSet res = statement.executeQuery(selectSql);
            int cnt = 0;
            while (res.next()) {
                Long stationId = res.getLong("id");
                TrainStation station = TrainStation.finder.byId(stationId);
                if (station != null)
                    continue;

                String areaCodeStr = res.getString("area_code");
                if (areaCodeStr != null && areaCodeStr.charAt(0) == '\ufeff')
                    areaCodeStr = areaCodeStr.substring(1);
                Locality locality = null;
                try {
                    locality = Locality.finder.byId(Long.parseLong(areaCodeStr));
                } catch (NumberFormatException ignore) {
                }

                String name = res.getString("city");

                // 试图从车站名查找locality
                if (locality == null) {
                    char lastChar = name.charAt(name.length() - 1);
                    if (name.length() > 2 && (lastChar == '东' || lastChar == '南' || lastChar == '西' | lastChar == '北')) {
                        String name2 = StringUtils.substring(name, 0, name.length() - 1);
                        List<Locality> localityList = Locality.finder.where()
                                .like("zhLocalityName", String.format("%s%%", name2)).findList();
                        if (localityList.size() == 1)
                            locality = localityList.get(0);
                        else
                            locality = null;
                    }
                }

                station = new TrainStation();
                station.id = stationId;
                station.name = name;
                station.pinyin = res.getString("pinyin");
                station.shortPY = res.getString("shortpy");
                station.locality = locality;
                station.save();

                cnt++;
            }

            final int finalCnt = cnt;
            return Json.toJson(new HashMap<String, Object>() {
                {
                    put("code", 0);
                    put("total", finalCnt);
                }
            });
        } catch (Exception e) {
            final Exception finalE = e;
            Ebean.rollbackTransaction();
            return Json.toJson(new HashMap<String, Object>() {
                {
                    put("code", -1);
                    put("msg", "MySQL error: " + finalE.getMessage());
                }
            });
        }
    }
}
