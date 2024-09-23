import com.raquo.laminar.api.L.*
import org.felher.beminar.*

object ClassDiv:
  val bem = Bem("/class-div")

  def render(): HtmlElement =
    val classes = Var(List.empty[String])

    val elem = div(
      bem,
      children <-- classes.signal.map: classes =>
        classes.map: cls =>
          div(
            bem("/class"),
            cls
          )
    )

    def updateClasses(): Unit =
      val classList = elem.ref.classList
      val numParts  = classList.length

      val relevantClasses = (0 until numParts).toList
        .map(classList.item)
        .filter(!_.startsWith(("class-div")))

      val newClasses = relevantClasses
        .zipWithIndex
        .map: (cls, i) =>
          if i == relevantClasses.length - 1 then cls
          else cls + ","

      classes.set(newClasses)

    val observer = org.scalajs.dom.MutationObserver((_, _) => updateClasses())
    observer.observe(elem.ref, new org.scalajs.dom.MutationObserverInit { attributes = true })

    elem
