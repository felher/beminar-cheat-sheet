import com.raquo.laminar.api.L.*
import org.felher.beminar.*

final case class Section(
    title: String,
    declarations: String,
    examples: List[Example]
)

object Section:
  val bem = Bem("/section")

  def render(section: Section): HtmlElement =
    val noEitherOrColumn = section.examples.forall(_.mods.size <= 1)
    div(
      bem,
      div(
        bem("/title"),
        section.title
      ),
      div(
        bem("/declarations"),
        section.declarations
      ),
      div(
        bem("/examples", ~noEitherOrColumn),
        div(
          bem("/examples-title", "code"),
          "Code"
        ),
        div(
          bem("/examples-title", "classes"),
          "Generated Classes"
        ),
        section.examples.map(Example.render)
      )
    )

  inline def fromCode(title: String)(inline code: Any): Section =
    ${ SectionMacroImpl.expandSection('title, 'code) }
