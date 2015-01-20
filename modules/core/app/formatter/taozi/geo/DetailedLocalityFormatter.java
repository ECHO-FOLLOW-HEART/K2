package formatter.taozi.geo;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import formatter.taozi.ImageItemSerializer;
import formatter.taozi.TaoziBaseFormatter;
import models.AizouBaseEntity;
import models.geo.Locality;
import models.misc.ImageItem;
import models.poi.AbstractPOI;

import java.util.Arrays;

/**
 * 返回用户的摘要（以列表形式获取用户信息时使用，比如获得好友列表，获得黑名单列表等）
 * <p/>
 * Created by zephyre on 10/28/14.
 */
public class DetailedLocalityFormatter extends TaoziBaseFormatter {

    public DetailedLocalityFormatter setImageWidth(int width) {
        imageWidth = width;
        return this;
    }

    @Override
    public JsonNode format(AizouBaseEntity item) {
        ObjectMapper mapper = getObjectMapper();

        ((SimpleFilterProvider) mapper.getSerializationConfig().getFilterProvider())
                .addFilter("localityFilter",
                        SimpleBeanPropertyFilter.filterOutAllExcept(
                                AizouBaseEntity.FD_ID,
                                AizouBaseEntity.FD_IS_FAVORITE,
                                Locality.FD_EN_NAME,
                                Locality.FD_ZH_NAME,
                                Locality.fnDesc,
                                Locality.fnLocation,
                                Locality.fnImages,
                                Locality.fnTimeCostDesc,
                                Locality.fnTravelMonth,
                                Locality.fnImageCnt
                        ));
        SimpleModule imageItemModule = new SimpleModule();
        imageItemModule.addSerializer(ImageItem.class,
                new ImageItemSerializer(imageWidth));
        mapper.registerModule(imageItemModule);

        ObjectNode result = mapper.valueToTree(item);

        stringFields.addAll(Arrays.asList(Locality.FD_EN_NAME, Locality.FD_ZH_NAME, Locality.fnTimeCostDesc));

        listFields.add(AbstractPOI.FD_IMAGES);

        mapFields.add(AbstractPOI.FD_LOCATION);

        return postProcess(result);
    }
}
