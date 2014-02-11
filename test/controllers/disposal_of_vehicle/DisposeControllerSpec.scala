package controllers.disposal_of_vehicle

import play.api.test.{FakeRequest, WithApplication}
import play.api.test.Helpers._
import controllers.{Mappings, disposal_of_vehicle}
import org.scalatest.{Matchers, WordSpec}
import org.specs2.mock.Mockito

class DisposeControllerSpec extends WordSpec with Matchers with Mockito{

  "Disposal - Controller" should {

    "present" in new WithApplication {
      // Arrange
      val request = FakeRequest().withSession()

      // Act
      val result = disposal_of_vehicle.Dispose.present(request)

      // Assert
      status(result) should equal(OK)
    }

    "redirect to next page after the dispose button is clicked" in new WithApplication {
      // Arrange
      val consentValid = "true"
      val mileageValid = "20000"
      val dateOfDisposalDayValid = "25"
      val dateOfDisposalMonthValid = "11"
      val dateOfDisposalYearValid = "1970"

      val request = FakeRequest().withSession()
        .withFormUrlEncodedBody(
          "consent" -> consentValid,
          "mileage" -> mileageValid,
          "dateOfDisposal.day" -> dateOfDisposalDayValid,
          "dateOfDisposal.month" -> dateOfDisposalMonthValid,
          "dateOfDisposal.year" -> dateOfDisposalYearValid
        )

      // Act
      val result = disposal_of_vehicle.Dispose.submit(request)

      // Assert
      status(result) should equal(SEE_OTHER)
      redirectLocation(result) should equal (Some("/disposal-of-vehicle/dispose-confirmation"))
    }

  }
}