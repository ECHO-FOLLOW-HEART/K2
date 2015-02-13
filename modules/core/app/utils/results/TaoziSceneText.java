package utils.results;

import play.Configuration;

import java.util.HashMap;
import java.util.Map;


/**
 * 提供场景文案信息
 * <p/>
 * Created by zephyre on 2/13/15.
 */
public class TaoziSceneText implements TextProvider {
    private static TextProvider provider;
    private final Map<String, String> sceneMap;

    @SuppressWarnings("unchecked")
    private TaoziSceneText() {
        Map<String, String> m = (Map<String, String>) Configuration.root().asMap().get("scene");
        if (m == null)
            m = new HashMap<>();
        sceneMap = m;
    }

    public static TextProvider instance() {
        if (provider == null) {
            synchronized (TaoziSceneText.class) {
                if (provider == null) {
                    provider = new TaoziSceneText();
                }
            }
        }
        return provider;
    }

    /**
     * 获得场景描述文案
     */
    @Override
    public String text(SceneID scene) {
        String sceneText = sceneMap.get(scene.toString());
        return sceneText != null ? sceneText : "";
    }
}
