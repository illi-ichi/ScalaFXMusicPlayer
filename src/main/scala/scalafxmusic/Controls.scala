package scalafxmusic

import scalafx.Includes._
import scalafx.scene.control.{Label, Button}
import scalafx.scene.layout.HBox
import scalafx.geometry.Insets
import scalafx.beans.property.StringProperty
import scalafx.application.Platform
import akka.actor.{ActorRef, Actor}

case class VolumeUpdate(level :Int)

case class VolumeDetla(level :Int)

class Controls(box: HBox) extends Actor {

  private var volumeLevel = 0
  private val volProp = new StringProperty()
  volProp.set(" ")

  def receive: Actor.Receive = {

    case vol: VolumeUpdate => {
      Platform.runLater {
        volumeLevel = vol.level
        volProp.set(volumeLevel.toString + "%")
      }
    }

  }


  def volDown() = {
    Platform.runLater {
      if (volumeLevel >= 10) {
        context.actorSelection("/user/player") ! new VolumeDetla(-10)
      }
    }
  }

  def volUp() = {
    Platform.runLater {
      if (volumeLevel <= 90) {
        context.actorSelection("/user/player") ! new VolumeDetla(+10)
      }
    }
  }

  createView()


  def createView() = {

    val play = new Button {
      text = "play"
      onAction = context.actorSelection("/user/player") ! "Play"
    }

    val pause = new Button {
      text = "pause"
      onAction = context.actorSelection("/user/player") ! "Pause"
    }

    val stop = new Button {
      text = "stop"
      onAction = context.actorSelection("/user/player") ! "Stop"
    }

    val previous = new Button {
      text = "previous"
      onAction = context.actorSelection("/user/playlist") ! "Previous"
    }

    val next = new Button {
      text = "next"
      onAction = context.actorSelection("/user/playlist") ! "Next"
    }

    val volumeDown = new Button {
      text = "volume down"
      onAction = volDown()
    }

    val volumeUp = new Button {
      text = "volume up"
      onAction = volUp()
    }


    val volumeLevelLabel = new Label {
      text <== volProp
    }

    box.padding = Insets(20)
    box.spacing = 10
    box.content = Seq(
      play, pause, stop, previous, next, volumeDown, volumeLevelLabel, volumeUp
    )
  }

}
