package formatter.taozi.poi;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import formatter.AizouSerializer;
import models.AizouBaseEntity;
import models.geo.GeoJsonPoint;
import models.geo.Locality;
import models.misc.ImageItem;
import models.poi.*;
import org.apache.commons.lang3.StringUtils;
import utils.Constants;
import utils.TaoziDataFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Created by zephyre on 12/6/14.
 */
public class PolymorphicPOISerializer<T extends AbstractPOI> extends AizouSerializer<AbstractPOI> {

    public enum Level {
        SIMPLE,
        DETAILED
    }

    private Level level;

    public PolymorphicPOISerializer() {
        this(Level.SIMPLE);
    }

    public PolymorphicPOISerializer(Level level) {
        this.level = level;
    }

    // Polymorphic List Use
    @Override
    public void serialize(AbstractPOI abstractPOI, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException, JsonProcessingException {
        jsonGenerator.writeStartObject();


        writeObjectId(abstractPOI, jsonGenerator, serializerProvider);
        jsonGenerator.writeStringField(AbstractPOI.FD_ZH_NAME, getString(abstractPOI.zhName));
        jsonGenerator.writeStringField(AbstractPOI.FD_EN_NAME, getString(abstractPOI.enName));
        jsonGenerator.writeObjectField(AbstractPOI.FD_RATING, getValue(abstractPOI.rating));
        jsonGenerator.writeStringField(AbstractPOI.FD_ADDRESS, getString(abstractPOI.address));
//        jsonGenerator.writeStringField(AbstractPOI.FD_STYLE, getString(abstractPOI.getStyle()));

        jsonGenerator.writeFieldName("style");
        jsonGenerator.writeStartArray();
        String style = abstractPOI.getStyle();
        if (style != null) {
            JsonSerializer<Object> retLocality = serializerProvider.findValueSerializer(String.class, null);
            for (String s : Arrays.asList(style)) {
                retLocality.serialize(s, jsonGenerator, serializerProvider);
            }
        }
        jsonGenerator.writeEndArray();

        jsonGenerator.writeFieldName("images");
        List<ImageItem> images = abstractPOI.getImages();
        if (level.equals(Level.SIMPLE))
            images = TaoziDataFilter.getOneImage(images);
        jsonGenerator.writeStartArray();
        if (images != null && !images.isEmpty()) {
            JsonSerializer<Object> ret = serializerProvider.findValueSerializer(ImageItem.class, null);
            for (ImageItem image : images)
                ret.serialize(image, jsonGenerator, serializerProvider);
        }
        jsonGenerator.writeEndArray();

        // Diff POI
        if (abstractPOI instanceof ViewSpot) {
            // Type use for serialize
            jsonGenerator.writeStringField("type", "vs");
            // TimeCost
            jsonGenerator.writeStringField(AbstractPOI.FD_TIMECOSTDESC, getString(abstractPOI.timeCostDesc));
            // PriceDesc
            jsonGenerator.writeStringField(AbstractPOI.FD_PRICE_DESC, getString(abstractPOI.priceDesc));

            // Tel
            List<String> tels = abstractPOI.tel;
            jsonGenerator.writeFieldName(AbstractPOI.FD_TELEPHONE);
            jsonGenerator.writeStartArray();
            if (tels != null && (!tels.isEmpty())) {
                for (String tel : tels)
                    jsonGenerator.writeString(getString(tel));
            }
            jsonGenerator.writeEndArray();

        } else if (abstractPOI instanceof Restaurant) {
            // Type use for serialize
            jsonGenerator.writeStringField("type", "restaurant");

            // PriceDesc
            jsonGenerator.writeStringField(AbstractPOI.FD_PRICE_DESC, getString(TaoziDataFilter.getPriceDesc(abstractPOI)));
            jsonGenerator.writeObjectField(AbstractPOI.FD_PRICE, getValue(abstractPOI.price));

        } else if (abstractPOI instanceof Shopping) {
            // Type use for serialize
            jsonGenerator.writeStringField("type", "shopping");
        } else if (abstractPOI instanceof Hotel) {
            jsonGenerator.writeStringField("type", "hotel");
        }

        // Rank
        jsonGenerator.writeObjectField(AbstractPOI.FD_RANK, getValue(checkRank(abstractPOI.getRank())));

        // Locality
        jsonGenerator.writeFieldName(AbstractPOI.FD_LOCALITY);
        Locality localities = abstractPOI.getLocality();
        JsonSerializer<Object> retLocality;
        if (localities != null) {
            retLocality = serializerProvider.findValueSerializer(Locality.class, null);
            retLocality.serialize(localities, jsonGenerator, serializerProvider);
        } else {
            retLocality = serializerProvider.findNullValueSerializer(null);
            retLocality.serialize(localities, jsonGenerator, serializerProvider);
        }

        // Location
        jsonGenerator.writeFieldName(AbstractPOI.FD_LOCATION);
        GeoJsonPoint geoJsonPoint = abstractPOI.getLocation();
        JsonSerializer<Object> retLocalition;
        if (geoJsonPoint != null) {
            retLocalition = serializerProvider.findValueSerializer(GeoJsonPoint.class, null);
            retLocalition.serialize(geoJsonPoint, jsonGenerator, serializerProvider);
        } else {
            retLocalition = serializerProvider.findNullValueSerializer(null);
            retLocalition.serialize(geoJsonPoint, jsonGenerator, serializerProvider);
        }

        if (level.equals(Level.DETAILED)) {
            String id = abstractPOI.getId().toString();
            jsonGenerator.writeBooleanField(AizouBaseEntity.FD_IS_FAVORITE, getValue(abstractPOI.getIsFavorite()));
            jsonGenerator.writeStringField(AbstractPOI.FD_DESC, getString(StringUtils.abbreviate(abstractPOI.desc, Constants.ABBREVIATE_LEN)));

            // Tel
            List<String> tels = abstractPOI.tel;
            jsonGenerator.writeFieldName(AbstractPOI.FD_TELEPHONE);
            jsonGenerator.writeStartArray();
            if (tels != null && (!tels.isEmpty())) {
                for (String tel : tels)
                    jsonGenerator.writeString(getString(tel));
            }
            jsonGenerator.writeEndArray();

            if (abstractPOI instanceof ViewSpot) {
                jsonGenerator.writeStringField(ViewSpot.FD_OPEN_TIME, getString(abstractPOI.openTime));
                jsonGenerator.writeStringField(ViewSpot.FD_TIME_COST_DESC, getString(((ViewSpot) abstractPOI).getTimeCostDesc()));
                jsonGenerator.writeStringField(ViewSpot.FD_TRAVEL_MONTH, getString(((ViewSpot) abstractPOI).getTravelMonth()));
                if (abstractPOI.getTrafficInfo() == null || abstractPOI.getTrafficInfo().equals(""))
                    jsonGenerator.writeStringField(AbstractPOI.FD_TRAFFICINFO_URL, "");
                else
                    jsonGenerator.writeStringField(AbstractPOI.FD_TRAFFICINFO_URL, "http://api.lvxingpai.com/app/poi/viewspots/" + id + "/detailsScala?category=trafficInfo");
                //jsonGenerator.writeStringField(AbstractPOI.FD_TRAFFICINFO_URL, "http://h5.taozilvxing.com/poi/traffic.php?tid=" + id);

                if (abstractPOI.getVisitGuide() == null || abstractPOI.getVisitGuide().equals(""))
                    jsonGenerator.writeStringField(AbstractPOI.FD_VISITGUIDE_URL, "");
                else
                    jsonGenerator.writeStringField(AbstractPOI.FD_VISITGUIDE_URL, "http://api.lvxingpai.com/app/poi/viewspots/" + id + "/detailsScala?category=visitGuide");
//                    jsonGenerator.writeStringField(AbstractPOI.FD_VISITGUIDE_URL, "http://h5.taozilvxing.com/poi/play.php?tid=" + id);

                if (abstractPOI.getTips() == null || abstractPOI.getTips().equals(""))
                    jsonGenerator.writeStringField(AbstractPOI.FD_TIPS_URL, "");
                else
                    jsonGenerator.writeStringField(AbstractPOI.FD_TIPS_URL, "http://api.lvxingpai.com/app/poi/viewspots/" + id + "/detailsScala?category=tips");
//                jsonGenerator.writeStringField(AbstractPOI.FD_TIPS_URL, "http://h5.taozilvxing.com/poi/tips.php?tid=" + id);
            }
        }

        jsonGenerator.writeEndObject();
    }

    // Polymorphic Single Bean Use
    @Override
    public void serializeWithType(AbstractPOI abstractPOI, JsonGenerator jsonGenerator,
                                  SerializerProvider serializerProvider, TypeSerializer typeSer)
            throws IOException, JsonProcessingException {

        jsonGenerator.writeStartObject();

        writeObjectId(abstractPOI, jsonGenerator, serializerProvider);
        jsonGenerator.writeStringField(AbstractPOI.FD_ZH_NAME, getString(abstractPOI.zhName));
        jsonGenerator.writeStringField(AbstractPOI.FD_EN_NAME, getString(abstractPOI.enName));
        jsonGenerator.writeObjectField(AbstractPOI.FD_RATING, getValue(abstractPOI.rating));
        jsonGenerator.writeStringField(AbstractPOI.FD_ADDRESS, getString(abstractPOI.address));
        // jsonGenerator.writeStringField(AbstractPOI.FD_STYLE, getString(abstractPOI.getStyle()));

        jsonGenerator.writeFieldName("style");
        jsonGenerator.writeStartArray();
        String style = abstractPOI.getStyle();
        if (style != null) {
            JsonSerializer<Object> retLocality = serializerProvider.findValueSerializer(String.class, null);
            for (String s : Arrays.asList(style)) {
                retLocality.serialize(s, jsonGenerator, serializerProvider);
            }
        }
        jsonGenerator.writeEndArray();


        jsonGenerator.writeFieldName("images");
        List<ImageItem> images = abstractPOI.getImages();
        jsonGenerator.writeStartArray();
        if (images != null && !images.isEmpty()) {
            JsonSerializer<Object> ret = serializerProvider.findValueSerializer(ImageItem.class, null);
            for (ImageItem image : images)
                ret.serialize(image, jsonGenerator, serializerProvider);
        }
        jsonGenerator.writeEndArray();

        // Diff POI
        if (abstractPOI instanceof ViewSpot) {
            // Type use for serialize
            jsonGenerator.writeStringField("type", "vs");
            // TimeCost
            jsonGenerator.writeStringField(AbstractPOI.FD_TIMECOSTDESC, getString(abstractPOI.timeCostDesc));
            // PriceDesc
            jsonGenerator.writeStringField(AbstractPOI.FD_PRICE_DESC, getString(abstractPOI.priceDesc));

            // Tel
            List<String> tels = abstractPOI.tel;
            jsonGenerator.writeFieldName(AbstractPOI.FD_TELEPHONE);
            jsonGenerator.writeStartArray();
            if (tels != null && (!tels.isEmpty())) {
                for (String tel : tels)
                    jsonGenerator.writeString(getString(tel));
            }
            jsonGenerator.writeEndArray();
        } else if (abstractPOI instanceof Restaurant) {
            // Type use for serialize
            jsonGenerator.writeStringField("type", "restaurant");


            // PriceDesc TaoziDataFilter.getPriceDesc(poiInfo)
            jsonGenerator.writeStringField(AbstractPOI.FD_PRICE_DESC, getString(TaoziDataFilter.getPriceDesc(abstractPOI)));
            jsonGenerator.writeObjectField(AbstractPOI.FD_PRICE, getValue(abstractPOI.price));
            // Tel
            List<String> tels = abstractPOI.tel;
            jsonGenerator.writeFieldName(AbstractPOI.FD_TELEPHONE);
            jsonGenerator.writeStartArray();
            if (tels != null && (!tels.isEmpty())) {
                for (String tel : tels)
                    jsonGenerator.writeString(getString(tel));
            }
            jsonGenerator.writeEndArray();
        } else if (abstractPOI instanceof Shopping) {
            // Type use for serialize
            jsonGenerator.writeStringField("type", "shopping");
            // Tel
            List<String> tels = abstractPOI.tel;
            jsonGenerator.writeFieldName(AbstractPOI.FD_TELEPHONE);
            jsonGenerator.writeStartArray();
            if (tels != null && (!tels.isEmpty())) {
                for (String tel : tels)
                    jsonGenerator.writeString(getString(tel));
            }
            jsonGenerator.writeEndArray();

        } else if (abstractPOI instanceof Hotel) {
            // Type use for serialize
            jsonGenerator.writeStringField("type", "hotel");
            // Tel
            List<String> tels = abstractPOI.tel;
            jsonGenerator.writeFieldName(AbstractPOI.FD_TELEPHONE);
            jsonGenerator.writeStartArray();
            if (tels != null && (!tels.isEmpty())) {
                for (String tel : tels)
                    jsonGenerator.writeString(getString(tel));
            }
            jsonGenerator.writeEndArray();
        }
        // Rank
        jsonGenerator.writeObjectField(AbstractPOI.FD_RANK, getValue(checkRank(abstractPOI.getRank())));
        // Targets
//        jsonGenerator.writeFieldName(AbstractPOI.detTargets);
//        List<ObjectId> targets = abstractPOI.targets;
//        JsonSerializer<Object> retObjectId;
//        if (targets != null && !targets.isEmpty()) {
//            jsonGenerator.writeStartArray();
//            retObjectId = serializerProvider.findValueSerializer(ObjectId.class, null);
//            for (ObjectId id : targets)
//                retObjectId.serialize(id, jsonGenerator, serializerProvider);
//            jsonGenerator.writeEndArray();
//        } else {
//            retObjectId = serializerProvider.findNullValueSerializer(null);
//            retObjectId.serialize(targets, jsonGenerator, serializerProvider);
//        }

        // Locality
//        jsonGenerator.writeFieldName(AbstractPOI.FD_LOCALITY);
//        Locality localities = abstractPOI.getLocality();
//        JsonSerializer<Object> retLocality;
//        if (localities != null) {
//            retLocality = serializerProvider.findValueSerializer(Locality.class, null);
//            retLocality.serialize(localities, jsonGenerator, serializerProvider);
//        } else {
//            retLocality = serializerProvider.findNullValueSerializer(null);
//            retLocality.serialize(localities, jsonGenerator, serializerProvider);
//        }

        // Location
        jsonGenerator.writeFieldName(AbstractPOI.FD_LOCATION);
        GeoJsonPoint geoJsonPoint = abstractPOI.getLocation();
        JsonSerializer<Object> retLocalition;
        if (geoJsonPoint != null) {
            retLocalition = serializerProvider.findValueSerializer(GeoJsonPoint.class, null);
            retLocalition.serialize(geoJsonPoint, jsonGenerator, serializerProvider);
        } else {
            retLocalition = serializerProvider.findNullValueSerializer(null);
            retLocalition.serialize(geoJsonPoint, jsonGenerator, serializerProvider);
        }

        if (level.equals(Level.DETAILED)) {
            String id = abstractPOI.getId().toString();
            jsonGenerator.writeBooleanField(AizouBaseEntity.FD_IS_FAVORITE, getValue(abstractPOI.getIsFavorite()));
            jsonGenerator.writeStringField(AbstractPOI.FD_DESC, getString(abstractPOI.desc));

            if (abstractPOI instanceof ViewSpot) {
                jsonGenerator.writeStringField(ViewSpot.FD_OPEN_TIME, getString(abstractPOI.openTime));
                jsonGenerator.writeStringField(ViewSpot.FD_TIME_COST_DESC, getString(((ViewSpot) abstractPOI).getTimeCostDesc()));
                jsonGenerator.writeStringField(ViewSpot.FD_TRAVEL_MONTH, getString(((ViewSpot) abstractPOI).getTravelMonth()));
                if (abstractPOI.getTrafficInfo() == null || abstractPOI.getTrafficInfo().equals(""))
                    jsonGenerator.writeStringField(AbstractPOI.FD_TRAFFICINFO_URL, "");
                else
                    jsonGenerator.writeStringField(AbstractPOI.FD_TRAFFICINFO_URL, "http://api.lvxingpai.com/app/poi/viewspots/" + id + "/detailsScala?category=trafficInfo");
//                jsonGenerator.writeStringField(AbstractPOI.FD_TRAFFICINFO_URL, "http://h5.taozilvxing.com/poi/traffic.php?tid=" + id);

                if (abstractPOI.getVisitGuide() == null || abstractPOI.getVisitGuide().equals(""))
                    jsonGenerator.writeStringField(AbstractPOI.FD_VISITGUIDE_URL, "");
                else
                    jsonGenerator.writeStringField(AbstractPOI.FD_VISITGUIDE_URL, "http://api.lvxingpai.com/app/poi/viewspots/" + id + "/detailsScala?category=visitGuide");
//                jsonGenerator.writeStringField(AbstractPOI.FD_VISITGUIDE_URL, "http://h5.taozilvxing.com/poi/play.php?tid=" + id);

                if (abstractPOI.getTips() == null || abstractPOI.getTips().equals(""))
                    jsonGenerator.writeStringField(AbstractPOI.FD_TIPS_URL, "");
                else
                    jsonGenerator.writeStringField(AbstractPOI.FD_TIPS_URL, "http://api.lvxingpai.com/app/poi/viewspots/" + id + "/detailsScala?category=tips");
//                jsonGenerator.writeStringField(AbstractPOI.FD_TIPS_URL, "http://h5.taozilvxing.com/poi/tips.php?tid=" + id);

                if (abstractPOI.getDesc() == null || abstractPOI.getDesc().equals(""))
                    jsonGenerator.writeStringField("descUrl", "");
                else
                    jsonGenerator.writeStringField("descUrl", "http://api.lvxingpai.com/app/poi/viewspots/" + id + "/descriptionsScala");
//                jsonGenerator.writeStringField("descUrl", "http://h5.taozilvxing.com/poi/desc.php?tid=" + id);
            }
        }
        jsonGenerator.writeEndObject();
    }

    private int checkRank(Integer rank) {
        if (rank == null || rank >= 1000000)
            return 0;
        return rank;
    }
}

