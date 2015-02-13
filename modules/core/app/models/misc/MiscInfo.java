package models.misc;

import com.fasterxml.jackson.databind.JsonNode;
import com.mongodb.BasicDBObjectBuilder;
import models.AizouBaseEntity;
import models.ITravelPiFormatter;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Transient;
import play.libs.Json;

import java.util.Map;

/**
 * 其它杂项信息。
 *
 * @author Zephyre
 */
@Entity
public class MiscInfo extends AizouBaseEntity implements ITravelPiFormatter {


    @Transient
    public static final String FD_ITEMID = "itemIds";

    @Transient
    public static final String FD_UPDATE_ANDROID_VERSION = "TAOZI_UPDATE_ANDROID_VERSION";

    @Transient
    public static final String FD_UPDATE_ANDROID_URL = "TAOZI_UPDATE_ANDROID_URL";

    @Transient
    public static final String FD_UPDATE_IOS_VERSION = "TAOZI_UPDATE_IOS_VERSION";

    @Transient
    public static final String FD_TAOZI_COVERSTORY_IMAGE = "TAOZI_COVERSTORY_IMAGE";

    @Transient
    public static final String FD_TAOZI_HUANXIN_INFO = "TAOZI_HUANXIN_INFO";

    @Id
    public ObjectId id;

    /**
     * 手机首页屏幕的图像。
     */
    public String appHomeImage;

    /**
     * 环信token
     */
    public String easemobToken;

    /**
     * 环信token的过期时间
     */
    public Long easemobTokenExpire;

    /**
     * 环信UUID
     */
    public String easemobUUID;

    /**
     * 封面故事
     */
    public Map<String, String> coverStory;
    /**
     * 用于哪个应用
     */
    public String application;

    public String key;

    public String viceKey;

    public String value;

    @Override
    public JsonNode toJson() {
        BasicDBObjectBuilder builder = BasicDBObjectBuilder.start().add("appHomeImage", appHomeImage);
        return Json.toJson(builder.get());
    }
}
