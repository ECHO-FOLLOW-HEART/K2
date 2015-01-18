package utils;

import models.misc.ImageItem;
import models.poi.AbstractPOI;
import models.poi.Restaurant;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by topy on 2015/1/13.
 */
public class TaoziDataFilter {

    public static List<ImageItem> getOneImage(List<ImageItem> images) {

        if (images == null)
            return new ArrayList<>();
        else
            return Arrays.asList(images.get(0));
    }

    public static String getPriceDesc(AbstractPOI poi) {

        if (poi.priceDesc != null && (!poi.priceDesc.equals("")))
            return poi.priceDesc;
        else {
            return suffixPriceDesc(poi);
        }
    }

    public static String suffixPriceDesc(AbstractPOI poi) {
        if (poi.price == null) {
            return "";
        }
        int priceInt = (int) (poi.price.doubleValue());
        if (poi instanceof Restaurant)
            return "人均" + "￥" + priceInt;
        else
            return "￥" + priceInt;
    }
}
