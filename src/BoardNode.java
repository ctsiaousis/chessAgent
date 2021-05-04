import java.util.ArrayList;
import java.util.List;

public class BoardNode {
	private int rows, cols;
	public int level, visitCount;
	public String[][] board;
	public String cameFromMove;
	public double score;
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
		score = Double.POSITIVE_INFINITY;
		visitCount = 0;
		children = new ArrayList<BoardNode>();
	}

	public void insertChildBoards(ArrayList<String> sortedAvailableMoves) {
		for (String move : sortedAvailableMoves) {
			BoardNode b = new BoardNode(this, this.createNewBoard(move), this.rows, this.cols);
			b.initialize(move);
			this.children.add(b);
		}
	}

	public String getBestMove() {
//// THIS WAS OUR INITIAL IMPLEMENTATION
//		double maxCount = 0;
//		int bestIndex = 0;
//		for (int i = 0; i < children.size(); i++) {
//			double s = children.get(i).score;
//			if (s > maxCount) {
//				maxCount = s;
//				bestIndex = i;
//			}
//		}
//		return children.get(bestIndex).cameFromMove;
//// THIS IS THE SECOND IMPLEMENTATION
		return this.uct().cameFromMove;
	}

	public void initialize(String move) {
		this.cameFromMove = move;
		this.score = this.winner();
		if (this.winner() == 1)// this is a terminal state
			this.children.removeAll(children);
	}

	public BoardNode backPropagate() {
		this.visitCount += 1;
		this.score = uct().score;
		if (this.parent != null)
			this.parent.score += this.score;
		return this.parent;
	}

	public BoardNode select() {
		if (this.visitCount > 0 && this.children.isEmpty())
			return this;
		BoardNode toRet = this.uct();
//		System.out.println("BoardNode--select()--index: " + this.children.indexOf(toRet) + " -- score: " + toRet.score);
		return toRet;
	}

	private BoardNode uct() {
		double argMax = 0.0;
		BoardNode chossen = null;
		for (BoardNode b : children) {
			double tmpUCT = b.score + 2 * Math.sqrt(2) * (Math.sqrt(Math.log(this.visitCount) / (b.visitCount)));
			if (Double.isNaN(tmpUCT))
				tmpUCT = Double.POSITIVE_INFINITY;
//			System.out.println("BoardNode::uct()::tmpUCT -- " + tmpUCT);
//			System.out.println("BoardNode::uct()::b.score  -- " + b.score);
			if (tmpUCT > argMax) {
				argMax = tmpUCT;
				chossen = b;
			}
		}
//		System.out.println("BoardNode::uct()::argMax -- " + argMax);
		return chossen;
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

	public int winner() {
		int x2 = Integer.parseInt(Character.toString(cameFromMove.charAt(2)));
		int y2 = Integer.parseInt(Character.toString(cameFromMove.charAt(3)));
		boolean iAmWhite = false;
		if (Character.toString(board[x2][y2].charAt(0)).equals("W")) {
			iAmWhite = true;
		}
		boolean whiteKingExists = false;
		boolean blackKingExists = false;
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				if (board[i][j].contains("WK")) {
					whiteKingExists = true;
				}
				if (board[i][j].contains("BK") && !iAmWhite) {
					blackKingExists = true;
				}
			}
		}
		if (iAmWhite && blackKingExists)
			return 0;
		else if (!iAmWhite && whiteKingExists)
			return 0;
		return 1;
	}
}
