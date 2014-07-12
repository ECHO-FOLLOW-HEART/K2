package models.morphia.misc;

import org.mongodb.morphia.annotations.Embedded;

import java.util.List;

/**
 * 联系信息。
 *
 * @author Zephyre
 */
@Embedded
public class Contact {
    public List<String> phoneList;
    public String fax;
    public String email;
}
