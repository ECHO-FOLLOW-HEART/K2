package formatter.taozi.geo;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import formatter.AizouFormatter;
import formatter.AizouSerializer;
import models.AizouBaseEntity;
import models.geo.Continent;

import java.io.IOException;
import java.util.Arrays;

/**
 * Created by zephyre on 1/20/15.
 */
public class SimpleContinentFormatter extends AizouFormatter<Continent> {

    public SimpleContinentFormatter() {
        registerSerializer(Continent.class, new SimpleContinentSerializer());
        initObjectMapper(null);
        filteredFields.addAll(Arrays.asList(AizouBaseEntity.FD_ID, "zhName", "enName", "code"));
    }


}
