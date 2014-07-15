package models.morphia.traffic;

import models.morphia.misc.SimpleRef;
import org.mongodb.morphia.annotations.Embedded;

import java.util.Date;
import java.util.Map;

/**
 * 列车车次的详细信息。
 *
 * @author Zephyre
 */
@Embedded
public class TrainEntry {
    @Embedded
    public SimpleRef stop;

    @Embedded
    public SimpleRef loc;

    public int idx;

    public int distance;

    public int stayTime;

    public Date arrTime;

    public Date depTime;

    public Map<String, Double> price;
}
