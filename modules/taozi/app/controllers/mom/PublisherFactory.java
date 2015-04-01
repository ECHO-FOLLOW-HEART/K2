package controllers.mom;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import play.Configuration;

import java.io.IOException;

/**
 * Created by Heaven on 2015/3/26.
 */
public class PublisherFactory {
    private static PublisherFactory factory = null;

    Log logger = LogFactory.getLog(this.getClass());

    private Connection connection = null;

    private PublisherFactory() {
        // 读取配置信息
        Configuration config = Configuration.root().getConfig("mom");
        String host = config.getString("host", "localhost");
        int port = config.getInt("port", 5672);

        // 创建连接
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(host);
        factory.setPort(port);
        try {
            connection = factory.newConnection();
        } catch (IOException e) {
            logger.error(this.getClass().getSimpleName() + " can not connect to " + host + ":" + port);
        }
    }

    public MessagePublisher getMessagePublisher(String exchangeName) {
        return getMessagePublisher(exchangeName, "topic", false);
    }

    public MessagePublisher getMessagePublisher(String exchangeName, String exchangeType, boolean durable) {
        Channel channel = null;
        try {
            channel = connection.createChannel();
            channel.exchangeDeclare(exchangeName, exchangeType, durable);
        } catch (Exception e) {
            logger.error("error curried when creating MessagePublisher");
            resetFactory();
        }
        return new MessagePublisher(exchangeName, channel);
    }

    public TaskPublisher getTaskPublisher(String exchangeName) {
        return getTaskPublisher(exchangeName, "direct", true);
    }

    public TaskPublisher getTaskPublisher(String exchangeName, String exchangeType, boolean durable) {
        Channel channel = null;
        try {
            channel = connection.createChannel();
            // Celery 中对exchangeType 和 default_durable 都有限定，因此不使用默认配置
            channel.exchangeDeclare(exchangeName, exchangeType, durable);
        } catch (Exception e) {
            logger.error("error curried when creating TaskPublisher");
            resetFactory();
        }
        return new TaskPublisher(exchangeName, channel);
    }

    private static void resetFactory() {
        factory.connection = null;
        factory = null;
    }

    public static PublisherFactory getInstance() {
        if (factory != null && factory.connection != null)
            return factory;

        synchronized ("factory") {
            if (factory != null && factory.connection != null)
                return factory;
            factory = new PublisherFactory();
        }
        return factory;
    }

}
