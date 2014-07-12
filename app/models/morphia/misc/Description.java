package models.morphia.misc;

import org.mongodb.morphia.annotations.Embedded;

/**
 * 介绍的类。
 *
 * @author Zephyre
 */
@Embedded
public class Description {
    public String desc;
    public String details;
}
