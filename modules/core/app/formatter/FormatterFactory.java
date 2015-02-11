package formatter;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Hashtable;
import java.util.Map;

public class FormatterFactory {

    private static final FormatterFactory instance = new FormatterFactory();

    @SuppressWarnings("rawtypes")
    private Map<String, AizouFormatter> mapHolder = new Hashtable<>();

    private FormatterFactory() {
    }


    @SuppressWarnings("unchecked")
    public static <T extends AizouFormatter> T getInstance(Class<T> classOf)
            throws InstantiationException, IllegalAccessException {

        if (!instance.mapHolder.containsKey(classOf.toString())) {
            synchronized (instance) {
                if (!instance.mapHolder.containsKey(classOf.toString())) {
                    T obj = classOf.newInstance();

                    instance.mapHolder.put(classOf.toString(), obj);
                }
            }
        }

        return (T) instance.mapHolder.get(classOf.toString());
    }

    @SuppressWarnings("unchecked")
    public static <T extends AizouFormatter> T getInstance(Class<T> classOf, Object... args) {
        StringBuilder argsKey = new StringBuilder();
        for (Object ob : args)
            argsKey.append(ob);
        String key = classOf.toString() + argsKey.toString();
        try {

            if (!instance.mapHolder.containsKey(key)) {
                synchronized (instance) {
                    if (!instance.mapHolder.containsKey(key)) {
                        Constructor constructor = classOf.getConstructor(int.class);
                        T obj = (T) constructor.newInstance(args);
                        instance.mapHolder.put(key, obj);
                    }
                }
            }
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return (T) instance.mapHolder.get(key);
    }

    public Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }
}
