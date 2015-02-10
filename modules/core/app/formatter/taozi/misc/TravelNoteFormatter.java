package formatter.taozi.misc;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import formatter.AizouFormatter;
import formatter.AizouSerializer;
import formatter.taozi.ImageItemSerializer;
import models.misc.ImageItem;
import models.misc.TravelNote;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Created by lxf on 14-11-1.
 */
public class TravelNoteFormatter extends AizouFormatter<TravelNote> {

    private Level level;

    public TravelNoteFormatter setLevel(Level level) {
        this.level = level;
        return this;
    }

    public enum Level {
        SIMPLE,
        DETAILED
    }

    public TravelNoteFormatter() {
        this(640);
    }

    public TravelNoteFormatter(int imgWidth) {
        imageWidth = imgWidth;
        registerSerializer(ImageItem.class, new ImageItemSerializer(imgWidth));
        registerSerializer(TravelNote.class, new TravelNoteSerializer());

        initObjectMapper(null);
//        filteredFields.addAll(Arrays.asList(TravelNote.fnId,
//                TravelNote.fnAuthorAvatar,
//                TravelNote.fnAuthorName,
//                TravelNote.fnImages,
//                TravelNote.fnTitle,
//                TravelNote.fnPublishTime,
//                TravelNote.fnTravelTime,
//                TravelNote.fnSummary,
//                TravelNote.fnEssence,
//                TravelNote.fnSource));
//        if (level.equals(Level.DETAILED)) {
//            filteredFields.addAll(Arrays.asList(
//                    TravelNote.fnRating,
//                    TravelNote.fnNoteContents,
//                    TravelNote.fnUpperCost,
//                    TravelNote.fnLowerCost,
//                    TravelNote.fnCommentCnt,
//                    TravelNote.fnViewCnt,
//                    TravelNote.fnFavorCnt));
//        }
    }

    class TravelNoteSerializer extends AizouSerializer<TravelNote> {
        @Override
        public void serialize(TravelNote travelNote, JsonGenerator jgen, SerializerProvider serializerProvider)
                throws IOException {
            jgen.writeStartObject();

            writeObjectId(travelNote, jgen, serializerProvider);
            jgen.writeStringField(TravelNote.fnAuthorAvatar, getString(travelNote.authorAvatar));
            jgen.writeStringField(TravelNote.fnAuthorName, getString(travelNote.authorName));
            jgen.writeStringField(TravelNote.fnTitle, getString(travelNote.title));
            jgen.writeStringField(TravelNote.fnSummary, getString(travelNote.summary));
            jgen.writeBooleanField(TravelNote.fnEssence, getValue(travelNote.essence));

            if (travelNote.source != null &&
                    (travelNote.source.contains("百度") || travelNote.source.contains("baidu")))
                jgen.writeStringField(TravelNote.fnSource, "baidu");
            else
                jgen.writeStringField(TravelNote.fnSource, "");

            // publishTime
            if (travelNote.publishTime == null)
                jgen.writeNullField(TravelNote.fnPublishTime);
            else
                jgen.writeNumberField(TravelNote.fnPublishTime, travelNote.publishTime == null ? null : getValue(travelNote.publishTime));

            // travelTime
            if (travelNote.travelTime == null)
                jgen.writeNullField(TravelNote.fnTravelTime);
            else
                jgen.writeNumberField(TravelNote.fnTravelTime, getValue(travelNote.travelTime));

            // Images
            jgen.writeFieldName("images");
            List<ImageItem> images = travelNote.images;
            jgen.writeStartArray();
            if (images != null && !images.isEmpty()) {
                JsonSerializer<Object> ret = serializerProvider.findValueSerializer(ImageItem.class, null);
                for (ImageItem image : images)
                    ret.serialize(image, jgen, serializerProvider);
            }
            jgen.writeEndArray();

            if (level.equals(Level.DETAILED)) {
                jgen.writeNumberField(TravelNote.fnRating, getValue(travelNote.rating));

                if (travelNote.upperCost == null)
                    jgen.writeNullField(TravelNote.fnUpperCost);
                else
                    jgen.writeNumberField(TravelNote.fnUpperCost, getValue(travelNote.upperCost));
                if (travelNote.lowerCost == null)
                    jgen.writeNullField(TravelNote.fnLowerCost);
                else
                    jgen.writeNumberField(TravelNote.fnLowerCost, getValue(travelNote.lowerCost));

                jgen.writeNumberField(TravelNote.fnCommentCnt, getValue(travelNote.commentCnt));
                jgen.writeNumberField(TravelNote.fnViewCnt, getValue(travelNote.viewCnt));
                jgen.writeNumberField(TravelNote.fnFavorCnt, getValue(travelNote.favorCnt));

                // contents
                jgen.writeFieldName(TravelNote.fnNoteContents);
                List<Map<String, String>> contents = travelNote.contents;
                jgen.writeStartArray();
                if (contents != null && !contents.isEmpty()) {
                    for (Map<String, String> cn : contents) {
                        jgen.writeStartObject();
                        for (Map.Entry<String, String> entry : cn.entrySet()) {
                            jgen.writeStringField(entry.getKey(), getString(entry.getValue()));
                        }
                        jgen.writeEndObject();
                    }
                }
                jgen.writeEndArray();
            } else {
                // Travel detailed info
                jgen.writeStringField("detailUrl", "http://h5.taozilvxing.com/dayDetail.php?id=" + getString(travelNote.getId().toString()));
            }

            jgen.writeEndObject();
        }

    }

}
