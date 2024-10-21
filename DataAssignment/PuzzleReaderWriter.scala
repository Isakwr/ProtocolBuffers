import java.io.{FileInputStream, FileOutputStream, PrintWriter}
import scala.jdk.CollectionConverters._
import puzzle_unsolved_pb2.PuzzleFile
import puzzle_unsolved_pb2.Puzzle

object PuzzleReaderWriter {
  def readPuzzles(filename: String): List[Puzzle] = {
    val fileInput = new FileInputStream(filename)
    val puzzleFile = PuzzleFile.parseFrom(fileInput)
    fileInput.close()

    var puzzles: List[Puzzle] = List.empty

    for (puzzleProto <- puzzleFile.getPuzzlesList.asScala) {
      val width = puzzleProto.getDimensions.getWidth
      val height = puzzleProto.getDimensions.getHeight
      println(s"Puzzle size: ${width}x${height}")

      val columnClues = puzzleProto.getColumnCluesList.asScala.map(_.getClue).toList

      val grid = Array.fill(height, width)(puzzle_unsolved.Block.newBuilder())
      val rowClues = List.newBuilder[Int]

      for (rowIdx <- 0 until height) {
        val rowProto = puzzleProto.getGrid.getRows(rowIdx)

        for (colIdx <- 0 until width) {
          val blockSymbol = rowProto.getBlocks(colIdx).getBlock
          val block = puzzle_unsolved.Block.newBuilder().setBlock(blockSymbol).build()
          grid(rowIdx)(colIdx) = block
        }
        rowClues += rowProto.getRowClue.getClue
      }

      puzzles = puzzles :+ puzzleProto
    }

    puzzles
  }

  def writeSolution(filename: String, solutions: List[Puzzle]): Unit = {
    val puzzleFileBuilder = PuzzleFile.newBuilder()

    for (solution <- solutions) {
      val puzzleBuilder = puzzle_unsolved.Puzzle.newBuilder()
      val dimensionsBuilder = puzzle_unsolved.Dimensions.newBuilder()
      dimensionsBuilder.setWidth(solution.getDimensions.getWidth)
      dimensionsBuilder.setHeight(solution.getDimensions.getHeight)
      puzzleBuilder.setDimensions(dimensionsBuilder)

      // Add column clues
      solution.getColumnCluesList.forEach { clue =>
        val clueBuilder = puzzle_unsolved.Clue.newBuilder()
        clueBuilder.setClue(clue.getClue)
        puzzleBuilder.addColumnClues(clueBuilder)
      }

      val gridBuilder = puzzle_unsolved.Grid.newBuilder()
      for (rowIdx <- 0 until solution.getGrid.getRowsCount) {
        val rowBuilder = puzzle_unsolved.Row.newBuilder()
        val rowProto = solution.getGrid.getRows(rowIdx)

        for (colIdx <- 0 until rowProto.getBlocksCount) {
          val blockBuilder = puzzle_unsolved.Block.newBuilder()
          blockBuilder.setBlock(rowProto.getBlocks(colIdx).getBlock)
          rowBuilder.addBlocks(blockBuilder)
        }

        val rowClueBuilder = puzzle_unsolved.Clue.newBuilder()
        rowClueBuilder.setClue(rowProto.getRowClue.getClue)
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

  def writeSolutionToText(filename: String, solutions: List[Puzzle]): Unit = {
    val writer = new PrintWriter(filename)
    writer.println(s"puzzles ${solutions.length}")
    solutions.zipWithIndex.foreach { case (solution, index) =>
      writer.println(s"size ${solution.getDimensions.getHeight}x${solution.getDimensions.getWidth}")
      solution.getGrid.getRowsList.forEach { row =>
        writer.print(row.getBlocksList.asScala.map(_.getBlock).mkString(" "))
        writer.println(s" ${row.getRowClue.getClue}")
      }
      if (index < solutions.length - 1) {
        writer.println()
      }
    }

    writer.close()
  }

}
