package formatter.travelpi.plan;

import com.fasterxml.jackson.databind.JsonNode;
import models.AizouBaseEntity;
import models.plan.UgcPlan;
import formatter.travelpi.TravelPiBaseFormatter;

import java.util.Arrays;
import java.util.HashSet;

/**
 * 路线的formatter
 *
 * @author Zephyre
 */
public class PlanFormatter extends TravelPiBaseFormatter {
    private static PlanFormatter instance;

    private PlanFormatter() {
        stringFields = new HashSet<>();
        stringFields.addAll(Arrays.asList(UgcPlan.FD_START_DATE, UgcPlan.FD_END_DATE, UgcPlan.FD_UID,
                UgcPlan.FD_TEMPLATE_ID));

        listFields = new HashSet<>();
    }

    public synchronized static PlanFormatter getInstance() {
        if (instance != null)
            return instance;
        else {
            instance = new PlanFormatter();
            return instance;
        }
    }

    @Override
    public JsonNode format(AizouBaseEntity item) {

        return null;
    }
}
