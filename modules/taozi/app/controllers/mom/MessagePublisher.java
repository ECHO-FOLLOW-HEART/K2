package controllers.mom;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import play.Configuration;

import java.io.IOException;

/**
 * Created by Heaven on 2015/3/26.
 */
public class MessagePublisher implements Publisher<Message> {
    public static String DEFAULT_ROUTING = "taozi.default.routing";

    private Channel channel = null;
    private String exchangeName = null;
    private AMQP.BasicProperties properties = null;

    private Log logger = LogFactory.getLog(this.getClass());

    public MessagePublisher(String exchangeName, Channel channel) {
        this.exchangeName = exchangeName;
        this.channel = channel;

        // 消息的默认过期时间为1天
        Configuration config = Configuration.root().getConfig("mom");
        int defaultExpiration = config.getInt("messageExpiration", 24 * 3600 * 1000);

        this.properties = new AMQP.BasicProperties.Builder()
                .contentEncoding("utf-8")
                .contentType("application/json")
                .expiration(defaultExpiration + "")
                .build();
    }


    public void publish(Message msg) {
        publish(msg, DEFAULT_ROUTING);
    }


    public void close() {
        try {
            if (channel != null) channel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void publish(Message msg, String routingKey) {
        try {
            channel.basicPublish(exchangeName, routingKey, properties, msg.toBytes());
            logger.info("msg publishMessage: " + exchangeName + " " + routingKey);
        } catch (Exception e) {
            logger.error("error curried while publishing message");
        }
    }

    public void publish(Message msg, String routingKey, int expiration) {
        // 拼装消息属性
        AMQP.BasicProperties basicProperties = new AMQP.BasicProperties.Builder()
                .contentEncoding("utf-8")
                .contentType("application/json")
                .expiration(expiration + "")
                .build();

        try {
            channel.basicPublish(exchangeName, routingKey, basicProperties, msg.toBytes());
            logger.info("msg publishMessage: " + exchangeName + " " + routingKey + " " + expiration);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
