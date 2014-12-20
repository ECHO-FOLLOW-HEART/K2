package taozi.test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import controllers.taozi.routes;
import exception.ErrorCode;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import play.Configuration;
import play.GlobalSettings;
import play.api.mvc.HandlerRef;
import play.libs.Json;
import play.mvc.Result;
import play.test.FakeApplication;
import play.test.FakeRequest;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.fest.assertions.Assertions.assertThat;
import static play.test.Helpers.*;

/**
 * Created by zephyre on 12/5/14.
 */
public class UserTest extends AizouTest {

    private static FakeApplication app;

    @BeforeClass
    public static void setup() {
        Config c = ConfigFactory.parseFile(new File("./conf/application.conf"));
        Configuration config = new Configuration(c);
        app = fakeApplication(config.asMap(), new GlobalSettings());
    }

    /**
     * 测试获得用户详情的功能
     */
    @Test
    public void userProfileCheck() {
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
     * 测试搜索用户功能
     */
    @Test
    public void userSearchCheck() {
        running(app, new Runnable() {
            @Override
            public void run() {
                for (String keyword : new String[]{"16612652380", "桃子_100087"}) {
                    HandlerRef<?> handler = routes.ref.UserCtrl.searchUser(keyword);
                    Result result = callAction(handler);
                    JsonNode node = Json.parse(contentAsString(result));
                    assertThat(node.get("code").asInt()).isEqualTo(0);
                    node = node.get("result");
                    assertThat(node.isArray()).isTrue();
                    assertThat(node.size()).isEqualTo(1);
                    node = node.get(0);

                    Set<String> txtKeyList = new HashSet<>();
                    txtKeyList.add("memo");
                    txtKeyList.addAll(Arrays.asList("gender", "signature", "avatar"));
                    assertText(node, txtKeyList.toArray(new String[txtKeyList.size()]), true);
                    assertText(node, new String[]{"id", "easemobUser", "nickName"}, false);
                    assertThat(node.get("userId").asLong()).isEqualTo(100087);
                }
            }
        });
    }

    /**
     * 根据环信用户名进行搜索
     */
    @Test
    public void easemobSearchCheck() {
        running(app, new Runnable() {
            @Override
            public void run() {
                HandlerRef<?> handler = routes.ref.UserCtrl.getUsersByEasemob();
                FakeRequest req = fakeRequest(routes.UserCtrl.getUsersByEasemob());
                ObjectNode postData = Json.newObject();
                postData.put("easemob", Json.toJson(Arrays.asList("vrebxwbvp4vrn4ciowz2xm3zni3kqlz4",
                        "o5qbk6sqpl9jhpf6jq8tjwdlcr6s3flj")));
                req.withJsonBody(postData);

                Result result = callAction(handler, req);
                JsonNode node = Json.parse(contentAsString(result));
                assertThat(node.get("code").asInt()).isEqualTo(0);
                node = node.get("result");
                assertThat(node.isArray()).isTrue();
                assertThat(node.size()).isEqualTo(2);

                Map<Long, String> valMap = new HashMap<>();
                valMap.put(100132L, "vrebxwbvp4vrn4ciowz2xm3zni3kqlz4");
                valMap.put(100133L, "o5qbk6sqpl9jhpf6jq8tjwdlcr6s3flj");
                for (int i = 0; i < 2; i++) {
                    JsonNode userNode = node.get(i);

                    Long userId = userNode.get("userId").asLong();
                    assertThat(valMap.get(userId)).isEqualTo(userNode.get("easemobUser").asText());

                    Set<String> txtKeyList = new HashSet<>();
                    txtKeyList.add("memo");
                    txtKeyList.addAll(Arrays.asList("gender", "signature", "avatar"));
                    assertText(userNode, txtKeyList.toArray(new String[txtKeyList.size()]), true);
                    assertText(userNode, new String[]{"id", "easemobUser", "nickName"}, false);
                }
            }
        });
    }

    /**
     * 测试自有账户登录功能
     */
    @Test
    public void loginCheck() {
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
    }

    /**
     * 测试手机号注册功能
     */
    @Test
    @Ignore
    public void signupCheck() {
        running(app, new Runnable() {
            @Override
            public void run() {
                String timeString = String.format("138%d", System.currentTimeMillis() / 100000);
                String magicVal = "85438734";
                String magicTel = "15313380121";
                String magicPasswd = "12345678";
                String[][] postData = new String[][]{
                        new String[]{magicTel, magicVal, magicPasswd},
                        new String[]{timeString, "123456", magicPasswd},
                        new String[]{timeString, magicVal, magicPasswd}
                };

                for (String[] d : postData) {
                    FakeRequest req = fakeRequest(routes.UserCtrl.signup());
                    ObjectNode postNode = Json.newObject();
                    postNode.put("tel", d[0]);
                    postNode.put("captcha", d[1]);
                    postNode.put("pwd", d[2]);
                    req.withJsonBody(postNode);

                    HandlerRef<?> handler = routes.ref.UserCtrl.signup();
                    Result result = callAction(handler, req);
                    JsonNode node = Json.parse(contentAsString(result));

                    if (d[0].equals(magicTel))
                        assertThat(node.get("code").asInt()).isEqualTo(ErrorCode.USER_EXIST);
                    else if (!d[1].equals(magicVal))
                        assertThat(node.get("code").asInt()).isEqualTo(ErrorCode.CAPTCHA_ERROR);
                    else {
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
    }


    /**
     * 测试修改账户信息的功能
     */
    @Test
    public void editCheck() {
        running(app, new Runnable() {
            @Override
            public void run() {
                String timeString = new SimpleDateFormat().format(new Date());
                List<String> fields = Arrays.asList("nickName", "signature");

                long targetId = 100076;
                for (String f : fields) {
                    for (long selfId : new long[]{0, targetId}) {
                        FakeRequest req = fakeRequest(routes.UserCtrl.editorUserInfo(targetId));
                        req.withJsonBody(Json.parse(String.format("{\"%s\": \"%s\"}", f, timeString +
                                String.format("%d", new Random().nextInt(Integer.MAX_VALUE)))));
                        req.withHeader("UserId", String.format("%d", selfId));

                        HandlerRef<?> handler = routes.ref.UserCtrl.editorUserInfo(targetId);
                        Result result = callAction(handler, req);
                        JsonNode node = Json.parse(contentAsString(result));

                        if (selfId == 0)
                            assertThat(node.get("code").asInt()).isEqualTo(ErrorCode.AUTH_ERROR);
                        else
                            assertThat(node.get("code").asInt()).isEqualTo(0);
                    }
                }
            }
        });
    }
}
