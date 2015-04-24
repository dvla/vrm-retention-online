package composition.webserviceclients.vrmretentionretain

import _root_.webserviceclients.vrmretentionretain.VRMRetentionRetainService
import _root_.webserviceclients.vrmretentionretain.VRMRetentionRetainServiceImpl
import com.tzavellas.sse.guice.ScalaModule

final class VrmRetentionRetainServiceBinding extends ScalaModule {

  def configure() = bind[VRMRetentionRetainService].to[VRMRetentionRetainServiceImpl].asEagerSingleton()
}