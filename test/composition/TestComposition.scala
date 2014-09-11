package composition

import com.google.inject.util.Modules
import com.google.inject.{Guice, Injector, Module}

trait TestComposition extends Composition {

  override lazy val injector: Injector = Guice.createInjector(new TestModule())

  def testInjectorOverrideDev(modules: Module*) = {
    val overriddenDevModule = Modules.`override`(new DevModule()).`with`(modules: _*)
    Guice.createInjector(overriddenDevModule)
  }
}