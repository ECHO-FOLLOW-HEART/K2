package formatter.taozi.poi;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ser.PropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import formatter.taozi.ImageItemSerializerOld;
import formatter.taozi.TaoziBaseFormatter;
import models.AizouBaseEntity;
import models.geo.Country;
import models.geo.Locality;
import models.misc.ImageItem;
import models.poi.AbstractPOI;
import models.poi.ViewSpot;

import java.util.*;

/**
 * 返回用户的摘要（以列表形式获取用户信息时使用，比如获得好友列表，获得黑名单列表等）
 * <p>
 * Created by zephyre on 10/28/14.
 */
public class DetailedPOIFormatter<T extends AbstractPOI> extends TaoziBaseFormatter {

    private final Class<T> poiClass;

    public Set<String> getFilteredFields() {
        return filteredFields;
    }

    public Class<T> getPoiClass() {
        return poiClass;
    }

    public DetailedPOIFormatter setImageWidth(int width) {
        imageWidth = width;
        return this;
    }

    public DetailedPOIFormatter(Class<T> poiClass) {
        this.poiClass = poiClass;

        stringFields.addAll(Arrays.asList(AbstractPOI.FD_EN_NAME, AbstractPOI.FD_ZH_NAME, AbstractPOI.FD_DESC,
                AbstractPOI.FD_ADDRESS, AbstractPOI.FD_PRICE_DESC));

        listFields.addAll(Arrays.asList(AbstractPOI.FD_IMAGES, AbstractPOI.FD_TELEPHONE));

        mapFields.add(AbstractPOI.FD_LOCATION);

        filteredFields = new HashSet<>();
        Collections.addAll(filteredFields,
                AizouBaseEntity.FD_ID,
                AizouBaseEntity.FD_IS_FAVORITE,
                AbstractPOI.FD_ZH_NAME,
                AbstractPOI.FD_EN_NAME,
                AbstractPOI.FD_DESC,
                AbstractPOI.FD_IMAGES,
                AbstractPOI.FD_LOCATION,
                AbstractPOI.FD_RATING,
                AbstractPOI.FD_ADDRESS,
                AbstractPOI.FD_PRICE,
                AbstractPOI.FD_PRICE_DESC,
                AbstractPOI.FD_TIPS,
                AbstractPOI.FD_VISITGUIDE,
                AbstractPOI.FD_TRAFFICINFO,
                AbstractPOI.FD_RANK
//                AbstractPOI.FD_TAOZIENA

        );

        if (DetailedPOIFormatter.this.getPoiClass() == ViewSpot.class) {
            String[] keyList = new String[]{
                    ViewSpot.FD_OPEN_TIME, ViewSpot.FD_TIME_COST_DESC, ViewSpot.FD_TRAVEL_MONTH,
                    ViewSpot.FD_TRAFFIC_URL, ViewSpot.FD_VISITGUIDE_URL, ViewSpot.FD_TIPS_URL
            };
            Collections.addAll(filteredFields, keyList);
            Collections.addAll(stringFields, keyList);
        }
    }

    @Override
    public JsonNode format(AizouBaseEntity item) {

        Map<String, PropertyFilter> filterMap = new HashMap<>();
        List<String> removeFields = new ArrayList<>();
        Collections.addAll(removeFields, AbstractPOI.FD_TIPS, AbstractPOI.FD_TRAFFICINFO, AbstractPOI.FD_VISITGUIDE);
        // tips内容由H5页面给出
        filteredFields.removeAll(removeFields);
        filterMap.put("abstractPOIFilter", SimpleBeanPropertyFilter.filterOutAllExcept(filteredFields));
        filterMap.put("countryFilter", SimpleBeanPropertyFilter.filterOutAllExcept(
                AizouBaseEntity.FD_ID, Country.FD_ZH_NAME, Country.FD_EN_NAME));
        filterMap.put("localityFilter", SimpleBeanPropertyFilter.filterOutAllExcept(
                AizouBaseEntity.FD_ID, Locality.FD_ZH_NAME, Locality.FD_EN_NAME));

        Map<Class<? extends ImageItem>, JsonSerializer<ImageItem>> serializerMap = new HashMap<>();
        serializerMap.put(ImageItem.class, new ImageItemSerializerOld(imageWidth));

        ObjectMapper mapper = getObjectMapper(filterMap, serializerMap);

        return postProcess((ObjectNode) mapper.valueToTree(item));
    }

}
