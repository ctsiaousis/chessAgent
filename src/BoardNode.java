import java.util.ArrayList;
import java.util.List;

public class BoardNode {
	private int rows, cols;
	public int level, visitCount = 0;
	public String[][] board;
	public String cameFromMove;
	public List<BoardNode> children;
	public BoardNode parent;
	
	
	public BoardNode(BoardNode parentIn, String[][] boardIn, int r, int c) {
		parent = parentIn;
		if(parent == null)
			level = 0;
		else
			level = parent.level+1;
		board = boardIn;
		rows = r; cols = c;
		cameFromMove = "";
		children = new ArrayList<BoardNode>();
	}
	
	public void insertChildBoards(ArrayList<String> availableMoves) {
		for(String move: availableMoves) {
			BoardNode b = new BoardNode(this, this.createNewBoard(move), this.rows, this.cols);
			b.cameFromMove = move;
			this.children.add(b);
		}
	}
	
	public String getBestMove() {
		int maxCount = 0, bestIndex = 0;
		for(int i = 0; i < children.size(); i++) {
			int thisCount = children.get(i).visitCount;
			if(thisCount > maxCount) {
				maxCount = thisCount;
				bestIndex = i;
			}
		}
		return children.get(bestIndex).cameFromMove;
	}
	
	public BoardNode select() {
		return children.get(0); //children are inserted sorted
	}
	
	public boolean isLeaf() {
		return children.isEmpty();
	}
	
	public boolean contains(String[][] boardIn) {
		if(this.isEqual(boardIn))
			return true;
		for(BoardNode n: children) {
			if(n.contains(boardIn))
				return true;
		}
		return false;
	}
	
	private boolean isEqual(String[][] boardIn) {
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				if(!this.board[i][j].equals(boardIn[i][j]))
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
}
