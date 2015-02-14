package utils.validator;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;

import static org.fest.assertions.Assertions.assertThat;
import static utils.TestHelpers.assertNumber;
import static utils.TestHelpers.assertText;

/**
 * Created by zephyre on 2/12/15.
 */
public class SimpleRestaurantValidator extends SimplePoiValidator {
    public SimpleRestaurantValidator(AbstractValidator validator, Collection<String> addedFields,
                                     Collection<String> removedFields) {
        super(validator, addedFields, removedFields);
    }

    public SimpleRestaurantValidator(Collection<String> addedFields, Collection<String> removedFields) {
        this(null, addedFields, removedFields);
    }

    public SimpleRestaurantValidator() {
        this(null, null);
    }

    @Override
    protected Collection<String> modifyFields(Set<String> fields) {
        fields.addAll(Arrays.asList("price", "priceDesc", "tel"));
        return fields;
    }

    @Override
    protected void postValidate(JsonNode item) {
        assertNumber(item.get("price"), true, new PositiveValidator(new DoubleValidator()));
        assertText(item, true, "priceDesc");
        assertThat(item.get("tel").isArray()).isTrue();
    }
}
