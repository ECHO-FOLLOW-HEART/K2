package formatter.taozi.geo;

import formatter.AizouFormatter;
import models.AizouBaseEntity;
import models.geo.Locality;

import java.util.Arrays;

/**
 * Created by zephyre on 1/20/15.
 */
public class SimpleLocalityFormatter extends AizouFormatter<Locality> {
    public SimpleLocalityFormatter() {
        registerSerializer(Locality.class, new SimpleLocalitySerializer());
        initObjectMapper(null);

        filteredFields.addAll(Arrays.asList(AizouBaseEntity.FD_ID, Locality.FD_ZH_NAME,Locality.FD_EN_NAME,Locality.fnLocation));
    }
}
