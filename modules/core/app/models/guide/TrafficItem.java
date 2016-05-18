package models.guide;

import com.fasterxml.jackson.annotation.JsonFilter;
import models.AizouBaseEntity;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Transient;

/**
 * Created by topy on 2014/11/4.
 */
@Embedded
@JsonFilter("trafficItemFilter")
public class TrafficItem extends AizouBaseEntity {

    @Transient
    public static String fdDayIndex = "dayIndex";
    @Transient
    public static String fdType = "type";

    public Integer dayIndex;

    // 类型：airline trainRoute other
    public String category;
    // 起点
    public String start;
    // 终点
    public String end;
    // 出发时间
    public String depTime;
    // 到达时间
    public String arrTime;

}
