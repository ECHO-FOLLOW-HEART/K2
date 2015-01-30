package formatter.taozi.user;

import formatter.AizouFormatter;
import models.user.Contact;

/**
 * Created by zephyre on 1/20/15.
 */
public class ContactFormatter extends AizouFormatter<Contact> {

    public ContactFormatter() {
        registerSerializer(Contact.class, new ContactSerializer());
        initObjectMapper(null);
    }
}
