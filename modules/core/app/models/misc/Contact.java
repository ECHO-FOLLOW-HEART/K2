package models.misc;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.databind.JsonNode;
import com.mongodb.BasicDBObjectBuilder;
import models.AizouBaseItem;
import models.ITravelPiFormatter;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Transient;
import play.libs.Json;

import java.util.ArrayList;
import java.util.List;

/**
 * 联系信息。
 *
 * @author Zephyre
 */
@Embedded
@JsonFilter("contactFilter")
public class Contact extends AizouBaseItem implements ITravelPiFormatter {
    @Transient
    public static String simpFax = "fax";
    @Transient
    public static String simpEmail = "email";
    @Transient
    public static String simpPhoneList = "phoneList";

    public List<String> phoneList;
    public String fax;
    public String email;

    public String getFax() {
        if (fax == null)
            return "";
        else
            return fax;
    }

    public String getEmail() {
        if (email == null)
            return "";
        else
            return email;
    }

    public List<String> getPhoneList() {
        if (phoneList == null)
            return new ArrayList<>();
        else
            return phoneList;
    }

    @Override
    public JsonNode toJson() {
        BasicDBObjectBuilder builder = BasicDBObjectBuilder.start();

        for (String k : new String[]{"fax", "email"}) {
            try {
                Object val = Ratings.class.getField(k).get(this);
                builder.add(k, val != null ? val : "");
            } catch (IllegalAccessException | NoSuchFieldException ignored) {
            }
        }
        builder.add("phoneList", (phoneList != null && !phoneList.isEmpty()) ? phoneList : new ArrayList<>());
        return Json.toJson(builder.get());
    }
}
