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

    private String host = null;
    private int port = 0;
    private boolean durable = false;
    private String exchangeType = null;
    
    private PublisherFactory() {
        // 读取配置信息
        Configuration config = Configuration.root().getConfig("mom");
        host = config.getString("host", "localhost");
        port = config.getInt("port", 5672);
        durable = config.getBoolean("durable", false);
        exchangeType = config.getString("exchangeType", "topic");

        // 创建连接
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(host);
        factory.setPort(port);
        try {
            connection = factory.newConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Publisher getPublisher(String exchangeName, String exchangeType, boolean exchangeDurable) {
        Channel channel = null;
        try {
            channel = connection.createChannel();
            channel.exchangeDeclare(exchangeName, exchangeType, exchangeDurable);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new MessagePublisher(exchangeName, channel);
    }

    public MessagePublisher getMessagePublisher(String exchangeName) {
        Channel channel = null;
        try {
            channel = connection.createChannel();
            channel.exchangeDeclare(exchangeName, this.exchangeType, this.durable);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new MessagePublisher(exchangeName, channel);
    }

    public MessagePublisher getMessagePublisher() {
        return getMessagePublisher("taozi.default.exchange");
    }

    public SimpleTaskPublisher getSimpleTaskPublisher(String exchangeName) {
        Channel channel = null;
        try {
            channel = connection.createChannel();
            channel.exchangeDeclare(exchangeName, "topic", true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new SimpleTaskPublisher(exchangeName, channel);
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
