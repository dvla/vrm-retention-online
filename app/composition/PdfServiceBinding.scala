package composition

import com.tzavellas.sse.guice.ScalaModule
import pdf.{PdfService, PdfServiceImpl}

final class PdfServiceBinding extends ScalaModule {

  def configure() = bind[PdfService].to[PdfServiceImpl].asEagerSingleton()
}