package utils.validator;

import com.fasterxml.jackson.databind.JsonNode;

import static org.fest.assertions.Assertions.assertThat;
import static utils.TestHelpers.*;

/**
 * Created by zephyre on 2/12/15.
 */
public class DetailedViewSpotValidator implements AbstractValidator {

    private AbstractValidator validator;

    public DetailedViewSpotValidator(AbstractValidator validator) {
        this.validator = validator;
    }

    public DetailedViewSpotValidator() {
        this(null);
    }

    @Override
    public void validate(JsonNode item) {
        // 验证item的字段是否正确
        assertFields(item, "id", "zhName", "enName", "rating", "address", "images", "type", "timeCostDesc",
                "priceDesc", "rank", "location", "isFavorite", "desc", "openTime", "travelMonth", "trafficInfoUrl",
                "visitGuideUrl", "tipsUrl", "descUrl", "comments", "commentCnt", "lyPoiUrl");

        assertText(item, false, "id", "zhName", "type", "trafficInfoUrl", "visitGuideUrl", "tipsUrl", "descUrl");
        assertText(item, true, "enName", "address", "priceDesc", "timeCostDesc", "desc", "travelMonth", "openTime",
                "lyPoiUrl");

        assertNumber(item.get("rating"),false,new RangeValidator(new DoubleValidator(),0.0,1.0,null));
        assertNumber(item.get("rank"), true, new PositiveValidator(new IntegerValidator(), true));
        assertNumber(item.get("commentCnt"), false, new PositiveValidator(new IntegerValidator(), false));

        assertThat(item.get("isFavorite").isBoolean());
        assertThat(item.get("comments").isArray());

        if (validator != null)
            validator.validate(item);
    }
}
