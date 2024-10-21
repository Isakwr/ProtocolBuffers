import puzzle_unsolved_pb2  # Import the generated classes from .proto

def decode_puzzle(input_file, output_file):
    # Create an instance of PuzzleFile to hold the deserialized data
    puzzle_file = puzzle_unsolved_pb2.PuzzleFile()

    # Read the binary data from the file
    with open(input_file, 'rb') as f:
        puzzle_file.ParseFromString(f.read())

    # Open the output file for writing the decoded text format
    with open(output_file, 'w') as f:
        # Write the number of puzzles
        f.write(f"puzzles {puzzle_file.number_of_puzzles}\n")

        # Iterate through each puzzle in the PuzzleFile
        for puzzle_idx, puzzle in enumerate(puzzle_file.puzzles):
            # Write the dimensions of the puzzle
            f.write(f"size {puzzle.dimensions.width}x{puzzle.dimensions.height}\n")

            # Write the column clues
            column_clues = " ".join(str(clue.clue) for clue in puzzle.column_clues)
            f.write(f"{column_clues}\n")

            # Write the grid rows with row clues
            for row in puzzle.grid.rows:
                # Convert the blocks into their string representation
                row_blocks = " ".join(block.block for block in row.blocks)
                f.write(f"{row_blocks} {row.row_clue.clue}\n")

    print(f"Decoded puzzle data has been saved to '{output_file}'")

decode_puzzle('puzzle_output.bin', 'puzzle_output.txt')
