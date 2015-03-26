package controllers.mom;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by Heaven on 2015/3/26.
 */
public class SimpleTask implements Task {

    private String taskId = null;
    private String taskName = null;
    private Object[] taskArgs = null;

    public SimpleTask(String taskId, String taskName, Object[] taskArgs) {
        this.taskId = taskId;
        this.taskName = taskName;
        this.taskArgs = taskArgs;
    }

    public static SimpleTask newTask(String taskName, Object[] args) {
        String id = taskName + "-" + randomStr(32);
        return new SimpleTask(id, taskName, args);
    }

    private static String randomStr(int length) {
        StringBuilder builder = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            builder.append((char) (ThreadLocalRandom.current().nextInt(33, 128)));
        }
        return builder.toString();
    }

    @Override
    public String getTaskId() {
        return taskId;
    }

    @Override
    public String getTaskName() {
        return taskName;
    }

    @Override
    public Object[] getTaskArgs() {
        return taskArgs;
    }

    @Override
    public byte[] toJsonBytes() throws JSONException {
        // TODO 此处可优化为直接的字符串拼接
        JSONObject json = new JSONObject();
        json.put("id", taskId);
        json.put("task", taskName);
        JSONArray args = new JSONArray();
        for (Object arg : taskArgs) {
            args.put(arg);
        }
        json.put("args", args);

        return json.toString().getBytes();
    }
}
