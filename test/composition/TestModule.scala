package composition

import com.google.inject.name.Names
import com.tzavellas.sse.guice.ScalaModule
import email.{EmailService, EmailServiceImpl}
import org.joda.time.{DateTime, Instant}
import org.mockito.Matchers.{any, _}
import org.mockito.Mockito.when
import org.mockito.invocation.InvocationOnMock
import org.mockito.stubbing.Answer
import org.scalatest.mock.MockitoSugar
import pdf.{PdfService, PdfServiceImpl}
import play.api.http.Status.{FORBIDDEN, OK}
import play.api.i18n.Lang
import play.api.libs.json.Json
import play.api.libs.ws.WSResponse
import play.api.{Logger, LoggerLike}
import services.fakes.AddressLookupServiceConstants.PostcodeInvalid
import services.fakes.BruteForcePreventionWebServiceConstants._
import services.fakes.DateServiceConstants._
import services.fakes.VehicleAndKeeperLookupWebServiceConstants._
import services.fakes.VrmRetentionEligibilityWebServiceConstants.ReplacementRegistrationNumberValid
import services.fakes.VrmRetentionRetainWebServiceConstants.CertificateNumberValid
import services.fakes._
import webserviceclients.vrmretentioneligibility.{VRMRetentionEligibilityService, VRMRetentionEligibilityServiceImpl, VRMRetentionEligibilityWebService}
import webserviceclients.vrmretentionretain.{VRMRetentionRetainService, VRMRetentionRetainServiceImpl, VRMRetentionRetainWebService}
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.{ClearTextClientSideSessionFactory, ClientSideSessionFactory, CookieFlags, NoCookieFlags}
import uk.gov.dvla.vehicles.presentation.common.filters.AccessLoggingFilter.AccessLoggerName
import uk.gov.dvla.vehicles.presentation.common.services.DateService
import uk.gov.dvla.vehicles.presentation.common.views.models.DayMonthYear
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.addresslookup.{AddressLookupService, AddressLookupWebService}
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.bruteforceprevention.{BruteForcePreventionService, BruteForcePreventionServiceImpl, BruteForcePreventionWebService}
import webserviceclients.vehicleandkeeperlookup.{VehicleAndKeeperDetailsRequest, VehicleAndKeeperLookupService, VehicleAndKeeperLookupServiceImpl, VehicleAndKeeperLookupWebService}
import webserviceclients.vrmretentioneligibility.{VRMRetentionEligibilityRequest, VRMRetentionEligibilityResponse}
import webserviceclients.vrmretentionretain.{VRMRetentionRetainRequest, VRMRetentionRetainResponse}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class TestModule() extends ScalaModule with MockitoSugar {

  /**
   * Bind the fake implementations the traits
   */
  def configure() {
    Logger.debug("Guice is loading TestModule")

    stubOrdnanceSurveyAddressLookup()
    stubDateService()
    stubBruteForcePreventionWebService()
    stubVehicleAndKeeperLookupWebService()
    stubVrmRetentionEligibilityWebService()
    stubVrmRetentionRetainWebService()

    bind[VehicleAndKeeperLookupService].to[VehicleAndKeeperLookupServiceImpl].asEagerSingleton()
    bind[CookieFlags].to[NoCookieFlags].asEagerSingleton()
    bind[ClientSideSessionFactory].to[ClearTextClientSideSessionFactory].asEagerSingleton()

    bind[BruteForcePreventionService].to[BruteForcePreventionServiceImpl].asEagerSingleton()
    bind[LoggerLike].annotatedWith(Names.named(AccessLoggerName)).toInstance(Logger("dvla.common.AccessLogger"))

    bind[VRMRetentionEligibilityService].to[VRMRetentionEligibilityServiceImpl].asEagerSingleton()

    bind[VRMRetentionRetainService].to[VRMRetentionRetainServiceImpl].asEagerSingleton()
    bind[PdfService].to[PdfServiceImpl].asEagerSingleton()
    bind[EmailService].to[EmailServiceImpl].asEagerSingleton()
  }

  private def stubOrdnanceSurveyAddressLookup() = {
    bind[AddressLookupService].to[uk.gov.dvla.vehicles.presentation.common.webserviceclients.addresslookup.ordnanceservey.AddressLookupServiceImpl]

    val stubbedWebServiceImpl = mock[AddressLookupWebService]
    when(stubbedWebServiceImpl.callPostcodeWebService(postcode = any[String], trackingId = any[String])(any[Lang])).thenReturn(AddressLookupWebServiceConstants.responseValidForPostcodeToAddress)
    when(stubbedWebServiceImpl.callPostcodeWebService(matches(PostcodeInvalid.toUpperCase), any[String])(any[Lang])).thenReturn(AddressLookupWebServiceConstants.responseWhenPostcodeInvalid)
    when(stubbedWebServiceImpl.callUprnWebService(uprn = matches(AddressLookupWebServiceConstants.traderUprnValid.toString), trackingId = any[String])(any[Lang])).thenReturn(AddressLookupWebServiceConstants.responseValidForUprnToAddress)
    when(stubbedWebServiceImpl.callUprnWebService(uprn = matches(AddressLookupWebServiceConstants.traderUprnInvalid.toString), trackingId = any[String])(any[Lang])).thenReturn(AddressLookupWebServiceConstants.responseValidForUprnToAddressNotFound)

    bind[AddressLookupWebService].toInstance(stubbedWebServiceImpl)
  }

  private def stubDateService() = {
    val dateTimeISOChronology: String = new DateTime(
      YearValid.toInt,
      MonthValid.toInt,
      DayValid.toInt,
      0,
      0).toString
    val today = DayMonthYear(
      DayValid.toInt,
      MonthValid.toInt,
      YearValid.toInt
    )
    val now = Instant.now()

    val dateService = mock[DateService]
    when(dateService.dateTimeISOChronology).thenReturn(dateTimeISOChronology)
    when(dateService.today).thenReturn(today)
    when(dateService.now).thenReturn(now)
    bind[DateService].toInstance(dateService)
  }

  private def stubBruteForcePreventionWebService() = {
    val bruteForcePreventionWebService = mock[BruteForcePreventionWebService]
    when(bruteForcePreventionWebService.callBruteForce(any[String])).thenReturn(Future {
      new FakeResponse(status = OK, fakeJson = responseFirstAttempt)
    })
    when(bruteForcePreventionWebService.callBruteForce(matches(VrmLocked))).thenReturn(Future {
      new FakeResponse(status = FORBIDDEN)
    })
    bind[BruteForcePreventionWebService].toInstance(bruteForcePreventionWebService)
  }

  private def stubVehicleAndKeeperLookupWebService() = {
    val vehicleAndKeeperLookupWebService = mock[VehicleAndKeeperLookupWebService]
    when(vehicleAndKeeperLookupWebService.invoke(any[VehicleAndKeeperDetailsRequest], any[String])).
      thenAnswer(
        new Answer[Future[WSResponse]] {
          override def answer(invocation: InvocationOnMock) = Future {
            val args: Array[AnyRef] = invocation.getArguments
            val request = args(0).asInstanceOf[VehicleAndKeeperDetailsRequest] // Cast first argument.
            val (responseStatus, response) = {
              request.referenceNumber match {
                case "99999999991" => vehicleAndKeeperDetailsResponseVRMNotFound
                case "99999999992" => vehicleAndKeeperDetailsResponseDocRefNumberNotLatest
                case "99999999999" => vehicleAndKeeperDetailsResponseNotFoundResponseCode
                case _ => vehicleAndKeeperDetailsResponseSuccess
              }
            }
            val responseAsJson = Json.toJson(response)
            new FakeResponse(status = responseStatus, fakeJson = Some(responseAsJson)) // Any call to a webservice will always return this successful response.
          }
        }
      )
    bind[VehicleAndKeeperLookupWebService].toInstance(vehicleAndKeeperLookupWebService)
  }

  private def stubVrmRetentionEligibilityWebService() = {
    val vrmRetentionEligibilityWebService = mock[VRMRetentionEligibilityWebService]
    when(vrmRetentionEligibilityWebService.invoke(any[VRMRetentionEligibilityRequest], any[String])).
      thenAnswer(
        new Answer[Future[WSResponse]] {
          override def answer(invocation: InvocationOnMock) = Future {
            val args: Array[AnyRef] = invocation.getArguments
            val request = args(0).asInstanceOf[VRMRetentionEligibilityRequest] // Cast first argument.
            val vrmRetentionEligibilityResponse = VRMRetentionEligibilityResponse(
                currentVRM = Some(request.currentVRM),
                replacementVRM = Some(ReplacementRegistrationNumberValid),
                responseCode = None)
            val asJson = Json.toJson(vrmRetentionEligibilityResponse)
            new FakeResponse(status = OK, fakeJson = Some(asJson))
          }
        }
      )
    bind[VRMRetentionEligibilityWebService].toInstance(vrmRetentionEligibilityWebService)
  }

  private def stubVrmRetentionRetainWebService() = {
    val vrmRetentionRetainWebService = mock[VRMRetentionRetainWebService]
    when(vrmRetentionRetainWebService.invoke(any[VRMRetentionRetainRequest], any[String])).
      thenAnswer(
        new Answer[Future[WSResponse]] {
          override def answer(invocation: InvocationOnMock) = Future {
            val args: Array[AnyRef] = invocation.getArguments
            val request = args(0).asInstanceOf[VRMRetentionRetainRequest] // Cast first argument.
            val vrmRetentionRetainResponse = VRMRetentionRetainResponse(
                certificateNumber = Some(CertificateNumberValid),
                currentVRM = request.currentVRM,
                replacementVRM = Some(ReplacementRegistrationNumberValid),
                responseCode = None)
            val asJson = Json.toJson(vrmRetentionRetainResponse)
            new FakeResponse(status = OK, fakeJson = Some(asJson))
          }
        }
      )
    bind[VRMRetentionRetainWebService].toInstance(vrmRetentionRetainWebService)
  }
}