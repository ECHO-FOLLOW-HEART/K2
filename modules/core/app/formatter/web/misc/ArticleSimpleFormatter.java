package formatter.web.misc;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.PropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import formatter.taozi.TaoziBaseFormatter;
import formatter.web.WebImageItemSerializer;
import models.AizouBaseEntity;
import models.misc.Article;
import models.misc.ImageItem;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * Created by topy on 2014/12/26.
 */
public class ArticleSimpleFormatter extends TaoziBaseFormatter {

    public ArticleSimpleFormatter() {

        filteredFields = new HashSet<>();
        Collections.addAll(filteredFields,
                Article.FD_ID,
                Article.FD_TITLE,
                Article.FD_DESC,
                Article.FD_SOURCE,
                Article.FD_AUTHORNAME,
                Article.FD_IMAGES,
                Article.FD_PUBLISHTIME
        );
    }

    @Override
    public JsonNode format(AizouBaseEntity item) {
        item.fillNullMembers(filteredFields);
        Map<String, PropertyFilter> filterMap = new HashMap<>();
        filterMap.put("articleFilter", SimpleBeanPropertyFilter.filterOutAllExcept(filteredFields));
        ObjectMapper mapper = getObjectMapper(filterMap, null);
        SimpleModule imageItemModule = new SimpleModule();
        imageItemModule.addSerializer(ImageItem.class,
                new WebImageItemSerializer());
        mapper.registerModule(imageItemModule);

        return mapper.valueToTree(item);
    }
}