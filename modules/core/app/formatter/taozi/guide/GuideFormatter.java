package formatter.taozi.guide;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import formatter.AizouFormatter;
import formatter.AizouSerializer;
import formatter.taozi.*;
import formatter.taozi.geo.LocalitySerializer;
import formatter.taozi.poi.POISerializer;
import models.geo.GeoJsonPoint;
import models.geo.Locality;
import models.guide.AbstractGuide;
import models.guide.Guide;
import models.guide.ItinerItem;
import models.misc.ImageItem;
import models.poi.AbstractPOI;
import models.poi.Restaurant;
import models.poi.Shopping;

import java.io.IOException;
import java.util.*;

/**
 * 返回攻略中行程单内容
 * <p>
 * Created by zephyre on 10/28/14.
 */
public class GuideFormatter extends AizouFormatter<Guide> {

    public GuideFormatter(int imgWidth) {
        registerSerializer(Guide.class, new GuideSerializer());
        registerSerializer(ImageItem.class, new ImageItemSerializer(imgWidth));
        registerSerializer(Locality.class, new LocalitySerializer());
        registerSerializer(ItinerItem.class, new ItinerItemSerializer());
        registerSerializer(AbstractPOI.class, new POISerializer());
        registerSerializer(GeoJsonPoint.class, new GeoJsonPointSerializer());

        initObjectMapper(null);

        filteredFields.addAll(Arrays.asList(
                AbstractGuide.fdId,
                AbstractGuide.fnTitle,
                AbstractGuide.fnItinerary,
                AbstractGuide.fnShopping,
                AbstractGuide.fnRestaurant,
                Guide.fnUserId,
                Guide.fnLocalities,
                Guide.fnUpdateTime,
                Guide.fnImages,
                Guide.fnItineraryDays));
    }

    class GuideSerializer extends AizouSerializer<Guide> {
        @Override
        public void serialize(Guide guide, JsonGenerator jgen, SerializerProvider serializerProvider)
                throws IOException {
            jgen.writeStartObject();

            writeObjectId(guide, jgen, serializerProvider);

            // Images
            jgen.writeFieldName("images");
            List<ImageItem> images = guide.getImages();
            jgen.writeStartArray();
            if (images != null && !images.isEmpty()) {
                JsonSerializer<Object> ret = serializerProvider.findValueSerializer(ImageItem.class, null);
                for (ImageItem image : images)
                    ret.serialize(image, jgen, serializerProvider);
            }
            jgen.writeEndArray();

            jgen.writeStringField(AbstractGuide.fnTitle, getString(guide.title));
            jgen.writeNumberField(Guide.fnUserId, getValue(guide.userId));
            jgen.writeNumberField(Guide.fnItineraryDays, getValue(guide.itineraryDays));
            jgen.writeNumberField(Guide.fnUpdateTime, getValue(guide.updateTime));

            // Locality
            jgen.writeFieldName(Guide.fnLocalities);
            jgen.writeStartArray();
            List<Locality> localities = guide.localities;
            if (localities != null && !localities.isEmpty()) {
                JsonSerializer<Object> retLocality = serializerProvider.findValueSerializer(Locality.class, null);
                for (Locality locality : localities) {
                    retLocality.serialize(locality, jgen, serializerProvider);
                }
            }
            jgen.writeEndArray();


            // Itinerary
            jgen.writeFieldName(Guide.fnItinerary);
            jgen.writeStartArray();
            List<ItinerItem> itinerItems = guide.itinerary;
            if (itinerItems != null && !itinerItems.isEmpty()) {
                JsonSerializer<Object> retItinerItems = serializerProvider.findValueSerializer(ItinerItem.class, null);
                for (ItinerItem itinerItem : itinerItems) {
                    if (itinerItem != null && itinerItem.poi != null) {
                        retItinerItems.serialize(itinerItem, jgen, serializerProvider);
                    }
                }
            }
            jgen.writeEndArray();

            //Shopping
            jgen.writeFieldName(Guide.fnShopping);
            jgen.writeStartArray();
            List<Shopping> shoppings = guide.shopping;
            if (shoppings != null && !shoppings.isEmpty()) {
                JsonSerializer<Object> retShoppings = serializerProvider.findValueSerializer(Shopping.class, null);
                for (Shopping shopping : shoppings) {
                    retShoppings.serialize(shopping, jgen, serializerProvider);
                }
            }
            jgen.writeEndArray();

            // Restaurant
            jgen.writeFieldName(Guide.fnRestaurant);
            jgen.writeStartArray();
            List<Restaurant> restaurants = guide.restaurant;
            if (restaurants != null && !restaurants.isEmpty()) {
                JsonSerializer<Object> retRestaurants = serializerProvider.findValueSerializer(Restaurant.class, null);
                for (Restaurant restaurant : restaurants) {
                    retRestaurants.serialize(restaurant, jgen, serializerProvider);
                }
            }
            jgen.writeEndArray();

            jgen.writeEndObject();
        }
    }

}
