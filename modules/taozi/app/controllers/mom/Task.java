package controllers.mom;

import org.json.JSONException;

/**
 * Created by Heaven on 2015/3/26.
 */
public interface Task {

    public String getTaskId();

    public String getTaskName();

    public Object[] getTaskArgs();

    public byte[] toJsonBytes() throws JSONException;
}
