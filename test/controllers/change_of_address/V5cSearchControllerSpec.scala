package controllers.change_of_address

import play.api.test.{FakeRequest, WithApplication}
import play.api.test.Helpers._
import org.scalatest.{Matchers, WordSpec}
import mappings.V5cSearch._
import helpers.change_of_address.LoginCachePopulate
import LoginCachePopulate._
import org.mockito.Mockito._
import org.mockito.Matchers._
import models.domain.change_of_address.V5cSearchModel
import modules.TestModule.FakeV5cSearchWebService
import org.scalatest.mock.MockitoSugar

class V5cSearchControllerSpec extends WordSpec with Matchers with MockitoSugar {

  "V5cSearch - Controller" should {
    val mockV5cSearchModel = mock[V5cSearchModel]
    val mockWebService = mock[services.V5cSearchWebService]
    when(mockWebService.invoke(any[V5cSearchModel])).thenReturn(FakeV5cSearchWebService().invoke(mockV5cSearchModel))
    val vehicleSearch = new VehicleSearch(mockWebService)

    "present when user has logged in" in new WithApplication {
      // Arrange
      setupCache()

      val request = FakeRequest().withSession()

      // Act
      val result = vehicleSearch.present(request)

      // Assert
      status(result) should equal(OK)
    }

    "present login page when user is not logged in" in new WithApplication {
      // Arrange
      val request = FakeRequest().withSession()

      // Act
      val result = vehicleSearch.present(request)

      // Assert
      redirectLocation(result) should equal(Some("/are-you-registered"))
    }

    "redirect to next page after the button is clicked" in new WithApplication {
      // Arrange
      val v5cReferenceNumberValid = "12345678910"
      val v5cRegistrationNumberValid = "a1"
      val v5cPostcodeValid = "sa44dw"
      val request = FakeRequest().withSession()
        .withFormUrlEncodedBody(v5cReferenceNumberId -> v5cReferenceNumberValid, v5cRegistrationNumberId -> v5cRegistrationNumberValid, v5cPostcodeId -> v5cPostcodeValid)

      // Act
      val result = vehicleSearch.submit(request)

      // Assert
      status(result) should equal(SEE_OTHER)
      redirectLocation(result) should equal(Some("/confirm-vehicle-details"))
    }

  }
}