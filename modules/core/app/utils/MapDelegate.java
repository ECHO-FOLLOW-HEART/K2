package utils;

/**
 * Created by zephyre on 7/2/14.
 */
public interface MapDelegate<FromType, ToType> {
    ToType map(FromType obj);
}
