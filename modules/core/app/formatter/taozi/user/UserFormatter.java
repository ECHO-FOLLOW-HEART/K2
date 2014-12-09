package formatter.taozi.user;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ser.PropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import formatter.taozi.TaoziBaseFormatter;
import models.TravelPiBaseItem;
import models.user.UserInfo;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * 返回用户的简要信息（即：查看别人的用户信息时使用）
 * <p/>
 * Created by zephyre on 10/28/14.
 */
public class UserFormatter extends TaoziBaseFormatter {
    public UserFormatter(boolean self) {
        stringFields.addAll(Arrays.asList(UserInfo.fnNickName, UserInfo.fnAvatar, UserInfo.fnGender,
                UserInfo.fnSignature, UserInfo.fnEasemobUser));

        if (self)
            stringFields.add(UserInfo.fnTel);
        else
            stringFields.add(UserInfo.fnMemo);

        filteredFields.addAll(stringFields);
        filteredFields.addAll(Arrays.asList(TravelPiBaseItem.FD_ID, UserInfo.fnUserId));
        if (self)
            filteredFields.add(UserInfo.fnDialCode);
        else
            filteredFields.remove(UserInfo.fnMemo);
    }

    @Override
    public JsonNode format(TravelPiBaseItem item) {
        Map<String, PropertyFilter> filterMap = new HashMap<>();
        filterMap.put("userInfoFilter", SimpleBeanPropertyFilter.filterOutAllExcept(filteredFields));
        ObjectMapper mapper = getObjectMapper(filterMap, null);

        return postProcess((ObjectNode) mapper.valueToTree(item));
    }
}
