package formatter.taozi.poi;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import formatter.taozi.ImageItemSerializer;
import formatter.taozi.TaoziBaseFormatter;
import models.TravelPiBaseItem;
import models.geo.Country;
import models.geo.Locality;
import models.misc.ImageItem;
import models.poi.AbstractPOI;
import models.poi.ViewSpot;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * 返回用户的摘要（以列表形式获取用户信息时使用，比如获得好友列表，获得黑名单列表等）
 * <p/>
 * Created by zephyre on 10/28/14.
 */
public class DetailedPOIFormatter<T extends AbstractPOI> extends TaoziBaseFormatter {

    private final Class<T> poiClass;

    public Class<T> getPoiClass() {
        return poiClass;
    }

    public DetailedPOIFormatter(Class<T> poiClass) {
        this.poiClass = poiClass;
    }

    @Override
    public JsonNode format(TravelPiBaseItem item) {
        ObjectMapper mapper = getObjectMapper();

        stringFields.addAll(Arrays.asList(AbstractPOI.FD_EN_NAME, AbstractPOI.FD_ZH_NAME, AbstractPOI.FD_DESC,
                AbstractPOI.FD_ADDRESS, AbstractPOI.FD_PRICE_DESC, AbstractPOI.FD_TELEPHONE));

        listFields.add(AbstractPOI.FD_IMAGES);

        mapFields.add(AbstractPOI.FD_LOCATION);

        Set<String> filteredFields = new HashSet<>();
        Collections.addAll(filteredFields,
                TravelPiBaseItem.FD_ID,
                AbstractPOI.FD_ZH_NAME,
                AbstractPOI.FD_EN_NAME,
                AbstractPOI.FD_DESC,
                AbstractPOI.FD_IMAGES,
                AbstractPOI.FD_LOCATION,
                AbstractPOI.FD_RATING,
                AbstractPOI.FD_ADDRESS,
                AbstractPOI.FD_PRICE_DESC,
                AbstractPOI.FD_TELEPHONE
        );

        if (DetailedPOIFormatter.this.getPoiClass() == ViewSpot.class) {
            String[] keyList = new String[]{
                    ViewSpot.FD_OPEN_TIME, ViewSpot.FD_TIME_COST_DESC, ViewSpot.FD_TRAVEL_MONTH,
                    ViewSpot.FD_TRAFFIC_URL, ViewSpot.FD_GUIDE_URL, ViewSpot.FD_KENGDIE_URL
            };
            Collections.addAll(filteredFields, keyList);
            Collections.addAll(stringFields, keyList);
        }

        SimpleFilterProvider provider = (SimpleFilterProvider) mapper.getSerializationConfig().getFilterProvider();
        provider.addFilter("abstractPOIFilter", SimpleBeanPropertyFilter.filterOutAllExcept(filteredFields))
                .addFilter("localityFilter", SimpleBeanPropertyFilter.filterOutAllExcept(
                        TravelPiBaseItem.FD_ID, Locality.FD_ZH_NAME, Locality.FD_EN_NAME))
                .addFilter("countryFilter", SimpleBeanPropertyFilter.filterOutAllExcept(
                        TravelPiBaseItem.FD_ID, Country.FD_ZH_NAME, Country.FD_EN_NAME));

        SimpleModule imageItemModule = new SimpleModule();
        imageItemModule.addSerializer(ImageItem.class,
                new ImageItemSerializer(ImageItemSerializer.ImageSizeDesc.MEDIUM));
        mapper.registerModule(imageItemModule);


        return postProcess((ObjectNode) mapper.valueToTree(item));
    }

}
