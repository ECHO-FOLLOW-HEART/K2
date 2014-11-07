package aizou.core;

import exception.TravelPiException;
import javassist.bytecode.Descriptor;
import models.MorphiaFactory;
import models.guide.Guide;
import models.poi.AbstractPOI;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;
import play.mvc.Result;

import java.util.Iterator;

/**
 * Created by topy on 2014/11/5.
 */
public class GuideAPI {

    /**
     *保存攻略标题
     * @param id
     * @param title
     * @throws TravelPiException
     */
    public static void saveGuideTitle(ObjectId id,String title) throws TravelPiException {
        Datastore ds=MorphiaFactory.getInstance().getDatastore(MorphiaFactory.DBType.GUIDE);
        UpdateOperations<Guide> uo=ds.createUpdateOperations(Guide.class);
        uo.set("title",title);
        ds.update(ds.createQuery(Guide.class).field("_id").equal(id),uo);
    }
}
