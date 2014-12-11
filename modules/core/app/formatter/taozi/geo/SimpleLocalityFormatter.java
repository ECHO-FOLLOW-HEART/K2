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
import models.misc.ImageItem;
import models.poi.AbstractPOI;

import java.util.Arrays;

/**
 * Created by lxf on 14-11-12.
 */
public class SimpleLocalityFormatter extends TaoziBaseFormatter {

    @Override
    public JsonNode format(AizouBaseEntity item) {
        ObjectMapper mapper = getObjectMapper();

        ((SimpleFilterProvider) mapper.getSerializationConfig().getFilterProvider())
                .addFilter("localityFilter",
                        SimpleBeanPropertyFilter.filterOutAllExcept(
                                AizouBaseEntity.FD_ID,
                                AbstractPOI.FD_EN_NAME,
                                AbstractPOI.FD_ZH_NAME

                        ));

        SimpleModule imageItemModule = new SimpleModule();
        imageItemModule.addSerializer(ImageItem.class,
                new ImageItemSerializer(ImageItemSerializer.ImageSizeDesc.MEDIUM));
        mapper.registerModule(imageItemModule);

        ObjectNode result = mapper.valueToTree(item);

        stringFields.addAll(Arrays.asList(AbstractPOI.FD_EN_NAME, AbstractPOI.FD_ZH_NAME, AizouBaseEntity.FD_ID));

        return postProcess(result);
    }
}
