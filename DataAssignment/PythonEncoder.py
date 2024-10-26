import schema_pb2
import os
import sys

infile = sys.argv[1]
outfile= sys.argv[2]

symbol_to_block_type = {
    '_': schema_pb2.BlockType.EMPTY,
    '╝': schema_pb2.BlockType.LEFT_UP,
    '╗': schema_pb2.BlockType.LEFT_DOWN,
    '╔': schema_pb2.BlockType.RIGHT_DOWN,
    '╚': schema_pb2.BlockType.RIGHT_UP,
    '═': schema_pb2.BlockType.LEFT_RIGHT,
    '║': schema_pb2.BlockType.UP_DOWN
}

def parse_input_file(input_file):
    with open(input_file, 'r', encoding="utf-8") as f:
        lines = f.readlines()

    puzzle_file = schema_pb2.PuzzleFile()

    current_line = 0
    number_of_puzzles = int(lines[current_line].split()[1])
    puzzle_file.number_of_puzzles = number_of_puzzles
    current_line += 1

   
    while current_line < len(lines):
        if lines[current_line].startswith('size'):
            size_info = lines[current_line].split()[1]
            rows, cols = map(int, size_info.split('x'))
            current_line += 1
            column_clues = list(map(int, lines[current_line].split()))
            current_line += 1

            puzzle = schema_pb2.Puzzle()
            puzzle.column_clues.extend(column_clues)

            grid = schema_pb2.Grid()

            for _ in range(rows):
                row_data = lines[current_line].split()
                row_clue = int(row_data[-1])
                block_symbols = row_data[:-1]

                row = schema_pb2.Row()
                row.row_clue = row_clue

                for symbol in block_symbols:
                    block = schema_pb2.Block()
                    block.block_type = symbol_to_block_type[symbol]
                    row.blocks.append(block)

                grid.rows.append(row)

                current_line += 1

            puzzle.grid.CopyFrom(grid)

            puzzle_file.puzzles.append(puzzle)

    return puzzle_file

def encode_puzzle_file(puzzle_file_data, output_file):
    binary_data = puzzle_file_data.SerializeToString()
    with open(output_file, 'wb') as f:
        f.write(binary_data)


puzzle_file = parse_input_file(infile)
encode_puzzle_file(puzzle_file, outfile)
