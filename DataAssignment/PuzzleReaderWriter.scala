import java.io.{FileInputStream, FileOutputStream, PrintWriter}
import puzzle_unsolved.PuzzleFile
import scala.jdk.CollectionConverters._
import scala.io._
import java.util._

object PuzzleReaderWriter {

  def readPuzzles(filename: String): List[Puzzle] = {
    val fileInput = new FileInputStream(filename)
    val puzzleFile = PuzzleFile.parseFrom(fileInput)
    fileInput.close()

    var puzzles = List.empty[Puzzle]
    for (puzzleProto <- puzzleFile.getPuzzlesList.asScala) {
      val width = puzzleProto.getDimensions.getWidth
      val height = puzzleProto.getDimensions.getHeight
      println(s"Puzzle size: ${width}x${height}")

      val columnClues = puzzleProto.getColumnCluesList.asScala.map(_.getClue).toList
      val grid = Array.fill(height, width)(Block())
      val rowClues = List.newBuilder[Int]

      for (rowIdx <- 0 until height) {
        val rowProto = puzzleProto.getGrid.getRows(rowIdx)
        for (colIdx <- 0 until width) {
          val blockSymbol = rowProto.getBlocks(colIdx).getBlock
          val block = blockSymbol match {
            case "═" => Block(state = Some(1), paths = Map(
              Direction.Left -> Some(1),
              Direction.Right -> Some(1),
              Direction.Up -> Some(0),
              Direction.Down -> Some(0)
            ))
            case "║" => Block(state = Some(1), paths = Map(
              Direction.Left -> Some(0),
              Direction.Right -> Some(0),
              Direction.Up -> Some(1),
              Direction.Down -> Some(1)
            ))
            case "╔" => Block(state = Some(1), paths = Map(
              Direction.Left -> Some(0),
              Direction.Right -> Some(1),
              Direction.Up -> Some(0),
              Direction.Down -> Some(1)
            ))
            case "╗" => Block(state = Some(1), paths = Map(
              Direction.Left -> Some(1),
              Direction.Right -> Some(0),
              Direction.Up -> Some(0),
              Direction.Down -> Some(1)
            ))
            case "╚" => Block(state = Some(1), paths = Map(
              Direction.Left -> Some(0),
              Direction.Right -> Some(1),
              Direction.Up -> Some(1),
              Direction.Down -> Some(0)
            ))
            case "╝" => Block(state = Some(1), paths = Map(
              Direction.Left -> Some(1),
              Direction.Right -> Some(0),
              Direction.Up -> Some(1),
              Direction.Down -> Some(0)
            ))
            case "1" => Block(state = Some(1), paths = Map(
              Direction.Left -> None,
              Direction.Right -> None,
              Direction.Up -> None,
              Direction.Down -> None
            ))
            case "0" => Block(state = Some(0), paths = Map(
              Direction.Left -> None,
              Direction.Right -> None,
              Direction.Up -> None,
              Direction.Down -> None
            ))
            case "_" => Block(state = None, paths = Map(
              Direction.Left -> None,
              Direction.Right -> None,
              Direction.Up -> None,
              Direction.Down -> None
            ))
            case _ => throw new IllegalArgumentException(s"Invalid character in puzzle: $blockSymbol")
          }
          grid(rowIdx)(colIdx) = block
        }

        rowClues += rowProto.getRowClue.getClue
      }

      puzzles = Puzzle((height, width), grid, rowClues.result(), columnClues) :: puzzles
    }

    puzzles.reverse
  }

  def writeSolution(filename: String, solutions: List[Solution]): Unit = {

    val puzzleFileBuilder = PuzzleFile.newBuilder()
    for (solution <- solutions) {
      val puzzleBuilder = puzzle_unsolved.Puzzle.newBuilder()

      val dimensionsBuilder = puzzle_unsolved.Dimensions.newBuilder()
      dimensionsBuilder.setWidth(solution.grid.head.length)
      dimensionsBuilder.setHeight(solution.grid.length)
      puzzleBuilder.setDimensions(dimensionsBuilder)

      solution.columnClues.foreach { clue =>
        val clueBuilder = puzzle_unsolved.Clue.newBuilder()
        clueBuilder.setClue(clue)
        puzzleBuilder.addColumnClues(clueBuilder)
      }

      val gridBuilder = puzzle_unsolved.Grid.newBuilder()
      for (rowIdx <- solution.grid.indices) {
        val rowBuilder = puzzle_unsolved.Row.newBuilder()
        for (colIdx <- solution.grid(rowIdx).indices) {
          val blockBuilder = puzzle_unsolved.Block.newBuilder()
          blockBuilder.setBlock(solution.grid(rowIdx)(colIdx).toString)
          rowBuilder.addBlocks(blockBuilder)
        }

        val rowClueBuilder = puzzle_unsolved.Clue.newBuilder()
        rowClueBuilder.setClue(solution.rowClues(rowIdx))
        rowBuilder.setRowClue(rowClueBuilder)
        gridBuilder.addRows(rowBuilder)
      }
      puzzleBuilder.setGrid(gridBuilder)
      puzzleFileBuilder.addPuzzles(puzzleBuilder)
    }

    val fileOutput = new FileOutputStream(filename)
    puzzleFileBuilder.build().writeTo(fileOutput)
    fileOutput.close()

    println(s"Binary data has been saved to '$filename'")
  }
  def writeSolutionToText(filename: String, solutions: List[Solution]): Unit = {
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

}
