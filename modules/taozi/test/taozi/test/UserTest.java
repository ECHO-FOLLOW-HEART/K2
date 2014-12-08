package taozi.test;

import com.fasterxml.jackson.databind.JsonNode;
import controllers.taozi.UserCtrl;
import org.junit.Test;
import play.test.WithApplication;

import java.lang.reflect.Method;

import static org.fest.assertions.Assertions.assertThat;

/**
 * Created by zephyre on 12/5/14.
 */
public class UserTest extends WithApplication {

    /**
     * 测试查看他人详情的功能
     *
     * @throws ReflectiveOperationException
     */
    @Test
    public void sideUserCheck() throws ReflectiveOperationException {
        Method method = UserCtrl.class.getDeclaredMethod("getSideUserProfileById", int.class);
        method.setAccessible(true);

        int userId = 100128;
        JsonNode ret = (JsonNode) method.invoke(UserCtrl.class, userId);

        for (String key : new String[]{"memo", "easemobUser", "nickName", "avatar", "gender", "signature"})
            assertThat(ret.get(key).asText()).isNotNull();

        assertThat(ret.get("userId").asInt()).isEqualTo(userId);
    }

    /**
     * 测试查看本人详情的功能
     *
     * @throws ReflectiveOperationException
     */
    @Test
    public void selfUserCheck() throws ReflectiveOperationException {
        Method method = UserCtrl.class.getDeclaredMethod("getSelfUserProfileById", int.class);
        method.setAccessible(true);

        int userId = 100128;
        JsonNode ret = (JsonNode) method.invoke(UserCtrl.class, userId);

        for (String key : new String[]{"easemobUser", "nickName", "avatar", "gender", "signature", "tel"})
            assertThat(ret.get(key).asText()).isNotNull();

        assertThat(ret.get("dialCode").asInt()).isGreaterThan(0);

        assertThat(ret.get("userId").asInt()).isEqualTo(userId);
    }

}
