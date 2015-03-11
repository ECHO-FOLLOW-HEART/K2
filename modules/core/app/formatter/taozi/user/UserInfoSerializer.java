package formatter.taozi.user;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.SerializerProvider;
import formatter.AizouSerializer;
import models.user.UserInfo;

import java.io.IOException;

public class UserInfoSerializer extends AizouSerializer<UserInfo> {

    private boolean selfView = false;

    public void setSelfView(boolean selfView) {
        this.selfView = selfView;
    }

    public boolean isSelfView() {
        return selfView;
    }

    @Override
    public void serialize(UserInfo userInfo, JsonGenerator jgen, SerializerProvider serializerProvider)
            throws IOException, JsonProcessingException {
        jgen.writeStartObject();

        writeObjectId(userInfo, jgen, serializerProvider);

        jgen.writeStringField("easemobUser", getString(userInfo.getEasemobUser()));
        jgen.writeStringField("nickName", getString(userInfo.getNickName()));
        jgen.writeStringField("avatar", getString(userInfo.getAvatar()));
        jgen.writeStringField("avatarSmall", getString(userInfo.getAvatarSmall()));
        jgen.writeStringField("gender", getString(userInfo.getGender()));
        jgen.writeStringField("signature", getString(userInfo.getSignature()));
        jgen.writeNumberField("userId", userInfo.getUserId());

        if (selfView) {
            jgen.writeStringField("tel", getString(userInfo.getTel()));
            jgen.writeObjectField("dialCode", userInfo.getDialCode());
        } else
            jgen.writeStringField("memo", getString(userInfo.getMemo()));

        jgen.writeEndObject();
    }
}
