package taozi.test;

import controllers.taozi.TravelNoteCtrl;
import models.misc.TravelNote;
import org.json.JSONArray;
import org.json.JSONObject;
import  play.mvc.Result;

import controllers.taozi.MiscCtrl;
import org.junit.Test;

import java.lang.reflect.Method;

import static org.fest.assertions.Assertions.assertThat;
import static play.mvc.Http.Status.METHOD_NOT_ALLOWED;
import static play.test.Helpers.*;

/**
 * Created by Heaven on 2014/12/16.
 */
public class TravelNoteTest extends AizouTest {
    /**
     * 针对 游记搜索 的测试
     */
    @Test
    public void testSearchNotes() throws Exception {
        Method method = TravelNoteCtrl.class.getDeclaredMethod("searchNotes",
                String.class, String.class, int.class, int.class);
        method.setAccessible(true);
        int page = 0;
        int pageSize = 3;
        Result res = (Result) method.invoke(TravelNoteCtrl.class,
                "北京", "", page, pageSize);
        JSONObject response = new JSONObject(contentAsString(res));

        // The response code should be zero
        assertThat(response.getInt("code")).isEqualTo(0);
    }
}
