package fairshare.frontend

import org.widok._
import org.widok.bindings.HTML

object FrontendApp extends PageApplication {
  def view(): View = Inline(
    HTML.Heading.Level1("Welcome to FairShare!"),
    HTML.Paragraph("This is just a beginning.")
  )

  def ready(): Unit = {
    log("Page loaded.")
  }
}
