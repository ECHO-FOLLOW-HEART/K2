package models.morphia.poi;

import org.mongodb.morphia.annotations.Embedded;

/**
 * POI的评论信息。
 *
 * @author Zephyre
 */
@Embedded
public class Ratings {
    public Integer score;

    public Integer viewCnt;

    public Integer favorCnt;

    public Integer dinningIdx;

    public Integer shoppingIdx;
}
