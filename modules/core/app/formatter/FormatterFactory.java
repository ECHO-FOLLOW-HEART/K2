package formatter;

import java.lang.reflect.Constructor;
import java.util.Hashtable;
import java.util.Map;

public class FormatterFactory {

    private static final FormatterFactory instance = new FormatterFactory();

    @SuppressWarnings("rawtypes")
    private Map<String, AizouFormatter> mapHolder = new Hashtable<>();

    private FormatterFactory() {
    }

//
//    @SuppressWarnings("unchecked")
//    public static <T extends AizouFormatter> T getInstance(Class<T> classOf)
//            throws InstantiationException, IllegalAccessException {
//
//        if (!instance.mapHolder.containsKey(classOf.toString())) {
//            synchronized (instance) {
//                if (!instance.mapHolder.containsKey(classOf.toString())) {
//                    T obj = classOf.newInstance();
//
//                    instance.mapHolder.put(classOf.toString(), obj);
//                }
//            }
//        }
//
//        return (T) instance.mapHolder.get(classOf.toString());
//    }

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
                        Class<?>[] signature = new Class<?>[args.length];
                        for (int i = 0; i < args.length; i++) {
                            signature[i] = args[i].getClass();
                        }
                        Constructor constructor = classOf.getConstructor(signature);
                        T obj = (T) constructor.newInstance(args);
                        instance.mapHolder.put(key, obj);
                    }
                }
            }
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }
        return (T) instance.mapHolder.get(key);
    }

    public Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }
}
