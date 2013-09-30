package scalafx_sample

import scalafx.Includes._
import scalafx.event.subscriptions.Subscription
import scalafx.beans.property.{DoubleProperty, StringProperty}
import scalafx.scene.control.{Slider, Label}
import scalafx.application.Platform
import scalafx.scene.layout.HBox
import scalafx.scene.Node
import scalafx.geometry.Insets
import scalafx.scene.media.MediaPlayer
import scalafx.scene.input.MouseEvent
import scalafx.util.Duration


class PositionSlider(mp: MusicPlayer) {

  // subscriptions to update GUI
  private var currentTimeSub: Subscription = _

  // properties used to bind data
  val currentTime = new StringProperty()
  val sliderPosition = new DoubleProperty()
  val title = new StringProperty()

  sliderPosition.set(0.0)

  val titleLabel = new Label {
    text <== title
  }

  val currentTimeLabel = new Label {
    // binding current time text to currentTime property
    text <== currentTime
  }

  val slider = new Slider {
    min = 0
    max = 1
    value <== sliderPosition
  }
  slider.onMousePressed = (event: MouseEvent) => {
    if(mp.mediaPlayer != null) {
      mp.mediaPlayer.seek(new Duration((((event.x-5) / 130.0) * mp.mediaPlayer.getMedia.getDuration.toSeconds) s))
      mp.mediaPlayer.play()
    }
  }


  def updateCurrentTime(time: Double, total: Double) = {
    val minutes = (time / 60).toInt
    val seconds = (time - minutes * 60).toInt
    val totalMinutes = (total / 60).toInt
    val totalSeconds = (total - totalMinutes * 60).toInt
    currentTime.set(
      "%02d:%02d / %02d:%02d".format(minutes, seconds, totalMinutes, totalSeconds)
    )
    sliderPosition.set(time / total)
  }

  def addAutomaticBinding(mediaPlayer: MediaPlayer) = {
    currentTimeSub = mediaPlayer.currentTime.onInvalidate {
      Platform.runLater {
        updateCurrentTime(math.round(mediaPlayer.currentTime.value.toSeconds),
          math.round(mediaPlayer.getMedia.getDuration.toSeconds))
      }
    }
  }

  def removeAutomaticBinding() = {
    currentTimeSub.cancel()
  }

  def positionSliderView(): Node = {
    new HBox() {
      padding = Insets(10)
      spacing = 10
      content = Seq(slider, currentTimeLabel, titleLabel)
    }
  }


}
