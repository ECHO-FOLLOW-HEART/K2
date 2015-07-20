package formatter.taozi.user;

import formatter.AizouFormatter;
import formatter.taozi.geo.LocalitySerializer;
import models.AizouBaseEntity;
import models.geo.Locality;
import models.user.UserInfo;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * 序列化用户信息
 * <p>
 * Created by zephyre on 1/20/15.
 */
public class UserInfoFormatter extends AizouFormatter<UserInfo> {

    private UserInfoSerializer serializer;

    public UserInfoFormatter() {
        serializer = new UserInfoSerializer();
        registerSerializer(UserInfo.class, serializer);
        registerSerializer(Locality.class, new LocalitySerializer(LocalitySerializer.Level.FORTRACKS));
//        registerSerializer(TravelNote.class, new TravelNoteSerializer());
//        registerSerializer(ImageItem.class, new ImageItemSerializer(640));

        initObjectMapper(null);

        filteredFields.addAll(Arrays.asList(AizouBaseEntity.FD_ID, UserInfo.fnEasemobUser, UserInfo.fnUserId, UserInfo.fnNickName,
                UserInfo.fnAvatar, UserInfo.fnAvatarSmall, UserInfo.fnGender, UserInfo.fnSignature, UserInfo.fnTel,
                UserInfo.fnDialCode, UserInfo.fnRoles, UserInfo.fnTravelStatus, UserInfo.fnTracks, UserInfo.fnTravelNotes, UserInfo.fnResidence,
                UserInfo.fnMemo, UserInfo.fnBirthday, UserInfo.fnZodiac,UserInfo.fnLevel));

        sideFieldList.addAll(Arrays.asList(AizouBaseEntity.FD_ID, UserInfo.fnEasemobUser, UserInfo.fnUserId, UserInfo.fnNickName,
                UserInfo.fnAvatar, UserInfo.fnAvatarSmall, UserInfo.fnGender, UserInfo.fnSignature, UserInfo.fnRoles, UserInfo.fnTravelStatus,
                UserInfo.fnTracks, UserInfo.fnTravelNotes, UserInfo.fnResidence, UserInfo.fnMemo, UserInfo.fnBirthday, UserInfo.fnZodiac,UserInfo.fnLevel));
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


//    class TravelNoteSerializer extends AizouSerializer<TravelNote> {
//        @Override
//        public void serialize(TravelNote travelNote, JsonGenerator jgen, SerializerProvider serializerProvider)
//                throws IOException {
//            jgen.writeStartObject();
//
//            writeObjectId(travelNote, jgen, serializerProvider);
////            jgen.writeStringField(TravelNote.fnAuthorAvatar, getString(travelNote.authorAvatar));
////            jgen.writeStringField(TravelNote.fnAuthorName, getString(travelNote.authorName));
//            jgen.writeStringField(TravelNote.fnTitle, getString(travelNote.title));
//            jgen.writeStringField(TravelNote.fnSummary, getString(travelNote.summary));
//
//
//            // publishTime
////            if (travelNote.publishTime == null)
////                jgen.writeNullField(TravelNote.fnPublishTime);
////            else
////                jgen.writeObjectField(TravelNote.fnPublishTime, travelNote.publishTime == null ? null : getValue(travelNote.publishTime));
//
//
//            // Images
//            jgen.writeFieldName("images");
//            List<ImageItem> images = travelNote.images;
//            jgen.writeStartArray();
//            if (images != null && !images.isEmpty()) {
//                JsonSerializer<Object> ret = serializerProvider.findValueSerializer(ImageItem.class, null);
//                for (ImageItem image : images)
//                    ret.serialize(image, jgen, serializerProvider);
//            }
//            jgen.writeEndArray();
//
//            // Travel detailed info
//            jgen.writeStringField("detailUrl", "http://h5.taozilvxing.com/dayDetail.php?id=" + getString(travelNote.getId().toString()));
//
//            jgen.writeEndObject();
//        }
//
//    }

}
