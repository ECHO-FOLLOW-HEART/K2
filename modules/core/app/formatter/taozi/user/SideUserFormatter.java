package formatter.taozi.user;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.PropertyFilter;
import com.fasterxml.jackson.databind.ser.PropertyWriter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import formatter.AizouBeanPropertyFilter;
import formatter.taozi.TaoziBaseFormatter;
import models.TravelPiBaseItem;
import models.user.UserInfo;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * 返回用户的简要信息（即：查看别人的用户信息时使用）
 * <p/>
 * Created by zephyre on 10/28/14.
 */
public class SideUserFormatter extends TaoziBaseFormatter {
    @Override
    public JsonNode format(TravelPiBaseItem item) {
        ObjectMapper mapper = getObjectMapper();

        PropertyFilter theFilter = new AizouBeanPropertyFilter() {
            @Override
            protected boolean includeImpl(PropertyWriter writer) {
                Set<String> includedFields = new HashSet<>();
                Collections.addAll(includedFields, "id", UserInfo.fnNickName, UserInfo.fnAvatar, UserInfo.fnUserId,
                        UserInfo.fnGender, UserInfo.fnSignature, UserInfo.fnMemo);

                return (includedFields.contains(writer.getName()));
            }
        };

        FilterProvider filters = new SimpleFilterProvider().addFilter("userInfoFilter", theFilter);
        mapper.setFilters(filters);


        return mapper.valueToTree(item);
    }
}
