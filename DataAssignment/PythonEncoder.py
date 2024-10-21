import puzzle_unsolved_pb2
import sys


infile = sys.argv[1]
outfile= sys.argv[2]

def encode_puzzle(input_file, output_file):
    puzzle_unsolved = puzzle_unsolved_pb2.PuzzleFile()

    with open(input_file, 'r', encoding='utf-8') as f:
        line = f.readline().strip()
        puzzle_unsolved.number_of_puzzles = int(line.split()[1])

        #for each puzzle
        for _ in range(puzzle_unsolved.number_of_puzzles):
            puzzle = puzzle_unsolved.puzzles.add()
            
            #adding dimensions
            line = f.readline().strip()
            size = line.split()[1]
            width, height = map(int, size.split('x'))
            puzzle.dimensions.width = width
            puzzle.dimensions.height = height

            #adding clues
            line = f.readline().strip()
            column_clues = map(int, line.split())
            for clue in column_clues:
                puzzle.column_clues.add(clue=clue)

            #adding grid
            grid = puzzle.grid
            for _ in range(height):
                line = f.readline().strip()
                *blocks, row_clue = line.split()

                row = grid.rows.add()
                for block_symbol in blocks:
                    block = row.blocks.add()
                    block.block = block_symbol
                row.row_clue.clue = int(row_clue)

    binary_input = puzzle_unsolved.SerializeToString()
    with open(output_file, 'wb') as f:
        f.write(binary_input)

    print(f"Binary data has been saved to '{output_file}'")

encode_puzzle(infile, outfile)