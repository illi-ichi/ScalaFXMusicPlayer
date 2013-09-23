package scalafx_sample

import java.io.File
import scalafx.Includes._
import scalafx.beans.binding._
import scalafx.application.{Platform, JFXApp}
import scalafx.application.JFXApp.PrimaryStage
import scalafx.scene.Scene
import scalafx.scene.media.{Media, MediaPlayer}

import scalafx.scene.layout.{BorderPane, HBox}
import scalafx.geometry.Insets
import scalafx.scene.control.{Button, Label}
import scalafx.beans.property.StringProperty

import scalafx.event.subscriptions.Subscription


object HelloScalaFX extends JFXApp {

  // subscriptions to update GUI
  private var currentTimeSub: Subscription = _
  private var volumeSub: Subscription = _

  // properties used to bind data
  private val currentTimeProp = new StringProperty()
  private val volumeLevelProp = new StringProperty()

  // open a file on computer. It can use also HTTP
  val MEDIA_URL = new File("./test.mp3").toURI().toString()

  stage = new PrimaryStage {
    title = "ScalaFX MediaPlayer"
    width = 450
    height = 150

    // create media player
    val media = new Media(MEDIA_URL)
    val mediaPlayer = new MediaPlayer(media)
    mediaPlayer.setAutoPlay(true)

    // start volume
    mediaPlayer.setVolume(1)

    val fileNameText = new Label {
      text = mediaPlayer.getMedia().getSource()
    }

    val currentTime = new Label {
      // binding current time text to currentTime property
      text <== currentTimeProp
    }
    currentTimeSub = mediaPlayer.currentTime.onChange {
      // when change
      Platform.runLater {
        currentTimeProp.set(math.round(mediaPlayer.currentTime.value.toSeconds).toString() + " / " +
          math.round(mediaPlayer.totalDuration().toSeconds).toString + " secs")
      }
    }

    val currentSongView = new HBox() {
      padding = Insets(20)
      spacing = 10
      content = List(currentTime, fileNameText)
    }

    val play = new Button {
      text = "play"
      onAction = {
        mediaPlayer.play()
      }
    }

    val pause = new Button {
      text = "pause"
      onAction = {
        mediaPlayer.pause()
      }
    }

    val stop = new Button {
      text = "stop"
      onAction = {
        mediaPlayer.stop()
      }
    }


    private def formatVolume(vol: Double) = {
      volumeLevelProp.set(math.round(vol * 100).toString + "%")
    }

    val volumeLevel = new Label {
      text <== volumeLevelProp
    }

    volumeSub = mediaPlayer.volume.onChange {
      // when change
      Platform.runLater {
        volumeLevelProp.set(math.round(mediaPlayer.getVolume() * 100).toString + "%")
      }
    }

    val volumeDown = new Button {
      text = "volume down"
      onAction = if( mediaPlayer.getVolume() > 0.0) mediaPlayer.setVolume(mediaPlayer.getVolume() - 0.0d)
    }

    val volumeUp = new Button {
      text = "volume up"
      onAction = if( mediaPlayer.getVolume() < 1.0) mediaPlayer.setVolume(mediaPlayer.getVolume() + 0.1d)
    }


    val controlsView = new HBox() {
      padding = Insets(20)
      spacing = 10
      content = List(
        play, pause, stop, volumeDown, volumeUp, volumeLevel
      )
    }


    scene = new Scene {
      root = new BorderPane {
        top = currentSongView
        bottom = controlsView
      }
    }

  }
}


