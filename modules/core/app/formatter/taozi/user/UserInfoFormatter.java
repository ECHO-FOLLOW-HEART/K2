package formatter.taozi.user;

import formatter.AizouFormatter;
import models.AizouBaseEntity;
import models.user.UserInfo;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * 序列化用户信息
 * <p/>
 * Created by zephyre on 1/20/15.
 */
public class UserInfoFormatter extends AizouFormatter<UserInfo> {

    private UserInfoSerializer serializer;

    public UserInfoFormatter() {
        serializer = new UserInfoSerializer();
        registerSerializer(UserInfo.class, serializer);

        initObjectMapper(null);

        filteredFields.addAll(Arrays.asList(AizouBaseEntity.FD_ID, UserInfo.fnEasemobUser, UserInfo.fnUserId, UserInfo.fnNickName,
                UserInfo.fnAvatar,UserInfo.fnAvatarSmall, UserInfo.fnGender, UserInfo.fnSignature, UserInfo.fnTel,
                UserInfo.fnDialCode));

        sideFieldList.addAll(Arrays.asList(AizouBaseEntity.FD_ID, UserInfo.fnEasemobUser, UserInfo.fnUserId, UserInfo.fnNickName,
                UserInfo.fnAvatar, UserInfo.fnAvatarSmall,UserInfo.fnGender, UserInfo.fnSignature));
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
