package utils.validator;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;

import static utils.TestHelpers.assertText;

/**
 * Created by zephyre on 2/12/15.
 */
public class SimpleViewSpotValidator extends SimplePoiValidator {
    public SimpleViewSpotValidator(AbstractValidator validator, Collection<String> addedFields,
                                   Collection<String> removedFields) {
        super(validator, addedFields, removedFields);
    }

    public SimpleViewSpotValidator() {
        this(null, null);
    }

    public SimpleViewSpotValidator(Collection<String> addedFields, Collection<String> removedFields) {
        this(null, addedFields, removedFields);
    }

    @Override
    protected Collection<String> modifyFields(Set<String> fields) {
        fields.addAll(Arrays.asList("timeCostDesc", "priceDesc"));
        return fields;
    }

    @Override
    protected void postValidate(JsonNode item) {
        assertText(item, true, "timeCostDesc", "priceDesc");
    }

}
