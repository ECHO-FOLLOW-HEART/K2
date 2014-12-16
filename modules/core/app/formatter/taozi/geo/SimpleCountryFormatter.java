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
import models.geo.Country;
import models.misc.ImageItem;
import models.poi.AbstractPOI;

import java.util.Arrays;

/**
 * 格式化国家的简单信息，主要使用在搜索列表中。
 * <p>
 * Created by lxf on 14-11-1.
 */
public class SimpleCountryFormatter extends TaoziBaseFormatter {

    @Override
    public JsonNode format(AizouBaseEntity item) {
        ObjectMapper mapper = getObjectMapper();

        ((SimpleFilterProvider) mapper.getSerializationConfig().getFilterProvider())
                .addFilter("countryFilter",
                        SimpleBeanPropertyFilter.filterOutAllExcept(
                                Country.fnDesc,
                                Country.FD_EN_NAME,
                                Country.FD_ZH_NAME,
                                Country.fnCode,
                                Country.FN_ID,
                                Country.fnImages
                        ));

        SimpleModule imageItemModule = new SimpleModule();
        imageItemModule.addSerializer(ImageItem.class,
                new ImageItemSerializer(ImageItemSerializer.ImageSizeDesc.MEDIUM));
        mapper.registerModule(imageItemModule);

        ObjectNode result = mapper.valueToTree(item);

        stringFields.addAll(Arrays.asList(Country.FD_EN_NAME, Country.FD_ZH_NAME, Country.FN_ID));

        listFields.add(AbstractPOI.FD_IMAGES);

        return postProcess(result);
    }
}
