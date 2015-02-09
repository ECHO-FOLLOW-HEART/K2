package formatter.taozi.geo;

import formatter.AizouFormatter;
import formatter.taozi.ImageItemSerializer;
import models.AizouBaseEntity;
import models.geo.Country;
import models.misc.ImageItem;

import java.util.Arrays;

/**
 * Created by zephyre on 1/20/15.
 */
public class SimpleCountryFormatter extends AizouFormatter<Country> {

    public void setImageWidth(int maxWidth) {
        imageItemSerializer.setWidth(maxWidth);
    }

    private ImageItemSerializer imageItemSerializer;

    public SimpleCountryFormatter() {
        registerSerializer(Country.class, new SimpleCountrySerializer());

        imageItemSerializer = new ImageItemSerializer();
        registerSerializer(ImageItem.class, imageItemSerializer);

        initObjectMapper(null);

        filteredFields.addAll(Arrays.asList(AizouBaseEntity.FD_ID, Country.FD_ZH_NAME, Country.FD_EN_NAME,
                Country.fnDesc, Country.fnCode, Country.fnImages));
    }
}
