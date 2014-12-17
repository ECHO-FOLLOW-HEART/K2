package taozi.test;

import play.mvc.Http;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by Heaven on 2014/12/17.
 */
public class MockRequest {
    private Http.Request request = null;
    private Http.Context context = null;

    public MockRequest() {
        this.request = mock(Http.Request.class);

        this.context = mock(Http.Context.class);
        when(context.request()).thenReturn(request);
    }

    public void setHeader(String key, String value) {
        when(request.getHeader(key)).thenReturn(value);
    }

    public Object apply(Method method, Object obj, Object...args) throws InvocationTargetException, IllegalAccessException {
        Http.Context.current.set(context);
        Object ret = method.invoke(obj, args);
        Http.Context.current.remove();
        return ret;
    }
}
