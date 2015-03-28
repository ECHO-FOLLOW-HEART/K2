package controllers.mom;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;
import org.json.JSONException;
import org.json.JSONObject;
import play.Configuration;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Heaven on 2015/3/28.
 */
public class ResultCollector {

    private static final String EXCHANGE_NAME = "celeryresult";
    private static final boolean DRUBLE = true;
    private static final boolean EXCLUSIVE = false;
    private static final boolean DISABLE_AUTO_DELETE = true;
    private static final java.lang.String EXCHANGE_TYPE = "direct";
    private static final boolean AUTO_ACK = true;

    private Task task = null;
    private QueueingConsumer consumer = null;
    private QueueingConsumer.Delivery delivery;
    private Channel channel = null;
    private JSONObject jsonObject;


    public ResultCollector(Task task){
        this.task = task;

        // 读取配置信息
        Configuration config = Configuration.root().getConfig("mom");
        String host = config.getString("host", "localhost");
        int port = config.getInt("port", 5672);
        int expires = config.getInt("expires", 86400000);

        try {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost(host);
            factory.setPort(port);
            Connection connection = factory.newConnection();

            channel = connection.createChannel();
            channel.exchangeDeclare(EXCHANGE_NAME, EXCHANGE_TYPE, DRUBLE);
            Map<String, Object> map = new HashMap<>();
            map.put("x-expires", expires);
            channel.queueDeclare(this.task.getTaskId(), DRUBLE, EXCLUSIVE, DISABLE_AUTO_DELETE, map);

            consumer = new QueueingConsumer(channel);
            channel.basicConsume(this.task.getTaskId(), AUTO_ACK, consumer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 返回Celery的result的byte[]数据,然后close channel
     * 注：一个ResultCollector应该只负责一个taskid的result收集, 因为Celery的amqp result回收机制比较蛋疼。。。
     * @return
     */
    public byte[] get() throws InterruptedException, IOException {
        delivery = consumer.nextDelivery();
        channel.close();
        return delivery.getBody();
    }

    public JSONObject getJsonResult() throws IOException, InterruptedException {
        if (jsonObject != null) {
            return jsonObject;
        }
        jsonObject = new JSONObject(this.get());
        return jsonObject;
    }

    public Object getResult() throws IOException, InterruptedException {
        try {
            return getJsonResult().get("result");
        } catch (JSONException ignored) {
        }
        return null;
    }

}
