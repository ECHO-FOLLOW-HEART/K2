package models.morphia.traffic;

import java.lang.reflect.Field;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;

/**
 * 交通路线的迭代器。
 *
 * @author Zephyre
 */
public class RouteIterator implements Iterator {

    private final Date baseDate;
    private Iterator<? extends AbstractRoute> innerIter;

    private RouteIterator(Iterator<? extends AbstractRoute> it, Date date) {
        innerIter = it;
        baseDate = date;
    }

    public static RouteIterator getInstance(Iterator<? extends AbstractRoute> it, Date date) {
        return new RouteIterator(it, date);
    }

    @Override
    public boolean hasNext() {
        return innerIter.hasNext();
    }

    @Override
    public AbstractRoute next() {
        AbstractRoute route = innerIter.next();
        for (String k : new String[]{"depTime", "arrTime"}) {
            try {
                Field field = AbstractRoute.class.getField(k);
                Date time = (Date) field.get(route);
                if (time == null)
                    continue;
                Calendar cal = Calendar.getInstance();
                cal.setTime(time);
                Calendar baseCal = Calendar.getInstance();
                baseCal.setTime(baseDate);

                cal.set(Calendar.YEAR, baseCal.get(Calendar.YEAR));
                cal.set(Calendar.MONTH, baseCal.get(Calendar.MONTH));
                cal.set(Calendar.DAY_OF_YEAR, baseCal.get(Calendar.DAY_OF_YEAR));
                field.set(route, cal.getTime());
            } catch (IllegalAccessException | NoSuchFieldException ignored) {
            }
        }
        return route;
    }

    @Override
    public void remove() {
        innerIter.remove();
    }
}
