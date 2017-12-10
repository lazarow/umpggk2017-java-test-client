package gawain;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class Nogo {

	class Map {
		public int[][] chunk;
		public int[][] position;
	}
	
	private static int side = 9;
	private static Map map;
	
	public Nogo() {
		int y, x, i, j, k;
		map = new Map();
		map.chunk = new int[side][];
		map.position = new int[side][];
		for (y = 0; y < side; ++y) {
			map.chunk[y] = new int[side];
			map.position[y] = new int[side];
			for (x = 0; x < side; ++x) {
				i = y * (side + 1) + x;
				j = (int) Math.floor(i / Integer.SIZE);
				k = i - j * Integer.SIZE;
				map.chunk[y][x] = j;
				map.position[y][x] = k;
			}
		}
	}
	
	static public int[] getEmptyBoard() {
		return new int[] {-537395713, -134348929, 33521631};
	}
	
	static public int[] getOnesBoard() {
		return new int[] {~0, ~0, ~0};
	}
	
	static public int[][] getInitialState() {
		return new int[][] {getEmptyBoard(), getEmptyBoard(), getOnesBoard(), getOnesBoard()};
    }
	
	static public int[] and(int[] a, int[] b) {
		return new int[] {a[0] & b[0], a[1] & b[1], a[2] & b[2]};
	}
	
	static public int[] or(int[] a, int[] b) {
		return new int[] {a[0] | b[0], a[1] | b[1], a[2] | b[2]};
	}
	
	static public int[] neg(int[] a) {
		return new int[] {~a[0], ~a[1], ~a[2]};
	}
	
	static public int[] clone(int[] a) {
		return new int[] {a[0], a[1], a[2]};
	}
	
	static public int[] shift(int[] a, int s) {
		int p = Integer.SIZE - s;
		if (s > 0) {
			return new int[] {a[0] >>> s | a[1] << p, a[1] >>> s | a[2] << p , a[2] >>> s};
		}
		s = -s;
		p = Integer.SIZE - s;
		return new int[] {a[0] << s, a[1] << s | a[0] >>> p, a[2] << s | a[1] >>> p};
	}
	
	static public int[] top(int[] a) {
		return shift(a, -(side + 1));
	}
	
	static public int[] bottom(int[] a) {
		return shift(a, side + 1);
	}
	
	static public int[] left(int[] a) {
		return shift(a, 1);
	}
	
	static public int[] right(int[] a) {
		return shift(a, -1);
	}
	
	static public boolean equals(int[] a, int[] b) {
		return a[0] == b[0] && a[1] == b[1] && a[2] == b[2];
	}
	
	static public boolean isEmpty(int[] board, int y, int x) {
		return (board[map.chunk[y][x]] & 1 << map.position[y][x]) != 0;
	}
	
	public static List<int[]> getLegalMoves(int[][] s, String c) {
		List<int[]> moves = new ArrayList<>();
		int[] board = and(and(s[0], s[1]), s[c.equals("black") ? 2 : 3]);
		for (int y = 0; y < side; ++y) {
			for (int x = 0; x < side; ++x) {
				if (isEmpty(board, y, x)) {
					moves.add(new int[] {y, x});
				}
			}
		}
		return moves;
	}
	
	public static int[][] play(int[][] s, String c, int y, int x) {
		int[][] state = new int[][] {clone(s[0]), clone(s[1]), clone(s[2]), clone(s[3])};
		boolean isBlack = c.equals("black");
		state[isBlack ? 0 : 1][map.chunk[y][x]] &= ~(1 << map.position[y][x]);
		state[2] = and(state[2], getIllegalMoves(state[0], state[1]));
		state[3] = and(state[3], getIllegalMoves(state[1], state[0]));
		state[2] = and(state[2], getSingleStoneKillMoves(state[0], state[1]));
		state[3] = and(state[3], getSingleStoneKillMoves(state[1], state[0]));
		state[2] = and(state[2], getLastLibertiesOfGroups(state[0], state[1], state[2]));
		state[3] = and(state[3], getLastLibertiesOfGroups(state[1], state[0], state[3]));
		return state;
	}
	
	private static int[] getIllegalMoves(int[] p, int[] o) {
        return or(neg(p), or(top(o), or(bottom(o), or(left(o), right(o)))));
    }
	
	private static int[] getSingleStoneKillMoves(int[] p, int[] o) {
		int[] horizontal = or(o, or(left(p), right(p)));
		int[] vertical = or(o, or(top(p), bottom(p)));
		int[] horizontal1 = or(horizontal, top(p));
		int[] horizontal2 = or(horizontal, bottom(p));
		int[] vertical1 = or(vertical, left(p));
		int[] vertical2 = or(vertical, right(p));
		int[] kills = getEmptyBoard();
		int[][] boards = new int[][] {horizontal1, horizontal2, vertical1, vertical2};
		int[][] offsets = new int[][] {{1,0}, {-1,0}, {0,-1}, {0,1}};
		for (int y = 0, chunk, position; y < side; ++y) {
            for (int x = 0; x < side; ++x) {
				for (int j = 0; j < 4; ++j) {
					if (isEmpty(boards[j], y, x) == false) {
						chunk = map.chunk[y + offsets[j][0]][x + offsets[j][1]];
						position = map.position[y + offsets[j][0]][x + offsets[j][1]];
						kills[chunk] &= ~(1 << position);
					}
				}
            }
        }
		return kills;
    }
	
	private static boolean hasDeadGroups(int[] p, int[] o) {
		int[] b = and(p, o);
		int[] r = clone(p);
		int[] a = and(or(b, or(top(b), or(bottom(b), or(left(b), right(b))))), o);
		while (equals(a, r) == false) {
			r = clone(a);
			a = and(or(a, or(top(a), or(bottom(a), or(left(a), right(a))))), o);
		}
		return equals(or(p, a), getEmptyBoard()) == false;
	}
	
	private static int[] getLastLibertiesOfGroups(int[] p, int[] o, int[] i) {
		int[] both = and(p, o);
		int[] empty = neg(both);
		int[] horizontal = or(both, or(left(both), right(both)));
		int[] vertical = or(both, or(top(both), bottom(both)));
		int[] horizontal1 = or(or(horizontal, top(both)), bottom(empty));
		int[] horizontal2 = or(or(horizontal, bottom(both)), top(empty));
		int[] vertical1 = or(or(vertical, left(both)), right(empty));
		int[] vertical2 = or(or(vertical, right(both)), left(empty));
		int[] kills = getEmptyBoard();
		int[][] boards = new int[][] {horizontal1, horizontal2, vertical1, vertical2};
		int[][] offsets = {{1,0}, {-1,0}, {0,-1}, {0,1}};
		int[] c;
		for (int y = 0, chunk, position, a, b; y < side; ++y) {
            for (int x = 0; x < side; ++x) {
				for (int j = 0; j < 4; ++j) {
					if (isEmpty(boards[j], y, x) == false) {
						a = y + offsets[j][0];
						b = x + offsets[j][1];
						if (a < 0 || a >= side || b < 0 || b >= side) {
							continue;
						}
						if (isEmpty(i, a, b) == false) {
							continue;
						}
						chunk = map.chunk[a][b]; position = map.position[a][b]; c = clone(p);
						c[chunk] &= ~(1 << position);
						if (hasDeadGroups(o, c) || hasDeadGroups(c, o)) {
							kills[chunk] &= ~(1 << position);
						}
					}
				}
            }
        }
		return kills;
	}
	
	public static void print(int[][] state) {
		System.out.println("  | 1 2 3 4 5 6 7 8 9      | 1 2 3 4 5 6 7 8 9      | 1 2 3 4 5 6 7 8 9");
		System.out.println("--+------------------    --+------------------    --+------------------");
		for (int y = 0; y < side; ++y) {
			System.out.print((y + 1) + " |");
            for (int x = 0; x < side; ++x) {
            	System.out.print(" " + (isEmpty(state[0], y, x) ? (isEmpty(state[1], y, x) ? "." : "W") : "B"));
            }
            System.out.print("    " + (y + 1) + " |");
            for (int x = 0; x < side; ++x) {
            	System.out.print(" " + (isEmpty(state[2], y, x) ? "." : "x"));
            }
            System.out.print("    " + (y + 1) + " |");
            for (int x = 0; x < side; ++x) {
            	System.out.print(" " + (isEmpty(state[3], y, x) ? "." : "x"));
            }
            System.out.println("");
		}
		System.out.println("");
	}
	
	public static void main(String[] args) {
		new Nogo(); // Init
		Random random = new Random();
		long startTime = System.nanoTime();
		for (int i = 0; i < 1; ++i) {
			int[][] state = Nogo.getInitialState();
			String color = "black";
			List<int[]> moves = Nogo.getLegalMoves(state, color);
			while (moves.size() > 0) {
				int[] move = moves.get(random.nextInt(moves.size()));
			    state = Nogo.play(state, color, move[0], move[1]);
			    Nogo.print(state);
			    color = color.equals("black") ? "white" : "black";
			    moves = Nogo.getLegalMoves(state, color);
			}
			System.out.println("Loser: " + color);
		}
		long estimatedTime = System.nanoTime() - startTime;
		System.out.println("Time: " + (estimatedTime / 1000000000.0));
	}
	
}
