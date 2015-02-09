package formatter.taozi.geo;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import formatter.AizouFormatter;
import formatter.taozi.ImageItemSerializer;
import formatter.taozi.ImageItemSerializerOld;
import formatter.taozi.TaoziBaseFormatter;
import models.AizouBaseEntity;
import models.geo.Locality;
import models.guide.AbstractGuide;
import models.guide.Guide;
import models.misc.ImageItem;
import models.poi.AbstractPOI;

import java.util.Arrays;

/**
 * 返回用户的摘要（以列表形式获取用户信息时使用，比如获得好友列表，获得黑名单列表等）
 * <p/>
 * Created by zephyre on 10/28/14.
 */
public class LocalityFormatter extends AizouFormatter<Locality> {

    public LocalityFormatter(int imgWidth){
        registerSerializer(Locality.class,new LocalitySerializer(LocalitySerializer.Level.DETAILED));
        registerSerializer(ImageItem.class, new ImageItemSerializer(imgWidth));

        initObjectMapper(null);

        filteredFields.addAll(Arrays.asList(
                AizouBaseEntity.FD_ID,
                AizouBaseEntity.FD_IS_FAVORITE,
                Locality.FD_EN_NAME,
                Locality.FD_ZH_NAME,
                Locality.fnDesc,
                Locality.fnLocation,
                Locality.fnImages,
                Locality.fnTimeCostDesc,
                Locality.fnTravelMonth,
                Locality.fnImageCnt));
    }

}
