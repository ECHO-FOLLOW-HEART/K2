package utils.results;

/**
 * 根据场景(SceneID)，提供相应的文案
 *
 * Created by zephyre on 2/13/15.
 */
public interface TextProvider {

    public String text(SceneID scene);
}
