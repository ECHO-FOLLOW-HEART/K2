package models.misc;

import play.data.validation.Constraints;
import play.db.ebean.Model;

import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

/**
 * 图像
 *
 * @author Haizi
 */
@MappedSuperclass
public class ImageItem extends Model{
    @Id
    public Long id;

    @Constraints.Required
    public String checksum;

    @Constraints.Required
    public String href;

    public String originalUrl;

    @Constraints.Required
    public Integer width;

    @Constraints.Required
    public Integer height;
}
