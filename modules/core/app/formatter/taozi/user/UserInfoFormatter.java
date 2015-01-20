package formatter.taozi.user;

import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.PropertyFilter;
import formatter.AizouFormatter;
import models.AizouBaseEntity;
import models.user.UserInfo;

import java.util.*;

/**
 * 序列化用户信息
 * <p/>
 * Created by zephyre on 1/20/15.
 */
public class UserInfoFormatter extends AizouFormatter<UserInfo> {
    @Override
    protected ObjectMapper initObjectMapper(Map<String, PropertyFilter> filterMap,
                                            Map<Class<? extends UserInfo>, JsonSerializer<UserInfo>> serializerMap) {
        super.initObjectMapper(filterMap, serializerMap);

        serializer = (UserInfoSerializer) serializerMap.get(UserInfo.class);

        return mapper;
    }

    private UserInfoSerializer serializer;

    public UserInfoFormatter() {
        Map<Class<? extends UserInfo>, JsonSerializer<UserInfo>> serializerMap = new HashMap<>();
        serializerMap.put(UserInfo.class, new UserInfoSerializer());
        initObjectMapper(null, serializerMap);

        filteredFields.addAll(Arrays.asList(AizouBaseEntity.FD_ID, UserInfo.fnEasemobUser, UserInfo.fnUserId, UserInfo.fnNickName,
                UserInfo.fnAvatar, UserInfo.fnGender, UserInfo.fnSignature, UserInfo.fnTel,
                UserInfo.fnDialCode));

        sideFieldList.addAll(Arrays.asList(AizouBaseEntity.FD_ID, UserInfo.fnEasemobUser, UserInfo.fnUserId, UserInfo.fnNickName,
                UserInfo.fnAvatar, UserInfo.fnGender, UserInfo.fnSignature));
    }

    public void setSelfView(boolean selfView) {
        serializer.setSelfView(selfView);
    }

    @Override
    public Set<String> getFilteredFields() {
        if (serializer.isSelfView())
            return filteredFields;
        else
            return sideFieldList;
    }

    private Set<String> sideFieldList = new HashSet<>();
}
