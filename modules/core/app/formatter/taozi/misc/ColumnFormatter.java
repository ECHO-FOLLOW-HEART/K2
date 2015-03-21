package formatter.taozi.misc;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.PropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import formatter.AizouFormatter;
import formatter.AizouSerializer;
import formatter.taozi.ImageItemSerializerOld;
import formatter.taozi.TaoziBaseFormatter;
import models.AizouBaseEntity;
import models.misc.Column;
import models.misc.ImageItem;
import models.misc.Recom;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * Created by zephyre on 12/11/14.
 */
public class ColumnFormatter extends AizouFormatter<Column> {

    public ColumnFormatter() {
        registerSerializer(Column.class, new ColumnSerializer());
        initObjectMapper(null);

        filteredFields = new HashSet<>();
        Collections.addAll(filteredFields,
                Column.FD_COVER,
                Column.FD_LINK,
                Column.FD_TITLE,
                Column.FD_TYPE,
                Column.FD_CONTENT,
                Column.FD_ID
        );
    }

    class ColumnSerializer extends AizouSerializer<Column> {
        @Override
        public void serialize(Column column, JsonGenerator jgen, SerializerProvider serializerProvider)
                throws IOException {
            jgen.writeStartObject();
            writeObjectId(column, jgen, serializerProvider);
            jgen.writeStringField(Column.FD_COVER, getString(column.getCover()));
            jgen.writeStringField(Column.FD_LINK, getString(column.getLink()));
            jgen.writeStringField(Column.FD_TITLE, getString(column.getTitle()));
            jgen.writeStringField(Column.FD_TYPE, getString(column.getType()));
            // IOS不支持tp=webp格式的页面
            String content = getString(column.getContent());
            content = content.replaceAll("\\?tp=webp&","\\?");
            jgen.writeStringField(Column.FD_CONTENT, content);
            jgen.writeEndObject();
        }
    }
}
