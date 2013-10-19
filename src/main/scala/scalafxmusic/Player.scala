package scalafxmusic

import scalafx.Includes._
import scalafx.scene.media.{Media, MediaPlayer}
import scalafx.application.Platform
import scalafx.beans.property.{DoubleProperty, StringProperty}
import scalafx.scene.control.{Slider, Label}
import scalafx.scene.layout.HBox
import scalafx.scene.input.MouseEvent
import scalafx.util.Duration
import scalafx.geometry.Insets
import scalafx.event.subscriptions.Subscription
import akka.actor.Actor


class Player(box: HBox) extends Actor {

  var volume = 10
  // in %
  var mediaPlayer: MediaPlayer = null

  // subscriptions to update GUI
  private var currentTimeSub: Subscription = _
  // properties used to bind data
  val currentTime = new StringProperty()
  val sliderPosition = new DoubleProperty()
  val title = new StringProperty()

  val titleLabel = new Label {
    text <== title
  }

  def receive: Actor.Receive = {

    case vol: VolumeDetla => updateVolume(vol.level)

    case "Play" => play()

    case "Pause" => pause()

    case "Stop" => stop()

    case song: NewSong => newSong(song.media)
  }

  def updateVolume(vol: Int) {
    Platform.runLater {
      volume += vol
      if (mediaPlayer != null) {
        mediaPlayer.setVolume(volume / 100.0)
      }
      println("Volume = " + volume)
      context.actorSelection("/user/controls") ! new VolumeUpdate(volume)
    }
  }

  def newSong(media: Media) = {
    Platform.runLater {
      if (mediaPlayer != null) {
        // remove old binding
        removeBinding()
        mediaPlayer.stop()
      }
      mediaPlayer = new MediaPlayer(media)
      mediaPlayer.setVolume(volume / 100.0)
      mediaPlayer.play()

      mediaPlayer.onEndOfMedia = {
        context.actorSelection("/us er/playlist") ! "EndOfMedia"
      }
//      title.set(media.source.split("/").last)

      title.set( media.getMetadata().get("artist") + " - " + media.getMetadata().get("title") )
      addAutomaticBinding(mediaPlayer)
    }
  }

  def play() = if (mediaPlayer != null) mediaPlayer.play()

  def stop() = if (mediaPlayer != null) mediaPlayer.stop()

  def pause() = if (mediaPlayer != null) mediaPlayer.pause()


  private def addAutomaticBinding(mediaPlayer: MediaPlayer) = {
    currentTimeSub = mediaPlayer.currentTime.onChange {
      Platform.runLater {
        updateCurrentTime(math.round(mediaPlayer.currentTime.value.toSeconds),
          math.round(mediaPlayer.getMedia.getDuration.toSeconds))
      }
    }
  }

  private def removeBinding() = {
    currentTimeSub.cancel()
  }

  private def updateCurrentTime(time: Double, total: Double) = {
    val minutes = (time / 60).toInt
    val seconds = (time - minutes * 60).toInt
    val totalMinutes = (total / 60).toInt
    val totalSeconds = (total - totalMinutes * 60).toInt
    currentTime.set(
      "%02d:%02d / %02d:%02d".format(minutes, seconds, totalMinutes, totalSeconds)
    )
    sliderPosition.set(time / total)
  }


  val slider = new Slider {
    min = 0
    max = 1
    value <== sliderPosition
  }
  slider.onMousePressed = (event: MouseEvent) => {
    Platform.runLater {
      if (mediaPlayer != null) {
        mediaPlayer.seek(new Duration(
          (((event.x - 5) / 130.0) * mediaPlayer.getMedia.getDuration.toSeconds) s)
        )
        mediaPlayer.play()
      }
    }
  }

  val currentTimeLabel = new Label {
    // binding current time text to currentTime property
    text <== currentTime
  }

  createView()

  def createView() = {
    box.padding = Insets(10)
    box.spacing = 10
    box.content = Seq(
      slider,
      currentTimeLabel,
      titleLabel
    )
  }

}
