package scalafx_sample

import scalafx.Includes._
import scalafx.scene.control.{Label, Button}
import scalafx.scene.layout.HBox
import scalafx.geometry.Insets
import scalafx.beans.property.StringProperty
import scalafx.scene.Node

class Controls(mp: MusicPlayer) {

  // properties used to bind data
  private val volumeLevelProp = new StringProperty()
  volumeLevelProp.set(math.round(mp.volume * 100).toString + "%")


  def initControlView(): Node = {
    val controlView = new HBox() {

      padding = Insets(20)
      spacing = 10
      val play = new Button {
        text = "play"
        onAction =  mp.play()
      }

      val pause = new Button {
        text = "pause"
        onAction =  mp.pause()
      }

      val stop = new Button {
        text = "stop"
        onAction =  mp.stop()
      }

      val previous = new Button {
        text = "previous"
        onAction =  mp.previous()
      }

      val next = new Button {
        text = "next"
        onAction =  mp.next()
      }

      val volumeDown = new Button {
        text = "volume down"
        onAction = volDown()
      }

      val volumeUp = new Button {
        text = "volume up"
        onAction = volUp()
      }

      val volumeLevel = new Label {
        text <== volumeLevelProp
      }

      content = List(
        play, pause, stop, previous, next, volumeDown, volumeLevel, volumeUp
      )
    }
    controlView
  }

  def volUp() {
    if (mp.volume <= 0.9) {
      mp.volume += 0.1
      if(mp.mediaPlayer != null) mp.mediaPlayer.setVolume(mp.volume)
      volumeLevelProp.set(math.round(mp.volume * 100).toString + "%")
    }
  }

  def volDown() = {
    if (mp.volume >= 0.1) {
      mp.volume -= 0.1
      if(mp.mediaPlayer != null) mp.mediaPlayer.setVolume(mp.volume)
      volumeLevelProp.set(math.round(mp.volume * 100).toString + "%")
    }

  }
}
