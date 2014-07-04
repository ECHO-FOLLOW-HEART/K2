package models.misc;

import com.avaje.ebean.annotation.UpdatedTimestamp;
import models.geos.Country;
import play.data.validation.Constraints;
import play.db.ebean.Model;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.List;

/**
 * 货币信息
 *
 * @author Zephyre
 */
@Entity
public class Currency extends Model {
    public static Finder<String, Currency> finder = new Finder<>(String.class, Currency.class);
    /**
     * 货币代码，比如CNY，USD等。
     */
    @Id
    @Constraints.MaxLength(value = 3)
    @Column(length = 3)
    public String code;
    public String symbol;
    public String name;
    @Constraints.Required
    public Float rate;
    @Version
    @UpdatedTimestamp
    public Timestamp updatedTime;
    /**
     * 哪些国家的使用该币种作为默认货币
     */
    @OneToMany(mappedBy = "defCurrency", fetch = FetchType.LAZY)
    public List<Country> countryItems;

    public static Currency create(String name, String symbol, float rate) {
        Currency ret = new Currency();
        ret.name = name;
        ret.symbol = symbol;
        ret.rate = rate;

        return ret;
    }

}
