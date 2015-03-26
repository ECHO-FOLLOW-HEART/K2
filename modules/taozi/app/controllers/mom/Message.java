package controllers.mom;

import org.json.JSONException;

/**
 * Created by Heaven on 2015/3/26.
 */
public interface Message {
    public byte[] toJsonBytes() throws JSONException;
}
