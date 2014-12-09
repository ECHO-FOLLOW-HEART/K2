package taozi.test;

import com.fasterxml.jackson.databind.JsonNode;
import controllers.taozi.UserCtrl;
import org.junit.Test;

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
     * 测试查看他人详情的功能
     *
     * @throws ReflectiveOperationException
     */
    @Test
    public void sideUserCheck() throws ReflectiveOperationException {
        Method method = UserCtrl.class.getDeclaredMethod("getUserProfileByIdImpl", Integer.class, Integer.class);
        method.setAccessible(true);

        Integer targetId = 100076;
        for (Integer selfId : new Integer[]{null, targetId}) {
            JsonNode ret = (JsonNode) method.invoke(UserCtrl.class, targetId, selfId);
            Set<String> txtKeyList = new HashSet<>();
            if (selfId != null)
                txtKeyList.add("tel");
            txtKeyList.addAll(Arrays.asList("memo", "gender", "signature", "avatar"));
            assertText(ret, txtKeyList.toArray(new String[txtKeyList.size()]), true);
            assertText(ret, new String[]{"easemobUser", "nickName"}, false);

            assertThat(ret.get("userId").asInt()).isGreaterThan(0);
        }
    }

    /**
     * 测试查看本人详情的功能
     *
     * @throws ReflectiveOperationException
     */
//    @Test
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
