package formatter.travelpi;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ser.DefaultSerializerProvider;
import formatter.taozi.ObjectIdSerializer;
import org.bson.types.ObjectId;
import play.libs.Json;
import formatter.JsonFormatter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * 旅行派的formatter基类
 * <p/>
 * Created by zephyre on 11/24/14.
 */
public abstract class TravelPiBaseFormatter implements JsonFormatter {

    protected Set<String> stringFields = new HashSet<>();

    protected Set<String> listFields = new HashSet<>();

    protected Set<String> mapFields = new HashSet<>();

    protected ObjectMapper getObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        mapper.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);

        SimpleModule objectIdModule = new SimpleModule();
        objectIdModule.addSerializer(ObjectId.class, new ObjectIdSerializer());
        mapper.registerModule(objectIdModule);

        DefaultSerializerProvider.Impl sp = new DefaultSerializerProvider.Impl();
        sp.setNullValueSerializer(new JsonSerializer<Object>() {
            @Override
            public void serialize(Object o, JsonGenerator jsonGenerator, SerializerProvider serializerProvider)
                    throws IOException {
                jsonGenerator.writeString("");
            }
        });
        mapper.setSerializerProvider(sp);

        return mapper;
    }

    protected ObjectNode postProcess(ObjectNode result) {
        // 处理字符串字段
        if (stringFields != null) {
            for (String key : stringFields) {
                if (result.get(key) == null || result.get(key).isNull())
                    result.put(key, "");
            }
        }

        // 处理列表字段
        if (listFields != null) {
            for (String key : listFields) {
                if (result.get(key) == null || result.get(key).isNull())
                    result.put(key, Json.toJson(new ArrayList<>()));
            }
        }

        // 处理字典字段
        if (mapFields != null) {
            for (String key : mapFields) {
                if (result.get(key) == null || result.get(key).isNull())
                    result.put(key, Json.toJson(new HashMap<>()));
            }
        }

        // 处理图像
        JsonNode images = result.get("images");
        if (images!=null){
            result.remove("images");
            result.put("imageList", images);
        }

        // 处理id
        JsonNode oid = result.get("id");
        int time = oid.get("timestamp").asInt();
        int mach = oid.get("machine").asInt();
        int inc = oid.get("inc").asInt();

        String oidText = ObjectId.createFromLegacyFormat(time, mach, inc).toString();
        result.put("_id", oidText);
        result.put("id", oidText);

        return result;
    }
}
