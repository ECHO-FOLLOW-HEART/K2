package models.poi;

import com.fasterxml.jackson.annotation.JsonFilter;
import models.AizouBaseEntity;
import models.misc.ImageItem;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Transient;

import java.util.List;

/**
 * 美食评论
 * <p>
 * Created by topy on 14-11-12.
 */
@JsonFilter("restaurantCommentFilter")
@Entity
public class RestaurantComment extends Comment {


}
