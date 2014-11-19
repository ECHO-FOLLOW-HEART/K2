package models.misc;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.databind.JsonNode;
import com.mongodb.BasicDBObjectBuilder;
import models.ITravelPiFormatter;
import models.geo.Address;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Transient;
import play.libs.Json;

/**
 * 表示一张图像。
 * <p/>
 * Created by zephyre on 8/14/14.
 */
@JsonFilter("imageItemPOIFilter")
@Embedded
public class ImageItem implements ITravelPiFormatter {

    @Transient
    public static String fnUrl = "url";


    public String url;

    /**
     * 图像宽度
     */
    public Integer w;

    /**
     * 图像高度
     */
    public Integer h;

    /**
     * 图像格式
     */
    public String fmt;

    /**
     * 图像色彩模型。
     */
    public String cm;

    /**
     * 图像MD5哈希校验
     */
    public String hash;

    /**
     * 图像文件的大小
     */
    public Integer fSize;

    public Boolean enabled;

    @Override
    public JsonNode toJson() {
        BasicDBObjectBuilder builder = BasicDBObjectBuilder.start().add("url", url).add("w", w).add("h", h).add("fmt", fmt == null ? "" : fmt)
                .add("cm", cm == null ? "" : cm).add("hash", hash == null ? "" : hash).add("fSize", fSize == null ? "" : fSize);
        return Json.toJson(builder.get());
    }
}
