package filters.csrf_prevention

import com.google.inject.Inject
import common.ClientSideSessionFactory
import play.api.mvc.{EssentialAction, EssentialFilter}

class CsrfPreventionFilter @Inject()
                           (implicit clientSideSessionFactory: ClientSideSessionFactory) extends EssentialFilter {

  def apply(next: EssentialAction): EssentialAction = new CsrfPreventionAction(next)
}