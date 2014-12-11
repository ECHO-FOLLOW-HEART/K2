package formatter.taozi.poi;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ser.PropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import formatter.taozi.ImageItemSerializer;
import formatter.taozi.TaoziBaseFormatter;
import models.AizouBaseEntity;
import models.misc.ImageItem;
import models.poi.AbstractPOI;
import models.traffic.AbstractTrafficHub;

import java.util.*;

/**
 * 返回用户的摘要（以列表形式获取用户信息时使用，比如获得好友列表，获得黑名单列表等）
 * <p/>
 * Created by zephyre on 10/28/14.
 */

public class TrafficHubFormatter extends TaoziBaseFormatter {

    public TrafficHubFormatter(){
        filteredFields = new HashSet<>();
        Collections.addAll(filteredFields,
                AbstractTrafficHub.FD_ID,
                AbstractTrafficHub.FD_DESC,
                AbstractTrafficHub.FD_ZHNAME,
                AbstractTrafficHub.FD_ENNAME
        );
    }
    @Override
    public JsonNode format(AizouBaseEntity item) {
        item.fillNullMembers(filteredFields);

        Map<String, PropertyFilter> filterMap = new HashMap<>();
        filterMap.put("abstractTrafficHubFilter", SimpleBeanPropertyFilter.filterOutAllExcept(filteredFields));
        ObjectMapper mapper = getObjectMapper(filterMap, null);

        return mapper.valueToTree(item);
    }
}
