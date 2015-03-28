package controllers.mom;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;

import java.io.IOException;

/**
 * Created by Heaven on 2015/3/27.
 */
public class TaskPublisher implements Publisher {
    public static String DEFAULT_ROUTING = "taozi.default.routing";

    private Channel channel = null;
    private String exchangeName = null;
    private AMQP.BasicProperties properties = null;

    private Log logger = LogFactory.getLog(this.getClass());

    public TaskPublisher(String exchangeName, Channel channel) {
        this.exchangeName = exchangeName;
        this.channel = channel;
        this.properties = new AMQP.BasicProperties.Builder()
                .contentEncoding("utf-8")
                .contentType("application/json")
                .build();
    }

    @Override
    public void publish(Message msg, String routingKey) {
        if (!(msg instanceof Task)) {
            throw new IllegalArgumentException("A TaskPublisher can only publish Task");
        }
        publishTask((Task) msg, routingKey);
    }

    public void publishTask(Task task, String routingKey) {
        try {
            channel.basicPublish(exchangeName, routingKey, properties, task.toBytes());
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        logger.info("task publishMessage: " + exchangeName + " " + routingKey + " " + task.getTaskId() + " " + task.getTaskName());
    }

    public void publishTask(Task task) {
        publishTask(task, DEFAULT_ROUTING);
    }
}
