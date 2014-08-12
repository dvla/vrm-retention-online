package filters

import com.google.inject.Guice
import com.tzavellas.sse.guice.ScalaModule
import helpers.UnitSpec
import common.ClientSideSessionFactory
import play.api.mvc.{RequestHeader, Results, Cookies}
import uk.gov.dvla.vehicles.presentation.common.clientsidesession.InvalidSessionException
import scala.concurrent.{ExecutionContext, Future}
import play.api.mvc.Cookie
import play.api.mvc.SimpleResult
import org.mockito.Mockito.{when, verify, never}
import org.mockito.Matchers.any
import play.api.test.FakeRequest
import play.api.http.HeaderNames
import ExecutionContext.Implicits.global
import scala.language.existentials

class EnsureSessionCreatedFilterSpec extends UnitSpec {

  "Create a session if there is not one" in setUp {
    case SetUp(filter, request, sessionFactory, nextFilter) =>
      val existingRequestCookie1 = Cookie("existingRequestCookie1", "existingRequestCookie1Value")
      val existingRequestCookie2 = Cookie("existingRequestCookie2", "existingRequestCookie2Value")
      val requestWithSomeCookies = request.withCookies(existingRequestCookie1, existingRequestCookie2)

      val trackingIdCookie = Cookie("trackingId", "trackingIdValue")
      val sessionCookie = Cookie("session", "sessionValue")
      when(sessionFactory.newSessionCookiesIfNeeded(requestWithSomeCookies.cookies)).
        thenReturn(Some(Seq(trackingIdCookie, sessionCookie)))

      val filterResult: Future[SimpleResult] = filter.apply(nextFilter)(requestWithSomeCookies)

      whenReady(filterResult) { result =>
        info("Request passed to the next filter should have the session cookies")
        nextFilter.passedRequest.cookies should contain(trackingIdCookie)
        nextFilter.passedRequest.cookies should contain(sessionCookie)

        info("Request should still contain the existing cookies")
        nextFilter.passedRequest.cookies should contain(existingRequestCookie1)
        nextFilter.passedRequest.cookies should contain(existingRequestCookie1)

        nextFilter.passedRequest.cookies should have size 4

        val responseCookies = toCookies(result)
        responseCookies should contain(trackingIdCookie)
        responseCookies should contain(sessionCookie)
        responseCookies should have size 2
      }

      verify(sessionFactory, never()).getSession(any())
  }

  "Not do anything if the session cookies are already present" in setUp {
    case SetUp(filter, request, sessionFactory, nextFilter) =>
      val existingRequestCookie1 = Cookie("existingRequestCookie1", "existingRequestCookie1Value")
      val existingRequestCookie2 = Cookie("existingRequestCookie2", "sexistingRequestCookie2Value")
      val requestWithSomeCookies = request.withCookies(existingRequestCookie1, existingRequestCookie2)

      when(sessionFactory.newSessionCookiesIfNeeded(requestWithSomeCookies.cookies)).thenReturn(None)

      val filterResult = filter.apply(nextFilter)(requestWithSomeCookies)

      info("Request should still contain the existing cookies")
      nextFilter.passedRequest.cookies should contain(existingRequestCookie1)
      nextFilter.passedRequest.cookies should contain(existingRequestCookie1)
      nextFilter.passedRequest.cookies should have size 2

      whenReady(filterResult) { result =>
        toCookies(result) should be(empty)
      }

      verify(sessionFactory, never()).getSession(any())
  }

  "Throw an exception if the session factory throws an exception" in setUp {
    case SetUp(filter, request, sessionFactory, nextFilter) =>
      when(sessionFactory.newSessionCookiesIfNeeded(any())).thenThrow(new InvalidSessionException(""))

      intercept[InvalidSessionException] {
        filter.apply(nextFilter)(request)
      }
  }

  private def toCookies(result: SimpleResult): Seq[Cookie] = {
    Cookies(result.header.headers.get(HeaderNames.SET_COOKIE)).cookies.map {
      case (k, cookie) => cookie
    }.toSeq
  }

  private class MockFilter extends ((RequestHeader) => Future[SimpleResult]) {
    var passedRequest: RequestHeader = _

    override def apply(rh: RequestHeader): Future[SimpleResult] = {
      passedRequest = rh
      Future(Results.Ok)
    }
  }

  private case class SetUp(filter: EnsureSessionCreatedFilter,
                           request: FakeRequest[_],
                           sessionFactory:ClientSideSessionFactory,
                           nextFilter: MockFilter)

  private def setUp(test: SetUp => Any) {
    val sessionFactory = mock[ClientSideSessionFactory]
    val injector = Guice.createInjector(new ScalaModule {
      override def configure(): Unit = bind[ClientSideSessionFactory].toInstance(sessionFactory)
    })

    test(SetUp(
      filter = injector.getInstance(classOf[EnsureSessionCreatedFilter]),
      request = FakeRequest(),
      sessionFactory = sessionFactory,
      nextFilter = new MockFilter()
    ))
  }
}