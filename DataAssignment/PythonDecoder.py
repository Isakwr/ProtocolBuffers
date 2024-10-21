import puzzle_unsolved_pb2
import sys

infile = sys.argv[1]
outfile= sys.argv[2]


def decode_puzzle(input_file, output_file):
    puzzle_file = puzzle_unsolved_pb2.PuzzleFile()

    with open(input_file, 'rb') as f:
        puzzle_file.ParseFromString(f.read())

    with open(output_file, 'w') as f:
        f.write(f"puzzles {puzzle_file.number_of_puzzles}\n")

        for puzzle_idx, puzzle in enumerate(puzzle_file.puzzles):
            f.write(f"size {puzzle.dimensions.width}x{puzzle.dimensions.height}\n")
            column_clues = " ".join(str(clue.clue) for clue in puzzle.column_clues)
            f.write(f"{column_clues}\n")

            for row in puzzle.grid.rows:
                row_blocks = " ".join(block.block for block in row.blocks)
                f.write(f"{row_blocks} {row.row_clue.clue}\n")

    print(f"Decoded puzzle data has been saved to '{output_file}'")

decode_puzzle(infile, outfile)
