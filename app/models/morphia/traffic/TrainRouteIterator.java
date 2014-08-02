package models.morphia.traffic;

import models.morphia.misc.SimpleRef;

import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import models.morphia.traffic.TrainRoute;
import models.morphia.traffic.AbstractRoute;
import models.morphia.traffic.RouteIterator;

/**
 * 交通路线-火车的迭代器。
 *
 * @author Rooben
 */
public class TrainRouteIterator  implements Iterator {

    private final Date baseDate;
    private Iterator<TrainRoute> innerIter;
    private String filterDepId;
    private String filteraArrId;

    private int index = 0;
    private int position = 0;

    private TrainRouteIterator(Iterator<TrainRoute> it, Date date,String depId,String arrId) {
        innerIter = it;
        baseDate = date;
        filterDepId = depId;
        filteraArrId = arrId;
    }

    public static TrainRouteIterator getInstance(Iterator<TrainRoute> it, Date date,String depId,String arrId) {

        return new TrainRouteIterator(it, date,depId,arrId);
    }

    public boolean hasNext() {

        return innerIter.hasNext();
    }

    public AbstractRoute next() {

        TrainRoute route = innerIter.next();
        while(!findNextValid( route)){
            if(hasNext() ){
                route = innerIter.next();
            }else{
                route.type = "EmptyFlag";
                return route;
            }

        }
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

    public void remove() {
        innerIter.remove();
    }

    private boolean findNextValid(TrainRoute route){

        // 按途经站过滤
        List<TrainEntry> detailsList = route.details;
        boolean hasDepId = false;
        boolean hasArrId = false;
        String stopId = null;
        String locId = null;
        if(null != detailsList && detailsList.size()!= 0){
            for(TrainEntry temp: detailsList){
                stopId = String.valueOf(temp.stop.id);
                locId =  String.valueOf(temp.loc.id);
                if(filterDepId.equals(stopId)||filterDepId.equals(locId)){
                    hasDepId = true;
                }
                if(filteraArrId.equals(stopId)||filteraArrId.equals(locId)){
                    hasArrId = true;
                }
                if(hasDepId&&hasArrId){
                    break;
                }
            }
        }

        return hasDepId&&hasArrId;
        // return true;

    }


}