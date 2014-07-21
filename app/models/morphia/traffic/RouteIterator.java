package models.morphia.traffic;

import java.util.Iterator;

/**
 * 交通路线的迭代器。
 *
 * @author Zephyre
 */
public class RouteIterator implements Iterator {

    private Iterator<? extends AbstractRoute> innerIter;

    private RouteIterator(Iterator<? extends AbstractRoute> it) {
        innerIter = it;
    }

    public static RouteIterator getInstance(Iterator<? extends AbstractRoute> it) {
        return new RouteIterator(it);
    }

    @Override
    public boolean hasNext() {
        return innerIter.hasNext();
    }

    @Override
    public AbstractRoute next() {
        return innerIter.next();
    }

    @Override
    public void remove() {
        innerIter.remove();
    }
}
