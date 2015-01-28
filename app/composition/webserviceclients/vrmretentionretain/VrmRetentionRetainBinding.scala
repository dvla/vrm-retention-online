package composition.webserviceclients.vrmretentionretain

import _root_.webserviceclients.vrmretentionretain.{VRMRetentionRetainService, VRMRetentionRetainServiceImpl, VRMRetentionRetainWebService, VRMRetentionRetainWebServiceImpl}
import com.tzavellas.sse.guice.ScalaModule

final class VrmRetentionRetainBinding extends ScalaModule {

  def configure() = {
    bind[VRMRetentionRetainWebService].to[VRMRetentionRetainWebServiceImpl].asEagerSingleton()
    bind[VRMRetentionRetainService].to[VRMRetentionRetainServiceImpl].asEagerSingleton()
  }
}