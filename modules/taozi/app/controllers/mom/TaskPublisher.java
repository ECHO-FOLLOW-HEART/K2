package controllers.mom;

import com.rabbitmq.client.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import play.Configuration;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

/**
 * Created by Heaven on 2015/3/27.
 */
public class TaskPublisher implements Publisher {
    public static String DEFAULT_ROUTING = "taozi.default.routing";

    private Channel channel = null;
    private String exchangeName = null;
    private AMQP.BasicProperties properties = null;

    private ResultCollector collector = null;

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
            logger.info("task publishMessage: " + exchangeName + " " + routingKey + " " + task.getTaskId() + " " + task.getTaskName());

        } catch (Exception e) {
            logger.error("error curried while publishing message {exchangeName=" + exchangeName + ",routingKey=" + routingKey + "}");
        }
    }

    public void publishTask(Task task) {
        publishTask(task, DEFAULT_ROUTING);
    }

    public ResultCollector publishWithResult(Task task, String routingKey) {
        this.collector = new ResultCollector(task);
        publishTask(task, routingKey);
        return this.collector;
    }

    public void close() {
        try {
            if (channel != null)
                channel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ResultCollector getCollector() {
        return collector;
    }


    /**
     * 负责回收result
     */
    public class ResultCollector {

        private static final String EXCHANGE_NAME = "celeryresults";
        private static final boolean DRUBLE = true;
        private static final boolean EXCLUSIVE = false;
        private static final boolean DISABLE_AUTO_DELETE = true;
        private static final java.lang.String EXCHANGE_TYPE = "direct";
        private static final boolean AUTO_ACK = true;

        private Task task = null;
        private QueueingConsumer consumer = null;
        private QueueingConsumer.Delivery delivery;
        private Channel channel = null;
        private JSONObject jsonObject = null;


        private ResultCollector(Task task){
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
            } catch (Exception e) {
                logger.error("error curried init " + this.getClass().getSimpleName());
            }
        }

        /**
         * 返回Celery的result的byte[]数据,然后close channel
         * 注：一个ResultCollector应该只负责一个taskid的result收集, 因为Celery的amqp result回收机制比较蛋疼。。。
         * @return
         */
        public byte[] get() throws InterruptedException, IOException {
            if (consumer == null) {
                logger.error(this.getClass().getSimpleName() + " can not get anything because of init error");
                return "{}".getBytes();
            }

            delivery = consumer.nextDelivery();
            channel.close();
            return delivery.getBody();
        }

        public byte[] get(long timeout) throws TimeoutException {
            final byte[][] ret = {null};
            Thread gettingThread = new Thread(){
                @Override
                public void run() {
                    try {
                        ret[0] = get();
                    } catch (InterruptedException | IOException ignored) {
                    }
                }
            };

            try {
                gettingThread.start();
                while (gettingThread.isAlive() && timeout > 0) {
                    Thread.sleep(1);
                    timeout--;
                }
                if (gettingThread.isAlive())
                    throw new TimeoutException("timeout while getting result");
            } catch (InterruptedException ignored) {
            }
            return ret[0];
        }

        public JSONObject getJsonResult() throws TimeoutException {
            if (jsonObject != null) {
                return jsonObject;
            }
            try {
                jsonObject = new JSONObject(new String(this.get(3000)));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return jsonObject;
        }

        public Object getResult() throws TimeoutException {
            try {
                return getJsonResult().get("result");
            } catch (JSONException ignored) {
            }
            return null;
        }

    }
}
