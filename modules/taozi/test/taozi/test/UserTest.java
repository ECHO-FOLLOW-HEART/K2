package taozi.test;

import com.fasterxml.jackson.databind.JsonNode;
import controllers.taozi.UserCtrl;
import exception.AizouException;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.fest.assertions.Assertions.assertThat;

/**
 * Created by zephyre on 12/5/14.
 */
public class UserTest extends AizouTest {

    /**
     * 测试获得用户详情的功能
     *
     * @throws ReflectiveOperationException
     */
    @Test
    public void sideUserCheck() throws ReflectiveOperationException {
        Method method = UserCtrl.class.getDeclaredMethod("getUserProfileByIdImpl", Long.class, Long.class);
        method.setAccessible(true);

        Long targetId = 100076L;
        for (Long selfId : new Long[]{null, targetId}) {
            JsonNode ret = (JsonNode) method.invoke(UserCtrl.class, targetId, selfId);
            Set<String> txtKeyList = new HashSet<>();
            if (selfId != null)
                txtKeyList.add("tel");
            else
                txtKeyList.add("memo");
            txtKeyList.addAll(Arrays.asList("gender", "signature", "avatar"));
            assertText(ret, txtKeyList.toArray(new String[txtKeyList.size()]), true);
            assertText(ret, new String[]{"easemobUser", "nickName"}, false);

            assertThat(ret.get("userId").asInt()).isGreaterThan(0);
        }
    }

    /**
     * 测试自有账户登录功能
     */
    @Test
    public void loginCheck() throws ReflectiveOperationException {
        Method method = UserCtrl.class.getDeclaredMethod("signinImpl", String.class, String.class);
        method.setAccessible(true);

        try {
            method.invoke(UserCtrl.class, "18600441776", "fake");
            assertThat(false).isTrue();
        } catch (InvocationTargetException e) {
            AizouException causeErr = (AizouException) e.getCause();
            assertThat(causeErr.getErrCode()).isEqualTo(407);
        }

        JsonNode ret = (JsonNode) method.invoke(UserCtrl.class, "18600441776", "james890526");

        assertText(ret, new String[]{"id", "easemobUser", "easemobPwd", "nickName", "secKey"}, false);
        assertText(ret, new String[]{"avatar", "gender", "signature", "tel"}, true);
        assertThat(ret.get("dialCode") != null).isTrue();
        assertThat(ret.get("userId").asInt()).isGreaterThan(0);
        assertThat(ret.get("tel").asText()).isEqualTo("18600441776");
    }

}
