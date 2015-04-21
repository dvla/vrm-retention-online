package composition

import play.api.GlobalSettings
import play.api.mvc.EssentialAction
import play.api.mvc.Filters

trait WithFilters extends Composition with GlobalSettings {

  override def doFilter(a: EssentialAction): EssentialAction = {
    Filters(super.doFilter(a), filters: _*)
  }
}