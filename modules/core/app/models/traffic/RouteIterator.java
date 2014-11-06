package models.traffic;

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

        Calendar baseCal = Calendar.getInstance();
        baseCal.setTime(baseDate);

        Calendar depCal = Calendar.getInstance();
        depCal.setTime(route.depTime);
        Calendar arrCal = Calendar.getInstance();
        arrCal.setTime(route.arrTime);
        long dt = arrCal.getTimeInMillis() - depCal.getTimeInMillis();

        for (int k : new int[]{Calendar.YEAR, Calendar.MONTH, Calendar.DAY_OF_MONTH})
            depCal.set(k, baseCal.get(k));
        arrCal.setTimeInMillis(depCal.getTimeInMillis() + dt);

        route.depTime = depCal.getTime();
        route.arrTime = arrCal.getTime();

        return route;
    }

    @Override
    public void remove() {
        innerIter.remove();
    }
}
