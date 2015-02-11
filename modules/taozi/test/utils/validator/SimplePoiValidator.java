package utils.validator;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static utils.TestHelpers.*;

/**
 * Created by zephyre on 2/11/15.
 */
public abstract class SimplePoiValidator implements AbstractValidator {
    private Collection<String> addedFields;
    private Collection<String> removedFields;
    private AbstractValidator validator;

    public SimplePoiValidator(AbstractValidator validator,
                              Collection<String> addedFields, Collection<String> removedFields) {
        this.validator = validator;
        this.addedFields = addedFields;
        this.removedFields = removedFields;
    }

    public SimplePoiValidator(Collection<String> addedFields, Collection<String> removedFields) {
        this(null, addedFields, removedFields);
    }

    /**
     * 返回POI的基础字段
     *
     * @return
     */
    protected Collection<String> getBaseFields() {
        Set<String> fields = new HashSet<>();
        fields.addAll(Arrays.asList("id", "zhName", "enName", "rating", "address", "images", "rank", "location"));

        return modifyFields(fields);
    }

    /**
     * 对基础字段进行修正
     *
     * @param fields
     * @return
     */
    protected Collection<String> modifyFields(Set<String> fields) {
        return fields;
    }

    /**
     * 其它一些validate规则
     * @param item
     */
    protected void postValidate(JsonNode item) {
    }

    @Override
    public void validate(JsonNode item) {
        // 验证item的字段是否正确
        Set<String> fields = new HashSet<>();
        fields.addAll(getBaseFields());
        if (addedFields != null)
            fields.addAll(addedFields);
        if (removedFields != null)
            fields.addAll(removedFields);
        assertFields(item, fields.toArray(new String[fields.size()]));

        assertText(item, false, "id", "zhName");
        assertText(item, true, "enName", "address");

        assertImages(item.get("images"), true);

        assertCoords(item.get("location"));

        assertNumber(item.get("rating"), false, new RangeValidator(new DoubleValidator(), 0.0, 1.0, null));
        assertNumber(item.get("rank"), true, new PositiveValidator(new IntegerValidator(), true));

        postValidate(item);

        if (validator != null)
            validator.validate(item);
    }
}
