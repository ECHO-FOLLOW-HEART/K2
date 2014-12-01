package utils.formatter.travelpi.geo;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.PropertyFilter;
import com.fasterxml.jackson.databind.ser.PropertyWriter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import models.TravelPiBaseItem;
import models.geo.Locality;
import org.bson.types.ObjectId;
import utils.formatter.AizouBeanPropertyFilter;
import utils.formatter.travelpi.TravelPiBaseFormatter;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Zephyre
 */
public class SimpleLocalityFormatter extends TravelPiBaseFormatter {

    private static SimpleLocalityFormatter instance;

    private SimpleLocalityFormatter() {
        stringFields = new HashSet<>();
        stringFields.addAll(Arrays.asList(Locality.fnEnName, Locality.fnZhName));
    }

    public synchronized static SimpleLocalityFormatter getInstance() {
        if (instance != null)
            return instance;
        else {
            instance = new SimpleLocalityFormatter();
            return instance;
        }
    }

    @Override
    public JsonNode format(TravelPiBaseItem item) {
        return format(item, true);
    }

    private JsonNode format(TravelPiBaseItem item, boolean includeParents) {
        ObjectMapper mapper = new ObjectMapper();

        Locality locItem = (Locality) item;

        mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        mapper.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);

        PropertyFilter theFilter = new AizouBeanPropertyFilter() {
            @Override
            protected boolean includeImpl(PropertyWriter writer) {
                Set<String> includedFields = new HashSet<>();
                includedFields.add(Locality.fnEnName);
                includedFields.add(Locality.fnZhName);
                includedFields.add("id");

                return (includedFields.contains(writer.getName()));
            }
        };

        FilterProvider filters = new SimpleFilterProvider().addFilter("localityFilter", theFilter);
        mapper.setFilters(filters);

        ObjectNode result = postProcess((ObjectNode) mapper.valueToTree(item));
        result.put("_id", result.get("id").asText());
        result.put("name", result.get("zhName").asText());
        result.put("fullName", result.get("zhName").asText());

        // 加入父行政区信息
        if (includeParents) {
            Locality parent = locItem.getSuperAdm();
            if (parent == null) {
                parent = new Locality();
                parent.setZhName("");
                parent.setEnName("");
                parent.setId(new ObjectId());
            }
            ObjectNode parentNode = (ObjectNode) format(parent, false);
            result.put("parent", parentNode);
        }

        return result;
    }
}
