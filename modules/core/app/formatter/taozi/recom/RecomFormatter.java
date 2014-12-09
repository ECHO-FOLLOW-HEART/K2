package formatter.taozi.recom;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import formatter.taozi.ImageItemSerializer;
import formatter.taozi.TaoziBaseFormatter;
import models.TravelPiBaseItem;
import models.misc.ImageItem;
import models.misc.Recom;
import models.poi.AbstractPOI;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * 返回用户的摘要（以列表形式获取用户信息时使用，比如获得好友列表，获得黑名单列表等）
 * <p/>
 * Created by zephyre on 10/28/14.
 */
public class RecomFormatter extends TaoziBaseFormatter {

    @Override
    public JsonNode format(TravelPiBaseItem item) {
        ObjectMapper mapper = getObjectMapper();

        ((SimpleFilterProvider) mapper.getSerializationConfig().getFilterProvider())
                .addFilter("recomFilter",
                        SimpleBeanPropertyFilter.filterOutAllExcept(
                                Recom.fnId,
                                Recom.fnZhName,
                                Recom.fnEnName,
                                Recom.fnLinkType,
                                Recom.fnLinkUrl,
                                Recom.fnDesc,
                                Recom.fnCover
                        ));
        ObjectNode result = mapper.valueToTree(item);
        stringFields.addAll(Arrays.asList(Recom.fnId));
        return postProcess(result);
    }
}
