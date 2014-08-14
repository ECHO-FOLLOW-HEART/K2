package models.morphia.misc;

import org.mongodb.morphia.annotations.Embedded;

/**
 * 表示一张图像。
 *
 * Created by zephyre on 8/14/14.
 */
@Embedded
public class ImageItem {
    public String url;

    /**
     * 图像宽度
     */
    public Integer w;

    /**
     * 图像高度
     */
    public Integer h;

    /**
     * 图像格式
     */
    public String fmt;

    /**
     * 图像色彩模型。
     */
    public String cm;

    /**
     * 图像MD5哈希校验
     */
    public String hash;

    /**
     * 图像文件的大小
     */
    public Integer fSize;
}
