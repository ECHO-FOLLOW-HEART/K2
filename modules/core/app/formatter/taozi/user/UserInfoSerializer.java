package formatter.taozi.user;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import formatter.AizouSerializer;
import models.geo.Locality;
import models.user.UserInfo;
import utils.TaoziDataFilter;

import java.io.IOException;
import java.util.List;
import java.util.Map;

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
        jgen.writeStringField(UserInfo.fnTravelStatus, getString(userInfo.getTravelStatus()));
        jgen.writeStringField(UserInfo.fnResidence, getString(userInfo.getResidence()));
        jgen.writeStringField(UserInfo.fnBirthday, getDate(userInfo.getBirthday()));
        jgen.writeStringField(UserInfo.fnZodiac, TaoziDataFilter.getZodiac(userInfo.getZodiac()));

        // List<Locality>
//        jgen.writeFieldName(UserInfo.fnTracks);
//        jgen.writeStartObject();
//        List<Locality> localities = userInfo.getTracks();
//        List<Locality> localitiesValue;
//        if (localities != null && !localities.isEmpty()) {
//            Map<String, List<Locality>> map = TaoziDataFilter.transLocalitiesByCountry(localities);
//            for (Map.Entry<String, List<Locality>> entry : map.entrySet()) {
//
//                jgen.writeFieldName(entry.getKey());
//                jgen.writeStartArray();
//                localitiesValue = entry.getValue();
//                if (localitiesValue != null && !localitiesValue.isEmpty()) {
//                    JsonSerializer<Object> retLocality = serializerProvider.findValueSerializer(Locality.class, null);
//                    for (Locality locality : localitiesValue) {
//                        retLocality.serialize(locality, jgen, serializerProvider);
//                    }
//                }
//                jgen.writeEndArray();
//            }
//        }
//        jgen.writeEndObject();

//        jgen.writeFieldName(UserInfo.fnTracks);
//        jgen.writeStartArray();
//        List<Locality> localities = userInfo.getTracks();
//        if (localities != null && !localities.isEmpty()) {
//            JsonSerializer<Object> retLocality = serializerProvider.findValueSerializer(Locality.class, null);
//            for (Locality locality : localities) {
//                retLocality.serialize(locality, jgen, serializerProvider);
//            }
//        }
//        jgen.writeEndArray();

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
