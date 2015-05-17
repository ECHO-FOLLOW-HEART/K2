package formatter.taozi.misc;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import formatter.AizouFormatter;
import formatter.AizouSerializer;
import formatter.taozi.ImageItemSerializer;
import models.misc.Album;
import models.misc.ImageItem;
import models.misc.TravelNote;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created by lxf on 14-11-1.
 */
public class AlbumFormatter extends AizouFormatter<Album> {

    public AlbumFormatter() {
        this(640);
    }

    public AlbumFormatter(Integer imgWidth) {
        imageWidth = imgWidth;
        registerSerializer(ImageItem.class, new ImageItemSerializer(imgWidth));
        registerSerializer(Album.class, new AlbumSerializer());

        initObjectMapper(null);

    }

    class AlbumSerializer extends AizouSerializer<Album> {
        @Override
        public void serialize(Album album, JsonGenerator jgen, SerializerProvider serializerProvider)
                throws IOException {
            jgen.writeStartObject();

            writeObjectId(album, jgen, serializerProvider);

            // Image
            jgen.writeFieldName(Album.FD_IMAGE);
            List<ImageItem> images = Arrays.asList(album.getImage());
            jgen.writeStartArray();
            if (images != null && !images.isEmpty()) {
                JsonSerializer<Object> ret = serializerProvider.findValueSerializer(ImageItem.class, null);
                for (ImageItem image : images)
                    ret.serialize(image, jgen, serializerProvider);
            }
            jgen.writeEndArray();

//            if (images != null && !images.isEmpty())
//                jgen.writeStringField("imageUrl", getString(images.get(0).getFullUrl()));
//            else
//                jgen.writeNullField("imageUrl");
            //jgen.writeNumberField(Album.FD_USERID, getValue(album.getUserId()));
            jgen.writeNumberField(Album.FD_CTIME, getValue(album.getcTime()));

            jgen.writeEndObject();
        }

    }

}
