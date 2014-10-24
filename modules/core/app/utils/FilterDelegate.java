package utils;

/**
 * Created by zephyre on 7/2/14.
 */
public interface FilterDelegate<T> {
    boolean filter(T item);
}
