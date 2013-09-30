package scalafx_sample

import java.io.File
import scalafx.Includes._
import scalafx.beans.binding._
import scalafx.application.{Platform, JFXApp}
import scalafx.application.JFXApp.PrimaryStage
import scalafx.scene.Scene
import scalafx.scene.media.{Media, MediaPlayer}
import scalafx.scene.layout.{VBox}
import scalafx.scene.control._
import scalafx.stage.FileChooser


object MusicPlayer extends JFXApp {

  var player = new MusicPlayer()

  stage = new PrimaryStage {
    title = "ScalaFX MediaPlayer"
    width = 600
    height = 450
    minWidth = 600
    minHeight = 450

    val menuBar = new MenuBar();

    val menuPlayList = new Menu("PlayList") {
      items = List(
        new MenuItem("Add file in Playlist") {
          onAction = addFileInPlaylist()
        }
      )
    }

    menuBar.getMenus().addAll(menuPlayList);

    scene = new Scene {
      root = new VBox {
        content = Seq(
          menuBar,
          player.playlst.playListView(),
          player.positionSlider.positionSliderView(),
          new Controls(player).initControlView()
        )
      }
    }

    def addFileInPlaylist() = {
      val fileChooser = new FileChooser()
      val filePath = fileChooser.showOpenDialog(stage)
      player.playlst.addSong(new Media(new File(filePath.toString).toURI().toString))
    }
  }
}

class MusicPlayer() {

  var volume = 0.1
  var mediaPlayer: MediaPlayer = null

  val playlst = new Playlist(this)

  val positionSlider = new PositionSlider(this)

  def newSong(media: Media) = {
    Platform.runLater {
      if (mediaPlayer != null) {
        positionSlider.removeAutomaticBinding()
        mediaPlayer.stop()
      }
      mediaPlayer = new MediaPlayer(media)
      mediaPlayer.setVolume(volume)
      mediaPlayer.play()
      positionSlider.title.set(playlst.currentTitle())
      positionSlider.addAutomaticBinding(mediaPlayer)
    }
  }

  def play() = if (mediaPlayer != null) mediaPlayer.play()

  def stop() = if (mediaPlayer != null) mediaPlayer.stop()

  def pause() = if (mediaPlayer != null) mediaPlayer.pause()

  def previous() = {
    if (!playlst.isFirstSong) newSong(playlst.previousSong())
  }

  def next() = {
    if (!playlst.isLastSong) {
      newSong(playlst.nextSong())
    }
  }

  def currentTitle(): String = {
    if (mediaPlayer != null && mediaPlayer.getStatus() == MediaPlayer.Status.READY) {
      playlst.currentTitle()
    } else ""
  }
}


