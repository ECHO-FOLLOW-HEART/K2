package serialization;

/**
 * Created by Heaven on 2015/1/30.
 */
public class ParserFactory {
    private static ParserFactory factory = null;

    private ParserFactory() {
    }

    public synchronized static ParserFactory getInstance() {
        if (factory == null) {
            synchronized (ParserFactory.class) {
                if (factory == null)
                    factory = new ParserFactory();
            }
        }
        return factory;
    }

    public SerializeParser create(String name) {
        SerializeParser serial = null;

        switch (name) {
            case "WrappedResult":
            case "WrappedResultSerializer":
                serial = new WrappedResultSerializer();
                break;
            case "String":
            case "StringSerializer":
                serial = new StringSerializer();
                break;
            default:
                try {
                    Class<?> cls = Class.forName(name);
                    if (SerializeParser.class.isAssignableFrom(cls))
                        serial = (SerializeParser) cls.newInstance();
                } catch (ReflectiveOperationException ignored) {
                }
        }

        return serial;
    }

    public SerializeParser getSerializeParser(Class<?> aClass) {
//        if (aClass.equals(Result.class)) {
//            return new ResultParser();
//        } else {
//            return new SimpleParser(aClass);
//        }
        return null;
    }
}
