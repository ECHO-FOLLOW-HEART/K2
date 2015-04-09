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
public class TaskPublisher implements Publisher<Task> {
    public static String DEFAULT_ROUTING = "taozi.default.routing";

    private Channel channel = null;
    private String exchangeName = null;
    private AMQP.BasicProperties properties = null;

    private Log logger = LogFactory.getLog(this.getClass());

    public TaskPublisher(String exchangeName, Channel channel) {
        this.exchangeName = exchangeName;
        this.channel = channel;

        // 任务的默认过期时间为1天
        Configuration config = Configuration.root().getConfig("mom");
        int defaultExpiration = config.getInt("messageExpiration", 24 * 3600 * 1000);

        this.properties = new AMQP.BasicProperties.Builder()
                .contentEncoding("utf-8")
                .contentType("application/json")
                .expiration(defaultExpiration + "")
                .build();
    }

    @Override
    public void publish(Task msg, String routingKey) {
        publishTask(msg, routingKey);
    }

    public void publish(Task task) {
        publishTask(task, DEFAULT_ROUTING);
    }

    public void publish(Task task, String routingKey, int expiration) {
        // 消息属性拼接
        AMQP.BasicProperties basicProperties = new AMQP.BasicProperties.Builder()
                .contentEncoding("utf-8")
                .contentType("application/json")
                .expiration(expiration + "")
                .build();

        try {
            channel.basicPublish(exchangeName, routingKey, basicProperties, task.toBytes());
            logger.info("task publishMessage: " + exchangeName + " " + routingKey + " " + task.getTaskId() +
                    " " + task.getTaskName() + " " + expiration);
        } catch (Exception e) {
            logger.error("error curried while publishing message {exchangeName=" + exchangeName + ",routingKey="
                    + routingKey + ",expiration=" + expiration + "}");
        }
    }

    /**
     * 向celery发布任务，但是不关心任务执行的结果
     * @param task 需要发布的任务
     * @param routingKey 任务对应的路由信息，用于worker的识别和筛选
     */
    private void publishTask(Task task, String routingKey) {
        try {
            channel.basicPublish(exchangeName, routingKey, properties, task.toBytes());
            logger.info("task publishMessage: " + exchangeName + " " + routingKey + " " + task.getTaskId() + " "
                    + task.getTaskName());

        } catch (Exception e) {
            logger.error("error curried while publishing message {exchangeName=" + exchangeName + ",routingKey="
                    + routingKey + "}");
        }
    }

    /**
     * 向celery发布任务，并希望获取任务的结果
     * @param task 需要发布的任务
     * @param routingKey 任务对应的路由信息，用于worker的识别和筛选
     * @return 返回对应的ResultCollector，用于在一段时间后收集结果
     */
    public ResultCollector publishWithResult(Task task, String routingKey) {
        ResultCollector collector = new ResultCollector(task);
        publishTask(task, routingKey);
        return collector;
    }

    public void close() {
        try {
            if (channel != null)
                channel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 负责从celery的worker回收result
     */
    public class ResultCollector {

        private static final String EXCHANGE_NAME = "celeryresults";
        private static final boolean DRUBLE = true;
        private static final boolean EXCLUSIVE = false;
        private static final boolean AUTO_DELETE = true;
        private static final String EXCHANGE_TYPE = "direct";
        private static final boolean AUTO_ACK = true;

        private Task task = null;
        private QueueingConsumer consumer = null;
        private Channel channel = null;

        private ResultCollector(Task task){
            this.task = task;

            // 读取配置信息
            Configuration config = Configuration.root().getConfig("mom");
            String host = config.getString("host", "localhost");
            int port = config.getInt("port", 5672);
            int expires = config.getInt("expires", 86400000);

            try {
                //建立与broker的连接
                ConnectionFactory factory = new ConnectionFactory();
                factory.setHost(host);
                factory.setPort(port);
                Connection connection = factory.newConnection();

                //创建通道
                channel = connection.createChannel();
                channel.exchangeDeclare(EXCHANGE_NAME, EXCHANGE_TYPE, DRUBLE);
                Map<String, Object> map = new HashMap<>();
                map.put("x-expires", expires);
                channel.queueDeclare(this.task.getTaskId(), DRUBLE, EXCLUSIVE, AUTO_DELETE, map);

                consumer = new QueueingConsumer(channel);
                channel.basicConsume(this.task.getTaskId(), AUTO_ACK, consumer);
            } catch (Exception e) {
                logger.error("error curried init " + this.getClass().getSimpleName());
            }
        }

        /**
         * 返回Celery的result的byte[]数据
         * 注：一个ResultCollector应该只负责一个taskid的result收集
         * @return
         */
        public byte[] get(){
            if (consumer == null) {
                logger.error(this.getClass().getSimpleName() + " can not get anything because of init error");
                return "{}".getBytes();
            }

            try {
                return consumer.nextDelivery().getBody();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return new byte[0];
        }

        /**
         * 从celery的worker获取具体的byte[]信息
         * @param timeout 超时，单位为毫秒
         * @return 返回信息的byte数组
         * @throws TimeoutException
         */
        public byte[] get(long timeout) throws TimeoutException {
            if (consumer == null) {
                logger.error(this.getClass().getSimpleName() + " can not get anything because of init error");
                return "{}".getBytes();
            }
            try {
                return consumer.nextDelivery(timeout).getBody();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (NullPointerException e) {
                throw new TimeoutException();
            }
            return new byte[0];
        }

        /**
         * 从celery的worker中获取对应的json格式的信息
         * @param timeout 超时时间，单位为毫秒
         * @return 对应的Json对象
         * @throws TimeoutException
         */
        public JSONObject getJsonResult(long timeout) throws TimeoutException {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject = new JSONObject(new String(this.get(timeout)));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return jsonObject;
        }

        /**
         * 获取任务的结果
         * 该函数先调用`getJsonResult()`函数获取Json对象，然后从其中获取对应的result
         * @param timeout 超时时间，单位为毫秒
         * @return 返回Object像，具体类型由实际调用的任务来决定
         * @throws TimeoutException
         */
        public Object getTaskResult(long timeout) throws TimeoutException {
            try {
                return getJsonResult(timeout).get("result");
            } catch (JSONException ignored) {
            }
            return null;
        }

    }
}
