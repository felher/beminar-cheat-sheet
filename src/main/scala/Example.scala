import com.raquo.laminar.api.L.*
import com.raquo.laminar.nodes.ReactiveHtmlElement
import org.felher.beminar.*

final case class Example(
    bemExpr: String,
    mods: List[Modifier[ReactiveHtmlElement.Base]]
)

object Example:
  val bem = Bem("/example")

  def render(example: Example): HtmlElement =
    div(
      bem,
      div(
        bem("/expr"),
        example.bemExpr
      ),
      div(
        bem("/mods"),
        example.mods.zipWithIndex.flatMap: (mod, i) =>
          val isFirst    = i == 0
          val showOr     = i > 0
          val showEither = i == 0 && example.mods.length > 1

          List(
            div(
              bem("/or", ~showOr, ~showEither),
              div("either"),
              div("or")
            ),
            div(),
            div(bem("/classes", ~isFirst), ClassDiv.render().amend(mod))
          )
      )
    )
