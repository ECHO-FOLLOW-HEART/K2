package models.plan;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.ITravelPiFormatter;
import org.mongodb.morphia.annotations.Entity;

import java.lang.reflect.Field;

/**
 * Created by topy on 2014/9/5.
 */
@Entity
public class SharePlan extends UgcPlan implements ITravelPiFormatter {

    public SharePlan() {

    }

    public SharePlan(UgcPlan plan) throws NoSuchFieldException, IllegalAccessException {
        this.tranfToUgcPlan(plan);
    }

    private void tranfToUgcPlan(UgcPlan plan) throws NoSuchFieldException, IllegalAccessException {
        String paraFiledStr = null;
        Field thisField = null;
        //获取传进来的映射对象
        Class c = plan.getClass();
        //取得对象的所有属性，放到一个数组中
        Field[] f = c.getFields();
        for (int i = 0; i < f.length; i++) {
            paraFiledStr = f[i].getName();
            thisField = this.getClass().getField(paraFiledStr);
            //获取对象的属性
            Object filedValue = UgcPlan.class.getField(f[i].getName()).get(plan);
            thisField.setAccessible(true);
            thisField.set(this, filedValue);
        }
    }

    @Override
    public JsonNode toJson() {

        ObjectNode node = (ObjectNode) super.toJson(true);

        return node;
    }
}
