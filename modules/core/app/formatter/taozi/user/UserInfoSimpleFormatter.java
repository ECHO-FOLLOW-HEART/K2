package formatter.taozi.user;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import formatter.AizouFormatter;
import formatter.AizouSerializer;
import formatter.taozi.geo.LocalitySerializer;
import models.AizouBaseEntity;
import models.geo.Locality;
import models.user.UserInfo;
import utils.TaoziDataFilter;

import java.io.IOException;
import java.util.*;

/**
 * 序列化用户信息
 * <p>
 * Created by zephyre on 1/20/15.
 */
public class UserInfoSimpleFormatter extends AizouFormatter<UserInfo> {

    private UserInfoSimpleSerializer serializer;

    public UserInfoSimpleFormatter() {
        serializer = new UserInfoSimpleSerializer();
        registerSerializer(UserInfo.class, serializer);

        initObjectMapper();

        filteredFields.addAll(Arrays.asList(AizouBaseEntity.FD_ID, UserInfo.fnUserId, UserInfo.fnNickName,
                UserInfo.fnAvatar, UserInfo.fnAvatarSmall, UserInfo.fnGender, UserInfo.fnSignature, UserInfo.fnRoles, UserInfo.fnLevel));

    }

    protected ObjectMapper initObjectMapper() {
        mapper = new ObjectMapper();
        mapper.registerModule(module);
        return mapper;
    }

    @Override
    public Set<String> getFilteredFields() {
        return filteredFields;

    }

    class UserInfoSimpleSerializer extends AizouSerializer<UserInfo> {

        @Override
        public void serialize(UserInfo userInfo, JsonGenerator jgen, SerializerProvider serializerProvider)
                throws IOException {
            jgen.writeStartObject();

            jgen.writeStringField("nickName", getString(userInfo.getNickName()));
            jgen.writeStringField("avatar", getString(userInfo.getAvatar()));
            jgen.writeStringField("avatarSmall", getString(userInfo.getAvatarSmall()));
            jgen.writeStringField("gender", getString(userInfo.getGender()));
            jgen.writeStringField("signature", getString(userInfo.getSignature()));
            jgen.writeNumberField("userId", userInfo.getUserId());

            // roles
            jgen.writeFieldName(UserInfo.fnRoles);
            jgen.writeStartArray();
            List<String> roles = userInfo.getRoles();
            if (roles != null && !roles.isEmpty()) {
                JsonSerializer<Object> retLocality = serializerProvider.findValueSerializer(String.class, null);
                for (String role : roles) {
                    retLocality.serialize(role, jgen, serializerProvider);
                }
            }
            jgen.writeEndArray();

            jgen.writeNumberField(UserInfo.fnLevel, userInfo.getLevel());

            jgen.writeEndObject();
        }


    }

}
