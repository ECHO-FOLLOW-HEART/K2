package formatter.taozi.user;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ser.PropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import formatter.taozi.ImageItemSerializer;
import formatter.taozi.TaoziBaseFormatter;
import models.AizouBaseEntity;
import models.geo.Locality;
import models.misc.ImageItem;
import models.user.Favorite;

import java.util.*;

/**
 * 返回用户的收藏信息（即：查看自己的收藏信息时使用）
 * <p>
 * Created by topy on 10/28/14.
 */
public class SelfFavoriteFormatter extends TaoziBaseFormatter {

    public SelfFavoriteFormatter(String type) {
        filteredFields = new HashSet<>();
        Collections.addAll(filteredFields,
                Favorite.fnZhName,
                Favorite.fnEnName,
                Favorite.fnItemId,
                Favorite.fnImage,
                Favorite.fnType,
                Favorite.fnUserId,
                Favorite.fnCreateTime,
                Favorite.fnId,
                Favorite.fnDesc,
                Favorite.fnLocality
        );

        if (type.equals("vs") || type.equals("locality")) {
            Collections.addAll(filteredFields, Favorite.fnTimeCostDesc);
        } else if (type.equals("restaurant") || type.equals("hotel")) {
            Collections.addAll(filteredFields, Favorite.fnTimeCostDesc, Favorite.fnRating,
                    Favorite.fnAddress,Favorite.fnPriceDesc,Favorite.fnTelephone);
        } else if (type.equals("shopping")) {
            Collections.addAll(filteredFields, Favorite.fnRating);
        }

    }

    @Override
    public JsonNode format(AizouBaseEntity item) {

        //过滤 Map
        item.fillNullMembers(filteredFields);
        Map<String, PropertyFilter> filterMap = new HashMap<>();
        filterMap.put("favoriteFilter", SimpleBeanPropertyFilter.filterOutAllExcept(filteredFields));
        filterMap.put("localityFilter", SimpleBeanPropertyFilter.filterOutAllExcept(
                AizouBaseEntity.FD_ID, Locality.FD_ZH_NAME, Locality.FD_EN_NAME));
        ObjectMapper mapper = getObjectMapper(filterMap, null);

        // 注册 SimpleModule
        SimpleModule imageItemModule = new SimpleModule();
        imageItemModule.addSerializer(ImageItem.class,
                new ImageItemSerializer(ImageItemSerializer.ImageSizeDesc.MEDIUM));
        mapper.registerModule(imageItemModule);


        ObjectNode result = mapper.valueToTree(item);

        stringFields.addAll(Arrays.asList(Favorite.fnZhName, Favorite.fnEnName, Favorite.fnDesc));

        listFields.add(Favorite.fnImage);

        mapFields.add(Favorite.fnLocality);

        return postProcess(result);
    }
}

