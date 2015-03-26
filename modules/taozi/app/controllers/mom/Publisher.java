package controllers.mom;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;

import java.io.IOException;

/**
 * Created by Heaven on 2015/3/26.
 */
public class Publisher {
    private Channel channel = null;
    private String exchangeName = null;
    private AMQP.BasicProperties properties = null;

    private Log logger = LogFactory.getLog(this.getClass());

    public Publisher(String exchangeName, Channel channel) {
        this.exchangeName = exchangeName;
        this.channel = channel;
        this.properties = new AMQP.BasicProperties.Builder()
                .contentEncoding("utf-8")
                .contentType("application/json")
                .build();
    }

    public void publishTask(Task task, String routingKey) {
        try {
            channel.basicPublish(exchangeName, routingKey, properties, task.toJsonBytes());
            logger.info("task publish: " + exchangeName + " " + routingKey + " " + task.getTaskId() + " " + task.getTaskName());
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }

    public void publishTask(Task task) {
        publishTask(task, "taozi.default.routing");
    }

    public void close() {
        try {
            if (channel != null)channel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
