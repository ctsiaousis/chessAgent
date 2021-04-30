import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import java.util.SortedSet;
import java.util.TreeSet;

public class World {
	private String[][] board = null;
	private int rows = 7;
	private int columns = 5;
	private int myColor = 0;
	private ArrayList<String> availableMoves = null;
	private int rookBlocks = 3; // rook can move towards <rookBlocks> blocks in any vertical or horizontal
								// direction
	private int nTurns = 0;
	private int nBranches = 0;
	private int noPrize = 9;
	private String chosenMove;
	private int minimaxDepth = 8;
	private int myScore = 0, enemyScore = 0;
	private int curMyScore = 0, curEnemyScore = 0;
	private static final double prizeChance = 0.9;

	public World() {

		board = new String[rows][columns];
		/*
		 * represent the board
		 * 
		 * BP|BR|BK|BR|BP BP|BP|BP|BP|BP --|--|--|--|-- P |P |P |P |P --|--|--|--|--
		 * WP|WP|WP|WP|WP WP|WR|WK|WR|WP
		 */

		// initialization of the board
		for (int i = 0; i < rows; i++)
			for (int j = 0; j < columns; j++)
				board[i][j] = " ";

		// setting the black player's chess parts

		// black pawns
		for (int j = 0; j < columns; j++)
			board[1][j] = "BP";

		board[0][0] = "BP";
		board[0][columns - 1] = "BP";

		// black rooks
		board[0][1] = "BR";
		board[0][columns - 2] = "BR";

		// black king
		board[0][columns / 2] = "BK";

		// setting the white player's chess parts

		// white pawns
		for (int j = 0; j < columns; j++)
			board[rows - 2][j] = "WP";

		board[rows - 1][0] = "WP";
		board[rows - 1][columns - 1] = "WP";

		// white rooks
		board[rows - 1][1] = "WR";
		board[rows - 1][columns - 2] = "WR";

		// white king
		board[rows - 1][columns / 2] = "WK";

		// setting the prizes
		for (int j = 0; j < columns; j++)
			board[rows / 2][j] = "P";

		availableMoves = new ArrayList<String>();

	}

	public void setMyColor(int myColor) {
		this.myColor = myColor;
	}

	public String selectAction() {
		availableMoves = new ArrayList<String>();
		
		String[][] temp_board = backupCurrentBoard();
		int eval;

		if (myColor == 0) { // I am the white player
			this.whiteMoves();
			myScore = Client.getScoreWhite();
			enemyScore = Client.getScoreBlack();
		} else { // I am the black player
			this.blackMoves();
			enemyScore = Client.getScoreWhite();
			myScore = Client.getScoreBlack();
		}
		curMyScore = myScore;
		curEnemyScore = enemyScore;

		// keeping track of the branch factor
		nTurns++;
		nBranches += availableMoves.size();

		int maxEval = Integer.MIN_VALUE;

//		children = customClone(availableMoves);
		ArrayList<String> moves = getSortedGoodMoves(availableMoves);

		for (String move : moves) {
			availableMoves.clear();

			calculateScore(move);
			// Make move
			move(move);

			eval = minimax(move, minimaxDepth, Integer.MIN_VALUE, Integer.MAX_VALUE, false, myScore, enemyScore);

			// Restore score
			myScore = curMyScore;
			enemyScore = curEnemyScore;
			// Restore board
			undoMove(temp_board);
			if (eval > maxEval) {
				maxEval = eval;
				chosenMove = move;
			}
		}
		System.gc();// garbage collection
		return chosenMove;
	}

	private int calcWhiteScore(String move) {
		int x1, y1, x2, y2;
		int returningWhiteScore = 0;
		x1 = Integer.parseInt(Character.toString(move.charAt(0)));
		y1 = Integer.parseInt(Character.toString(move.charAt(1)));
		x2 = Integer.parseInt(Character.toString(move.charAt(2)));
		y2 = Integer.parseInt(Character.toString(move.charAt(3)));
//		System.out.println("Move: "+move+"\tx:"+x+" y:"+y+"\t cell: "+board[x][y]);
		if (Character.toString(board[x1][y1].charAt(0)).equals("W")) {
			if (Character.toString(board[x2][y2].charAt(0)).equals("B")) {
				// String chesspart = Character.toString(board[x2][y2].charAt(1)); // what kind
				// of enemy we are about to capture
				if (Character.toString(board[x2][y2].charAt(1)).equals("P")) {
					returningWhiteScore += 1;
				} else if (Character.toString(board[x2][y2].charAt(1)).equals("R")) {
					returningWhiteScore += 3;
				} else if (Character.toString(board[x2][y2].charAt(1)).equals("K")) {
					returningWhiteScore += 8;
				}
			} else if (Character.toString(board[x2][y2].charAt(0)).equals("P")) {// prize
				if (Math.random() > prizeChance)
					returningWhiteScore += 1;
			}
			if (Character.toString(board[x1][y1].charAt(1)).equals("P")) {
				if ((x1 == rows - 2 && x2 == rows - 1) || (x1 == 1 && x2 == 0)) { // last row
					returningWhiteScore += 1;
				}
			}
		}
//		System.out.println("Calc mooooooo \t"+returningScore);
		return returningWhiteScore;
	}

	private int calcBlackScore(String move) {
		int x1, y1, x2, y2;
		int returningBlackScore = 0;
		x1 = Integer.parseInt(Character.toString(move.charAt(0)));
		y1 = Integer.parseInt(Character.toString(move.charAt(1)));
		x2 = Integer.parseInt(Character.toString(move.charAt(2)));
		y2 = Integer.parseInt(Character.toString(move.charAt(3)));
//		System.out.println("Move: "+move+"\tx:"+x+" y:"+y+"\t cell: "+board[x][y]);
		if (Character.toString(board[x1][y1].charAt(0)).equals("B")) {
			if (Character.toString(board[x2][y2].charAt(0)).equals("W")) {
				// String chesspart = Character.toString(board[x2][y2].charAt(1)); // what kind
				// of enemy we are about to capture
				if (Character.toString(board[x2][y2].charAt(1)).equals("P")) {
					returningBlackScore += 1;
				} else if (Character.toString(board[x2][y2].charAt(1)).equals("R")) {
					returningBlackScore += 3;
				} else if (Character.toString(board[x2][y2].charAt(1)).equals("K")) {
					returningBlackScore += 8;
				}
			} else if (Character.toString(board[x2][y2].charAt(0)).equals("P")) {// prize
				if (Math.random() > prizeChance)
					returningBlackScore += 1;
			}
			if (Character.toString(board[x1][y1].charAt(1)).equals("P")) {
				if ((x1 == rows - 2 && x2 == rows - 1) || (x1 == 1 && x2 == 0)) { // last row
					returningBlackScore += 1;
				}
			}
		}
//		System.out.println("Calc mooooooo \t"+returningScore);
		return returningBlackScore;
	}

	private Boolean game_over() {
		Boolean white = false;
		Boolean black = false;
		int pawns = 0;

		for (int r = 0; r < this.rows; r++) {
			for (int c = 0; c < this.columns; c++) {
//				String chesspart = Character.toString(board[r][c].charAt(1)); 
				if (Character.toString(board[r][c].charAt(0)).equals("W")) {
					if (Character.toString(board[r][c].charAt(1)).equals("K")) {
						white = true;
					} else if (Character.toString(board[r][c].charAt(1)).equals("P")
							|| Character.toString(board[r][c].charAt(1)).equals("R"))
						pawns++;
				} else if (Character.toString(board[r][c].charAt(0)).equals("B")) {
					if (Character.toString(board[r][c].charAt(1)).equals("K")) {
						black = true;
					} else if (Character.toString(board[r][c].charAt(1)).equals("P")
							|| Character.toString(board[r][c].charAt(1)).equals("R"))
						pawns++;
				}
			}
		}
		if (white && black)// Both kings dead
			return false;
		else if (white || black)// One of the kings is captured
			return true;
		else if (!white && !black && pawns == 0) // Only the 2 kings left
			return true;
		return false;
	}

	private ArrayList<String> customClone(ArrayList<String> in) {
		ArrayList<String> ret = new ArrayList<String>(in.size());
		for (String s : in) {
			ret.add(s);
		}
//		ret = (ArrayList<String>)in.clone();
		return ret;
	}

	private ArrayList<String> getSortedGoodMoves(ArrayList<String> in) {
		ArrayList<String> goodMoves = new ArrayList<String>(in.size());
		ArrayList<String> backup = customClone(in);
		// first add the moves with actions
		for (String move : in) {
			int score;
			if (myColor == 0) // I am the white player
				score = calcWhiteScore(move);
			else
				score = calcBlackScore(move);
			if (score > 0)
				goodMoves.add(move);
		}
		// they can be dump so add the others as well
		for (String move : backup) {
			if (goodMoves.contains(move))
				continue;
			goodMoves.add(move);
		}
		backup = null;
		return goodMoves;
	}

	private void calculateScore(String move) {
		if (myColor == 0) { // I am the white player
			myScore += calcWhiteScore(move);
			enemyScore += calcBlackScore(move);
		} else { // I am the black player
			myScore += calcBlackScore(move);
			enemyScore += calcWhiteScore(move);
		}
	}

	private int minimax(String move, int depth, int alpha, int beta, Boolean maximizingPlayer, int mScore, int eScore) {
		ArrayList<String> children = new ArrayList<String>();
		String[][] temp_board = backupCurrentBoard();

		if (depth == 0 || game_over()) {
//			System.out.println("Depth"+depth);
//			System.out.println("Recursion fin: \tmine: "+myScore+"\tenemy: "+enemyScore);
			return static_eval(mScore, eScore);
		}

		if (maximizingPlayer) {
			int maxEval = Integer.MIN_VALUE;
//			ArrayList<String> children;
			availableMoves.clear();
			if (myColor == 0) // I am the white player
				this.whiteMoves();
			else // I am the black player
				this.blackMoves();

//			children = customClone(availableMoves);
			children = getSortedGoodMoves(availableMoves);

			for (String child : children) {

				// calculate score
				calculateScore(child);
				// Make Move
				move(child);

				maxEval = Math.max(maxEval, minimax(child, depth - 1, alpha, beta, false, myScore, enemyScore));
//				System.out.println("PRWTO GAMIIESAI cur: " + curMyScore + " input: " + mScore);
				myScore = mScore;
				enemyScore = eScore;
				// restore board
				undoMove(temp_board);
				alpha = Math.max(alpha, maxEval);
				if (beta <= alpha)
					break;
			}
//			System.out.println("gurnaw max");
			return maxEval;
		} else {
			int minEval = Integer.MAX_VALUE;
//			ArrayList<String> children;
			availableMoves.clear();
			if (myColor != 0) // I am the white player
				this.whiteMoves();
			else // I am the black player
				this.blackMoves();

//			children = customClone(availableMoves);
			children = getSortedGoodMoves(availableMoves);

			for (String child : children) {

				// calculate score
				calculateScore(child);
				// Make Move
				move(child);

				minEval = Math.min(minEval, minimax(child, depth - 1, alpha, beta, true, myScore, enemyScore));
				myScore = mScore;
				enemyScore = eScore;
				// restore board
				undoMove(temp_board);
				beta = Math.min(beta, minEval);
				if (beta <= alpha)
					break;
			}
//			System.out.println("gurnaw min");
			return minEval;
		}
	}
	
	private String[][] backupCurrentBoard(){
		String[][] temp_board = new String[rows][columns];
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < columns; j++) {
				temp_board[i][j] = board[i][j];
			}
		}
		return temp_board;
	}

	public String mcts() {
		String[][] originalBoard = backupCurrentBoard();
		BoardNode root = new BoardNode(null, backupCurrentBoard(), rows, columns);

		availableMoves.clear();
		if (myColor == 0) // I am the white player
			this.whiteMoves();
		else // I am the black player
			this.blackMoves();
		
		
		root.insertChildBoards(getSortedGoodMoves(availableMoves));
		
		BoardNode currentNode 	= null;
		BoardNode lastNode 		= null;
		long end = System.currentTimeMillis() + 5500; //5.5 seconds for each move
		
		while (System.currentTimeMillis() < end) {
			currentNode = root;
			System.out.println("mcts -- 1");
			while(root.contains(currentNode.board)) {
				System.out.println("mcts -- 1.1");
				lastNode = currentNode;
				if(currentNode.isLeaf())
					break;
				currentNode = currentNode.select();
				currentNode.visitCount += 1;
			}
			//PlayOut
			this.playOut(currentNode);
			//Expand
			currentNode.insertChildBoards(getSortedGoodMoves(availableMoves));
			//BackPropagate Result
			currentNode = lastNode;
			while(root.contains(currentNode.board)) {
				//backPropagation
				currentNode.visitCount += 1;
				currentNode = currentNode.parent;
				if(currentNode == null)
					break;
			}
		}
		//restore board
		undoMove(originalBoard);
		return root.getBestMove();
	}
	
	private void playOut(BoardNode nodeIn) {
		undoMove(nodeIn.board);
		availableMoves.clear();
		if(nodeIn.level%2 == 0) {//maximizing
			if (myColor == 0) // I am the white player
				this.whiteMoves();
			else // I am the black player
				this.blackMoves();
		}else { // minimizing
			if (myColor == 0)//me white, opponent black
				this.blackMoves();
			else //me black, opponent white
				this.whiteMoves();
		}
	}

	private int static_eval(int mScore, int eScore) {
		int whitePawnValue = 0;
		int blackPawnValue = 0;
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < columns; j++) {
				if (Character.toString(board[i][j].charAt(0)) == "W") {
					if (Character.toString(board[i][j].charAt(1)).equals("P")) {
						whitePawnValue++;
					} else if (Character.toString(board[i][j].charAt(1)).equals("R")) {
						whitePawnValue += 3;
					} else if (Character.toString(board[i][j].charAt(1)).equals("K")) {
						whitePawnValue += 8;
					}
				} else if (Character.toString(board[i][j].charAt(0)) == "B") {
					if (Character.toString(board[i][j].charAt(1)).equals("P")) {
						blackPawnValue++;
					} else if (Character.toString(board[i][j].charAt(1)).equals("R")) {
						blackPawnValue += 3;
					} else if (Character.toString(board[i][j].charAt(1)).equals("K")) {
						blackPawnValue += 8;
					}
				}
			}
		}

		if (myColor == 0) // I am the white player
			return (mScore + whitePawnValue) - (eScore + blackPawnValue);
		else // I am the black player
			return (mScore + blackPawnValue) - (eScore + whitePawnValue);
	}

	private String selectRandomAction() {
		Random ran = new Random();
		int x = ran.nextInt(availableMoves.size());

		return availableMoves.get(x);
	}

	public double getAvgBFactor() {
		return nBranches / (double) nTurns;
	}

	public void move(String move) {
		int x1, y1, x2, y2;

		x1 = Integer.parseInt(Character.toString(move.charAt(0)));
		y1 = Integer.parseInt(Character.toString(move.charAt(1)));
		x2 = Integer.parseInt(Character.toString(move.charAt(2)));
		y2 = Integer.parseInt(Character.toString(move.charAt(3)));

		String chesspart = Character.toString(board[x1][y1].charAt(1));

		boolean pawnLastRow = false;

		// check if it is a move that has made a move to the last line
		if (chesspart.equals("P"))
			if ((x1 == rows - 2 && x2 == rows - 1) || (x1 == 1 && x2 == 0)) {
				board[x2][y2] = " "; // in a case an opponent's chess part has just been captured
				board[x1][y1] = " ";
				pawnLastRow = true;
			}

		// otherwise
		if (!pawnLastRow) {
			board[x2][y2] = board[x1][y1];
			board[x1][y1] = " ";
		}

	}

	public void undoMove(String[][] tempBoard) {
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < columns; j++) {
				board[i][j] = tempBoard[i][j];
			}
		}
	}

	public void makeMove(int x1, int y1, int x2, int y2, int prizeX, int prizeY) {
		String chesspart = Character.toString(board[x1][y1].charAt(1));

		boolean pawnLastRow = false;

		// check if it is a move that has made a move to the last line
		if (chesspart.equals("P"))
			if ((x1 == rows - 2 && x2 == rows - 1) || (x1 == 1 && x2 == 0)) {
				board[x2][y2] = " "; // in a case an opponent's chess part has just been captured
				board[x1][y1] = " ";
				pawnLastRow = true;
			}

		// otherwise
		if (!pawnLastRow) {
			board[x2][y2] = board[x1][y1];
			board[x1][y1] = " ";
		}

		// check if a prize has been added in the game
		if (prizeX != noPrize)
			board[prizeX][prizeY] = "P";
	}

	private void whiteMoves() {
		String firstLetter = "";
		String secondLetter = "";
		String move = "";

		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < columns; j++) {
				firstLetter = Character.toString(board[i][j].charAt(0));

				// if it there is not a white chess part in this position then keep on searching
				if (firstLetter.equals("B") || firstLetter.equals(" ") || firstLetter.equals("P"))
					continue;

				// check the kind of the white chess part
				secondLetter = Character.toString(board[i][j].charAt(1));

				if (secondLetter.equals("P")) // it is a pawn
				{

					// check if it can move one vertical position ahead
					firstLetter = Character.toString(board[i - 1][j].charAt(0));

					if (firstLetter.equals(" ") || firstLetter.equals("P")) {
						move = Integer.toString(i) + Integer.toString(j) + Integer.toString(i - 1)
								+ Integer.toString(j);

						availableMoves.add(move);
					}

					// check if it can move crosswise to the left
					if (j != 0 && i != 0) {
						firstLetter = Character.toString(board[i - 1][j - 1].charAt(0));
						if (!(firstLetter.equals("W") || firstLetter.equals(" ") || firstLetter.equals("P"))) {
							move = Integer.toString(i) + Integer.toString(j) + Integer.toString(i - 1)
									+ Integer.toString(j - 1);

							availableMoves.add(move);
						}
					}

					// check if it can move crosswise to the right
					if (j != columns - 1 && i != 0) {
						firstLetter = Character.toString(board[i - 1][j + 1].charAt(0));
						if (!(firstLetter.equals("W") || firstLetter.equals(" ") || firstLetter.equals("P"))) {

							move = Integer.toString(i) + Integer.toString(j) + Integer.toString(i - 1)
									+ Integer.toString(j + 1);
							availableMoves.add(move);
						}
					}
				} else if (secondLetter.equals("R")) // it is a rook
				{
					// check if it can move upwards
					for (int k = 0; k < rookBlocks; k++) {
						if ((i - (k + 1)) < 0)
							break;

						firstLetter = Character.toString(board[i - (k + 1)][j].charAt(0));

						if (firstLetter.equals("W"))
							break;

						move = Integer.toString(i) + Integer.toString(j) + Integer.toString(i - (k + 1))
								+ Integer.toString(j);

						availableMoves.add(move);

						// prevent detouring a chesspart to attack the other
						if (firstLetter.equals("B") || firstLetter.equals("P"))
							break;
					}

					// check if it can move downwards
					for (int k = 0; k < rookBlocks; k++) {
						if ((i + (k + 1)) == rows)
							break;

						firstLetter = Character.toString(board[i + (k + 1)][j].charAt(0));

						if (firstLetter.equals("W"))
							break;

						move = Integer.toString(i) + Integer.toString(j) + Integer.toString(i + (k + 1))
								+ Integer.toString(j);

						availableMoves.add(move);

						// prevent detouring a chesspart to attack the other
						if (firstLetter.equals("B") || firstLetter.equals("P"))
							break;
					}

					// check if it can move on the left
					for (int k = 0; k < rookBlocks; k++) {
						if ((j - (k + 1)) < 0)
							break;

						firstLetter = Character.toString(board[i][j - (k + 1)].charAt(0));

						if (firstLetter.equals("W"))
							break;

						move = Integer.toString(i) + Integer.toString(j) + Integer.toString(i)
								+ Integer.toString(j - (k + 1));

						availableMoves.add(move);

						// prevent detouring a chesspart to attack the other
						if (firstLetter.equals("B") || firstLetter.equals("P"))
							break;
					}

					// check of it can move on the right
					for (int k = 0; k < rookBlocks; k++) {
						if ((j + (k + 1)) == columns)
							break;

						firstLetter = Character.toString(board[i][j + (k + 1)].charAt(0));

						if (firstLetter.equals("W"))
							break;

						move = Integer.toString(i) + Integer.toString(j) + Integer.toString(i)
								+ Integer.toString(j + (k + 1));

						availableMoves.add(move);

						// prevent detouring a chesspart to attack the other
						if (firstLetter.equals("B") || firstLetter.equals("P"))
							break;
					}
				} else // it is the king
				{
					// check if it can move upwards
					if ((i - 1) >= 0) {
						firstLetter = Character.toString(board[i - 1][j].charAt(0));

						if (!firstLetter.equals("W")) {
							move = Integer.toString(i) + Integer.toString(j) + Integer.toString(i - 1)
									+ Integer.toString(j);

							availableMoves.add(move);
						}
					}

					// check if it can move downwards
					if ((i + 1) < rows) {
						firstLetter = Character.toString(board[i + 1][j].charAt(0));

						if (!firstLetter.equals("W")) {
							move = Integer.toString(i) + Integer.toString(j) + Integer.toString(i + 1)
									+ Integer.toString(j);

							availableMoves.add(move);
						}
					}

					// check if it can move on the left
					if ((j - 1) >= 0) {
						firstLetter = Character.toString(board[i][j - 1].charAt(0));

						if (!firstLetter.equals("W")) {
							move = Integer.toString(i) + Integer.toString(j) + Integer.toString(i)
									+ Integer.toString(j - 1);

							availableMoves.add(move);
						}
					}

					// check if it can move on the right
					if ((j + 1) < columns) {
						firstLetter = Character.toString(board[i][j + 1].charAt(0));

						if (!firstLetter.equals("W")) {
							move = Integer.toString(i) + Integer.toString(j) + Integer.toString(i)
									+ Integer.toString(j + 1);

							availableMoves.add(move);
						}
					}
				}
			}
		}
	}

	private void blackMoves() {
		String firstLetter = "";
		String secondLetter = "";
		String move = "";

		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < columns; j++) {
				firstLetter = Character.toString(board[i][j].charAt(0));

				// if it there is not a black chess part in this position then keep on searching
				if (firstLetter.equals("W") || firstLetter.equals(" ") || firstLetter.equals("P"))
					continue;

				// check the kind of the white chess part
				secondLetter = Character.toString(board[i][j].charAt(1));

				if (secondLetter.equals("P")) // it is a pawn
				{

					// check if it can move one vertical position ahead
					firstLetter = Character.toString(board[i + 1][j].charAt(0));

					if (firstLetter.equals(" ") || firstLetter.equals("P")) {
						move = Integer.toString(i) + Integer.toString(j) + Integer.toString(i + 1)
								+ Integer.toString(j);

						availableMoves.add(move);
					}

					// check if it can move crosswise to the left
					if (j != 0 && i != rows - 1) {
						firstLetter = Character.toString(board[i + 1][j - 1].charAt(0));

						if (!(firstLetter.equals("B") || firstLetter.equals(" ") || firstLetter.equals("P"))) {
							move = Integer.toString(i) + Integer.toString(j) + Integer.toString(i + 1)
									+ Integer.toString(j - 1);

							availableMoves.add(move);
						}
					}

					// check if it can move crosswise to the right
					if (j != columns - 1 && i != rows - 1) {
						firstLetter = Character.toString(board[i + 1][j + 1].charAt(0));

						if (!(firstLetter.equals("B") || firstLetter.equals(" ") || firstLetter.equals("P"))) {
							move = Integer.toString(i) + Integer.toString(j) + Integer.toString(i + 1)
									+ Integer.toString(j + 1);

							availableMoves.add(move);
						}

					}
				} else if (secondLetter.equals("R")) // it is a rook
				{
					// check if it can move upwards
					for (int k = 0; k < rookBlocks; k++) {
						if ((i - (k + 1)) < 0)
							break;

						firstLetter = Character.toString(board[i - (k + 1)][j].charAt(0));

						if (firstLetter.equals("B"))
							break;

						move = Integer.toString(i) + Integer.toString(j) + Integer.toString(i - (k + 1))
								+ Integer.toString(j);

						availableMoves.add(move);

						// prevent detouring a chesspart to attack the other
						if (firstLetter.equals("W") || firstLetter.equals("P"))
							break;
					}

					// check if it can move downwards
					for (int k = 0; k < rookBlocks; k++) {
						if ((i + (k + 1)) == rows)
							break;

						firstLetter = Character.toString(board[i + (k + 1)][j].charAt(0));

						if (firstLetter.equals("B"))
							break;

						move = Integer.toString(i) + Integer.toString(j) + Integer.toString(i + (k + 1))
								+ Integer.toString(j);

						availableMoves.add(move);

						// prevent detouring a chesspart to attack the other
						if (firstLetter.equals("W") || firstLetter.equals("P"))
							break;
					}

					// check if it can move on the left
					for (int k = 0; k < rookBlocks; k++) {
						if ((j - (k + 1)) < 0)
							break;

						firstLetter = Character.toString(board[i][j - (k + 1)].charAt(0));

						if (firstLetter.equals("B"))
							break;

						move = Integer.toString(i) + Integer.toString(j) + Integer.toString(i)
								+ Integer.toString(j - (k + 1));

						availableMoves.add(move);

						// prevent detouring a chesspart to attack the other
						if (firstLetter.equals("W") || firstLetter.equals("P"))
							break;
					}

					// check of it can move on the right
					for (int k = 0; k < rookBlocks; k++) {
						if ((j + (k + 1)) == columns)
							break;

						firstLetter = Character.toString(board[i][j + (k + 1)].charAt(0));

						if (firstLetter.equals("B"))
							break;

						move = Integer.toString(i) + Integer.toString(j) + Integer.toString(i)
								+ Integer.toString(j + (k + 1));

						availableMoves.add(move);

						// prevent detouring a chesspart to attack the other
						if (firstLetter.equals("W") || firstLetter.equals("P"))
							break;
					}
				} else // it is the king
				{
					// check if it can move upwards
					if ((i - 1) >= 0) {
						firstLetter = Character.toString(board[i - 1][j].charAt(0));

						if (!firstLetter.equals("B")) {
							move = Integer.toString(i) + Integer.toString(j) + Integer.toString(i - 1)
									+ Integer.toString(j);

							availableMoves.add(move);
						}
					}

					// check if it can move downwards
					if ((i + 1) < rows) {
						firstLetter = Character.toString(board[i + 1][j].charAt(0));

						if (!firstLetter.equals("B")) {
							move = Integer.toString(i) + Integer.toString(j) + Integer.toString(i + 1)
									+ Integer.toString(j);

							availableMoves.add(move);
						}
					}

					// check if it can move on the left
					if ((j - 1) >= 0) {
						firstLetter = Character.toString(board[i][j - 1].charAt(0));

						if (!firstLetter.equals("B")) {
							move = Integer.toString(i) + Integer.toString(j) + Integer.toString(i)
									+ Integer.toString(j - 1);

							availableMoves.add(move);
						}
					}

					// check if it can move on the right
					if ((j + 1) < columns) {
						firstLetter = Character.toString(board[i][j + 1].charAt(0));

						if (!firstLetter.equals("B")) {
							move = Integer.toString(i) + Integer.toString(j) + Integer.toString(i)
									+ Integer.toString(j + 1);

							availableMoves.add(move);
						}
					}
				}
			}
		}
	}
}
