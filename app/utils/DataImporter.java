package utils;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.Expr;
import com.avaje.ebean.ExpressionList;
import com.fasterxml.jackson.databind.JsonNode;
import models.geos.Locality;
import models.geos.Country;
import models.misc.Currency;
import models.traffic.plane.*;
import models.traffic.train.TrainRoute;
import models.traffic.train.TrainSchedule;
import models.traffic.train.TrainStation;
import org.apache.commons.lang3.StringUtils;
import play.libs.Json;

import java.sql.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 导入数据。
 *
 * @author Zephyre
 */
public class DataImporter {
    private static DataImporter importer;
    private int port;
    private String db;
    private String password;
    private String user;
    private String hostName;
    private Connection conn;

    public DataImporter(String hostName, int port, String user, String password, String db) {
        this.hostName = hostName;
        this.port = port;
        this.user = user;
        this.password = password;
        this.db = db;
    }


    public static DataImporter init(String hostName, int port, String user, String password, String db) {
        if (importer != null)
            return importer;

        importer = new DataImporter(hostName, port, user, password, db);
        return importer;
    }

    public void connect() throws ClassNotFoundException, IllegalAccessException, InstantiationException, SQLException {
        Class.forName("com.mysql.jdbc.Driver").newInstance(); //MYSQL驱动
        String connStr = String.format("jdbc:mysql://%s:%d/%s", importer.hostName, importer.port, importer.db);
        importer.conn = DriverManager.getConnection(connStr, importer.user, importer.password);
    }

    public void close() throws SQLException {
        if (conn != null) {
            conn.close();
            conn = null;
        }
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
                for (Locality sibling : locality.chiLocalityList) {
                    sibling.supLocality = parent;
                    sibling.level -= 1;
                    sibling.save();
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
     *
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

    public JsonNode importTrainRoute(int start, int count) {
        try {
            Class.forName("com.mysql.jdbc.Driver").newInstance(); //MYSQL驱动
            String connStr = String.format("jdbc:mysql://%s:%d/%s", this.hostName, this.port, this.db);
            Connection conn = DriverManager.getConnection(connStr, this.user, this.password);

            Statement statement = conn.createStatement();
            String selectSql = String.format("SELECT * FROM vxp_train LIMIT %d, %d", start, count);
            ResultSet res = statement.executeQuery(selectSql);
            int cnt = 0;

            List<String> missingStation = new ArrayList<>();

            while (res.next()) {
                String trainCode = res.getString("traincode");
                String alias = null;
                // 处理1112/1113这种情况
                String[] codeParts = StringUtils.split(trainCode, '/');
                if (codeParts.length > 1) {
                    alias = StringUtils.join(Arrays.copyOfRange(codeParts, 1, codeParts.length), '/');
                    trainCode = codeParts[0];
                }

                TrainRoute route = TrainRoute.finder.byId(trainCode);
                if (route != null)
                    continue;

                // 首字母作为路线类型，或者"O"表示普通列车
                String header = trainCode.substring(0, 1);
                String trainType = (StringUtils.isNumeric(header) ? "O" : header.toUpperCase());

                // 始发站和终到站
                String fromStationName = res.getString("fromstation");
                String toStationName = res.getString("tostation");
                TrainStation fromStation = TrainStation.finder.where().eq("name", fromStationName).findUnique();
                TrainStation toStation = TrainStation.finder.where().eq("name", toStationName).findUnique();

                if (fromStation == null)
                    missingStation.add(fromStationName);
                if (toStation == null)
                    missingStation.add(toStationName);
                if (fromStation == null || toStation == null)
                    continue;

                // 时间
                Pattern pattern = Pattern.compile("([0-9]{2}):([0-9]{2})");
                String timeString = res.getString("fromtime");
                Matcher matcher = pattern.matcher(timeString);
                Time fromTime = null;
                if (matcher.find())
                    fromTime = Time.valueOf(timeString + ":00");
                timeString = res.getString("totime");
                matcher = pattern.matcher(timeString);
                Time toTime = null;
                if (matcher.find())
                    toTime = Time.valueOf(timeString + ":00");
                int dayLag = res.getInt("arriveday");

                int distance = res.getInt("distance");

                // 获得基础价格和完整价格列表
                Map<String, Float> priceMap = new HashMap<>();
                for (String priceTag : new String[]{"yz", "rz", "ywx", "yws", "ywz", "rws", "rwx", "gjrwx", "gjrws", "ydz", "edz", "tdz", "ggz", "bz", "swz"}) {
                    float price = res.getFloat(priceTag);
                    if (price <= 0)
                        continue;

                    priceMap.put(priceTag, price);
                }
                Float basePrice = null;
                if (priceMap.size() > 0)
                    basePrice = Collections.min(priceMap.values());
                else
                    basePrice = null;

                route = new TrainRoute();
                route.trainCode = trainCode;
                route.aliasCode = alias;
                route.routeType = trainType;
                route.departure = fromStation;
                route.arrival = toStation;
                route.departureTime = fromTime;
                route.arrivalTime = toTime;
                route.dayLag = dayLag - 1;
                route.duration = (int) ((toTime.getTime() + route.dayLag * 3600 * 24 * 1000L - fromTime.getTime()) / (1000 * 60));
                route.distance = distance;
                route.currency = models.misc.Currency.finder.byId("CNY");
                if (basePrice != null)
                    route.price = (int) (basePrice * 100);
                route.save();

                cnt++;
            }

            final int finalCnt = cnt;
            final List<String> finalMissingStation = missingStation;
            return Json.toJson(new HashMap<String, Object>() {
                {
                    put("code", 0);
                    put("total", finalCnt);
                    put("missingStation", finalMissingStation);
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
     * 导入列车时刻表。
     *
     * @param start
     * @param count
     * @return
     */
    public JsonNode importTrainTimetable(int start, int count) {
        try {
            Class.forName("com.mysql.jdbc.Driver").newInstance(); //MYSQL驱动
            String connStr = String.format("jdbc:mysql://%s:%d/%s", this.hostName, this.port, this.db);
            Connection conn = DriverManager.getConnection(connStr, this.user, this.password);

            Statement statement = conn.createStatement();
            String selectSql = String.format("SELECT * FROM vxp_train_stop LIMIT %d, %d", start, count);
            ResultSet res = statement.executeQuery(selectSql);

            int cnt = 0;
            List<String> missingRoute = new ArrayList<>();
            List<String> missingStation = new ArrayList<>();

            while (res.next()) {
                Long id = res.getLong("id");
                TrainSchedule schedule = TrainSchedule.finder.byId(id);
                if (schedule != null)
                    continue;

                String trainCode = res.getString("traincode");
                if (trainCode.contains("/"))
                    trainCode = trainCode.substring(0, trainCode.indexOf("/"));
                TrainRoute route = TrainRoute.finder.byId(trainCode);
                if (route == null) {
                    missingRoute.add(trainCode);
                    continue;
                }

                String stationName = res.getString("stationname");
                TrainStation station = TrainStation.finder.where().eq("name", stationName).findUnique();
                if (station == null) {
                    missingStation.add(stationName);
                    continue;
                }

                // 时间
                Pattern pattern = Pattern.compile("([0-9]{2}):([0-9]{2})");
                String timeString = res.getString("arrivetime");
                Matcher matcher = pattern.matcher(timeString);
                Time arriveTime = null;
                if (matcher.find())
                    arriveTime = Time.valueOf(timeString + ":00");
                timeString = res.getString("fromtime");
                matcher = pattern.matcher(timeString);
                Time departTIme = null;
                if (matcher.find())
                    departTIme = Time.valueOf(timeString + ":00");

                // 站点类型
                TrainSchedule.StopType stopType = TrainSchedule.StopType.NORMAL;
                if (arriveTime != null && departTIme == null)
                    stopType = TrainSchedule.StopType.END;
                else if (arriveTime == null && departTIme != null)
                    stopType = TrainSchedule.StopType.START;
                else if (arriveTime == null && departTIme != null) {
                    missingStation.add(stationName);
                    continue;
                }

                int dayLag = res.getInt("arriveday") - 1;
                int stopIdx = res.getInt("stopno") - 1;
                int distance = res.getInt("distance");

                // 获得基础价格和完整价格列表
                Map<String, Float> priceMap = new HashMap<>();
                for (String priceTag : new String[]{"yz", "rz", "ywx", "yws", "ywz", "rws", "rwx", "gjrwx", "gjrws", "ydz", "edz", "tdz", "ggz", "bz", "swz"}) {
                    float price = res.getFloat(priceTag);
                    if (price <= 0)
                        continue;
                    priceMap.put(priceTag, price);
                }
                Float basePrice = null;
                if (priceMap.size() > 0)
                    basePrice = Collections.min(priceMap.values());
                else
                    basePrice = null;

                schedule = new TrainSchedule();
                schedule.id = id;
                schedule.route = route;
                schedule.stop = station;
                schedule.stopIdx = stopIdx;
                schedule.stopType = stopType;
                schedule.arrivalTime = arriveTime;
                schedule.departureTime = departTIme;
                schedule.dayLag = dayLag;
                schedule.distance = distance;
                schedule.currency = Currency.finder.byId("CNY");
                schedule.price = ((basePrice == null) ? 0 : (int) (basePrice * 100));
                if (arriveTime != null)
                    schedule.runTime = (int) ((arriveTime.getTime() + dayLag * 3600 * 24 * 1000L - schedule.route.departureTime.getTime()) / (1000 * 60));
                schedule.save();

                cnt++;
            }

            final int finalCnt = cnt;
            final List<String> finalMissingStation = missingStation;
            final List<String> finalMissingRoute = missingRoute;
            return Json.toJson(new HashMap<String, Object>() {
                {
                    put("code", 0);
                    put("total", finalCnt);
                    put("missingStation", finalMissingStation);
                    put("missingStation", finalMissingRoute);
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
     * 导入机场数据
     *
     * @param start
     * @param count
     * @return
     */
    public JsonNode importAirport(int start, int count) {
        try {
            Statement stmt = this.conn.createStatement();
            String selectSql = String.format("SELECT * FROM vxp_plane_timetable_new GROUP BY sname LIMIT %d, %d", start, count);
            ResultSet res = stmt.executeQuery(selectSql);

            List<String> missingCity = new ArrayList<>();
            int cnt = 0;
            while (res.next()) {
                String cityName = res.getString("sname");
                String airportName = res.getString("arrAirport");
                List<Locality> cityList = Locality.finder.where().like("zhLocalityName", String.format("%s%%", cityName)).findList();
                if (cityList.size() == 0) {
                    missingCity.add(cityName);
                    continue;
                }
                Locality city = cityList.get(0);

                Airport airport = Airport.finder.where().eq("name", airportName).findUnique();
                if (airport != null)
                    continue;
                airport = new Airport();
                airport.name = airportName;
                airport.locality = city;
                airport.save();
                cnt++;
            }

            final int finalCnt = cnt;
            final List<String> finalMissingCity = missingCity;
            return Json.toJson(new HashMap<String, Object>() {
                {
                    put("code", 0);
                    put("total", finalCnt);
                    put("missingCity", finalMissingCity);
                }
            });
        } catch (SQLException e) {
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
     * 导入航班数据
     *
     * @param start
     * @param count
     * @return
     */
    public JsonNode importAirRoutes(int start, int count) {
        try {
            Statement stmt = this.conn.createStatement();
            String selectSql = String.format("SELECT * FROM vxp_plane_timetable_new LIMIT %d, %d", start, count);
            ResultSet res = stmt.executeQuery(selectSql);

            int cnt = 0;
            List<String> missingAirport = new ArrayList<>();
            while (res.next()) {
                String flightCode = res.getString("code");
                AirRoute route = AirRoute.finder.where().eq("flightCode", flightCode).findUnique();
                if (route != null)
                    continue;

                route = new AirRoute();
                route.flightCode = flightCode;

                String airportName = res.getString("depAirport");
                Airport airport = Airport.finder.where().eq("name", airportName).findUnique();
                if (airport == null) {
                    missingAirport.add(airportName);
                    continue;
                }
                route.departure = airport;
                airportName = res.getString("arrAirport");
                airport = Airport.finder.where().eq("name", airportName).findUnique();
                if (airport == null) {
                    missingAirport.add(airportName);
                    continue;
                }
                route.arrival = airport;

                String terminalDesc = res.getString("depTerminal");
                if (terminalDesc != null && !terminalDesc.isEmpty())
                    route.departureTerminal = terminalDesc;
                terminalDesc = res.getString("arrTerminal");
                if (terminalDesc != null && !terminalDesc.isEmpty())
                    route.arrivalTerminal = terminalDesc;

                String carrierName = res.getString("name");
                String carrierFullName = res.getString("fullName");
                String carrierShortName = res.getString("airwaysShortName");
                String carrierCode = res.getString("carrier");
                route.airline = importAirline(carrierCode, carrierName, carrierFullName, carrierShortName);

                route.jetType = res.getString("planetype");
                route.jetDescription = res.getString("flight_type_fullname");

                route.offerFood = res.getBoolean("meal");
                route.selfCheckin = res.getBoolean("zhiji");
                route.distance = res.getInt("distance");
                route.nonStopFlight = !res.getBoolean("stops");
                String onTimeStr = res.getString("correct");
                if (onTimeStr.contains("%"))
                    route.onTimeStat = Integer.parseInt(onTimeStr.substring(0, onTimeStr.indexOf('%'))) / 100f;

                route.departureTime = res.getTime("btime");
                route.arrivalTime = res.getTime("etime");
                route.dayLag = (route.arrivalTime.getTime() > route.departureTime.getTime() ? 0 : 1);
                route.duration = (int) ((route.arrivalTime.getTime() + route.dayLag * 24 * 3600 * 1000L
                        - route.departureTime.getTime()) / (1000 * 60));


                float surcharge = res.getFloat("tof");
                float tax = res.getFloat("arf");
                String agencyName = res.getString("provider");
                String agencyTel = res.getString("providerTelephone");
                AirTicketAgency agency = null;
                if (agencyName != null)
                    agency = importAirAgency(agencyName, agencyTel);
                float price = res.getFloat("min_price");
                float discount = res.getFloat("bareDiscount");
                FlightPrice priceItem = new FlightPrice();
                priceItem.route = route;
                priceItem.agency = agency;
                priceItem.discount = discount / 10;
                priceItem.currency = Currency.finder.byId("CNY");
                priceItem.ticketPrice = (int) (price * 100);
                priceItem.fuelSurcharge = (int) (surcharge * 100);
                priceItem.tax = (int) (tax * 100);

                route.currency = Currency.finder.byId("CNY");
                route.price = priceItem.ticketPrice;

                route.save();
                priceItem.save();
                cnt++;
            }
            final int finalCnt = cnt;
            final List<String> finalMissingAirport = missingAirport;
            return Json.toJson(new HashMap<String, Object>() {
                {
                    put("code", 0);
                    put("total", finalCnt);
                    put("missingAirport", finalMissingAirport);
                }
            });
        } catch (SQLException e) {
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
     * 导入机票代理
     *
     * @param agencyName
     * @param agencyTel
     * @return
     */
    private AirTicketAgency importAirAgency(String agencyName, String agencyTel) {
        AirTicketAgency agency = AirTicketAgency.finder.where().eq("name", agencyName).findUnique();
        if (agency != null)
            return agency;

        agency = new AirTicketAgency();
        agency.name = agencyName;
        agency.telephone = agencyTel;
        agency.save();
        return agency;
    }

    /**
     * 导入航空公司
     *
     * @param carrierCode
     * @param carrierName
     * @param carrierFullName
     * @param carrierShortName
     * @return
     */
    private Airline importAirline(String carrierCode, String carrierName, String carrierFullName, String carrierShortName) {
        Airline airline = Airline.finder.byId(carrierCode);
        if (airline != null)
            return airline;

        airline = new Airline();
        airline.airlineCode = carrierCode;
        airline.airlineName = carrierName;
        airline.airlineShortName = carrierShortName;
        airline.airlineFullName = carrierFullName;
        airline.save();

        return airline;
    }
}
