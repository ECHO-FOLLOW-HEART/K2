import com.fasterxml.jackson.databind.JsonNode;
import controllers.MiscCtrl;
import exception.TravelPiException;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;
import static play.test.Helpers.fakeApplication;
import static play.test.Helpers.running;

public class TravelPiTest {

    @Test
    public void exploreLocCheck() {
        running(fakeApplication(), new Runnable() {

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
                JsonNode result = MiscCtrl.exploreImpl(false, true, false, false, false, abroad, 0, 10);
                for (JsonNode loc : result.get("loc")) {
                    for (String key : new String[]{"id", "zhName", "enName", "desc", "_id", "name", "fullName"}) {
                        JsonNode value = loc.get(key);
                        assertThat(!value.isNull() && value.asText() != null);
                    }
                    for (String key : new String[]{"tags", "relVs", "imageList"}) {
                        JsonNode value = loc.get(key);
                        assertThat(value != null && !value.isNull() && value.isArray());
                    }
                    assertThat(loc.get("abroad").asBoolean() == abroad);
                }
            }
        });


    }

}