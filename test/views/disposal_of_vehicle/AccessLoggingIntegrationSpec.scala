package views.disposal_of_vehicle

import com.google.inject.name.Names
import com.google.inject.{Guice, Injector}
import com.tzavellas.sse.guice.ScalaModule
import common.GlobalLike
import composition.TestComposition
import filters.AccessLoggingFilter.AccessLoggerName
import filters.MockLogger
import helpers.UiSpec
import helpers.webbrowser.{TestHarness, WebBrowserDSL, WebDriverFactory}
import org.apache.http.client.methods.{HttpGet, HttpPost}
import org.apache.http.impl.client.HttpClients
import org.scalatest.mock.MockitoSugar
import pages.disposal_of_vehicle.{BeforeYouStartPage, BusinessChooseYourAddressPage}
import play.api.LoggerLike
import play.api.test.FakeApplication

class AccessLoggingIntegrationSpec extends UiSpec with TestHarness with MockitoSugar with WebBrowserDSL {

  "Access Logging" should {

    "Log access that complete with success" in new WebBrowser(testApp1) {
      go to BeforeYouStartPage

      val infoLogs = mockLoggerTest1.captureLogInfos(1)
      infoLogs.get(0) should include( """] "GET /sell-to-the-trade/before-you-start HTTP/1.1" 200""")
    }

    "Log access that are completed because of Exception" in new WebBrowser(testApp2) {
      val httpClient = HttpClients.createDefault()
      val post = new HttpPost(BusinessChooseYourAddressPage.url)
      val httpResponse = httpClient.execute(post)
      httpResponse.close()

      val infoLogs = mockLoggerTest2.captureLogInfos(2)
      infoLogs.get(0) should include( """] "POST /sell-to-the-trade/business-choose-your-address HTTP/1.1" 303""")
      infoLogs.get(1) should include( """] "GET /sell-to-the-trade/error/""")
    }

    "Log access to unknown urls" in new WebBrowser(testApp3) {
      val httpClient = HttpClients.createDefault()
      val post = new HttpPost(WebDriverFactory.testUrl + "/some/unknown/url")
      val httpResponse = httpClient.execute(post)
      httpResponse.close()

      val infoLogs = mockLoggerTest3.captureLogInfos(2)

      infoLogs.get(0) should include( """] "POST /some/unknown/url HTTP/1.1" 303""")
      infoLogs.get(1) should include( """] "GET /sell-to-the-trade/error/""")
    }

    "not log any access for the healthcheck url" in new WebBrowser(testApp4) {
      val httpClient = HttpClients.createDefault()
      val post = new HttpGet(WebDriverFactory.testUrl + "/healthcheck")
      val httpResponse = httpClient.execute(post)
      httpResponse.close()

      val infoLogs = mockLoggerTest4.captureLogInfos(0)
    }

    "not log any access for the healthcheck url with parameters" in new WebBrowser(testApp5) {
      val httpClient = HttpClients.createDefault()
      val post = new HttpGet(WebDriverFactory.testUrl + "/healthcheck?param1=a&b=c")
      val httpResponse = httpClient.execute(post)
      httpResponse.close()

      val infoLogs = mockLoggerTest5.captureLogInfos(0)
    }

    "log any access for the healthcheck url that has extra in the path or parameters" in new WebBrowser(testApp6) {
      val httpClient = HttpClients.createDefault()
      val post = new HttpGet(WebDriverFactory.testUrl + "/healthcheck/some/extra")
      val httpResponse = httpClient.execute(post)
      httpResponse.close()

      val infoLogs = mockLoggerTest6.captureLogInfos(1)
    }
  }

  private class TestGlobalWithMockLogger(mockLogger: MockLogger) extends GlobalLike with TestComposition {

    override lazy val injector: Injector = Guice.createInjector(testModule(new ScalaModule {
      override def configure(): Unit = {
        bind[LoggerLike].annotatedWith(Names.named(AccessLoggerName)).toInstance(mockLogger)
      }
    }))
  }

  private val mockLoggerTest1 = new MockLogger

  private def testApp1 = FakeApplication(withGlobal = Some(new TestGlobalWithMockLogger(mockLoggerTest1)))

  private val mockLoggerTest2 = new MockLogger

  private def testApp2 = FakeApplication(withGlobal = Some(new TestGlobalWithMockLogger(mockLoggerTest2)))

  private val mockLoggerTest3 = new MockLogger

  private def testApp3 = FakeApplication(withGlobal = Some(new TestGlobalWithMockLogger(mockLoggerTest3)))

  private val mockLoggerTest4 = new MockLogger

  private def testApp4 = FakeApplication(withGlobal = Some(new TestGlobalWithMockLogger(mockLoggerTest4)))

  private val mockLoggerTest5 = new MockLogger

  private def testApp5 = FakeApplication(withGlobal = Some(new TestGlobalWithMockLogger(mockLoggerTest5)))

  private val mockLoggerTest6 = new MockLogger

  private def testApp6 = FakeApplication(withGlobal = Some(new TestGlobalWithMockLogger(mockLoggerTest6)))
}