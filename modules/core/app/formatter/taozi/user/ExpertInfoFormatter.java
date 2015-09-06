package formatter.taozi.user;

import formatter.AizouFormatter;
import formatter.taozi.geo.LocalitySerializer;
import models.AizouBaseEntity;
import models.geo.Locality;
import models.user.ExpertInfo;
import models.user.UserInfo;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * 序列化用户信息
 * <p/>
 * Created by zephyre on 1/20/15.
 */
public class ExpertInfoFormatter extends AizouFormatter<ExpertInfo> {

    private ExpertInfoSerializer serializer;

    public ExpertInfoFormatter() {
        serializer = new ExpertInfoSerializer();
        registerSerializer(ExpertInfo.class, serializer);
        initObjectMapper(null);
    }


}
