@main def PuzzleSolver(inFile: String, outFile: String): Unit = {

    // read all puzzles from the input file
    val puzzles = PuzzleReaderWriter.readPuzzles(inFile)

    // solve each puzzle and gather the solutions
    val solutions = puzzles.map { puzzle =>
      for (rowIndex <- puzzle.grid.indices) {
        if (PuzzleChecker.isFullRow(puzzle, rowIndex)) {
          println(s"Row $rowIndex is complete!")
        }
      }

      for (colIndex <- puzzle.grid.head.indices) {
        if (PuzzleChecker.isFullColumn(puzzle, colIndex)) {
          println(s"Column $colIndex is complete!")
        }
      }

      Puzzle.solve(puzzle)
    }

    PuzzleReaderWriter.writeSolution(outFile, solutions)
}

