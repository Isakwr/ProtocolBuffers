import Puzzle.Solution
import Schema.{Block as jvBlock, Grid as jvGrid, Puzzle as jvPuzzle, PuzzleFile as jvPuzzleFile, Row as jvRow}

import java.io.{FileInputStream, FileOutputStream, PrintWriter}
import java.util
import scala.collection.mutable.ListBuffer
import scala.io.Source
import scala.jdk.CollectionConverters.*


def mapBlockTypeToBlock(blockType: Schema.BlockType): Block = {
  val state = blockType match {
    case Schema.BlockType.EMPTY      => None
    case Schema.BlockType.LEFT_UP    => Some(1)
    case Schema.BlockType.LEFT_DOWN  => Some(1)
    case Schema.BlockType.RIGHT_DOWN => Some(1)
    case Schema.BlockType.RIGHT_UP   => Some(1)
    case Schema.BlockType.LEFT_RIGHT => Some(1)
    case Schema.BlockType.UP_DOWN    => Some(1)
    case _                           => None
  }
  
  val paths: Map[Direction.Value, Option[Int]] = blockType match {
    case Schema.BlockType.EMPTY      => Map(Direction.Left -> None, Direction.Up -> None, Direction.Right -> None, Direction.Down -> None)
    case Schema.BlockType.LEFT_UP    => Map(Direction.Left -> Some(1), Direction.Up -> Some(1), Direction.Right -> Some(0), Direction.Down -> Some(0))
    case Schema.BlockType.LEFT_DOWN  => Map(Direction.Left -> Some(1), Direction.Up -> Some(0), Direction.Right -> Some(0), Direction.Down -> Some(1))
    case Schema.BlockType.RIGHT_DOWN => Map(Direction.Left -> Some(0), Direction.Up -> Some(0),Direction.Right -> Some(1), Direction.Down -> Some(1))
    case Schema.BlockType.RIGHT_UP   => Map(Direction.Left -> Some(0), Direction.Up -> Some(1), Direction.Right -> Some(1), Direction.Down -> Some(0))
    case Schema.BlockType.LEFT_RIGHT => Map(Direction.Left -> Some(1), Direction.Up -> Some(0), Direction.Right -> Some(1), Direction.Down -> Some(0))
    case Schema.BlockType.UP_DOWN    => Map(Direction.Left -> Some(0), Direction.Up -> Some(1), Direction.Right -> Some(0), Direction.Down -> Some(1))
    case _                           => Map.empty
  }

  Block(state, paths)
}


object PuzzleReaderWriter {


  def readPuzzles(filename: String): List[Puzzle] = {
    val input = new FileInputStream(filename)
    var puzzlefile = jvPuzzleFile.parseFrom(input)

    var puzzleList = puzzlefile.getPuzzlesList
    var puzzleboard: jvPuzzle = null
    var puzzleGrid: jvGrid = null
    var puzzleRows: jvRow = null
    var puzzleBlock: jvBlock = null

    //size (int, int)
    //grid Array[Array[Block]]
    //rowClues List[Int]
    //colClues List[Int]
    val puzzle1: ListBuffer[Puzzle] = ListBuffer()


    for(i <- 0 until puzzlefile.getNumberOfPuzzles){

      var height: Int = puzzleList.get(i).getGrid.getRowsList.size()
      var width: Int = puzzleList.get(i).getGrid.getRowsList.getFirst.getBlocksCount
      var size: (Int, Int) = (height, width)
      var temp = puzzleList.get(i).getColumnCluesList
      var colClues: List[Int] = temp.asScala.map(_.intValue()).toList
      var javGrid = puzzleList.get(i).getGrid
      var rowClues: List[Int] = javGrid.getRowsList.asScala.map(_.getRowClue.intValue()).toList
      var grid: Array[Array[Block]] = javGrid.getRowsList.asScala.map { row =>
        row.getBlocksList.asScala.map { block =>
          mapBlockTypeToBlock(block.getBlockType)
        }.toArray
      }.toArray

      var puzzle: Puzzle = new Puzzle(size, grid, rowClues, colClues)
      puzzle1 += puzzle

    }

    val returningPuzzleList: List[Puzzle] = puzzle1.toList
    return returningPuzzleList

  }


  def ToProtoPuzzle(solution: Puzzle.Solution): Schema.Puzzle = {
    val protoGridBuilder = Schema.Grid.newBuilder()
    
    solution.grid.zip(solution.rowClues).foreach { case (rowChars, rowClue) =>
      val protoRowBuilder = Schema.Row.newBuilder().setRowClue(rowClue)

      rowChars.foreach { char =>
        val blockType = char match {
          case ' ' => Schema.BlockType.EMPTY
          case '╝' => Schema.BlockType.LEFT_UP
          case '╗' => Schema.BlockType.LEFT_DOWN
          case '╔' => Schema.BlockType.RIGHT_DOWN
          case '╚' => Schema.BlockType.RIGHT_UP
          case '═' => Schema.BlockType.LEFT_RIGHT
          case '║' => Schema.BlockType.UP_DOWN
          case _   => Schema.BlockType.EMPTY 
        }
        protoRowBuilder.addBlocks(Schema.Block.newBuilder().setBlockType(blockType).build())
      }

      protoGridBuilder.addRows(protoRowBuilder.build())
    }
    val columnCluesJava = new util.ArrayList[Integer]()
    solution.columnClues.foreach(clue => columnCluesJava.add(Int.box(clue)))

    Schema.Puzzle.newBuilder()
      .setGrid(protoGridBuilder.build())
      .addAllColumnClues(columnCluesJava)
      .build()
  }

  def ToProtoPuzzleFile(solutions: List[Puzzle.Solution]): Schema.PuzzleFile = {
    val protoPuzzles = new util.ArrayList[Schema.Puzzle]()
    solutions.foreach { solution =>
      protoPuzzles.add(ToProtoPuzzle(solution))
    }

    Schema.PuzzleFile.newBuilder()
      .setNumberOfPuzzles(solutions.size)
      .addAllPuzzles(protoPuzzles)
      .build()
  }


  def writeSolution(filename: String, solutions: List[Puzzle.Solution]): Unit = {

    val protoPuzzleFile = ToProtoPuzzleFile(solutions)
    val outputStream = new FileOutputStream(filename)

    try {
      protoPuzzleFile.writeTo(outputStream)
    } finally {
      outputStream.close()
    }
  }
  

  /*
  def writeSolution(filename: String, solutions: List[Solution]): Unit = {
    val writer = new PrintWriter(filename)
    writer.println(s"puzzles ${solutions.length}")


    solutions.zipWithIndex.foreach { case (solution, index) =>
      writer.println(s"size ${solution.grid.length}x${solution.grid.head.length}")
      writer.print(solution.toString)
      if (index < solutions.length - 1) {
        writer.println()
      }
    }
    writer.close()
  }

   */



}
