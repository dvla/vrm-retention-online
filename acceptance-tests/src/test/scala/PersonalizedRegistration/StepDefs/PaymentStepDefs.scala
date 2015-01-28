package PersonalizedRegistration.StepDefs

import _root_.common.CommonStepDefs
import cucumber.api.java.After
import cucumber.api.java.en.{Given, Then, When}
import cucumber.api.scala.{EN, ScalaDsl}
import org.scalatest.Matchers
import org.scalatest.concurrent.Eventually.{PatienceConfig, eventually}
import org.scalatest.selenium.WebBrowser._
import pages._
import uk.gov.dvla.vehicles.presentation.common.helpers.webbrowser.WebBrowserDriver

import scala.concurrent.duration.DurationInt

final class PaymentStepDefs(implicit webDriver: WebBrowserDriver) extends ScalaDsl with EN with Matchers {

  //  private implicit val webDriver: EventFiringWebDriver = {
  //    import com.typesafe.config.ConfigFactory
  //    val conf = ConfigFactory.load()
  //    conf.getString("browser.type") match {
  //      case "firefox" => new WebBrowserFirefoxDriver
  //      case _ => new WebBrowserDriver
  //    }
  //  }
  implicit val timeout = PatienceConfig(timeout = 30.seconds)
  val beforeYouStart = new BeforeYouStartPageSteps()(webDriver, timeout)
  val vehicleLookup = new VehicleLookupPageSteps()(webDriver, timeout)
  val payment = new PaymentPageSteps()(webDriver, timeout)
  val success = new SuccessPaymentPageSteps()(webDriver, timeout)
  val paymentFailure = new PaymentFailurePageSteps()(webDriver, timeout)
  val paymentCallBack = new PaymentCallbackPageSteps()(webDriver, timeout)
  val vehicleNotFound = new VehicleNotFoundPageSteps()(webDriver, timeout)
  val vrmLocked = new VrmLockedPageSteps()(webDriver, timeout)
  val vehicleLookupFailure = new VehicleLookupFailurePageSteps()(webDriver, timeout)
  val setupBusinessDetails = new SetupBusinessDetailsPageSteps()(webDriver, timeout)
  val businessChooseYourAddress = new BusinessChooseYourAddressPageSteps()(webDriver, timeout)
  val confirmBusiness = new ConfirmBusinessPageSteps()(webDriver, timeout)
  val user = new CommonStepDefs(
    beforeYouStart,
    vehicleLookup,
    vehicleNotFound,
    vrmLocked,
    confirmBusiness,
    setupBusinessDetails,
    businessChooseYourAddress
  )(webDriver, timeout)

  @Given("^that I have started the PR Retention Service for payment$")
  def `that I have started the PR Retention Service for payment`() {
    user.`start the PR service`
  }

  @Given("^I search and confirm the vehicle to be registered$")
  def `i search and confirm the vehicle to be registered`() = {
    vehicleLookup.
      `is displayed`.
      enter("A1", "11111111111", "AA11AA").
      `keeper is acting`.
      `find vehicle`
    user.confirmDetails
  }

  @When("^I enter payment details as \"(.*?)\",\"(.*?)\" and \"(.*?)\"$")
  def `i enter payment details as <CardName>,<CardNumber> and <SecurityCode>`(cardName: String, cardNumber: String, cardExpiry: String) = {
    payment
      .`is displayed`
      .enter(cardName, cardNumber, cardExpiry)
      .`expiryDate`
  }

  @When("^proceed to the payment$")
  def `proceed to the payment`() = {
    payment.`paynow`
  }

  @Then("^following \"(.*?)\" should be displayed$")
  def `following should be displayed`(Message: String) = {
    eventually {
      pageSource contains (Message)
    }
    if (Message == "Payment Successful") {
      pageTitle contains (Message)
    }
    else if (Message == "Payment Cancelled or Not Authorised") {
      pageTitle contains ("/payment-not-authorised")
    }
    else
      fail(s"not the message we expected: $Message")
  }

  /** DO NOT REMOVE **/
  @After()
  def teardown() = webDriver.quit()
}
