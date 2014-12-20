package taozi.test;

import com.fasterxml.jackson.databind.JsonNode;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import controllers.taozi.UserCtrl;
import controllers.taozi.routes;
import exception.AizouException;
import exception.ErrorCode;
import org.junit.BeforeClass;
import org.junit.Test;
import play.Configuration;
import play.api.mvc.HandlerRef;
import play.libs.Json;
import play.mvc.Result;
import play.test.FakeApplication;
import play.test.FakeRequest;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.fest.assertions.Assertions.assertThat;
import static play.test.Helpers.*;

/**
 * Created by zephyre on 12/5/14.
 */
public class UserTest extends AizouTest {

    private static FakeApplication app;

    @BeforeClass
    public static void setup() {
        Config c = ConfigFactory.parseFile(new File("../conf/application.conf"));
        Configuration config = new Configuration(c);
        app = fakeApplication(config.asMap());
    }

    /**
     * 测试获得用户详情的功能
     *
     * @throws ReflectiveOperationException
     */
    @Test
    public void userProfileCheck() throws ReflectiveOperationException {
        running(app, new Runnable() {
            @Override
            public void run() {
                Long targetId = 100076L;
                for (Long selfId : new Long[]{null, targetId}) {
                    FakeRequest req = fakeRequest(routes.UserCtrl.getUserProfileById(targetId));
                    if (selfId != null)
                        req.withHeader("UserId", selfId.toString());

                    HandlerRef<?> handler = routes.ref.UserCtrl.getUserProfileById(targetId);
                    Result result = callAction(handler, req);
                    JsonNode node = Json.parse(contentAsString(result));
                    assertThat(node.get("code").asInt()).isEqualTo(0);
                    node = node.get("result");

                    Set<String> txtKeyList = new HashSet<>();
                    if (selfId != null) {
                        txtKeyList.add("tel");
                        assertThat(node.get("dialCode").asInt()).isNotEqualTo(0);
                    } else
                        txtKeyList.add("memo");
                    txtKeyList.addAll(Arrays.asList("gender", "signature", "avatar"));
                    assertText(node, txtKeyList.toArray(new String[txtKeyList.size()]), true);
                    assertText(node, new String[]{"id", "easemobUser", "nickName"}, false);
                    assertThat(node.get("userId").asLong()).isEqualTo(targetId);
                }
            }
        });
    }

    /**
     * 测试自有账户登录功能
     */
    @Test
    public void loginCheck() throws ReflectiveOperationException {
        running(app, new Runnable() {
            @Override
            public void run() {
                for (String passwd : new String[]{"fake", "james890526"}) {
                    FakeRequest req = fakeRequest(routes.UserCtrl.signin());
                    req.withJsonBody(Json.parse(String.format("{\"loginName\": \"18600441776\", \"pwd\": \"%s\"}",
                            passwd)));

                    HandlerRef<?> handler = routes.ref.UserCtrl.signin();
                    Result result = callAction(handler, req);
                    JsonNode node = Json.parse(contentAsString(result));

                    if (passwd.equals("fake")) {
                        assertThat(node.get("code").asInt()).isEqualTo(ErrorCode.AUTH_ERROR);
                        assertThat(node.get("result")).isEqualTo(null);
                    } else {
                        assertThat(node.get("code").asInt()).isEqualTo(0);
                        node = node.get("result");

                        Set<String> txtKeyList = new HashSet<>();
                        txtKeyList.add("tel");
                        assertThat(node.get("dialCode").asInt()).isNotEqualTo(0);
                        txtKeyList.addAll(Arrays.asList("gender", "signature", "avatar"));
                        assertText(node, txtKeyList.toArray(new String[txtKeyList.size()]), true);
                        assertText(node, new String[]{"id", "easemobUser", "nickName"}, false);
                    }
                }
            }
        });


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
