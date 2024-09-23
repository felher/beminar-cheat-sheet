import scala.quoted.*
import com.raquo.laminar.api.L.*
import com.raquo.laminar.nodes.ReactiveHtmlElement
import scala.compiletime.asMatchable

class SectionMacroImpl(using val q: Quotes):
  import q.reflect.*
  given CanEqual[Symbol, Symbol] = CanEqual.derived

  def expandSection(title: Expr[String], code: Expr[Any]): Expr[Section] =
    val (statements, bemExpressions) = code.asTerm match
      case Inlined(_, _, Block(statements, Apply(_, List(Typed(Repeated(bemExpressions, _), _))))) =>
        (statements, bemExpressions)

      case _ => report.errorAndAbort("Unknown code structure")

    val declarationsText = Expr(statements.map(_.pos.sourceCode.get).mkString("\n"))
    val examples         = Expr.ofList(bemExpressions.map(makeExample(_, getSymbolsAndInstancesForValDefs(statements))))

    '{
      Section(
        title = $title,
        declarations = $declarationsText,
        examples = ${ examples }
      )
    }

  private def makeExample(bemExpr: Term, allInstances: Map[Symbol, List[Expr[Any]]]): Expr[Example] =
    val symbolsUsed      = getAllReferencedSymbols(allInstances.keySet, bemExpr)
    val reducedInstances = allInstances.view.filterKeys(symbolsUsed.contains).toMap
    val usedInstances    = allSubsets(reducedInstances)
    val replacedExprs    = usedInstances.map(replaceInUser(_, bemExpr)).map(_.asExprOf[Modifier[ReactiveHtmlElement.Base]])
    val modifiers        = Expr.ofList(replacedExprs)
    '{
      Example(
        bemExpr = ${ Expr(reindent(bemExpr.pos)) },
        mods = $modifiers
      )
    }

  private def allSubsets[K, V](m: Map[K, List[V]]): List[Map[K, V]] =
    m.foldLeft(List(Map.empty[K, V])) { case (acc, (k, vs)) =>
      for
        a <- acc
        v <- vs
      yield a + (k -> v)
    }

  private def replaceInUser(replacements: Map[Symbol, Expr[Any]], user: Term): Term =
    val transformer = new TreeMap:
      override def transformTerm(tree: Term)(owner: Symbol): Term =
        tree match
          case i: Ident if replacements.contains(i.symbol) => replacements(i.symbol).asTerm
          case _                                           => super.transformTerm(tree)(owner)

    transformer.transformTerm(user)(Symbol.spliceOwner)

  private def getSymbolsAndInstancesForValDefs(statements: List[Statement]): Map[Symbol, List[Expr[Any]]] =
    statements
      .flatMap:
        case vd: ValDef =>
          if isUnimpemented(vd.rhs.get) then List(vd.symbol -> buildInstances(vd.tpt.tpe))
          else List(vd.symbol                               -> List(vd.rhs.get.asExpr))
        case _          => Nil
      .toMap

  private def isUnimpemented(t: Tree): Boolean =
    t match
      case i: Ident => i.symbol == Symbol.requiredMethod("scala.Predef.???")
      case _        => false

  private def buildInstances(tpe: TypeRepr): List[Expr[Any]] =
    val mapTycon = Symbol.requiredClass("scala.collection.immutable.Map").typeRef

    if tpe =:= TypeRepr.of[Boolean] then List('{ false }, '{ true })
    else if tpe =:= TypeRepr.of[Int] then List('{ 1 }, '{ 2 }, '{ 3 })
    else
      tpe.asMatchable match
        case ConstantType(StringConstant(value))            => List(Expr(value))
        case OrType(left, right)                            => buildInstances(left) ++ buildInstances(right)
        case AppliedType(tycon, args) if tycon =:= mapTycon =>
          val keyRepr   = args(0)
          val valueRepr = args(1)
          println("Map for key: " + keyRepr.show)
          println("Map for value: " + valueRepr.show)
          keyRepr.asType match
            case '[k] =>
              valueRepr.asType match
                case '[v] =>
                  val keysExprs   = buildInstances(keyRepr)
                  val valuesExprs = buildInstances(valueRepr)
                  val all         = allSubsets(keysExprs.map(keyExpr => keyExpr -> valuesExprs).toMap)
                  all.map: exprMap =>
                    val tuples = exprMap.toList
                    '{ ${ Expr.ofList(tuples.map(Expr.ofTuple)) }.toMap.asInstanceOf[Map[k, v]] }
        case _                                              =>
          tpe.asType match
            case '[Signal[t]] =>
              val innerRepr = TypeRepr.of[t]
              buildInstances(innerRepr).map(expr => '{ Signal.fromValue($expr.asInstanceOf[t]) })
            case _            => report.errorAndAbort("Unknown type: " + tpe)

  private def getAllReferencedSymbols(usable: Set[Symbol], user: Term): Set[Symbol] =
    val accu = new TreeAccumulator[Set[Symbol]]:
      override def foldTree(symbolsFound: Set[Symbol], tree: Tree)(owner: Symbol): Set[Symbol] =
        tree match
          case i: Ident if usable.contains(i.symbol) => symbolsFound + i.symbol
          case _                                     => foldOverTree(symbolsFound, tree)(owner)

    accu.foldTree(Set.empty, user)(Symbol.spliceOwner)

  /* This method can only reindent a very specific kind of very simple expressions */
  private def reindent(pos: Position): String =
    val text  = pos.sourceCode.get
    val lines = text.split("\n").toList

    lines match
      case Nil          => ""
      case head :: Nil  => head
      case head :: tail =>
        val levels     = tail.map(_.takeWhile(_.isWhitespace).length).sorted.distinct
        val reindented = tail.map: line =>
          val indent = line.takeWhile(_.isWhitespace).length
          val level  = levels.indexOf(indent)
          "  " * level + line.trim
        (head :: reindented).mkString("\n")

object SectionMacroImpl:
  def expandSection(title: Expr[String], code: Expr[Any])(using Quotes): Expr[Section] =
    new SectionMacroImpl(using summon).expandSection(title, code)
