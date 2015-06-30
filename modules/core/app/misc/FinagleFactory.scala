package misc

import java.net.InetSocketAddress

import com.lvxingpai.yunkai.Userservice.FinagledClient
import com.twitter.finagle.builder.ClientBuilder
import com.twitter.finagle.thrift.ThriftClientFramedCodec
import org.apache.thrift.protocol.TBinaryProtocol

/**
 * Created by zephyre on 6/30/15.
 */
object FinagleFactory {
  lazy val client = {
    val backends = CoreConfig.conf.getConfig("backends.yunkai").get
    val services = backends.subKeys.toSeq map (backends.getConfig(_).get)

    val server = services.head.getString("host").get -> services.head.getInt("port").get

//    val server = "localhost" -> 9000

    val service = ClientBuilder()
      .hosts(new InetSocketAddress(server._1, server._2))
      //      .hosts(new InetSocketAddress("192.168.100.2", 9400))
      .hostConnectionLimit(1000)
      .codec(ThriftClientFramedCodec())
      .build()
    new FinagledClient(service, new TBinaryProtocol.Factory())
  }
}
