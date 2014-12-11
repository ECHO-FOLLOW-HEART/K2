package formatter.taozi.guide;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ser.PropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import formatter.taozi.ImageItemSerializer;
import formatter.taozi.TaoziBaseFormatter;
import models.AizouBaseEntity;
import models.geo.Locality;
import models.guide.AbstractGuide;
import models.guide.Guide;
import models.guide.ItinerItem;
import models.misc.ImageItem;
import models.poi.AbstractPOI;

import java.util.*;

/**
 * 返回攻略中行程单内容
 * <p>
 * Created by zephyre on 10/28/14.
 */
public class GuideFormatter extends TaoziBaseFormatter {

    private Set<String> poiStringFields;

    private Set<String> localityStringFields;

    @Override
    public JsonNode format(AizouBaseEntity item) {

        Map<String, PropertyFilter> filterMap = new HashMap<>();
        filteredFields = new HashSet<>();
        Collections.addAll(filteredFields,
                AizouBaseEntity.FD_ID,
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
        filterMap.put("abstractPOIFilter", SimpleBeanPropertyFilter.filterOutAllExcept(filteredFields));
        filterMap.put("itinerItemFilter", SimpleBeanPropertyFilter.filterOutAllExcept(
                ItinerItem.fdDayIndex, ItinerItem.fdPoi));
        filterMap.put("localityFilter", SimpleBeanPropertyFilter.filterOutAllExcept(
                AizouBaseEntity.FD_ID, Locality.FD_ZH_NAME, Locality.FD_EN_NAME));

        Map<Class<? extends ImageItem>, JsonSerializer<ImageItem>> serializerMap = new HashMap<>();
        serializerMap.put(ImageItem.class, new ImageItemSerializer(ImageItemSerializer.ImageSizeDesc.MEDIUM));
        ObjectMapper mapper = getObjectMapper(filterMap, serializerMap);


        ((SimpleFilterProvider) mapper.getSerializationConfig().getFilterProvider())
                .addFilter("guideFilter",
                        SimpleBeanPropertyFilter.filterOutAllExcept(
                                AbstractGuide.fdId,
                                AbstractGuide.fnTitle,
                                AbstractGuide.fnItinerary,
                                AbstractGuide.fnShopping,
                                AbstractGuide.fnRestaurant,
                                Guide.fnUserId,
                                Guide.fnLocalities,
                                Guide.fnUpdateTime,
                                Guide.fnImages,
                                Guide.fnItineraryDays
                        ));

        SimpleModule imageItemModule = new SimpleModule();
        imageItemModule.addSerializer(ImageItem.class,
                new ImageItemSerializer(ImageItemSerializer.ImageSizeDesc.MEDIUM));
        mapper.registerModule(imageItemModule);

        ObjectNode result = mapper.valueToTree(item);

        poiStringFields = new HashSet<String>() {
        };
        Collections.addAll(poiStringFields,
                AbstractPOI.FD_ADDRESS,
                AbstractPOI.FD_EN_NAME,
                AbstractPOI.FD_PRICE_DESC,
                AbstractPOI.FD_TELEPHONE,
                AbstractPOI.FD_RATING);

        localityStringFields = new HashSet<String>() {
        };
        Collections.addAll(localityStringFields,
                Locality.FD_EN_NAME);

        listFields.add(AbstractPOI.FD_IMAGES);

        return postProcess(result);
    }

    protected ObjectNode postProcess(ObjectNode result) {

        // 处理字符串字段
        JsonNode oNode = result.get("itinerary");
        postProcessPoiInItinerary(oNode);
        oNode = result.get("shopping");
        postProcessPoiInList(oNode, poiStringFields);
        oNode = result.get("restaurant");
        postProcessPoiInList(oNode, poiStringFields);
        oNode = result.get("localities");
        postProcessPoiInList(oNode, localityStringFields);
        return result;
    }

    private void postProcessPoiInItinerary(JsonNode oNode) {
        ObjectNode tempObjNode;
        if (oNode.findValues("poi") != null) {
            for (JsonNode node : oNode.findValues("poi")) {
                tempObjNode = (ObjectNode) node;
                for (String key : poiStringFields) {
                    if (tempObjNode.get(key) == null || tempObjNode.get(key).isNull())
                        tempObjNode.put(key, "");
                }

            }
        }
    }

    private void postProcessPoiInList(JsonNode oNode, Set<String> fields) {
        ObjectNode tempObjNode;
        if (oNode != null && oNode.isArray()) {
            for (JsonNode node : oNode) {
                tempObjNode = (ObjectNode) node;
                for (String key : fields) {
                    if (tempObjNode.get(key) == null || tempObjNode.get(key).isNull())
                        tempObjNode.put(key, "");
                }
            }
        }
    }

}
