//package cache
//
//import org.sedis.Pool
//import redis.clients.jedis.{ JedisPool, JedisPoolConfig }
//
///**
// * Created by topy on 2015/10/15.
// */
//object SedisDemo extends App {
//  main(args)
//  //
//  //  override def main(args: Array[String]) = {
//  //    System.out.print("OK")
//  //    val pool = new Pool(new JedisPool(new JedisPoolConfig(), "192.168.200.4", 6379))
//  //
//  //    pool.withClient { client =>
//  //      client.set("name1", "00")
//  //    }
//  //    val re = pool.withClient { client =>
//  //      client.get("name")
//  //    }
//  //
//  //    System.out.print(re.get)
//  //  }
//
//}
