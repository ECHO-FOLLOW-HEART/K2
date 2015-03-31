package formatter.taozi.user;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import formatter.AizouSerializer;
import models.AizouBaseEntity;
import models.geo.Locality;
import models.guide.Guide;
import models.misc.TravelNote;
import models.user.UserInfo;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Transient;
import utils.TaoziDataFilter;

import java.io.IOException;
import java.util.List;

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
            throws IOException {
        jgen.writeStartObject();

        writeObjectId(userInfo, jgen, serializerProvider);

        jgen.writeStringField("easemobUser", getString(userInfo.getEasemobUser()));
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
        jgen.writeStringField(UserInfo.fnTravelStatus, userInfo.getTravelStatus());
        jgen.writeStringField(UserInfo.fnResidence, userInfo.getResidence());
        jgen.writeStringField(UserInfo.fnBirthday, getDate(userInfo.getBirthday()));
        jgen.writeStringField(UserInfo.fnZodiac, TaoziDataFilter.getZodiac(userInfo.getZodiac()));

        // List<Locality>
        jgen.writeFieldName(UserInfo.fnTracks);
        jgen.writeStartArray();
        List<Locality> localities = userInfo.getTracks();
        if (localities != null && !localities.isEmpty()) {
            JsonSerializer<Object> retLocality = serializerProvider.findValueSerializer(Locality.class, null);
            for (Locality locality : localities) {
                retLocality.serialize(locality, jgen, serializerProvider);
            }
        }
        jgen.writeEndArray();

        // List<TravelNote>
//        jgen.writeFieldName(UserInfo.fnTravelNotes);
//        jgen.writeStartArray();
//        List<TravelNote> travelNotes = userInfo.getTravelNotes();
//        if (travelNotes != null && !travelNotes.isEmpty()) {
//            JsonSerializer<Object> retTn = serializerProvider.findValueSerializer(TravelNote.class, null);
//            for (TravelNote travelNote : travelNotes) {
//                retTn.serialize(travelNote, jgen, serializerProvider);
//            }
//        }
//        jgen.writeEndArray();

        if (selfView) {
            jgen.writeStringField("tel", getString(userInfo.getTel()));
            jgen.writeObjectField("dialCode", userInfo.getDialCode());
        } else
            jgen.writeStringField("memo", getString(userInfo.getMemo()));

        jgen.writeEndObject();
    }
}
