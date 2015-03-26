package controllers.mom;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import play.Configuration;

import java.io.IOException;

/**
 * Created by Heaven on 2015/3/26.
 */
public class PublisherFactory {
    private static PublisherFactory factory = null;

    private Connection connection = null;

    private PublisherFactory() {
        Configuration config = Configuration.root().getConfig("mom");
        String host = config.getString("host", "localhost");
        int port = config.getInt("port", 5672);

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(host);
        factory.setPort(port);
        try {
            connection = factory.newConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Publisher getPublisher(String exchangeName) {
        Channel channel = null;
        try {
            channel = connection.createChannel();
            channel.exchangeDeclare(exchangeName, "topic", true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new Publisher(exchangeName, channel);
    }

    public Publisher getPublisher() {
        return getPublisher("taozi.default.exchange");
    }

    public static PublisherFactory getInstance() {
        if (factory != null)
            return factory;

        synchronized ("factory") {
            if (factory != null)
                return factory;
            factory = new PublisherFactory();
        }
        return factory;
    }

}
