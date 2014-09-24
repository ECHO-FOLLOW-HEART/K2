package models.morphia.misc;

import com.fasterxml.jackson.databind.JsonNode;
import com.mongodb.BasicDBObjectBuilder;
import models.ITravelPiFormatter;
import models.TravelPiBaseItem;
import play.libs.Json;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by topy on 2014/9/13.
 */
public class Recommendation extends TravelPiBaseItem implements ITravelPiFormatter {

    /**
     * 名称
     */
    public String name;

    /**
     * 图片
     */
    public List<String> imageList;
    /**
     * 图片信息
     */
    public List<ImageItem> images;
    /**
     * 热门景点
     */
    public Integer hotVs;

    /**
     * 热门城市
     */
    public Integer hotCity;

    /**
     * 新鲜出炉
     */
    public Integer newItemWeight;

    /**
     * 不可不去
     */
    public Integer mustGoWeight;

    /**
     * 小编推荐
     */
    public Integer editorWeight;

    /**
     * 人气之旅
     */
    public Integer popularityWeight;

    /**
     * 路线编辑的昵称
     */
    public String editorNickName;

    /**
     * 路线编辑的头像
     */
    public String editorAvatar;

    /**
     * 理由
     */
    public String reason;

    @Override
    public JsonNode toJson() {
        BasicDBObjectBuilder builder = BasicDBObjectBuilder.start().add("name", name).add("id", id.toString()).add("reason", reason == null ? "" : reason)
                .add("editor", editorNickName == null ? "" : editorNickName).add("editorAvatar", editorAvatar == null ? "" : editorAvatar);

        // 如果存在更高阶的images字段，则使用之
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
            for (ImageItem img : imgList.subList(0, imgList.size() >= 5 ? 5 : imgList.size())) {
                if (img.url != null)
                    ret.add(img.url);
            }
            builder.add("imageList", ret);

            if (images != null) {
                ArrayList<JsonNode> tmpList = new ArrayList<>();
                for (ImageItem img : images.subList(0, (images.size() >= 5 ? 5 : images.size())))
                    tmpList.add(img.toJson());
                builder.add("image", tmpList);
            }

        } else if (imageList != null) {
            ArrayList<String> tmpList = new ArrayList<>();
            for (String img : imageList.subList(0, (imageList.size() >= 5 ? 5 : imageList.size())))
                tmpList.add(img);
            builder.add("imageList", tmpList);
        }
        return Json.toJson(builder.get());
    }

}
