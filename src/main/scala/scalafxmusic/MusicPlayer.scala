package scalafxmusic

import scalafx.Includes._
import scalafx.application.JFXApp
import scalafx.application.JFXApp.PrimaryStage
import scalafx.scene.Scene
import scalafx.scene.layout.{HBox, VBox}
import scalafx.scene.control._
import scala.Predef._

import akka.actor.ActorSystem
import akka.actor.Props



object MusicPlayer extends JFXApp {


  val system = ActorSystem("System")

  var menuBar = new MenuBar()

  val lbox = new VBox() {
    minHeight = 200
  }
  val playlist = system.actorOf(Props(classOf[Playlist], menuBar, lbox), name = "playlist")

  var currentSongView = new HBox()
  val player = system.actorOf(Props(classOf[Player], currentSongView), name = "player")

  var controlsViewBox = new HBox()
  val controlsAct = system.actorOf(Props(classOf[Controls], controlsViewBox), name = "controls")
  controlsAct ! new VolumeUpdate(50)

  stage = new PrimaryStage {
    title = "ScalaFX MediaPlayer"
    width = 600
    height = 450
    minWidth = 600
    minHeight = 450

    scene = new Scene {
      root = new VBox {
        content = Seq(
          menuBar,
          lbox,
          currentSongView,
          controlsViewBox
        )
      }
    }
  }


  // call when app when app is closed
  override def stopApp() {
    // stop actors systems
    system.shutdown()
  }
}














