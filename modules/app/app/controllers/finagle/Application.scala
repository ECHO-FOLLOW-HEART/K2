import java.net.InetSocketAddress

import com.twitter.finagle.builder.ClientBuilder
import com.twitter.finagle.thrift.ThriftClientFramedCodec
import org.apache.thrift.protocol.TBinaryProtocol

/**
 * Created by zephyre on 4/30/15.
 */
object Application extends App {

  override def main(args: Array[String]) = {
//    val (host, port) = ("192.168.100.2", 9400)
//    val (host, port) = ("localhost", 9000)

//    val service = ClientBuilder()
//      .hosts(new InetSocketAddress(host, port))
//      .hostConnectionLimit(1000)
//      .codec(ThriftClientFramedCodec())
//      .build()
//
//    val client = new Userservice.FinagledClient(service, new TBinaryProtocol.Factory())
//


    val loginName = "13699851562"
    val passwd = "000999"
    //
    //    val user = Await.result(client.login(loginName, passwd))
    //
    //    println(user)
    //
    //    client.service.close()
  }

  main(args)
}

