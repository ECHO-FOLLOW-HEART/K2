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
                imgList.add(img);
            }

            Collections.sort(imgList, new Comparator<ImageItem>() {
                @Override
                public int compare(ImageItem o1, ImageItem o2) {
                    if (o1.getSize() != null && o2.getSize() != null)
                        return o2.getSize() - o1.getSize();
                    else if (o1.getW() != null && o2.getW() != null)
                        return o2.getW() - o1.getW();
                    else if (o1.getH() != null && o2.getH() != null)
                        return o2.getH() - o1.getH();
                    else
                        return 0;
                }
            });

            List<String> ret = new ArrayList<>();
            for (ImageItem img : imgList) {
                if (img.getUrl() != null)
                    ret.add(img.getUrl());
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
