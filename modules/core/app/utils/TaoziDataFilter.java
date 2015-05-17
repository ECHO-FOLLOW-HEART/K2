package utils;

import models.geo.Locality;
import models.misc.ImageItem;
import models.poi.AbstractPOI;
import models.poi.Restaurant;

import java.util.*;

/**
 * Created by topy on 2015/1/13.
 */
public class TaoziDataFilter {

    public static List<ImageItem> getOneImage(List<ImageItem> images) {

        if (images == null || images.isEmpty())
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

    public static String getZodiac(int zodiacType) {
        String result;
        switch (zodiacType) {
            case 1:
                result = "白羊座";
                break;
            case 2:
                result = "金牛座";
                break;
            case 3:
                result = "双子座";
                break;
            case 4:
                result = "巨蟹座";
                break;
            case 5:
                result = "狮子座";
                break;
            case 6:
                result = "处女座";
                break;
            case 7:
                result = "天秤座";
                break;
            case 8:
                result = "天蝎座";
                break;
            case 9:
                result = "射手座";
                break;
            case 10:
                result = "摩羯座";
                break;
            case 11:
                result = "水瓶座";
                break;
            case 12:
                result = "双鱼座";
                break;
            default:
                result = "";
                break;
        }
        return result;
    }

    public static int getZodiac(String zodiacDesc) {
        int result;
        switch (zodiacDesc) {
            case "白羊座":
                result = 1;
                break;
            case "金牛座":
                result = 2;
                break;
            case "双子座":
                result = 3;
                break;
            case "巨蟹座":
                result = 4;
                break;
            case "狮子座":
                result = 5;
                break;
            case "处女座":
                result = 6;
                break;
            case "天秤座":
                result = 7;
                break;
            case "天蝎座":
                result = 8;
                break;
            case "射手座":
                result = 9;
                break;
            case "摩羯座":
                result = 10;
                break;
            case "水瓶座":
                result = 11;
                break;
            case "双鱼座":
                result = 12;
                break;
            default:
                result = 0;
                break;
        }
        return result;
    }

    public static Map<String, List<Locality>> transLocalitiesByCountry(List<Locality> localities) {
        Map<String, List<Locality>> map = new HashMap<>();
        String country;
        List<Locality> locs;
        for (Locality loc : localities) {
            if (loc.getCountry() == null || loc.getCountry().getZhName() == null)
                country = "外国";
            else
                country = loc.getCountry().getZhName();
            if (map.get(country) == null)
                map.put(country, Arrays.asList(loc));
            else {
                locs = new ArrayList<>();
                locs.addAll(map.get(country));
                locs.add(loc);
                map.put(country, locs);
            }


        }
        return map;
    }
}
