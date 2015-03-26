package controllers.mom;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import org.json.JSONException;

import java.io.IOException;

/**
 * Created by Heaven on 2015/3/26.
 */
public class Publisher {
    private Channel channel = null;
    private String exchangeName = null;
    private AMQP.BasicProperties properties = null;

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
