package composition.webserviceclients.vrmretentionretain

import _root_.webserviceclients.vrmretentionretain.VRMRetentionRetainWebService
import _root_.webserviceclients.vrmretentionretain.VRMRetentionRetainWebServiceImpl
import com.tzavellas.sse.guice.ScalaModule

final class VrmRetentionRetainWebServiceBinding extends ScalaModule {

  def configure() = bind[VRMRetentionRetainWebService].to[VRMRetentionRetainWebServiceImpl].asEagerSingleton()
}