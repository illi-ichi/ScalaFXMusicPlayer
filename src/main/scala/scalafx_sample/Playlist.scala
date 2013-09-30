package scalafx_sample

import scalafx.Includes._
import scalafx.scene.media.Media
import scalafx.scene.Node
import scalafx.scene.control.ListView
import scalafx.scene.layout.VBox
import javafx.collections.FXCollections
import scalafx.scene.input.MouseEvent
import scalafx.scene.input.{KeyEvent, KeyCode}
import scalafx.application.Platform

class Playlist(var mp: MusicPlayer) {

  private var medias = Vector[Media]()
  private var currentCursor: Int = 0

  // observable list
  private var titlesList = FXCollections.observableArrayList[String]()

  def getMediasList(): Vector[Media] = this.medias

  def currentTitle(): String = {
    medias(currentCursor).source.split("/").last
  }

  def addSong(newMedia: Media) = {
    medias = medias :+ newMedia
    titlesList.add(newMedia.source.split("/").last)
  }

  def removeSong(indice: Int) = {
    titlesList.remove(indice)
    medias = medias.patch (from = indice, patch = Nil, replaced = 1)
  }

  def previousSong(): Media = {
    currentCursor -= 1
    medias(currentCursor)
  }

  def nextSong(): Media = {
    currentCursor += 1
    medias(currentCursor)
  }

  def isFirstSong(): Boolean = if (currentCursor > 0) false else true

  def isLastSong(): Boolean = if (currentCursor < medias.size - 1) false else true

  val list = new ListView[String]() {
    minHeight = 200
    items = titlesList
  }
  // on double click play music
  list.onMouseClicked = (event: MouseEvent) => {
    if (event.clickCount > 1) {
      Platform.runLater {
        currentCursor = list.getSelectionModel().getSelectedIndices()(0)
        mp.newSong(medias(currentCursor))
        list.getSelectionModel().selectIndices(currentCursor)
        list.getFocusModel().focus(currentCursor)
        list.scrollTo(currentCursor)
      }
    }
  }
  list.onKeyReleased = (event: KeyEvent) => {
    if (event.code == KeyCode.DELETE) {
        val indice = list.getSelectionModel().getSelectedIndices()(0)
        removeSong(0)
    }
  }


  def playListView(): Node = {
    new VBox() {
      content = Seq(list)
    }
  }

}
