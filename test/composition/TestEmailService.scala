package composition

import com.tzavellas.sse.guice.ScalaModule
import email.{EmailData, EmailFlags, RetainEmailService}
import models.{BusinessDetailsModel, ConfirmFormModel, EligibilityModel}
import org.scalatest.mock.MockitoSugar
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.TrackingId
import uk.gov.dvla.vehicles.presentation.common.model.VehicleAndKeeperDetailsModel

final class TestEmailService extends ScalaModule with MockitoSugar {

  val stub = mock[RetainEmailService]
  when(stub.emailRequest(
    any[String],
    any[VehicleAndKeeperDetailsModel],
    any[EligibilityModel],
    any[EmailData],
    any[Option[ConfirmFormModel]],
    any[Option[BusinessDetailsModel]],
    any[EmailFlags],
    any[TrackingId]
  )).thenReturn(None)

  def configure() = bind[RetainEmailService].toInstance(stub)
}
