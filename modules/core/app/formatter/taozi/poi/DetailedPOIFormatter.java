package formatter.taozi.poi;


import formatter.AizouFormatter;
import formatter.taozi.GeoJsonPointSerializer;
import formatter.taozi.ImageItemSerializer;
import formatter.taozi.geo.LocalitySerializer;
import formatter.taozi.guide.ItinerItemSerializer;
import models.AizouBaseEntity;
import models.geo.GeoJsonPoint;
import models.geo.Locality;
import models.guide.ItinerItem;
import models.misc.ImageItem;
import models.poi.*;

import java.io.IOException;
import java.util.*;

/**
 * 返回用户的摘要（以列表形式获取用户信息时使用，比如获得好友列表，获得黑名单列表等）
 * <p>
 * Created by zephyre on 10/28/14.
 */
public class DetailedPOIFormatter<T extends AbstractPOI> extends AizouFormatter<AbstractPOI> {

    public Set<String> getFilteredFields(Class<? extends AbstractPOI> cls) {

        if (cls == ViewSpot.class) {
            String[] keyList = new String[]{
                    ViewSpot.FD_OPEN_TIME, ViewSpot.FD_TIME_COST_DESC, ViewSpot.FD_TRAVEL_MONTH,
                    ViewSpot.FD_TRAFFIC_URL, ViewSpot.FD_VISITGUIDE_URL, ViewSpot.FD_TIPS_URL
            };
            Collections.addAll(filteredFields, keyList);
        }
        return filteredFields;
    }

    public DetailedPOIFormatter(int imgWidth) {
        registerSerializer(AbstractPOI.class, new PolymorphicPOISerializer(PolymorphicPOISerializer.Level.DETAILED));
        registerSerializer(ImageItem.class, new ImageItemSerializer(imgWidth));
        registerSerializer(Locality.class, new LocalitySerializer());
        registerSerializer(ItinerItem.class, new ItinerItemSerializer());
        registerSerializer(GeoJsonPoint.class, new GeoJsonPointSerializer());

        initObjectMapper(null);

        filteredFields = new HashSet<>();
        Collections.addAll(filteredFields,
                AizouBaseEntity.FD_ID,
                AizouBaseEntity.FD_IS_FAVORITE,
                AbstractPOI.FD_ZH_NAME,
                AbstractPOI.FD_EN_NAME,
                AbstractPOI.FD_DESC,
                AbstractPOI.FD_IMAGES,
                AbstractPOI.FD_LOCATION,
                AbstractPOI.FD_RATING,
                AbstractPOI.FD_ADDRESS,
                AbstractPOI.FD_PRICE,
                AbstractPOI.FD_PRICE_DESC,
                AbstractPOI.FD_TIPS,
                AbstractPOI.FD_VISITGUIDE,
                AbstractPOI.FD_TRAFFICINFO,
                AbstractPOI.FD_RANK
        );
    }


}
