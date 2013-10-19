package scalafxmusic

import scalafx.Includes._
import scalafx.scene.media.Media
import scalafx.scene.control._
import scalafx.scene.input.MouseEvent
import scalafx.scene.input.{KeyEvent, KeyCode}
import scalafx.application.Platform
import scalafx.stage.{DirectoryChooser, Stage, FileChooser}
import java.io.File
import akka.actor.Actor
import scalafx.scene.layout.VBox

case class NewSong(media: Media)

case class RemoveSong(media: Media)

class Playlist(var menuBar: MenuBar, var vbox :VBox) extends Actor {

  case class Metatag(title: String, album :String)

  // medias data
  private var medias = Vector[Media]()
  private var metatags = Vector[Metatag]()

  // current playing indice
  private var currentCursor: Int = 0


  def receive: Actor.Receive = {

    // when the current media is end, the player send an "EndOfMedia" event to the playlist
    case "EndOfMedia" => Platform.runLater {
      // check if there is a next song in the playlist
      if (!isLastSong) {
        // if yes, play it
        nextSong()
      }
    }

    case "Previous" => Platform.runLater {
      if (!isFirstSong()) {
        previousSong()
      }
    }

    case "Next" => Platform.runLater {
      if (!isLastSong) {
        nextSong()
      }
    }

    // this case is used only internally. It is for avoid recursion issue
    case song: RemoveSong => removeSong(song.media)
  }


  def addSong(newMedia: Media) = {
    Platform.runLater {
      // using file title
      // newMedia.source.split("/").last

      // using idtag
      metatags = {
        val title = newMedia.metadata.getOrElse("title", "").toString
        val album = newMedia.metadata.getOrElse("album", "").toString
        metatags :+ new Metatag(title,album)
      }

      medias = medias :+ newMedia

      // metadata are read asynchronously, implements onChange to get the value
      newMedia.metadata.onChange {
        val index = medias.indexOf(newMedia)

        metatags = metatags updated (index,
          {
            val title = newMedia.metadata.getOrElse("title", "").toString
            val album = newMedia.metadata.getOrElse("album", "").toString
            new Metatag(title,album)
          }
          )

        // update the TreeView
        generatePlaylist()
        ()
      }
    }
  }

  def removeSong(media :Media) = {
    val indice = medias.indexOf(media)

    // remove song in media and metatags
    medias = medias.patch(from = indice, patch = Nil, replaced = 1)
    metatags = metatags.patch(from = indice, patch = Nil, replaced = 1)

    // update playlist treeview
    generatePlaylist()
  }

  def previousSong() = {
    currentCursor -= 1
    context.actorSelection("/user/player") ! new NewSong(medias(currentCursor))
  }

  def nextSong() {
    currentCursor += 1
    context.actorSelection("/user/player") ! new NewSong(medias(currentCursor))
  }


  def isFirstSong(): Boolean = if (currentCursor > 0) false else true

  def isLastSong(): Boolean = if (currentCursor < medias.size - 1) false else true


  def generatePlaylist() = {
    Platform.runLater {
      // extract albums from metatags, toSet to have uniq album
      val albumList = metatags.map(x => x.album).toSet

      // TreeView generation
      var albumTree = {
        var tree = new TreeView[String]()
        val rootItem = new TreeItem[String]("albums")
        rootItem.setExpanded(true)

        // generate TreeItem for each album
        val childs = albumList.map(album => {
            val item = new TreeItem[String](album)
            item.setExpanded(true)
            tree.root = rootItem
            // get song in this album
            item.children = (for (meta <- metatags if meta.album == album )
            yield (new TreeItem[String](meta.title))).toSeq
            item }
          ).toSeq
        childs.foreach(rootItem.children.add(_))
        tree.root = rootItem

        tree.getSelectionModel.setSelectionMode(SelectionMode.SINGLE)

        // mouse event on a song
        tree.onMouseClicked = (event: MouseEvent) => {
          if (event.clickCount > 1) { // on double click
            Platform.runLater {
              // get the selected item
              var item =  tree.getSelectionModel.getSelectedItems()(0)
              findMedia (item.getValue) match {
                  // is a valid media (not a album item), send it to the player to play it
                case Some(media) => {
                  // play the new song
                  context.actorSelection("/user/player") ! new NewSong(media)
                }
                case None => println("Song not found")
              }
            }
          }
        }

        // press delete key to remove a song
        tree.onKeyReleased = (event: KeyEvent) => {
              if (event.code == KeyCode.DELETE) {
                val item = tree.getSelectionModel().getSelectedItems()(0)
                findMedia (item.getValue) match {
                  // remove the song, send a RemoveSong internally to avoid recursion issue on compilation
                  case Some(media) =>  context.actorSelection("/user/playlist") ! new RemoveSong(media)
                  case None => ()
                }
              }
            }

        tree
      }

    vbox.content = albumTree
    }
  }

  // find a media object in the medias list using the title
  // it is possible to use the selected indice in treeview instead of title but is it more complex because
  // the selected indice change if some album are expanded or not
  def findMedia(title :String) :Option[Media] =   {
    val tag = metatags.filter(x => x.title equals title)
    if(tag.size > 0){
      val indice = metatags.indexOf(tag(0))
      Some(medias(indice))
    }
    else None
  }


  val menuPlayList = new Menu("PlayList") {
    items = List(
      new MenuItem("Add file in Playlist") {
        onAction = addFileInPlaylist()
      },
      new MenuItem("Add folder in Playlist") {
        onAction = addFolderInPlaylist()
      }
    )
  }

  def addFileInPlaylist() = {
    try {
      val fileChooser = new FileChooser()
      val filePath = fileChooser.showOpenDialog(new Stage())
      Platform.runLater {
        if( filePath != null){
          val newMedia = new Media(new File(filePath.toString).toURI.toString)
          addSong(newMedia)
        }
      }
    } catch {
      case ex :Exception => println(ex)
    }

  }

  def addFolderInPlaylist() = {
    try{
      val fileChooser = new DirectoryChooser()
      val filePath = fileChooser.showDialog(new Stage())
      Platform.runLater {
        if (filePath != null){
          var files = filePath.listFiles();
          for(file <- files){
            try {
              val newMedia = new Media(file.toURI.toString)
              addSong(newMedia)
            } catch {
              case ex : Exception => println(ex)
            }
          }
        }
      }
    } catch {
      case ex :Exception => println(ex)
    }
  }

  menuBar.getMenus().addAll(menuPlayList);
  generatePlaylist()

}
