package composition

import com.tzavellas.sse.guice.ScalaModule
import email.{EmailServiceImpl, EmailService}
import pdf.{PdfService, PdfServiceImpl}

final class EmailServiceBinding extends ScalaModule {

  def configure() = bind[EmailService].to[EmailServiceImpl].asEagerSingleton()
}