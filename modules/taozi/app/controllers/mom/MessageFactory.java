package controllers.mom;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import play.Configuration;

import java.io.IOException;

/**
 * Created by Heaven on 2015/3/26.
 */
public class MessageFactory {
    private static MessageFactory factory = null;

    private Connection connection = null;

    private String host = null;
    private int port = 0;
    private boolean durable = false;
    private String exchangeType = null;
    
    private MessageFactory() {
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

    public TaskPublisher getTaskPublisher(String exchangeName) {
        Channel channel = null;
        try {
            channel = connection.createChannel();
            // Celery 中对exchangeType  和 durable 都有限定，因此不使用默认配置
            channel.exchangeDeclare(exchangeName, "topic", true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new TaskPublisher(exchangeName, channel);
    }

    public static MessageFactory getInstance() {
        if (factory != null)
            return factory;

        synchronized ("factory") {
            if (factory != null)
                return factory;
            factory = new MessageFactory();
        }
        return factory;
    }

}
