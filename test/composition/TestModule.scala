package composition

import com.google.inject.name.Names
import com.tzavellas.sse.guice.ScalaModule
import composition.TestModule.AddressLookupServiceConstants.PostcodeInvalid
import composition.TestModule.DateServiceConstants.{DateOfDisposalDayValid, DateOfDisposalMonthValid, DateOfDisposalYearValid}
import org.joda.time.{DateTime, Instant}
import org.mockito.Mockito._
import org.mockito.Matchers._
import org.scalatest.mock.MockitoSugar
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import pdf.{PdfService, PdfServiceImpl}
import play.api.http.Status.OK
import play.api.i18n.Lang
import play.api.{Logger, LoggerLike}
import services.brute_force_prevention.{BruteForcePreventionService, BruteForcePreventionServiceImpl, BruteForcePreventionWebService}
import services.fakes.FakeAddressLookupWebServiceImpl.{traderUprnValid2, traderUprnValid}
import services.fakes._
import services.fakes.brute_force_protection.FakeBruteForcePreventionWebServiceImpl
import services.vehicle_and_keeper_lookup.{VehicleAndKeeperLookupService, VehicleAndKeeperLookupServiceImpl, VehicleAndKeeperLookupWebService}
import services.vrm_retention_eligibility.{VRMRetentionEligibilityService, VRMRetentionEligibilityServiceImpl, VRMRetentionEligibilityWebService}
import services.vrm_retention_retain.{VRMRetentionRetainService, VRMRetentionRetainServiceImpl, VRMRetentionRetainWebService}
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.{ClearTextClientSideSessionFactory, ClientSideSessionFactory, CookieFlags, NoCookieFlags}
import uk.gov.dvla.vehicles.presentation.common.filters.AccessLoggingFilter.AccessLoggerName
import uk.gov.dvla.vehicles.presentation.common.model.AddressModel
import uk.gov.dvla.vehicles.presentation.common.services.DateService
import uk.gov.dvla.vehicles.presentation.common.views.models.DayMonthYear
import uk.gov.dvla.vehicles.presentation.common.webserviceclients.addresslookup.{AddressLookupService, AddressLookupWebService}
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import scala.concurrent.Future

class TestModule() extends ScalaModule with MockitoSugar {

  /**
   * Bind the fake implementations the traits
   */
  def configure() {
    Logger.debug("Guice is loading TestModule")

    ordnanceSurveyAddressLookup()

    bind[VehicleAndKeeperLookupWebService].to[FakeVehicleAndKeeperLookupWebService].asEagerSingleton()
    bind[VehicleAndKeeperLookupService].to[VehicleAndKeeperLookupServiceImpl].asEagerSingleton()
    bind[DateService].toInstance(stubDateService)
    bind[CookieFlags].to[NoCookieFlags].asEagerSingleton()
    bind[ClientSideSessionFactory].to[ClearTextClientSideSessionFactory].asEagerSingleton()

    bind[BruteForcePreventionWebService].to[FakeBruteForcePreventionWebServiceImpl].asEagerSingleton()
    bind[BruteForcePreventionService].to[BruteForcePreventionServiceImpl].asEagerSingleton()
    bind[LoggerLike].annotatedWith(Names.named(AccessLoggerName)).toInstance(Logger("dvla.common.AccessLogger"))

    bind[VRMRetentionEligibilityWebService].to[FakeVRMRetentionEligibilityWebServiceImpl].asEagerSingleton()
    bind[VRMRetentionEligibilityService].to[VRMRetentionEligibilityServiceImpl].asEagerSingleton()

    bind[VRMRetentionRetainWebService].to[FakeVRMRetentionRetainWebServiceImpl].asEagerSingleton()
    bind[VRMRetentionRetainService].to[VRMRetentionRetainServiceImpl].asEagerSingleton()
    bind[PdfService].to[PdfServiceImpl].asEagerSingleton()
  }

  private def ordnanceSurveyAddressLookup() = {
    bind[AddressLookupService].to[uk.gov.dvla.vehicles.presentation.common.webserviceclients.addresslookup.ordnanceservey.AddressLookupServiceImpl]

    val stubbedWebServiceImpl = mock[AddressLookupWebService]
    when(stubbedWebServiceImpl.callPostcodeWebService(postcode = any[String], trackingId = any[String])(any[Lang])).thenReturn(FakeAddressLookupWebServiceImpl.responseValidForPostcodeToAddress)
    when(stubbedWebServiceImpl.callPostcodeWebService(matches(PostcodeInvalid.toUpperCase),  any[String])(any[Lang])).thenReturn(FakeAddressLookupWebServiceImpl.responseWhenPostcodeInvalid)
    when(stubbedWebServiceImpl.callUprnWebService(uprn = any[String], trackingId = any[String])(any[Lang])).thenReturn(FakeAddressLookupWebServiceImpl.responseValidForUprnToAddress)

    bind[AddressLookupWebService].toInstance(stubbedWebServiceImpl)
  }

  private val stubDateService: DateService = {
    val dateTimeISOChronology: String = new DateTime(
      DateOfDisposalYearValid.toInt,
      DateOfDisposalMonthValid.toInt,
      DateOfDisposalDayValid.toInt,
      0,
      0).toString
    val today = DayMonthYear(
      DateOfDisposalDayValid.toInt,
      DateOfDisposalMonthValid.toInt,
      DateOfDisposalYearValid.toInt
    )
    val now = Instant.now()

    val dateService = mock[DateService]
    when(dateService.dateTimeISOChronology).thenReturn(dateTimeISOChronology)
    when(dateService.today).thenReturn(today)
    when(dateService.now).thenReturn(now)
    dateService
  }
}

object TestModule {

  object DateServiceConstants {

    final val DateOfDisposalDayValid = "25"
    final val DateOfDisposalMonthValid = "11"
    final val DateOfDisposalYearValid = "1970"
  }

  object AddressLookupServiceConstants {
    final val TraderBusinessNameValid = "example trader name"
    final val TraderBusinessContactValid = "example trader contact"
    final val PostcodeInvalid = "xx99xx"
    final val PostcodeValid = "QQ99QQ"
    val addressWithoutUprn = AddressModel(address = Seq("44 Hythe Road", "White City", "London", PostcodeValid))
    val addressWithUprn = AddressModel(
      uprn = Some(traderUprnValid),
      address = Seq("44 Hythe Road", "White City", "London", PostcodeValid)
    )
    final val BuildingNameOrNumberValid = "1234"
    final val Line2Valid = "line2 stub"
    final val Line3Valid = "line3 stub"
    final val PostTownValid = "postTown stub"

    final val PostcodeValidWithSpace = "QQ9 9QQ"
    final val PostcodeNoResults = "SA99 1DD"
    val fetchedAddresses = Seq(
      traderUprnValid.toString -> addressWithUprn.address.mkString(", "),
      traderUprnValid2.toString -> addressWithUprn.address.mkString(", ")
    )
  }
}