package formatter;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.PropertyWriter;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;

/**
 * Jackson序列化的辅助类
 * <p/>
 * Created by zephyre on 11/24/14.
 */
public abstract class AizouBeanPropertyFilter extends SimpleBeanPropertyFilter {
    protected abstract boolean includeImpl(PropertyWriter beanPropertyWriter);

    @Override
    protected boolean include(BeanPropertyWriter beanPropertyWriter) {
        return includeImpl(beanPropertyWriter);
    }

    @Override
    protected boolean include(PropertyWriter writer) {
        return includeImpl(writer);
    }

    @Override
    public void serializeAsField
            (Object pojo, JsonGenerator jgen, SerializerProvider provider, PropertyWriter writer) throws Exception {
        if (include(writer)) {
            writer.serializeAsField(pojo, jgen, provider);
        } else if (!jgen.canOmitFields()) { // since 2.3
            writer.serializeAsOmittedField(pojo, jgen, provider);
        }
    }
}
