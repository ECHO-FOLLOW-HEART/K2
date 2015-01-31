package utils;

import play.mvc.Result;

/**
 * Created by Heaven on 2015/1/30.
 */
public class ParserFactory {
    private static ParserFactory factory = null;

    private ParserFactory(){}

    public synchronized static ParserFactory getInstance() {
        if (factory == null)
            factory = new ParserFactory();
        return factory;
    }

    public SerializeParser getSerializeParser(Class<?> aClass) {
        if (aClass.equals(Result.class)) {
            return new ResultParser();
        } else {
            return new SimpleParser(aClass);
        }
    }
}
