package composition.webserviceclients.vrmretentionretain

import _root_.webserviceclients.vrmretentionretain.{VRMRetentionRetainService, VRMRetentionRetainServiceImpl}
import com.tzavellas.sse.guice.ScalaModule

final class VrmRetentionRetainServiceBinding extends ScalaModule {

  def configure() = bind[VRMRetentionRetainService].to[VRMRetentionRetainServiceImpl].asEagerSingleton()
}