package formatter.taozi.recom;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import formatter.AizouFormatter;
import formatter.AizouSerializer;
import formatter.taozi.ImageItemSerializer;
import formatter.taozi.TaoziBaseFormatter;
import models.AizouBaseEntity;
import models.misc.ImageItem;
import models.misc.Recom;
import models.misc.TravelNote;

import java.io.IOException;
import java.util.Arrays;

/**
 * 返回用户的摘要（以列表形式获取用户信息时使用，比如获得好友列表，获得黑名单列表等）
 * <p>
 * Created by zephyre on 10/28/14.
 */
public class RecomFormatter extends AizouFormatter<Recom> {

    public RecomFormatter() {
        registerSerializer(Recom.class, new RecomSerializer());

        initObjectMapper(null);
        filteredFields.addAll(Arrays.asList(Recom.fnItemId,
                Recom.fnTitle,
                Recom.fnItemType,
                Recom.fnLinkType,
                Recom.fnLinkUrl,
                Recom.fnDesc,
                Recom.fnCover));
    }

    class RecomSerializer extends AizouSerializer<Recom> {
        @Override
        public void serialize(Recom recom, JsonGenerator jgen, SerializerProvider serializerProvider)
                throws IOException {
            jgen.writeStartObject();
            jgen.writeStringField(Recom.fnItemId, getString(recom.itemId));
            jgen.writeStringField(Recom.fnTitle, getString(recom.title));
            jgen.writeStringField(Recom.fnItemType, getString(recom.itemType));
            jgen.writeStringField(Recom.fnLinkType, getString(recom.linkType));
            jgen.writeStringField(Recom.fnLinkUrl, getString(recom.linkUrl));
            jgen.writeStringField(Recom.fnDesc, getString(recom.desc));
            jgen.writeStringField(Recom.fnCover, getString(recom.cover));
            jgen.writeEndObject();
        }
    }
}
