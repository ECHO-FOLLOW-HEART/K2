package formatter.taozi.misc;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import formatter.taozi.TaoziBaseFormatter;
import models.AizouBaseEntity;
import models.misc.Images;

/**
 * Created by lxf on 14-11-12.
 */
public class ImageFormatter extends TaoziBaseFormatter {

    @Override
    public JsonNode format(AizouBaseEntity item) {
        ObjectMapper mapper = getObjectMapper();
//
        ((SimpleFilterProvider) mapper.getSerializationConfig().getFilterProvider())
                .addFilter("imagesFilter",
                        SimpleBeanPropertyFilter.filterOutAllExcept(
//                                Images.FD_ID,
//                                Images.FD_WIDTH,
//                                Images.FD_HEIGHT,
                                Images.FD_URL
                        ));

//        SimpleModule imageItemModule = new SimpleModule();
//        imageItemModule.addSerializer("",
//                new ImageItemSerializer(ImageItemSerializer.ImageSizeDesc.MEDIUM));
//        mapper.registerModule(imageItemModule);
        Images images = ((Images)item);
        images.setUrl(images.getFullUrl());
        ObjectNode result = mapper.valueToTree(item);

        //stringFields.addAll(Arrays.asList(AbstractPOI.FD_EN_NAME, AbstractPOI.FD_ZH_NAME, AizouBaseEntity.FD_ID));

        return postProcess(result);
    }
}
