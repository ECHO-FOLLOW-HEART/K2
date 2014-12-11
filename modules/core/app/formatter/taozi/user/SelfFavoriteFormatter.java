package formatter.taozi.user;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.PropertyFilter;
import com.fasterxml.jackson.databind.ser.PropertyWriter;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import formatter.taozi.ImageItemSerializer;
import formatter.taozi.TaoziBaseFormatter;
import models.AizouBaseEntity;
import models.misc.ImageItem;
import models.poi.AbstractPOI;
import models.traffic.AbstractTrafficHub;
import models.user.Favorite;
import formatter.JsonFormatter;

import java.util.*;

/**
 * 返回用户的详细信息（即：查看自己的用户信息时使用）
 * <p/>
 * Created by zephyre on 10/28/14.
 */
public class SelfFavoriteFormatter extends TaoziBaseFormatter {

    public SelfFavoriteFormatter(){
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
                Favorite.fnDesc
        );
    }
    @Override
    public JsonNode format(AizouBaseEntity item) {

        //过滤 Map
        item.fillNullMembers(filteredFields);
        Map<String, PropertyFilter> filterMap = new HashMap<>();
        filterMap.put("favoriteFilter", SimpleBeanPropertyFilter.filterOutAllExcept(filteredFields));
        ObjectMapper mapper = getObjectMapper(filterMap, null);

        // 注册 SimpleModule
        SimpleModule imageItemModule = new SimpleModule();
        imageItemModule.addSerializer(ImageItem.class,
                new ImageItemSerializer(ImageItemSerializer.ImageSizeDesc.MEDIUM));
        mapper.registerModule(imageItemModule);

        
        ObjectNode result = mapper.valueToTree(item);

        stringFields.addAll(Arrays.asList(Favorite.fnZhName,Favorite.fnEnName,Favorite.fnDesc));

        listFields.add(AbstractPOI.FD_IMAGES);

        mapFields.add(AbstractPOI.FD_LOCATION);

        return postProcess(result);
    }
}

