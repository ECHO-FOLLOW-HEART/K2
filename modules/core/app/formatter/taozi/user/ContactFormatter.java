package formatter.taozi.user;

import com.fasterxml.jackson.databind.JsonSerializer;
import formatter.AizouFormatter;
import models.user.Contact;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by zephyre on 1/20/15.
 */
public class ContactFormatter extends AizouFormatter<Contact> {

    public ContactFormatter() {
        Map<Class<? extends Contact>, JsonSerializer<Contact>> serializerMap = new HashMap<>();
        serializerMap.put(Contact.class, new ContactSerializer());
        initObjectMapper(null, serializerMap);
    }
}
