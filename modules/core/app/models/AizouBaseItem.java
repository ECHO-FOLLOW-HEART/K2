package models;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * Created by zephyre on 12/10/14.
 */
public abstract class AizouBaseItem {
    public void fillNullMembers() {
        fillNullMembers(null);
    }

    public void fillNullMembers(Collection<String> retrievedFields) {
        Class<?> cls = this.getClass();

        for (; ; ) {

            for (Field field : cls.getDeclaredFields()) {
                if (retrievedFields != null && !retrievedFields.contains(field.getName()))
                    continue;

                try {
                    int mod = field.getModifiers();
                    if (Modifier.isStatic(mod) || Modifier.isFinal(mod))
                        continue;

                    Class<?> fieldCls = field.getType();
                    field.setAccessible(true);
                    if (String.class.isAssignableFrom(fieldCls) && field.get(this) == null) {
                        field.set(this, "");
                    } else if (List.class.isAssignableFrom(fieldCls) && field.get(this) == null) {
                        field.set(this, new ArrayList<>());
                    } else if (Map.class.isAssignableFrom(fieldCls) && field.get(this) == null) {
                        field.set(this, new HashMap<>());
                    } else if (AizouBaseItem.class.isAssignableFrom(fieldCls)) {
                        if (field.get(this) == null)
                            field.set(this, new HashMap<>());
                        else
                            ((AizouBaseItem) field.get(this)).fillNullMembers();
                    }
                } catch (IllegalAccessException ignored) {
                }
            }

            if (cls == AizouBaseItem.class)
                break;
            else
                cls = cls.getSuperclass();
        }
    }
}
