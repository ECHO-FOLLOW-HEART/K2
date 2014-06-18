package models.misc;

import play.data.validation.Constraints;
import play.db.ebean.Model;

import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

/**
 * 图像的基类。
 *
 * @author Haizi
 */
@MappedSuperclass
public class ImageItem extends Model{
    @Id
    public Long id;

    /**
     * 图像的MD5校验值。
     */
    @Constraints.Required
    public String checksum;

    /**
     * 图像对应的URL（指的是图像位于我们自己服务器的URL）
     */
    @Constraints.Required
    public String href;

    public String originalUrl;

    @Constraints.Required
    public Integer width;

    @Constraints.Required
    public Integer height;
}
