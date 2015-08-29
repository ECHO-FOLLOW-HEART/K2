package database

import com.mongodb.{ MongoClient, MongoClientOptions, MongoCredential, ServerAddress }
import misc.CoreConfig
import org.mongodb.morphia.{ Morphia, ValidationExtension }

import scala.collection.JavaConversions._
import scala.language.postfixOps

/**
 * Created by zephyre on 6/28/15.
 */
object MorphiaFactory {
  lazy val client = {
    val backends = CoreConfig.conf.getConfig("backends.mongo").get
    val services = backends.subKeys.toSeq map (backends.getConfig(_).get)

    val serverAddresses = services map (c => {
      new ServerAddress(c.getString("host").get, c.getInt("port").get)
    })

    val mongoConfig = CoreConfig.conf.getConfig("k2.mongo").get
    val user = mongoConfig.getString("user")
    val password = mongoConfig.getString("password")
    val dbName = mongoConfig.getString("db").get
    val credentialOpt = for {
      u <- user
      p <- password
    } yield {
      MongoCredential.createScramSha1Credential(u, dbName, p.toCharArray)
    }

    val options = new MongoClientOptions.Builder()
      //连接超时
      .connectTimeout(60000)
      //IO超时
      .socketTimeout(10000)
      //与数据库能够建立的最大连接数
      .connectionsPerHost(50)
      //每个连接可以有多少线程排队等待
      .threadsAllowedToBlockForConnectionMultiplier(50)
      .build()

    if (credentialOpt nonEmpty)
      new MongoClient(serverAddresses, Seq(credentialOpt.get), options)
    else
      new MongoClient(serverAddresses, options)
  }

  lazy val morphia = {
    val m = new Morphia()
    new ValidationExtension(m)
    m.mapPackage("models.geo", true)
    m.mapPackage("models.guide", true)
    m.mapPackage("models.misc", true)
    m.mapPackage("models.plan", true)
    m.mapPackage("models.poi", true)
    m.mapPackage("models.traffic", true)
    m
  }

  lazy val datastore = {
    val dbName = CoreConfig.conf.getString("k2.mongo.db").get
    val d = morphia.createDatastore(client, dbName)
    //    d.ensureIndexes()
    d
  }

}
