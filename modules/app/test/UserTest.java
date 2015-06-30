//import com.fasterxml.jackson.databind.JsonNode;
//import com.fasterxml.jackson.databind.node.ObjectNode;
//import com.typesafe.config.Config;
//import com.typesafe.config.ConfigFactory;
//import controllers.app.routes.bac;
//import exception.ErrorCode;
//import org.junit.BeforeClass;
//import org.junit.Ignore;
//import org.junit.Test;
//import play.Configuration;
//import play.api.mvc.HandlerRef;
//import play.libs.Json;
//import play.mvc.Result;
//import play.test.FakeApplication;
//import play.test.FakeRequest;
//
//import java.io.File;
//import java.text.SimpleDateFormat;
//import java.util.*;
//
//import static org.fest.assertions.Assertions.assertThat;
//import static play.test.Helpers.*;
//import static utils.TestHelpers.*;
//
///**
// * Created by zephyre on 12/5/14.
// */
//public class UserTest extends AizouTest {
//
//    private static FakeApplication app;
//
//    @BeforeClass
//    public static void setup() {
//        Config c = ConfigFactory.parseFile(new File("../../conf/application.conf"));
//        Configuration config = new Configuration(c);
//        app = fakeApplication(config.asMap());
//    }
//
//    public void userInfoHandler(JsonNode userInfo, boolean self) {
//        if (self) {
//            assertFields(userInfo, "id", "easemobUser", "nickName", "avatar", "avatarSmall", "gender",
//                    "signature", "userId", "tel", "dialCode");
//            assertText(userInfo, true, "tel");
//            JsonNode dialCodeNode = userInfo.get("dialCode");
//            if (dialCodeNode != null && !dialCodeNode.isNull())
//                assertThat(dialCodeNode.asInt()).isPositive();
//        } else {
//            assertFields(userInfo, "id", "easemobUser", "nickName", "avatar", "avatarSmall", "gender",
//                    "signature", "userId", "memo");
//            assertText(userInfo, true, "memo");
//        }
//
//        assertText(userInfo, false, new String[]{"id", "easemobUser", "nickName"});
//        assertText(userInfo, true, new String[]{"avatar", "avatarSmall", "gender", "signature"});
//        assertThat(userInfo.get("userId").asLong()).isPositive();
//    }
//
//    /**
//     * 测试获得用户详情的功能
//     */
//    @Test
//    public void userProfileCheck() {
//        running(app, new Runnable() {
//            @Override
//            public void run() {
//                Long targetId = 100027L;
//                for (Long selfId : new Long[]{null, targetId}) {
//                    FakeRequest req = fakeRequest(routes.UserCtrl.getUserProfileById(targetId));
//                    if (selfId != null)
//                        req.withHeader("UserId", selfId.toString());
//
//                    HandlerRef<?> handler = routes.ref.UserCtrl.getUserProfileById(targetId);
//                    JsonNode node = getResultNode(handler, req);
//                    userInfoHandler(node, selfId != null);
//                    assertThat(node.get("userId").asLong()).isEqualTo(targetId);
//                }
//            }
//        });
//    }
//
//    /**
//     * 测试搜索用户功能
//     */
//    @Test
//    public void userSearchCheck() {
//        running(app, new Runnable() {
//            @Override
//            public void run() {
//                for (String keyword : new String[]{"13261783631", "莫小桃"}) {
//                    HandlerRef<?> handler = routes.ref.UserCtrl.searchUser(keyword);
//
//                    for (JsonNode node : getResultNode(handler)) {
//                        userInfoHandler(node, false);
//                        assertThat(node.get("userId").asLong()).isEqualTo(100025L);
//                    }
//                }
//            }
//        });
//    }
//
//    /**
//     * 获得联系人
//     */
//    @Test
//    public void getContactsCheck() {
//        running(app, new Runnable() {
//            @Override
//            public void run() {
//                HandlerRef<?> ref = routes.ref.UserCtrl.getContactList();
//
//                Long selfId = 100027L;
//                FakeRequest req = fakeRequest(routes.UserCtrl.getUserProfileById(selfId));
//                req.withHeader("UserId", selfId.toString());
//                JsonNode node = getResultNode(ref, req);
//
//                JsonNode contacts = node.get("contacts");
//                assertThat(contacts.size()).isGreaterThan(0);
//                for (JsonNode c : contacts)
//                    userInfoHandler(c, false);
//            }
//        });
//    }
//
//    /**
//     * 删除联系人
//     */
//    @Test
//    public void delContactCheck() {
//        running(app, new Runnable() {
//            @Override
//            public void run() {
//                Long selfId = 100027L;
//                Long targetId = 100025L;
//
//                HandlerRef<?> ref = routes.ref.UserCtrl.delContact(targetId);
//
//                FakeRequest req = fakeRequest(routes.UserCtrl.delContact(targetId));
//                req.withHeader("UserId", selfId.toString());
//                getResultNode(ref, req);
//            }
//        });
//    }
//
//    /**
//     * 添加联系人
//     */
//    @Test
//    public void addContactCheck() {
//        running(app, new Runnable() {
//            @Override
//            public void run() {
//                Long selfId = 100027L;
//
//                HandlerRef<?> ref = routes.ref.UserCtrl.addContact();
//
//                FakeRequest req = fakeRequest(routes.UserCtrl.addContact());
//                req.withHeader("UserId", selfId.toString());
//
//                Long targetId = 100025L;
//                ObjectNode body = Json.newObject();
//                body.put("userId", targetId);
//                req.withJsonBody(body);
//
//                getResultNode(ref, req);
//            }
//        });
//    }
//
//    /**
//     * 根据环信用户名进行搜索
//     */
//    @Test
//    public void easemobSearchCheck() {
//        running(app, new Runnable() {
//            @Override
//            public void run() {
//                HandlerRef<?> handler = routes.ref.UserCtrl.getUsersByEasemob();
//                FakeRequest req = fakeRequest(routes.UserCtrl.getUsersByEasemob());
//                ObjectNode postData = Json.newObject();
//                postData.put("easemob", Json.toJson(Arrays.asList(
//                        "br7ma2vvuwra87002jlf0jqd0tgfy7ka",
//                        "sqtvpiblkiwr4kiqbjzch5ja48o4scqp",
//                        "5m7ua7ila5178h6y848gw9fmlzoslots"
//                )));
//                req.withJsonBody(postData);
//
//                JsonNode node = getResultNode(handler, req);
//                assertThat(node.isArray()).isTrue();
//                assertThat(node.size()).isEqualTo(3);
//
//                for (JsonNode user : node)
//                    userInfoHandler(user, false);
//            }
//        });
//    }
//
//    /**
//     * 测试自有账户登录功能
//     */
//    @Test
//    public void loginCheck() {
//        running(app, new Runnable() {
//            @Override
//            public void run() {
//                for (String passwd : new String[]{"fake", "james890526"}) {
//                    FakeRequest req = fakeRequest(routes.UserCtrl.signin());
//                    req.withJsonBody(Json.parse(String.format("{\"loginName\": \"18600441776\", \"pwd\": \"%s\"}",
//                            passwd)));
//
//                    HandlerRef<?> handler = routes.ref.UserCtrl.signin();
//
//                    if (passwd.equals("fake")) {
//                        Result result = callAction(handler, req);
//                        JsonNode node = Json.parse(contentAsString(result));
//                        assertThat(node.get("code").asInt()).isEqualTo(ErrorCode.AUTH_ERROR.getVal());
//                        assertThat(node.get("result")).isEqualTo(null);
//                    } else {
//                        JsonNode node = getResultNode(handler, req);
//
//                        assertText(node, false, "easemobPwd");
//                        assertText(node, false, "secKey");
//                        ObjectNode info2 = node.deepCopy();
//                        info2.remove(Arrays.asList("easemobPwd", "secKey"));
//                        userInfoHandler(info2, true);
//                        assertThat(node.get("userId").asLong()).isEqualTo(100009L);
//                    }
//                }
//            }
//        });
//    }
//
//    /**
//     * 使用通讯录查找用户
//     */
//    @Test
//    public void searchByAddrBookCheck() {
//        running(app, new Runnable() {
//            @Override
//            public void run() {
//                FakeRequest req = fakeRequest(routes.UserCtrl.matchAddressBook());
//                req.withJsonBody(Json.parse("{ \"contacts\":[ { \"entryId\":1, \"sourceId\":1, " +
//                        "\"name\":\"Harry\", \"tel\":\"13261783632\" },{ \"entryId\":2, \"sourceId\":2, " +
//                        "\"name\":\"Zephyre\", \"tel\":\"18600441776\" } ] }"));
//                Long selfId = 100027L;
//                req.withHeader("UserId", selfId.toString());
//
//                HandlerRef<?> handler = routes.ref.UserCtrl.matchAddressBook();
//                JsonNode node = getResultNode(handler, req);
//
//                assertThat(node.size()).isEqualTo(2);
//
//                for (JsonNode user : node) {
//                    assertFields(user,
//                            "entryId",
//                            "sourceId",
//                            "isUser",
//                            "isContact",
//                            "userId",
//                            "name",
//                            "tel",
//                            "weixin");
//                    assertText(user, true, new String[]{"name", "tel", "weixin"});
//                    for (String key : new String[]{"entryId", "sourceId"})
//                        assertThat(user.get(key).asLong()).isPositive();
//                    for (String key : new String[]{"isUser", "isContact"}) {
//                        JsonNode val = user.get(key);
//                        if (val == null)
//                            assertThat(false).isTrue();
//                        else {
//                            assertThat(!val.isNull()).isTrue();
//                            assertThat(val.isBoolean()).isTrue();
//                            if (user.get("isUser").asBoolean())
//                                assertThat(user.get("userId").asLong()).isPositive();
//                        }
//                    }
//                }
//            }
//        });
//    }
//
//    /**
//     * 测试手机号注册功能
//     */
//    @Test
//    @Ignore
//    public void signupCheck() {
//        running(app, new Runnable() {
//            @Override
//            public void run() {
//                String timeString = String.format("138%d", System.currentTimeMillis() / 100000);
//                String magicVal = "85438734";
//                String magicTel = "15313380121";
//                String magicPasswd = "12345678";
//                String[][] postData = new String[][]{
//                        new String[]{magicTel, magicVal, magicPasswd},
//                        new String[]{timeString, "123456", magicPasswd},
//                        new String[]{timeString, magicVal, magicPasswd}
//                };
//
//                for (String[] d : postData) {
//                    FakeRequest req = fakeRequest(routes.UserCtrl.signup());
//                    ObjectNode postNode = Json.newObject();
//                    postNode.put("tel", d[0]);
//                    postNode.put("captcha", d[1]);
//                    postNode.put("pwd", d[2]);
//                    req.withJsonBody(postNode);
//
//                    HandlerRef<?> handler = routes.ref.UserCtrl.signup();
//                    Result result = callAction(handler, req);
//                    JsonNode node = Json.parse(contentAsString(result));
//
//                    if (d[0].equals(magicTel))
//                        assertThat(node.get("code").asInt()).isEqualTo(ErrorCode.USER_EXIST.getVal());
//                    else if (!d[1].equals(magicVal))
//                        assertThat(node.get("code").asInt()).isEqualTo(ErrorCode.CAPTCHA_ERROR.getVal());
//                    else {
//                        assertThat(node.get("code").asInt()).isEqualTo(0);
//                        node = node.get("result");
//
//                        Set<String> txtKeyList = new HashSet<>();
//                        txtKeyList.add("tel");
//                        assertThat(node.get("dialCode").asInt()).isNotEqualTo(0);
//                        txtKeyList.addAll(Arrays.asList("gender", "signature", "avatar"));
//                        assertText(node, true, txtKeyList.toArray(new String[txtKeyList.size()]));
//                        assertText(node, false, new String[]{"id", "easemobUser", "nickName"});
//                    }
//                }
//            }
//        });
//    }
//
//    @Test
//    public void contactRequestCheck() {
//        running(app, new Runnable() {
//            @Override
//            public void run() {
//                HandlerRef<?> handler = routes.ref.UserCtrl.requestAddContact();
//                FakeRequest req = fakeRequest(routes.UserCtrl.matchAddressBook());
//                req.withJsonBody(Json.parse("{ \"userId\": 100009, \"message\": \"加一下吧\" }"));
//                Long selfId = 100027L;
//                req.withHeader("UserId", selfId.toString());
//
//                getResultNode(handler, req);
//            }
//        });
//    }
//
//
//    /**
//     * 测试修改账户信息的功能
//     */
//    @Test
//    public void editCheck() {
//        running(app, new Runnable() {
//            @Override
//            public void run() {
//                String timeString = new SimpleDateFormat().format(new Date());
//                List<String> fields = Arrays.asList("nickName", "signature");
//
//                Long targetId = 100027L;
//                for (String f : fields) {
//                    for (Long selfId : new Long[]{0L, targetId}) {
//                        HandlerRef<?> handler = routes.ref.UserCtrl.editorUserInfo(targetId);
//                        FakeRequest req = fakeRequest(routes.UserCtrl.editorUserInfo(targetId));
//                        req.withJsonBody(Json.parse(String.format("{\"%s\": \"%s\"}", f, timeString +
//                                String.format("%d", new Random().nextInt(Integer.MAX_VALUE)))));
//                        req.withHeader("UserId", selfId.toString());
//
//                        if (selfId == 0) {
//                            Result result = callAction(handler, req);
//                            JsonNode node = Json.parse(contentAsString(result));
//                            assertThat(node.get("code").asInt()).isEqualTo(ErrorCode.USER_NOT_EXIST.getVal());
//                        } else {
//                            getResultNode(handler, req);
//                        }
//                    }
//                }
//            }
//        });
//    }
//}
