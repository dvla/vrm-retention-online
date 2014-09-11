package composition

import com.google.inject.util.Modules
import com.google.inject.{Guice, Injector, Module}

trait TestComposition extends Composition {

  override lazy val injector: Injector = Guice.createInjector(new TestModule())

  def testModule(module: Module*) = Modules.`override`(new TestModule()).`with`(module: _*)

  def testInjector(module: Module*) = Guice.createInjector(testModule(module: _*))

  def testInjectorOverrideDev(module: Module*) = {
    val overridingModule = Modules.`override`(new DevModule()).`with`(module: _*)
    Guice.createInjector(overridingModule)
  }
}