import java.util.ArrayList;
import java.util.List;

public class BoardNode {
	private int rows, cols;
	public int level, visitCount;
	public String[][] board;
	public String cameFromMove;
	public double movesScore;
	public List<BoardNode> children;
	public BoardNode parent;

	public BoardNode(BoardNode parentIn, String[][] boardIn, int r, int c) {
		parent = parentIn;
		if (parent == null)
			level = 0;
		else
			level = parent.level + 1;
		board = boardIn;
		rows = r;
		cols = c;
		cameFromMove = "";
		movesScore = 0;
		visitCount = 0;
		children = new ArrayList<BoardNode>();
	}

	public void insertChildBoards(ArrayList<String> sortedAvailableMoves) {
		for (String move : sortedAvailableMoves) {
			BoardNode b = new BoardNode(this, this.createNewBoard(move), this.rows, this.cols);
			b.cameFromMove = move;
			b.movesScore = calculateScore(move);
			this.children.add(b);
		}
	}

	public String getBestMove() {
//		System.out.println("BoardNode::uct()::getBestMove() -- start");
//		int maxCount = 0, bestIndex = 0;
//		for (int i = 0; i < children.size(); i++) {
//			int thisCount = children.get(i).visitCount;
//			if (thisCount > maxCount) {
//				maxCount = thisCount;
//				bestIndex = i;
//			}
//		}
//		return children.get(bestIndex).cameFromMove;
		return this.uct().cameFromMove;
	}

	public BoardNode backPropagate() {
		this.visitCount += 1;
		if (this.parent == null)
			return this.parent;
		double argMax = 0.0;
		for (BoardNode b : this.children) {
			double tmpUCT = b.movesScore + 2 * Math.sqrt(2) * (Math.sqrt(Math.log(this.visitCount) / (b.visitCount)));
			if (tmpUCT > argMax && Double.isFinite(tmpUCT)) {
				argMax = tmpUCT;
			}
		}
		this.movesScore += argMax;
		this.parent.movesScore += argMax;
		return this.parent;
	}

	private BoardNode uct() {
		double argMax = 0.0;
		BoardNode chossen = null;
		for (BoardNode b : children) {
			double tmpUCT = b.movesScore + 2 * Math.sqrt(2) * (Math.sqrt(Math.log(this.visitCount) / (b.visitCount)));
//			double tmpUCT = b.movesScore + 2 * (Math.sqrt(Math.log(this.visitCount) / (2 * b.visitCount)));
			if (Double.isNaN(tmpUCT))
				tmpUCT = Double.POSITIVE_INFINITY;

//			System.out.println("BoardNode::uct()::tmpUCT -- " + tmpUCT);
			if (tmpUCT > argMax) {
				argMax = tmpUCT;
				chossen = b;
			}
		}
//		System.out.println("BoardNode::uct()::argMax -- " + argMax);
		return chossen;
	}

	public BoardNode select() {
		return this.uct(); // children are inserted sorted
	}

	public boolean isLeaf() {
		return children.isEmpty();
	}

	public boolean contains(String[][] boardIn) {
		if (this.isEqual(boardIn))
			return true;
		for (BoardNode n : children) {
			if (n.contains(boardIn))
				return true;
		}
		return false;
	}

	private boolean isEqual(String[][] boardIn) {
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				if (!this.board[i][j].equals(boardIn[i][j]))
					return false;
			}
		}
		return true;
	}

	private String[][] createNewBoard(String move) {
		String[][] newBoard = new String[rows][cols];

		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				newBoard[i][j] = this.board[i][j];
			}
		}
		int x1 = Integer.parseInt(Character.toString(move.charAt(0)));
		int y1 = Integer.parseInt(Character.toString(move.charAt(1)));
		int x2 = Integer.parseInt(Character.toString(move.charAt(2)));
		int y2 = Integer.parseInt(Character.toString(move.charAt(3)));

		String chesspart = Character.toString(board[x1][y1].charAt(1));
		boolean pawnLastRow = false;

		// check if it is a move that has made a move to the last line
		if (chesspart.equals("P"))
			if ((x1 == rows - 2 && x2 == rows - 1) || (x1 == 1 && x2 == 0)) {
				newBoard[x2][y2] = " "; // in a case an opponent's chess part has just been captured
				newBoard[x1][y1] = " ";
				pawnLastRow = true;
			}

		// otherwise
		if (!pawnLastRow) {
			newBoard[x2][y2] = board[x1][y1];
			newBoard[x1][y1] = " ";
		}
		return newBoard;
	}

	private double calculateScore(String move) {
		double returningScore = 1.0;
		double prizeChance = 0.9;
		int x1 = Integer.parseInt(Character.toString(move.charAt(0)));
		int y1 = Integer.parseInt(Character.toString(move.charAt(1)));
		int x2 = Integer.parseInt(Character.toString(move.charAt(2)));
		int y2 = Integer.parseInt(Character.toString(move.charAt(3)));

		if (Character.toString(board[x1][y1].charAt(0)).equals("W")) {
			if (Character.toString(board[x2][y2].charAt(0)).equals("B")) {
				if (Character.toString(board[x2][y2].charAt(1)).equals("P")) {
					returningScore += 1;
				} else if (Character.toString(board[x2][y2].charAt(1)).equals("R")) {
					returningScore += 3;
				} else if (Character.toString(board[x2][y2].charAt(1)).equals("K")) {
					returningScore += 8;
				}
			} else if (Character.toString(board[x2][y2].charAt(0)).equals("P")) {// prize
				if (Math.random() > prizeChance)
					returningScore += 1;
			}
			if (Character.toString(board[x1][y1].charAt(1)).equals("P")) {
				if ((x1 == rows - 2 && x2 == rows - 1) || (x1 == 1 && x2 == 0)) { // last row
					returningScore += 1;
				}
			}
		}
		if (Character.toString(board[x1][y1].charAt(0)).equals("B")) {
			if (Character.toString(board[x2][y2].charAt(0)).equals("W")) {
				if (Character.toString(board[x2][y2].charAt(1)).equals("P")) {
					returningScore += 1;
				} else if (Character.toString(board[x2][y2].charAt(1)).equals("R")) {
					returningScore += 3;
				} else if (Character.toString(board[x2][y2].charAt(1)).equals("K")) {
					returningScore += 8;
				}
			} else if (Character.toString(board[x2][y2].charAt(0)).equals("P")) {// prize
				if (Math.random() > prizeChance)
					returningScore += 1;
			}
			if (Character.toString(board[x1][y1].charAt(1)).equals("P")) {
				if ((x1 == rows - 2 && x2 == rows - 1) || (x1 == 1 && x2 == 0)) { // last row
					returningScore += 1;
				}
			}
		}
		// max score is 10, so divide it to be normalized in [0,1] as the assignment
		// suggests
		return returningScore / 10;
	}
}
