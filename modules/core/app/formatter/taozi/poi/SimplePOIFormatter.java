package formatter.taozi.poi;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import formatter.taozi.ImageItemSerializer;
import formatter.taozi.TaoziBaseFormatter;
import models.AizouBaseEntity;
import models.misc.ImageItem;
import models.poi.AbstractPOI;

import java.util.Arrays;

/**
 * 返回用户的摘要（以列表形式获取用户信息时使用，比如获得好友列表，获得黑名单列表等）
 * <p/>
 * Created by zephyre on 10/28/14.
 */
public class SimplePOIFormatter extends TaoziBaseFormatter {

    @Override
    public JsonNode format(AizouBaseEntity item) {
        ObjectMapper mapper = getObjectMapper();

        ((SimpleFilterProvider) mapper.getSerializationConfig().getFilterProvider())
                .addFilter("abstractPOIFilter",
                        SimpleBeanPropertyFilter.filterOutAllExcept(
                                AizouBaseEntity.FD_ID,
                                AbstractPOI.FD_EN_NAME,
                                AbstractPOI.FD_ZH_NAME,
                                AbstractPOI.FD_DESC,
                                AbstractPOI.FD_IMAGES,
                                AbstractPOI.FD_LOCATION,
                                AbstractPOI.FD_RATING
                        ));

        SimpleModule imageItemModule = new SimpleModule();
        imageItemModule.addSerializer(ImageItem.class,
                new ImageItemSerializer(ImageItemSerializer.ImageSizeDesc.MEDIUM));
        mapper.registerModule(imageItemModule);

        ObjectNode result = mapper.valueToTree(item);

        stringFields.addAll(Arrays.asList(AbstractPOI.FD_EN_NAME, AbstractPOI.FD_ZH_NAME, AbstractPOI.FD_DESC,AbstractPOI.fnRating));

        listFields.add(AbstractPOI.FD_IMAGES);

        mapFields.add(AbstractPOI.FD_LOCATION);

        return postProcess(result);
    }
}
