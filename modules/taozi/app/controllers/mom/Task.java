package controllers.mom;

/**
 * Created by Heaven on 2015/3/26.
 */
public interface Task extends Message {

    public String getTaskId();

    public String getTaskName();

    public Object[] getTaskArgs();

}
