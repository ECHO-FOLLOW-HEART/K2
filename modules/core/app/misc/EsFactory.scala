package misc

import org.elasticsearch.client.transport.TransportClient
import org.elasticsearch.common.settings.ImmutableSettings
import org.elasticsearch.common.transport.InetSocketTransportAddress

/**
 * Created by topy on 2015/9/25.
 */
object EsFactory {

  lazy val client = {
    //      val backends = CoreConfig.conf.getConfig("backends.yunkai").get
    //      val services = backends.subKeys.toSeq map (backends.getConfig(_).get)

    val settings = ImmutableSettings.settingsBuilder()
      .put("cluster.name", "es-cluster-default").build()
    new TransportClient(settings)
      .addTransportAddress(new InetSocketTransportAddress("192.168.200.3", 9311))
  }

}
