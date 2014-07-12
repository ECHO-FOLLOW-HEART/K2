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
	public Address address;

	public String zhName;

	public String enName;

	public String url;

	public String desc;

	public String tel;

	public List<String> alias;
    @Embedded
    public Contact contact;

	@Override
	public JsonNode toJson() {
		BasicDBObjectBuilder builder = BasicDBObjectBuilder.start();
		builder.add("_id", id.toString()).add("name", zhName).add("url", url)
		.add("tel", tel).add("alias", alias);
		if (alias != null) {
			BasicDBList aliasListNodes = new BasicDBList();
			for (String a : alias) {
				aliasListNodes.add(a);
			}
			builder.add("alias", aliasListNodes);
		}

		if (address != null) {
			BasicDBObjectBuilder addressBuilder = BasicDBObjectBuilder.start();
			addressBuilder.add("address", (address.address != null ? address.address : ""));
			if(address.loc != null){
				addressBuilder.add("loc", BasicDBObjectBuilder.start().add("_id", address.loc.id).add("name", address.loc.zhName));
			}
			if(address.coords != null){
				BasicDBObjectBuilder coordsBuilder = BasicDBObjectBuilder.start();
				coordsBuilder.add("blat", (address.coords.blat != null ? address.coords.blat : ""));
				coordsBuilder.add("blng", (address.coords.blng != null ? address.coords.blng : ""));
				coordsBuilder.add("lat", (address.coords.lat != null ? address.coords.lat : ""));
				coordsBuilder.add("lng", (address.coords.lng != null ? address.coords.lng : ""));
				addressBuilder.add("coords", coordsBuilder.get());
			}
			builder.add("address", addressBuilder);
		}
		return Json.toJson(builder.get());
	}
}
