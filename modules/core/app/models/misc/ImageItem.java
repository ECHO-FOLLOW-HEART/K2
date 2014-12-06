package models.misc;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.databind.JsonNode;
import com.mongodb.BasicDBObjectBuilder;
import models.ITravelPiFormatter;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Transient;
import play.data.validation.Constraints;
import play.libs.Json;

import java.util.Map;

/**
 * 表示一张图像。
 * <p/>
 * Created by zephyre on 8/14/14.
 */
@JsonFilter("imageItemFilter")
@Embedded
public class ImageItem implements ITravelPiFormatter {

    @Transient
    public static final String FD_URL = "url";

    @Transient
    public static final String FD_CROP_HINT = "cropHint";

    @Transient
    public static final String FD_WIDTH = "w";

    @Transient
    public static final String FD_HEIGHT = "h";

    private Map<String, Integer> cropHint;

    @Constraints.Required
    private String key;

    private String bucket;

    /**
     * 原始url
     */
    private String url;

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

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getBucket() {
        return bucket;
    }

    public void setBucket(String bucket) {
        this.bucket = bucket;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Integer getW() {
        return w;
    }

    public void setW(Integer w) {
        this.w = w;
    }

    public Integer getH() {
        return h;
    }

    public void setH(Integer h) {
        this.h = h;
    }

    public String getFmt() {
        return fmt;
    }

    public void setFmt(String fmt) {
        this.fmt = fmt;
    }

    public String getCm() {
        return cm;
    }

    public void setCm(String cm) {
        this.cm = cm;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public Map<String, Integer> getCropHint() {
        return cropHint;
    }

    public void setCropHint(Map<String, Integer> cropHint) {
        this.cropHint = cropHint;
    }

    @Override
    public JsonNode toJson() {
        BasicDBObjectBuilder builder = BasicDBObjectBuilder.start().add("url", url).add("w", w).add("h", h).add("fmt", fmt == null ? "" : fmt)
                .add("cm", cm == null ? "" : cm).add("hash", hash == null ? "" : hash).add("size", size == null ? "" : size);
        return Json.toJson(builder.get());
    }
}
