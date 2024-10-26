import schema_pb2
import os
import sys

infile = sys.argv[1]
outfile= sys.argv[2]

block_type_to_symbol = {
    schema_pb2.BlockType.EMPTY: '_',
    schema_pb2.BlockType.LEFT_UP: '╝',
    schema_pb2.BlockType.LEFT_DOWN: '╗',
    schema_pb2.BlockType.RIGHT_DOWN: '╔',
    schema_pb2.BlockType.RIGHT_UP: '╚',
    schema_pb2.BlockType.LEFT_RIGHT: '═',
    schema_pb2.BlockType.UP_DOWN: '║'
}

def decode_puzzle_file(input_file):
    if not os.path.exists(input_file):
        return None

    with open(input_file, 'rb') as f:
        binary_data = f.read()

    puzzle_file = schema_pb2.PuzzleFile()
    puzzle_file.ParseFromString(binary_data)
    return puzzle_file

def write_decoded_to_text(decoded_puzzle_file, output_txt_file):

    with open(output_txt_file, 'w', encoding='utf-8') as f:
        f.write(f"puzzles {decoded_puzzle_file.number_of_puzzles}\n")
        for puzzle in decoded_puzzle_file.puzzles:
            num_rows = len(puzzle.grid.rows)
            num_columns = len(puzzle.column_clues)

            f.write(f"size {num_rows}x{num_columns}\n")
            f.write(" ".join(map(str, puzzle.column_clues)) + "\n")


            for row in puzzle.grid.rows:
                row_symbols = [block_type_to_symbol[block.block_type] for block in row.blocks]
                f.write(" ".join(row_symbols) + f" {row.row_clue}\n")




if __name__ == "__main__":
    decoded_puzzle_file = decode_puzzle_file(infile)
    if decoded_puzzle_file:
        write_decoded_to_text(decoded_puzzle_file, outfile)
