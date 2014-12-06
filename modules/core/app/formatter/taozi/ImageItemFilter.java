package formatter.taozi;

import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.PropertyWriter;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import models.misc.ImageItem;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * ImageItem的filter。默认情况下，只输出url字段
 *
 * @author Zephyre
 */
public class ImageItemFilter extends SimpleBeanPropertyFilter {

    private Set<String> filterFields;

    public ImageItemFilter() {
        filterFields = new HashSet<>();
        Collections.addAll(filterFields, ImageItem.FD_URL);
    }

    public ImageItemFilter(Collection<String> filterFieldNames) {
        filterFields = new HashSet<>();
        Collections.addAll(filterFields, filterFieldNames.toArray(new String[filterFieldNames.size()]));
    }

    @Override
    protected boolean include(BeanPropertyWriter beanPropertyWriter) {
        return filterFields.contains(beanPropertyWriter.getName());
    }

    @Override
    protected boolean include(PropertyWriter writer) {
        return filterFields.contains(writer.getName());
    }
}
