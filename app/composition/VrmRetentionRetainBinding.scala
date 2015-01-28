package composition

import com.tzavellas.sse.guice.ScalaModule
import webserviceclients.vrmretentionretain.{VRMRetentionRetainService, VRMRetentionRetainServiceImpl, VRMRetentionRetainWebService, VRMRetentionRetainWebServiceImpl}

final class VrmRetentionRetainBinding extends ScalaModule {

  def configure() = {
    bind[VRMRetentionRetainWebService].to[VRMRetentionRetainWebServiceImpl].asEagerSingleton()
    bind[VRMRetentionRetainService].to[VRMRetentionRetainServiceImpl].asEagerSingleton()
  }
}