package formatter.taozi.group;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.PropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import formatter.AizouFormatter;
import formatter.AizouSerializer;
import formatter.taozi.ImageItemSerializer;
import formatter.taozi.ObjectIdSerializer;
import models.geo.GeoJsonPoint;
import models.group.ChatGroup;
import models.guide.AbstractGuide;
import models.guide.Guide;
import models.misc.ImageItem;
import org.bson.types.ObjectId;

import java.io.IOException;
import java.util.*;

/**
 * 返回攻略的摘要
 * <p>
 * Created by topy on 2014/11/07.
 */
public class ChatGroupFormatter extends AizouFormatter<ChatGroup> {

    public ChatGroupFormatter() {
        registerSerializer(ChatGroup.class, new ChatGroupSerializer());
        initObjectMapper();
        filteredFields = new HashSet<>();
        Collections.addAll(filteredFields,
                ChatGroup.FD_GROUPID, ChatGroup.FD_NAME,
                ChatGroup.FD_AVATAR, ChatGroup.FD_DESC, ChatGroup.FD_MAXUSERS

        );
    }

    private class ChatGroupSerializer extends AizouSerializer<ChatGroup> {
        @Override
        public void serialize(ChatGroup chatGroup, JsonGenerator jgen, SerializerProvider serializerProvider)
                throws IOException {
            jgen.writeStartObject();
            jgen.writeObjectField(ChatGroup.FD_GROUPID, getValue(chatGroup.getGroupId()));
            jgen.writeStringField(ChatGroup.FD_NAME, getString(chatGroup.getName()));
            jgen.writeStringField(ChatGroup.FD_AVATAR, getString(chatGroup.getAvatar()));
            jgen.writeStringField(ChatGroup.FD_DESC, getString(chatGroup.getDesc()));
            jgen.writeObjectField(ChatGroup.FD_MAXUSERS, getValue(chatGroup.getMaxUsers()));
            jgen.writeEndObject();
        }
    }

    protected ObjectMapper initObjectMapper() {
        mapper = new ObjectMapper();
        mapper.registerModule(module);
        return mapper;
    }
}