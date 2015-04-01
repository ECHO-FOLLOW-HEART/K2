package controllers.mom;

/**
 * Created by Heaven on 2015/3/27.
 */
public interface Publisher <T extends Message> {
    public void publish(T msg, String routingKey);
}
