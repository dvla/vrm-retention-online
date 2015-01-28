package composition.webserviceclients.vrmretentioneligibility

import _root_.webserviceclients.vrmretentioneligibility.{VRMRetentionEligibilityWebService, VRMRetentionEligibilityWebServiceImpl}
import com.tzavellas.sse.guice.ScalaModule

final class VRMRetentionEligibilityWebServiceBinding extends ScalaModule {

  def configure() = {
    bind[VRMRetentionEligibilityWebService].to[VRMRetentionEligibilityWebServiceImpl].asEagerSingleton()
  }
}