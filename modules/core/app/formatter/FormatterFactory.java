package formatter;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Hashtable;
import java.util.Map;

public class FormatterFactory {

    private static final FormatterFactory instance = new FormatterFactory();

    @SuppressWarnings("rawtypes")
    private Map<Class<? extends AizouFormatter>, AizouFormatter> mapHolder = new Hashtable<>();

    private FormatterFactory() {
    }


    @SuppressWarnings("unchecked")
    public static <T extends AizouFormatter> T getInstance(Class<T> classOf)
            throws InstantiationException, IllegalAccessException {

        if (!instance.mapHolder.containsKey(classOf)) {
            synchronized (instance) {
                if (!instance.mapHolder.containsKey(classOf)) {
                    T obj = classOf.newInstance();

                    instance.mapHolder.put(classOf, obj);
                }
            }
        }

        return (T) instance.mapHolder.get(classOf);
    }

    @SuppressWarnings("unchecked")
    public static <T extends AizouFormatter> T getInstance(Class<T> classOf, Object... args)
            throws InstantiationException, IllegalAccessException {
        try {
            if (!instance.mapHolder.containsKey(classOf)) {
                synchronized (instance) {
                    if (!instance.mapHolder.containsKey(classOf)) {
                        Constructor constructor = classOf.getConstructor(int.class);
                        T obj = (T) constructor.newInstance(args);

                        instance.mapHolder.put(classOf, obj);
                    }
                }
            }
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return (T) instance.mapHolder.get(classOf);
    }

    public Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }
}
