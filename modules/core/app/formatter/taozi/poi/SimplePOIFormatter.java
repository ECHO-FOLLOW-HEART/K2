package formatter.taozi.poi;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ser.PropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import formatter.AizouFormatter;
import formatter.taozi.GeoJsonPointSerializer;
import formatter.taozi.ImageItemSerializer;
import formatter.taozi.ImageItemSerializerOld;
import formatter.taozi.TaoziBaseFormatter;
import formatter.taozi.geo.LocalitySerializer;
import models.AizouBaseEntity;
import models.geo.GeoJsonPoint;
import models.geo.Locality;
import models.misc.ImageItem;
import models.poi.AbstractPOI;

import java.util.*;

/**
 * 返回用户的摘要（以列表形式获取用户信息时使用，比如获得好友列表，获得黑名单列表等）
 * <p>
 * Created by zephyre on 10/28/14.
 */
public class SimplePOIFormatter<T extends AbstractPOI> extends AizouFormatter<AbstractPOI> {

    public Set<String> getFilteredFields() {
        return filteredFields;
    }

    public SimplePOIFormatter(int imgWidth) {
        registerSerializer(ImageItem.class, new ImageItemSerializer(imgWidth));
        registerSerializer(Locality.class, new LocalitySerializer());
        registerSerializer(GeoJsonPoint.class, new GeoJsonPointSerializer());
        registerSerializer(AbstractPOI.class, new PolymorphicPOISerializer(PolymorphicPOISerializer.Level.SIMPLE));
        initObjectMapper(null);

        filteredFields = new HashSet<>();
        Collections.addAll(filteredFields,
                AizouBaseEntity.FD_ID,
                AbstractPOI.FD_EN_NAME,
                AbstractPOI.FD_ZH_NAME,
                // AbstractPOI.FD_DESC,
                AbstractPOI.FD_IMAGES,
                AbstractPOI.FD_LOCATION,
                AbstractPOI.FD_RATING,
                AbstractPOI.FD_PRICE_DESC,
                AbstractPOI.FD_ADDRESS,
                AbstractPOI.FD_TIMECOSTDESC,
                AbstractPOI.FD_RANK,
                AbstractPOI.FD_LOCALITY

        );

    }

}
