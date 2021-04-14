import java.util.ArrayList;
import java.util.Random;

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
	private int minimaxDepth = 9;
	private int myScore = 0, enemyScore = 0;
	private int currMyScore=0, currEnemyScore=0;
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
		String[][] temp_board = new String[rows][columns]; // copy of board
		int eval;

		if (myColor == 0) // I am the white player
			this.whiteMoves();
		else // I am the black player
			this.blackMoves();

		// keeping track of the branch factor
		nTurns++;
		nBranches += availableMoves.size();
		ArrayList<String> moves = customClone(availableMoves);
//		System.out.println(moves);
		int maxEval = Integer.MIN_VALUE;
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < columns; j++) {
				temp_board[i][j] = board[i][j];
			}
		}
		int curEnemyScore 	= this.enemyScore;
		int curMyScore 		= this.myScore;
		for (String move : moves) {
			availableMoves.clear();
			// Make move
			move(move);

			eval = minimax(move, minimaxDepth, Integer.MIN_VALUE, Integer.MAX_VALUE, false);
			
			// Restore score
			this.myScore 	= curMyScore;
			this.enemyScore	= curEnemyScore;
						
			// Restore board
			undoMove(temp_board);
			
			if (eval > maxEval) {
				maxEval = eval;
				chosenMove = move;
			}
		}
		return chosenMove;
	}

	private int calcMovesScore(String move) {
		int x1,y1,x2,y2;
		int returningScore = 0;
		x1 = Integer.parseInt(Character.toString(move.charAt(0)));
		y1 = Integer.parseInt(Character.toString(move.charAt(1)));
		x2 = Integer.parseInt(Character.toString(move.charAt(2)));
		y2 = Integer.parseInt(Character.toString(move.charAt(3)));
//		System.out.println("Move: "+move+"\tx:"+x+" y:"+y+"\t cell: "+board[x][y]);

		if (Character.toString(board[x2][y2].charAt(0)).equals("P")) {// present
			if (Math.random() > prizeChance)
				returningScore++;
		} else if ((Character.toString(board[x2][y2].charAt(0)).equals("B"))
				|| Character.toString(board[x2][y2].charAt(0)).equals("W")) {
			String chesspart = Character.toString(board[x2][y2].charAt(1)); // what kind of enemy we are about to capture
			if (chesspart.equals("P")) {
				returningScore++;
			} else if (chesspart.equals("R")) {
				returningScore += 3;
			} else if (chesspart.equals("K")) {
				returningScore += 8;
			}
		}
		if( (x1==rows-2 && x2==rows-1) || (x1==1 && x2==0) ) { //last row
			returningScore++;
	   }
//		System.out.println("Calc mooooooo \t"+returningScore);
		return returningScore;
	}

	private Boolean game_over() {
		Boolean white = false;
		Boolean black = false;
		int pawns=0;

		for(int r = 0; r < this.rows; r++) {
			for(int c = 0; c < this.columns; c++) {
//				String chesspart = Character.toString(board[r][c].charAt(1)); 
				if(Character.toString(board[r][c].charAt(0)).equals("W")) {
					if(Character.toString(board[r][c].charAt(1)).equals("K"))
						white = true;
					else if(Character.toString(board[r][c].charAt(1)).equals("P") 
							|| Character.toString(board[r][c].charAt(1)).equals("R"))
						pawns++;
				}
				else if(Character.toString(board[r][c].charAt(0)).equals("B")) {
					if(Character.toString(board[r][c].charAt(1)).equals("K"))
						black = true;
					else if(Character.toString(board[r][c].charAt(1)).equals("P") 
							|| Character.toString(board[r][c].charAt(1)).equals("R"))
						pawns++;
				}
			}
		}
		if (white && black)// Both kings dead
			return false;
		else if(white||black)// One of the kings is captured
			return true;
		else if(!white && !black && pawns==0) // Only the 2 kings left
			return true;
		return false;
	}

	
	private ArrayList<String> customClone(ArrayList<String> in){
		ArrayList<String> ret = new ArrayList<String>(in.size());
		for(String s: in) {
			ret.add(s);
		}
		return ret;
	}
	/**
	 * first call alpha=Integer.MIN_VALUE and beta=Integer.MAX_VALUE and
	 * maximizingPlayer=true
	 */
	private int minimax(String move, int depth, int alpha, int beta, Boolean maximizingPlayer) {
		String[][] temp_board = new String[rows][columns];
		ArrayList<String> children=new ArrayList<String>();
		if (depth == 0 || game_over()) {
//			System.out.println("111111111111111");
//			System.out.println("Recursion fin: \tmine: "+myScore+"\tenemy: "+enemyScore);
			return static_eval();// (myScore, enemyScore);
		}

		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < columns; j++) {
				temp_board[i][j] = board[i][j];
			}
		}

		if (maximizingPlayer) {
			int maxEval = Integer.MIN_VALUE;
//			ArrayList<String> children;
			if (myColor == 0) // I am the white player
				this.whiteMoves();
			else // I am the black player
				this.blackMoves();
			children = customClone(availableMoves);
			currMyScore=this.myScore;
			this.myScore+=calcMovesScore(move);
//			System.out.println("MINIMAX::maxPlayer"+availableMoves);
			for (String child : children) {
				availableMoves.clear();
				
				// Make Move
				move(child);

				maxEval = Math.max(maxEval, minimax(child, depth - 1, alpha, beta, false));
				this.myScore=currMyScore;
				undoMove(temp_board);
				alpha = Math.max(alpha, maxEval);
				if (beta <= alpha)
					break;
			}
			return alpha;
		} else {
			int minEval = Integer.MAX_VALUE;
//			ArrayList<String> children;
			if (myColor != 0) // I am the white player
				this.whiteMoves();
			else // I am the black player
				this.blackMoves();
			children = customClone(availableMoves);
			currEnemyScore=this.enemyScore;
			this.enemyScore+=calcMovesScore(move);
//			System.out.println("MINIMAX::minPlayer"+availableMoves);
			for (String child : children) {
				availableMoves.clear();

				// Make Move
				move(child);

				minEval = Math.min(minEval, minimax(child, depth - 1, alpha, beta, true));
				this.enemyScore=currEnemyScore;
				undoMove(temp_board);
				beta = Math.min(beta, minEval);
				if (beta <= alpha)
					break;
			}
			return beta;
		}
	}

	private int static_eval() {
		int whitePawnValue=0;
		int blackPawnValue=0;
		for(int i = 0; i < this.rows; i++) {
	         for(int j = 0; j < this.columns; j++){
	        	 if( Character.toString(board[i][j].charAt(0)) == "W") {
	        		 if(Character.toString(board[i][j].charAt(1)).equals("P")) {
	        			 whitePawnValue++;
	        		 }else if(Character.toString(board[i][j].charAt(1)).equals("R")) {
	        			 whitePawnValue+=3;
	        		 }else if(Character.toString(board[i][j].charAt(1)).equals("K")) {
	        			 whitePawnValue+=8;
	        		 }
	        	 }else if ( Character.toString(board[i][j].charAt(0)) == "B") {
	        		 if(Character.toString(board[i][j].charAt(1)).equals("P")) {
	        			 blackPawnValue++;
	        		 }else if(Character.toString(board[i][j].charAt(1)).equals("R")) {
	        			 blackPawnValue+=3;
	        		 }else if(Character.toString(board[i][j].charAt(1)).equals("K")) {
	        			 blackPawnValue+=8;
	        		 }
	        	 }
	        }
		}
		int value=(this.myScore+whitePawnValue)-(this.enemyScore+blackPawnValue);
		if(myColor == 0)		// I am the white player
			return value;
		else					// I am the black player
			return (this.myScore+blackPawnValue)-(this.enemyScore+whitePawnValue);
	}

	private int static_eval(String move) {

		int eval = 0;
		String[] parts = move.split("\n|\\||-|\\s+");
		for (String part : parts) {
			if (part.equals("WP"))
				eval++;
			else if (part.equals("BP"))
				eval--;
			else if (part.equals("WR"))
				eval = eval + 3;
			else if (part.equals("BR"))
				eval = eval - 3;
			else if (part.equals("WK"))
				eval = eval + 8;
			else if (part.equals("BK"))
				eval = eval - 8;
		}
		if (myColor != 0)
			eval = -eval;

		return eval;
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
		int x1,y1,x2,y2;
		
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
					// check if it can move towards the last row
					if (i - 1 == 0 && (Character.toString(board[i - 1][j].charAt(0)).equals(" ")
							|| Character.toString(board[i - 1][j].charAt(0)).equals("P"))) {
						move = Integer.toString(i) + Integer.toString(j) + Integer.toString(i - 1)
								+ Integer.toString(j);

						availableMoves.add(move);
						continue;
					}

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

						if (firstLetter.equals("W") || firstLetter.equals(" ") || firstLetter.equals("P"))
							continue;

						move = Integer.toString(i) + Integer.toString(j) + Integer.toString(i - 1)
								+ Integer.toString(j - 1);

						availableMoves.add(move);
					}

					// check if it can move crosswise to the right
					if (j != columns - 1 && i != 0) {
						firstLetter = Character.toString(board[i - 1][j + 1].charAt(0));

						if (firstLetter.equals("W") || firstLetter.equals(" ") || firstLetter.equals("P"))
							continue;

						move = Integer.toString(i) + Integer.toString(j) + Integer.toString(i - 1)
								+ Integer.toString(j + 1);

						availableMoves.add(move);
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
					// check if it is at the last row
					if (i + 1 == rows - 1 && (Character.toString(board[i + 1][j].charAt(0)).equals(" ")
							|| Character.toString(board[i + 1][j].charAt(0)).equals("P"))) {
						move = Integer.toString(i) + Integer.toString(j) + Integer.toString(i + 1)
								+ Integer.toString(j);

						availableMoves.add(move);
						continue;
					}

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

						if (firstLetter.equals("B") || firstLetter.equals(" ") || firstLetter.equals("P"))
							continue;

						move = Integer.toString(i) + Integer.toString(j) + Integer.toString(i + 1)
								+ Integer.toString(j - 1);

						availableMoves.add(move);
					}

					// check if it can move crosswise to the right
					if (j != columns - 1 && i != rows - 1) {
						firstLetter = Character.toString(board[i + 1][j + 1].charAt(0));

						if (firstLetter.equals("B") || firstLetter.equals(" ") || firstLetter.equals("P"))
							continue;

						move = Integer.toString(i) + Integer.toString(j) + Integer.toString(i + 1)
								+ Integer.toString(j + 1);

						availableMoves.add(move);
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
