package models.misc;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.databind.JsonNode;
import com.mongodb.BasicDBObjectBuilder;
import models.AizouBaseEntity;
import models.ITravelPiFormatter;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Transient;
import play.libs.Json;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * web用的推荐内容
 *
 * Created by topy on 2014/9/13.
 */
@JsonFilter("recommendationFilter")
@Entity
public class Recommendation extends AizouBaseEntity implements ITravelPiFormatter {

    @Transient
    public static String FD_NAME = "name";

    @Transient
    public static String FD_IMAGES = "images";
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
     * 路线编辑的头像
     */
    public Date editorDate;
    /**
     * 浏览量
     */
    public Integer planViews;

    /**
     * 介绍
     */
    public Description description;

    /**
     * 理由
     */
    public String reason;

    @Override
    public JsonNode toJson() {
        final DateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
        BasicDBObjectBuilder builder = BasicDBObjectBuilder.start().add("name", name).add("id", getId().toString())
                .add("reason", reason == null ? "" : reason)
                .add("editor", editorNickName == null ? "" : editorNickName)
                .add("editorAvatar", editorAvatar == null ? "" : editorAvatar)
                .add("description", description == null ? "" : description.toJson())
                .add("editorDate", editorDate == null ? "" : fmt.format(editorDate))
                .add("planViews", planViews == null ? "" : planViews);

        // 如果存在更高阶的images字段，则使用之
        if (images != null && !images.isEmpty()) {

            ArrayList<JsonNode> tmpList = new ArrayList<>();
            for (ImageItem img : images.subList(0, (images.size() >= 5 ? 5 : images.size()))) {

                BasicDBObjectBuilder bld = BasicDBObjectBuilder.start()
                        .add("url", img.getUrl())
                        .add("w", img.getW())
                        .add("h", img.getH());

                tmpList.add(Json.toJson(bld.get()));
            }
            builder.add("images", tmpList);

        }
        return Json.toJson(builder.get());
    }

}
