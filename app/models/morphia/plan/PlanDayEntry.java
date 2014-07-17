package models.morphia.plan;

import org.mongodb.morphia.annotations.Embedded;

import java.util.Date;
import java.util.List;

/**
 * 路线规划中，每一天的数据。
 *
 * @author Zephyre
 */
@Embedded
public class PlanDayEntry {
    public Date date;

    @Embedded
    public List<PlanItem> actv;
}
