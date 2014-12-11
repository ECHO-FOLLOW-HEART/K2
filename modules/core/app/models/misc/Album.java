package models.misc;

import com.fasterxml.jackson.annotation.JsonFilter;
import models.AizouBaseEntity;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Entity;

import java.util.List;

/**
 * 相册集合
 *
 * @author Zephyre
 */
@Entity
@JsonFilter("albumFilter")
public class Album extends AizouBaseEntity {
    /**
     * 照片信息
     */
    @Embedded
    private ImageItem image;

    /**
     * 该照片对应的locality，viewspot等项目
     */
    private List<ObjectId> itemIds;
}
