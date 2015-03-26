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

    /**
     * 获取制定exchange的publisher
     * @param exchangeName 对应的exchange名字，默认为taozi.default.exchange
     */
    public Publisher getPublisher(String exchangeName) {
        Channel channel = null;
        try {
            channel = connection.createChannel();
            channel.exchangeDeclare(exchangeName, exchangeType, durable);
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
