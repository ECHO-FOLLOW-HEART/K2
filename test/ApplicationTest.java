import com.fasterxml.jackson.databind.JsonNode;
import controllers.MiscCtrl;
import controllers.POICtrl;
import controllers.PlanCtrl;
import exception.AizouException;
import org.junit.Test;
import play.test.WithApplication;

import java.lang.reflect.Method;

import static org.fest.assertions.Assertions.assertThat;


/**
 * Simple (JUnit) tests that can call all parts of a play app.
 * If you are interested in mocking a whole application, see the wiki for more details.
 */
public class ApplicationTest extends WithApplication {

//    @Test
//    public void simpleCheck() {
//        int a = 1 + 1;
//        assertThat(a).isEqualTo(2);
//    }
//
//    @Test
//    public void renderTemplate() {
//        Content html = views.html.index.render("Your new application is ready.");
//        assertThat(contentType(html)).isEqualTo("text/html");
//        assertThat(contentAsString(html)).contains("Your new application is ready.");
//    }

    @Test
    public void exploreLocCheck() {
        Runnable r = new Runnable() {

            @Override
            public void run() {
                try {
                    testHelper(true);
                    testHelper(false);
                } catch (AizouException e) {
                    assertThat(false);
                }
            }

            private void testHelper(boolean abroad) throws AizouException {
                JsonNode result = MiscCtrl.exploreImpl(false, true, false, false, false, abroad, 0, 200);
                for (JsonNode loc : result.get("loc")) {
                    for (String key : new String[]{"id", "zhName", "enName", "desc", "_id", "name", "fullName"}) {
                        JsonNode value = loc.get(key);
                        assertThat(!value.isNull() && value.asText() != null);
                    }
                    for (String key : new String[]{"tags", "relVs", "imageList"}) {
                        JsonNode value = loc.get(key);
                        assertThat(value != null && !value.isNull() && value.isArray());
                    }
                    double lat = loc.get("lat").asDouble();
                    assertThat(lat != 0 && Math.abs(lat) < 90);
                    double lng = loc.get("lng").asDouble();
                    assertThat(lng != 0 && Math.abs(lng) < 180);
                    assertThat(loc.get("abroad").asBoolean() == abroad);
                }
            }
        };

        r.run();
    }

    @Test
    public void getSuggestionsCheck() {
        Runnable r = new Runnable() {

            @Override
            public void run() {
                try {
                    testHelper();
                } catch (AizouException e) {
                    assertThat(false);
                }
            }

            private void parseLoc(JsonNode locNode) {
                for (String key : new String[]{"id", "zhName", "enName", "_id", "name", "fullName"}) {
                    JsonNode value = locNode.get(key);
                    assertThat(!value.isNull() && value.asText() != null);
                }
            }

            private void parsePoi(JsonNode poi) {
                JsonNode value = poi.get("name");
                assertThat(!value.isNull() && value.asText() != null);
                double ranking = poi.get("ratings").get("ranking").asDouble();
                assertThat(ranking >= 0 && ranking <= 1);
            }

            private void testHelper() throws AizouException {
                JsonNode result = MiscCtrl.getSuggestionsImpl("北京", true, true, true, true, 10);
                for (JsonNode loc : result.get("loc")) {
                    parseLoc(loc);
                    parseLoc(loc.get("parent"));
                }

                for (JsonNode vs : result.get("vs")) {
                    parsePoi(vs);
                    JsonNode cost = vs.get("timeCost");
                    assertThat(!cost.isNull() && cost.asText() != null);
                }

                for (JsonNode hotel : result.get("hotel")) {
                    parsePoi(hotel);
                    double price = hotel.get("price").asDouble();
                    assertThat(price >= 0);
                }
            }
        };

        r.run();
    }

    @Test
    public void getUgcByIdCheck() {
        Runnable r = new Runnable() {

            @Override
            public void run() {
                try {
                    testHelper();
                } catch (AizouException e) {
                    assertThat(false);
                }
            }

            private void parseTarget(JsonNode node) {
                for (String key : new String[]{"_id"})
                    assertThat(node.get(key).asText().trim().isEmpty()).isFalse();
            }

            private void testHelper() throws AizouException {
                JsonNode result = PlanCtrl.getUgcPlanByIdImpl("547f3714e4b0fda55223d7c4");

                for (String key : new String[]{"_id", "title", "moreDesc", "uid",
                        "templateId", "startDate", "endDate"}) {
                    JsonNode textNode = result.get(key);
                    if (textNode == null)
                        assertThat(false).isTrue();
                    else {
                        assertThat(textNode.isNull()).isFalse();
                        assertThat(textNode.isTextual()).isTrue();
                        assertThat(textNode.asText().trim().isEmpty()).isFalse();
                    }
                }

                for (String key : new String[]{"authorName", "authorAvatar",}) {
                    JsonNode textNode = result.get(key);
                    if (textNode == null)
                        assertThat(false).isTrue();
                    else {
                        assertThat(textNode.isNull()).isFalse();
                        assertThat(textNode.isTextual()).isTrue();
                    }
                }

                for (String key : new String[]{"stayBudget", "trafficBudget", "viewBudget", "forkedCnt"}) {
                    assertThat(result.get(key).asInt()).isGreaterThanOrEqualTo(0);
                }
                for (String key : new String[]{"days", "vsCnt", "updateTime"})
                    assertThat(result.get(key).asInt()).isGreaterThan(0);

                for (String key : new String[]{"tags", "imageList", "lxpTag", "summary"}) {
                    JsonNode listNode = result.get(key);
                    if (listNode == null)
                        assertThat(false).isTrue();
                    else {
                        assertThat(listNode.isNull()).isFalse();
                        assertThat(listNode.isArray()).isTrue();
                        assertThat(listNode.size()).isGreaterThanOrEqualTo(0);
                        for (JsonNode eleNode : listNode) {
                            if (eleNode == null)
                                assertThat(false).isTrue();
                            else {
                                assertThat(eleNode.isNull()).isFalse();
                                assertThat(eleNode.isTextual()).isTrue();
                                assertThat(eleNode.asText().trim().isEmpty()).isFalse();
                            }
                        }
                    }
                }

                for (JsonNode node : result.get("targets"))
                    parseTarget(node);

                JsonNode details = result.get("details");
                assertThat(details.size()).isGreaterThan(0);
                for (JsonNode detailEntry : details) {
                    assertThat(detailEntry.get("date").asText().trim().isEmpty()).isFalse();
                    JsonNode actv = detailEntry.get("actv");
                    assertThat(actv.size()).isGreaterThan(0);
                    for (JsonNode actvEntry : actv) {
                        for (String key : new String[]{"itemId", "itemName", "type", "subType", "ts"}) {
                            JsonNode node = actvEntry.get(key);
                            if (node == null)
                                assertThat(false).isTrue();
                            else {
                                assertThat(node.isTextual()).isTrue();
                                assertThat(node.asText() != null).isTrue();
                            }
                        }
                    }
                }
            }
        };

        r.run();
    }

    @Test
    public void poiSearchByLocCheck() {
        Runnable r = new Runnable() {

            @Override
            public void run() {
                try {
                    testHelper();
                } catch (AizouException e) {
                    assertThat(false);
                }
            }

            private void testHelper() throws AizouException {
                JsonNode result = POICtrl.poiSearchImpl("vs", "546f2da8b8ce0440eddb2870", "", "", 0, 20, "", "asc", "");

                for (JsonNode poi : result) {
                    for (String key : new String[]{"_id", "name", "desc"}) {
                        assertThat(poi.get(key).isTextual());
                        assertThat(poi.get(key).asText() != null);
                    }

                    JsonNode imageListNode = poi.get("imageList");
                    assertThat(imageListNode.isArray()).isTrue();
                    assertThat(imageListNode.size()).isGreaterThan(0);
                    for (JsonNode ele : imageListNode)
                        assertThat(ele.isTextual());
                }
            }
        };

        r.run();
    }

    @Test
    public void homeImageCheck() throws ReflectiveOperationException {
        Method method = MiscCtrl.class.getDeclaredMethod("appHomeImageImpl", int.class, int.class, int.class,
                String.class, int.class);
        method.setAccessible(true);
        JsonNode ret = (JsonNode) method.invoke(MiscCtrl.class, 1024, 480, 85, "jpg", 1);
        JsonNode imageNode = ret.get("image");
        if (imageNode == null)
            assertThat(true).isFalse();
        else {
            assertThat(imageNode.isNull()).isFalse();
            assertThat(imageNode.isTextual()).isTrue();
            assertThat(imageNode.asText().trim().isEmpty()).isFalse();
        }
    }
}
