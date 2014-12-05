package formatter.taozi.user;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import formatter.taozi.TaoziBaseFormatter;
import models.TravelPiBaseItem;
import models.user.UserInfo;

import java.util.*;

/**
 * 返回用户的简要信息（即：查看别人的用户信息时使用）
 * <p/>
 * Created by zephyre on 10/28/14.
 */
public class SideUserFormatter extends TaoziBaseFormatter {
    public static final List<String> retrievedFields;

    private static Set<String> filterSet;

    static {
        retrievedFields = Arrays.asList(TravelPiBaseItem.FD_ID, UserInfo.fnNickName,
                UserInfo.fnAvatar, UserInfo.fnUserId, UserInfo.fnGender, UserInfo.fnSignature);

        filterSet = new HashSet<>();
        Collections.addAll(filterSet, retrievedFields.toArray(new String[retrievedFields.size()]));
        filterSet.add(UserInfo.fnEasemobUser);
        filterSet.add(UserInfo.fnMemo);
    }

    @Override
    public JsonNode format(TravelPiBaseItem item) {
        ObjectMapper mapper = getObjectMapper();

        FilterProvider filters = new SimpleFilterProvider().addFilter("userInfoFilter",
                SimpleBeanPropertyFilter.filterOutAllExcept(filterSet));
        mapper.setFilters(filters);

        return mapper.valueToTree(item);
    }
}
