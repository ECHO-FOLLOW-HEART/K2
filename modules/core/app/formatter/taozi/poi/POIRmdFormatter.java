package formatter.taozi.poi;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.PropertyFilter;
import com.fasterxml.jackson.databind.ser.PropertyWriter;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import formatter.taozi.ImageItemSerializer;
import formatter.taozi.TaoziBaseFormatter;
import models.AizouBaseEntity;
import models.misc.ImageItem;
import models.poi.AbstractPOI;
import models.poi.POIRmd;
import formatter.JsonFormatter;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * 返回POI的推荐
 * <p/>
 * Created by zephyre on 10/28/14.
 */
public class POIRmdFormatter extends TaoziBaseFormatter {

    @Override
    public JsonNode format(AizouBaseEntity item) {
        ObjectMapper mapper = getObjectMapper();

        SimpleFilterProvider simpleFilterProvider = ((SimpleFilterProvider) mapper.getSerializationConfig().getFilterProvider());
        simpleFilterProvider.addFilter("poiRmdFilter",
                        SimpleBeanPropertyFilter.filterOutAllExcept(
                                POIRmd.fnTitle,
                                POIRmd.fnImages,
                                POIRmd.fnRating

                        ));

        SimpleModule imageItemModule = new SimpleModule();
        imageItemModule.addSerializer(ImageItem.class,
                new ImageItemSerializer(ImageItemSerializer.ImageSizeDesc.MEDIUM));
        mapper.registerModule(imageItemModule);

        ObjectNode result = mapper.valueToTree(item);

        return postProcess(result);
    }

}
