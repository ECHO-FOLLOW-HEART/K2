package models.morphia.traffic;

import java.util.List;

import models.TravelPiBaseItem;
import models.morphia.geo.Address;
import models.morphia.misc.Contact;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;

import play.libs.Json;

import com.fasterxml.jackson.databind.JsonNode;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObjectBuilder;

/**
 * 机场。
 * 
 * @author Zephyre
 */
@Entity
public class Airport extends TravelPiBaseItem {

	@Id
	public ObjectId id;

	@Embedded
	public Address addr;

	public String zhName;

	public String enName;

	public String url;

	public String desc;

	@Embedded
    public Contact contact;

	public List<String> alias;

	@Override
	public JsonNode toJson() {
		BasicDBObjectBuilder builder = BasicDBObjectBuilder.start();
		builder.add("_id", id.toString()).add("name", zhName).add("url", url).add("alias", alias);
        if (contact!=null && contact.phoneList!=null && !contact.phoneList.isEmpty()){
            BasicDBList phoneList = new BasicDBList();
            for (String val:contact.phoneList)
                phoneList.add(val);
            builder.add("tel", phoneList);
        }

		if (alias != null) {
			BasicDBList aliasListNodes = new BasicDBList();
			for (String a : alias) {
				aliasListNodes.add(a);
			}
			builder.add("alias", aliasListNodes);
		}

		if (addr != null) {
			BasicDBObjectBuilder addressBuilder = BasicDBObjectBuilder.start();
			addressBuilder.add("addr", (addr.address != null ? addr.address : ""));
			if(addr.loc != null){
				addressBuilder.add("loc", BasicDBObjectBuilder.start().add("_id", addr.loc.id).add("name", addr.loc.zhName));
			}
			if(addr.coords != null){
				BasicDBObjectBuilder coordsBuilder = BasicDBObjectBuilder.start();
				coordsBuilder.add("blat", (addr.coords.blat != null ? addr.coords.blat : ""));
				coordsBuilder.add("blng", (addr.coords.blng != null ? addr.coords.blng : ""));
				coordsBuilder.add("lat", (addr.coords.lat != null ? addr.coords.lat : ""));
				coordsBuilder.add("lng", (addr.coords.lng != null ? addr.coords.lng : ""));
				addressBuilder.add("coords", coordsBuilder.get());
			}
			builder.add("addr", addressBuilder);
		}
		return Json.toJson(builder.get());
	}
}
