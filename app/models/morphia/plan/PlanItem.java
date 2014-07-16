package models.morphia.plan;

import models.morphia.misc.SimpleRef;
import org.mongodb.morphia.annotations.Embedded;

/**
 * 路线规划中的基本单元。
 *
 * @author Zephyre
 */
@Embedded
public class PlanItem {
    @Embedded
    public SimpleRef item;

    @Embedded
    public SimpleRef loc;

    public Integer idx;

    public String type;
}
