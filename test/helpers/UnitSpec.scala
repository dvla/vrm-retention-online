package helpers

import composition.TestComposition
import org.scalatest.concurrent.PatienceConfiguration.Timeout
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mock.MockitoSugar
import org.scalatest.time.Second
import org.scalatest.time.Span
import org.scalatest.Matchers
import org.scalatest.WordSpec

import scala.concurrent.duration._

abstract class UnitSpec extends WordSpec with Matchers with MockitoSugar with ScalaFutures with TestComposition {

  protected val timeout = Timeout(Span(1, Second))
  protected val finiteTimeout = FiniteDuration(2, SECONDS)
}