package models.misc;

import com.fasterxml.jackson.annotation.JsonFilter;
import models.TravelPiBaseItem;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Transient;

/**
 * Created by lxf on 14-11-12.
 */
@JsonFilter("travelColumnsFilter")
@Entity
public class PageFirst extends TravelPiBaseItem{

    @Transient
    public static String fnCover="cover";
    @Transient
    public static String fnLink="link";
    @Transient
    public static String fnTitle="title";

    /**
     * 标题
     */
    public String title;
    /**
     * 图片的url
     */
    public String cover;

    /**
     * 跳转的url
     */
    public String link;

    public String getCover(){
        if (cover==null)
            return "";
        else
            return cover;
    }

    public String getLink(){
        if (link==null)
            return "";
        else
            return link;
    }

}
