package utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zephyre on 7/2/14.
 */
public class FPUtils {
    public static <FromType, ToType> List<ToType> map(ArrayList<FromType> list, MapDelegate<FromType, ToType> mapDelegate) {
        List<ToType> retval = new ArrayList<>(list.size());
        for (FromType item : list)
            retval.add(mapDelegate.map(item));
        return retval;
    }

    public static <T> List<T> filter(List<T> list, FilterDelegate<T> filterDelegate) {
        List<T> retval = new ArrayList<>(list.size());
        for (T item : list)
            if (filterDelegate.filter(item))
                retval.add(item);

        return retval;
    }
}
