import com.fasterxml.jackson.databind.JsonNode;
import controllers.MiscCtrl;
import controllers.POICtrl;
import controllers.PlanCtrl;
import exception.TravelPiException;
import org.junit.Test;
import play.test.WithApplication;

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
                } catch (TravelPiException e) {
                    assertThat(false);
                }
            }

            private void testHelper(boolean abroad) throws TravelPiException {
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
                } catch (TravelPiException e) {
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

            private void testHelper() throws TravelPiException {
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
                } catch (TravelPiException e) {
                    assertThat(false);
                }
            }

            private void parseLoc(JsonNode locNode) {
                for (String key : new String[]{"id", "zhName", "enName", "_id", "name", "fullName"}) {
                    JsonNode value = locNode.get(key);
                    assertThat(!value.isNull() && value.asText() != null);
                }
            }

            private void parseTarget(JsonNode node) {
                for (String key : new String[]{"_id", "name", "enName"})
                    assertThat(node.get(key).isTextual());
            }

            private void testHelper() throws TravelPiException {
                JsonNode result = PlanCtrl.getUgcPlanByIdImpl("547f3714e4b0fda55223d7c4");

                for (String key : new String[]{"_id", "title", "moreDesc", "authorName", "authorAvatar", "uid",
                        "templateId", "startDate", "endDate"}) {
                    assertThat(result.get(key).isTextual());
                }

                for (String key : new String[]{"days", "stayBudget", "trafficBudget", "viewBudget", "vsCnt",
                        "forkedCnt", "updateTime"}) {
                    assertThat(result.get(key).asInt() > 0);
                }

                for (String key : new String[]{"tags", "imageList", "lxpTag", "summary"}) {
                    assertThat(result.get(key).isTextual());
                }

                parseTarget(result.get("target"));
                for (JsonNode node : result.get("targets"))
                    parseTarget(node);
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
                } catch (TravelPiException e) {
                    assertThat(false);
                }
            }

            private void testHelper() throws TravelPiException {
                JsonNode result = POICtrl.poiSearchImpl("vs", "546f2da8b8ce0440eddb2870", "", "", 0, 20, "", "asc", "");

                for (JsonNode poi : result) {
                    for (String key : new String[]{"_id", "name", "desc", "timeCost"})
                        assertThat(poi.get(key).isTextual());

                    for (JsonNode ele : poi.get("imageList"))
                        assertThat(ele.isTextual());
                }
            }
        };

        r.run();
    }
}
