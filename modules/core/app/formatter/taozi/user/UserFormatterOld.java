package formatter.taozi.user;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ser.PropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import formatter.taozi.TaoziBaseFormatter;
import models.AizouBaseEntity;
import models.user.UserInfo;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * 返回用户的简要信息（即：查看别人的用户信息时使用）
 * <p/>
 * Created by zephyre on 10/28/14.
 */
public class UserFormatterOld extends TaoziBaseFormatter {
    private boolean selfFormatter;

    public UserFormatterOld(boolean self) {
        selfFormatter = self;

        stringFields.addAll(Arrays.asList(UserInfo.fnNickName, UserInfo.fnAvatar,UserInfo.fnAvatarSmall, UserInfo.fnGender,
                UserInfo.fnSignature, UserInfo.fnEasemobUser));

        if (self)
            stringFields.add(UserInfo.fnTel);
        else
            stringFields.add(UserInfo.fnMemo);

        filteredFields.addAll(stringFields);
        filteredFields.addAll(Arrays.asList(AizouBaseEntity.FD_ID, UserInfo.fnUserId));
        if (self)
            filteredFields.add(UserInfo.fnDialCode);
        else
            filteredFields.remove(UserInfo.fnMemo);
    }

    @Override
    public JsonNode format(AizouBaseEntity item) {
        item.fillNullMembers(filteredFields);

        Map<String, PropertyFilter> filterMap = new HashMap<>();
        filterMap.put("userInfoFilter", SimpleBeanPropertyFilter.filterOutAllExcept(filteredFields));
        ObjectMapper mapper = getObjectMapper(filterMap, null);

        ObjectNode results = mapper.valueToTree(item);
        if (!selfFormatter){
            JsonNode memoNode = results.get("memo");
            if (memoNode==null || memoNode.isNull())
                results.put("memo", "");
        }

        return results;
    }
}
