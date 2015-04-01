package controllers.mom;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;

/**
 * Created by Heaven on 2015/3/26.
 */
public class MessagePublisher implements Publisher {
    public static String DEFAULT_ROUTING = "taozi.default.routing";

    private Channel channel = null;
    private String exchangeName = null;
    private AMQP.BasicProperties properties = null;

    private Log logger = LogFactory.getLog(this.getClass());

    public MessagePublisher(String exchangeName, Channel channel) {
        this.exchangeName = exchangeName;
        this.channel = channel;
        this.properties = new AMQP.BasicProperties.Builder()
                .contentEncoding("utf-8")
                .contentType("application/json")
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
            logger.error("error curried while publishing message {exchangeName=" + exchangeName + ",routingKey=" + routingKey + "}");
        }
    }
}
