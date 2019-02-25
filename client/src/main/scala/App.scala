import scala.scalajs.js
import scalatags.JsDom.all._
import scala.scalajs._
import scala.scalajs.js.annotation._
import org.scalajs.dom

import org.scalajs.dom
import org.scalajs.dom.html
import scala.util.Random
import scala.scalajs.js
import js.annotation.JSExport

import com.ofenbeck.nmm.logic._

// 0 1 2 3 4 5 6 7 8 9 101112
// 0 - - - - - 1 - - - - - 2
// |           |           |
// |   3 - - - 4 - - - 5   |
// |   |       |       |   |
// |   |   6 - 7 - 8   |   |
// |   |   |       |   |   |
// 9 - 10-11       12- 13-14
// |   |   |       |   |   |
// |   |   15-16 -17   |   |
// |   |       |       |   |
// |   18- - -19 - - -20   |
// |           |           |
// 21- - - - -22 - - - - -23

case class Point(x: Double, y: Double) {
  def +(p: Point) = Point(x + p.x, y + p.y)
  def -(p: Point) = Point(x - p.x, y - p.y)
  def *(d: Double) = Point(x * d, y * d)
  def /(d: Double) = Point(x / d, y / d)
  def length = Math.sqrt(x * x + y * y)
}

class Enemy(var pos: Point, var vel: Point)
@JSExportTopLevel("MyApp")
object MyApp {
  var startTime = js.Date.now()

  val canvas = dom.document
    .getElementById("mycanvas")
    .asInstanceOf[html.Canvas]
  val ctx = canvas
    .getContext("2d")
    .asInstanceOf[dom.CanvasRenderingContext2D]
  var player =
    Point(dom.window.innerWidth.toInt / 2, dom.window.innerHeight.toInt / 2)

  var enemies = Seq.empty[Enemy]
  var death: Option[(String, Int)] = None

  var graph: GameGraph with GameGraph2Viz = com.ofenbeck.nmm.logic.NMM2d_empty
  val spacing = 10
  val markersize = 10

  def run() = {

    canvas.height = dom.window.innerHeight.toInt
    canvas.width = dom.window.innerWidth.toInt

    // 0 1 2 3 4 5 6 7 8 9 101112
    // 0 - - - - - 1 - - - - - 2
    // |           |           |
    // |   3 - - - 4 - - - 5   |
    // |   |       |       |   |
    // |   |   6 - 7 - 8   |   |
    // |   |   |       |   |   |
    // 9 - 10-11       12- 13-14
    // |   |   |       |   |   |
    // |   |   15-16 -17   |   |
    // |   |       |       |   |
    // |   18- - -19 - - -20   |
    // |           |           |
    // 21- - - - -22 - - - - -23

    // doing
    enemies = enemies.filter(
      e =>
        e.pos.x >= 0 && e.pos.x <= canvas.width &&
          e.pos.y >= 0 && e.pos.y <= canvas.height)

    def randSpeed = Random.nextInt(5) - 3
    enemies = enemies ++ Seq.fill(20 - enemies.length)(
      new Enemy(
        Point(Random.nextInt(canvas.width.toInt), 0),
        Point(randSpeed, randSpeed)
      )
    )

    for (enemy <- enemies) {
      enemy.pos = enemy.pos + enemy.vel
      val delta = player - enemy.pos
      enemy.vel = enemy.vel + delta / delta.length / 100
    }

    if (enemies.exists(e => (e.pos - player).length < 20)) {
      death = Some((s"You lasted $deltaT seconds", 100))
      enemies = enemies.filter(e => (e.pos - player).length > 20)
    }
  }

  def deltaT = ((js.Date.now() - startTime) / 1000).toInt

  def draw(graph: GameGraph with GameGraph2Viz) = {

    // drawing
    ctx.fillStyle = "white"
    ctx.fillRect(0, 0, canvas.width, canvas.height)

    graph.positions.map {
      case (id, pos) => {
        graph.states(id.id) match {
          case NoChip    => ctx.fillStyle = "grey"
          case WhiteChip => ctx.fillStyle = "cyan"
          case BlackChip => ctx.fillStyle = "red"
        }
        ctx.fillRect(pos.x * spacing, pos.y * spacing, markersize, markersize)
      }
    }

    graph.edges.map(e => {
      val posfrom = graph.positions(e.from)
      val posto = graph.positions(e.to)

      ctx.moveTo(posfrom.x * spacing + markersize / 2,
                 posfrom.y * spacing + markersize / 2)
      ctx.lineTo(posto.x * spacing + markersize / 2,
                 posto.y * spacing + markersize / 2)
      ctx.closePath();
      ctx.stroke();
    })
  }
  @JSExport
  def main(): Unit = {
    dom.console.log("main")

    dom.document.onmousedown = { (e: dom.MouseEvent) =>
      val click = new Pos(e.clientX.toInt, e.clientY.toInt, 0)

      graph.positions.map {
        case (id, pos) => {
          if (pos.x * spacing <= click.x && pos.x * spacing + markersize >= click.x &&
              pos.y * spacing <= click.y && pos.y * spacing + markersize >= click.y) {
            val ngraph = graph.states(id.id) match {
              case NoChip    => graph.update(id, WhiteChip)
              case WhiteChip => graph.update(id, BlackChip)
              case BlackChip => graph.update(id, NoChip)
            }
            graph = new GameGraph(ngraph.nodes, ngraph.edges, ngraph.states)
            with GameGraph2Viz {
              val positions = graph.positions
            }
            draw(graph)
          }
        }
      }
      (): js.Any
    }

    dom.document.onmousemove = { (e: dom.MouseEvent) =>
      player = Point(e.clientX.toInt, e.clientY.toInt)
      (): js.Any
    }
    draw(com.ofenbeck.nmm.logic.NMM2d_empty);
    //dom.window.setInterval(() => { run(); draw() }, 20)
  }
}
