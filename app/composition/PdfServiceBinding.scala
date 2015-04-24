package composition

import com.tzavellas.sse.guice.ScalaModule
import pdf.PdfService
import pdf.PdfServiceImpl

final class PdfServiceBinding extends ScalaModule {

  def configure() = bind[PdfService].to[PdfServiceImpl].asEagerSingleton()
}