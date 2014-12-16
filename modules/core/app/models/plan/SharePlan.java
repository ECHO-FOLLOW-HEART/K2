package models.plan;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.ITravelPiFormatter;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * Created by topy on 2014/9/5.
 */
@Entity
public class SharePlan extends UgcPlan implements ITravelPiFormatter {

    public SharePlan() {

    }

    public SharePlan(UgcPlan plan) throws NoSuchFieldException, IllegalAccessException {
//        this.tranfToUgcPlan(plan);

        this();
        Class<?> cls = UgcPlan.class;
        while (!cls.equals(Object.class)) {
            for (Method method : cls.getDeclaredMethods()) {
                if (!(Modifier.isPublic(method.getModifiers()) && isGetter(method)))
                    continue;

                String setterName = method.getName().replaceFirst("^get", "set");
                try {
                    Method setterMethod = this.getClass().getMethod(setterName, method.getReturnType());
                    if (!isSetter(setterMethod))
                        continue;
                    setterMethod.invoke(this, method.invoke(plan));
                } catch (ReflectiveOperationException ignored) {
                }
            }
            cls = cls.getSuperclass();
        }

        //设置ID
//        this.setId(new ObjectId());
        this.setEnabled(true);
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
