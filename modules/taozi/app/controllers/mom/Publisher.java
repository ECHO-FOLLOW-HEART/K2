package controllers.mom;

/**
 * Created by Heaven on 2015/3/27.
 */
public interface Publisher {
    public void publish(Message msg, String routingKey);
}
