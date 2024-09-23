import com.raquo.laminar.api.L.*
import org.scalajs.dom
import org.felher.beminar.*

@main def main(): Unit =
  val bem = Bem("/app")

  val data = List(
    Section.fromCode("Blocks, Elements and Sub-Elements"):
      import org.felher.beminar.Bem
      val bem = Bem("/profile")

      List(
        bem,
        bem("/picture"),
        bem("/picture", "/sub-element")
      )
    ,
    Section.fromCode("Boolean Modifiers"):
      //@formatter:off
      import org.felher.beminar.Bem
      val bem = Bem("/profile")
      val isFriend: Boolean = ???
      val isFriendSig: Signal[Boolean] = ???
      val isModerator: Signal[Boolean] = ???
      val isMap: Map["is-friend" | "is-moderator", Boolean] = ???
      val isMapSig: Map["is-friend" | "is-moderator", Signal[Boolean]] = ???
      val sigIsMap: Signal[Map["is-friend" | "is-moderator", Boolean]] = ???

      List(
        bem("/picture", "is-friend" -> false),
        bem("/picture", "is-friend" -> true),
        bem("/picture", "is-friend" -> isFriend),
        bem("/picture", "is-friend" -> isFriendSig),
        bem("/picture", ~isFriend),
        bem(
          "/picture",
          ~isFriend,
          ~isModerator
        ),
        bem(isMap),
        bem(isMapSig),
        bem(sigIsMap)
      )
      //@formatter:on
    ,
    Section.fromCode("Raw String Modifiers"):
      //@formatter:off
      import org.felher.beminar.Bem
      val bem  = Bem("/profile")
      val state: "is-own" | "is-other" = ???
      val stageSig: Signal["is-own" | "is-other"] = ???
      //@formatter:on

      List(
        bem("is-own"),
        bem(state),
        bem(stageSig)
      )
    ,
    Section.fromCode("String Modifiers"):
      //@formatter:off
      import org.felher.beminar.Bem
      val bem  = Bem("/accordion")
      val state: "open" | "closed"  = ???
      val stateSig: Signal["open" | "closed"] = ???
      val stateMap: Map["state" | "featured", "yes" | "no"] = ???
      val stateMapSig: Map["state" | "featured", Signal["yes" | "no"]] = ???
      val sigStateMap: Signal[Map["state" | "featured", "yes" | "no"]] = ???
      //@formatter:on

      List(
        bem(~state),
        bem(~stateSig),
        bem("state" -> "open"),
        bem("state" -> state),
        bem("state" -> stateSig),
        bem(stateMap),
        bem(stateMapSig),
        bem(sigStateMap)
      )
    ,
    Section.fromCode("Int Modifiers"):
      //@formatter:off
      import org.felher.beminar.Bem
      val bem  = Bem("/table-cell")
      val colSize = 1
      val colSizeSig: Signal[Int] = ???
      val sizes: Map["col-size" | "row-size", Int] = ???
      val sizsedSig: Map["col-size" | "row-size", Signal[Int]] = ???
      val sigSizes: Signal[Map["col-size" | "row-size", Int]] = ???
      //@formatter:on

      List(
        bem("col-size" -> 1),
        bem("col-size" -> colSize),
        bem("col-size" -> colSizeSig),
        bem(sizes),
        bem(sizsedSig),
        bem(sigSizes)
      )
  )

  val app =
    div(
      bem,
      data.map(Section.render)
    )

  render(dom.document.getElementsByTagName("body")(0), app)
  ()
