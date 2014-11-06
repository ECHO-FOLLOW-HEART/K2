package models.misc;

import com.fasterxml.jackson.databind.JsonNode;
import com.mongodb.BasicDBObjectBuilder;
import models.ITravelPiFormatter;
import models.TravelPiBaseItem;
import org.mongodb.morphia.annotations.Entity;
import play.libs.Json;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by topy on 2014/9/3.
 */
@Entity
public class Share extends TravelPiBaseItem implements ITravelPiFormatter {

    public List<ImageItem> images;

    public List<String> urls;

    @Override
    public JsonNode toJson() {

        BasicDBObjectBuilder builder = BasicDBObjectBuilder.start();


        if (images != null && !images.isEmpty()) {
            List<ImageItem> imgList = new ArrayList<>();
            for (ImageItem img : images) {
                if (img.enabled != null && !img.enabled)
                    continue;
                imgList.add(img);
            }

            Collections.sort(imgList, new Comparator<ImageItem>() {
                @Override
                public int compare(ImageItem o1, ImageItem o2) {
                    if (o1.fSize != null && o2.fSize != null)
                        return o2.fSize - o1.fSize;
                    else if (o1.w != null && o2.w != null)
                        return o2.w - o1.w;
                    else if (o1.h != null && o2.h != null)
                        return o2.h - o1.h;
                    else
                        return 0;
                }
            });

            List<String> ret = new ArrayList<>();
            for (ImageItem img : imgList) {
                if (img.url != null)
                    ret.add(img.url);
            }
            builder.add("imageList", ret);
        }

        List<String> urlList = new ArrayList<String>();
        for (String url : urls) {

            urlList.add(url);
        }
        builder.add("urls", urlList);

        return Json.toJson(builder.get());
    }
}
