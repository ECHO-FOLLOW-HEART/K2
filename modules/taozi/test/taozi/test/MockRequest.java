package taozi.test;

import com.fasterxml.jackson.databind.JsonNode;
import com.mongodb.BasicDBObjectBuilder;
import play.libs.Json;
import play.mvc.Http;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

/**
 * Created by Heaven on 2014/12/17.
 */
public class MockRequest {
    private Http.Request request = null;
    private Http.RequestBody requestBody = null;
    private Http.Context context = null;

    public MockRequest() {
        this.requestBody = mock(Http.RequestBody.class);


        this.request = mock(Http.Request.class);
        when(request.body()).thenReturn(requestBody);

        this.context = mock(Http.Context.class);
        when(context.request()).thenReturn(request);
    }

    public void setHeader(String key, String value) {
        when(request.getHeader(key)).thenReturn(value);
    }

    public void setRequestJson(Map<String, String> map) {
        when(requestBody.asJson()).thenReturn(toJson(map));
    }

    public void setRequestMap(Map<String, String[]> map) {
        when(requestBody.asFormUrlEncoded()).thenReturn(map);
    }


    public Object apply(Method method, Object obj, Object...args) throws InvocationTargetException, IllegalAccessException {
        Http.Context.current.set(context);
        Object ret = method.invoke(obj, args);
        Http.Context.current.remove();
        return ret;
    }

    private JsonNode toJson(Map<String, String> map) {
        BasicDBObjectBuilder builder = BasicDBObjectBuilder.start();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            builder.add(entry.getKey(), entry.getValue());
        }
        return Json.toJson(builder.get());
    }

}
