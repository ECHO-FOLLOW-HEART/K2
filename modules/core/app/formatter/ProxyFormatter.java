package formatter;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.PropertyFilter;
import com.fasterxml.jackson.databind.ser.PropertyWriter;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import models.AizouBaseEntity;
import models.misc.Proxy;

import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.Set;

/**
 * 获得代理服务器信息
 * <p/>
 * Created by zephyre on 10/29/14.
 */
public class ProxyFormatter implements JsonFormatter {
    public String format(Object item) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();

        SimpleModule module = new SimpleModule();
        module.addSerializer(Proxy.class, new ProxySerializer());
        mapper.registerModule(module);

        return mapper.writeValueAsString(item);
    }


    @Override
    public JsonNode format(AizouBaseEntity item) {
        return null;
    }
}
