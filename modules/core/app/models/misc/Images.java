package models.misc;

import com.fasterxml.jackson.annotation.JsonFilter;
import models.AizouBaseEntity;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Transient;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

/**
 * 相册集合
 *
 * @author Zephyre
 */
@Entity
@JsonFilter("imagesFilter")
public class Images extends AizouBaseEntity {
    @Transient
    public static final String FD_URL = "url";

    @Transient
    public static final String FD_WIDTH = "w";

    @Transient
    public static final String FD_HEIGHT = "h";

    @Transient
    public static final String FD_ITEMID = "itemIds";

    private Map<String, Integer> cropHint;


    @NotNull
    private String key;

    private String bucket;

    /**
     * 图像宽度
     */
    private Integer w;

    /**
     * 图像高度
     */
    private Integer h;

    /**
     * 图像格式
     */
    private String fmt;

    /**
     * 图像色彩模型。
     */
    private String cm;

    /**
     * 图像MD5哈希校验
     */
    private String hash;

    /**
     * 图像文件的大小
     */
    private Integer size;

    /**
     * 项目ID
     */
    private List<ObjectId> itemIds;

    private String url;
    /**
     * 根据bucket和key，生成完整的图像链接
     */
    public String getFullUrl() {
        return String.format("http://images.taozilvxing.com/%s", key);
    }

    public String getKey() {
        return key;
    }

    public String getBucket() {
        return bucket;
    }

    public Integer getW() {
        return w;
    }

    public Integer getH() {
        return h;
    }

    public String getFmt() {
        return fmt;
    }

    public String getCm() {
        return cm;
    }

    public String getHash() {
        return hash;
    }

    public Integer getSize() {
        return size;
    }

    public Map<String, Integer> getCropHint() {
        return cropHint;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

}
