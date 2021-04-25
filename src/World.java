import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;

public class World{
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
	private int currMyScore=0, currEnemyScore=0;
	private int curMyScore=0, curEnemyScore=0;
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
     	
//		cl=new Client();
//		this.myScore=0;
//		this.enemyScore=0;
		
		
		 
		if (myColor == 0) { // I am the white player
			this.whiteMoves();
			myScore=Client.getScoreWhite();
			enemyScore=Client.getScoreBlack();
			curMyScore=myScore;
			curEnemyScore=enemyScore;
		}
		else { // I am the black player
			this.blackMoves();
			enemyScore=Client.getScoreWhite();
			myScore=Client.getScoreBlack();
			curMyScore=myScore;
			curEnemyScore=enemyScore;
		}
		
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
//		System.out.println("--------------1-------------");
//		for (int i = 0; i < rows; i++) {
//			for (int j = 0; j < columns; j++) {
//				if(Character.toString(board[i][j].charAt(0)).equals("P"))
//					System.out.print("|P|");
//				else if(!((Character.toString(board[i][j].charAt(0)).equals("B")) || Character.toString(board[i][j].charAt(0)).equals("W")))
//					System.out.print("|-|");
//				else
//					System.out.print("|"+board[i][j]+"|");
//			}
//			System.out.println("");
//		}
//		System.out.println("--------------1--------------");
		for (String move : moves) {
			for(int k = 0; k < rows; k++) {
				for(int j = 0; j < columns; j++) {
		           board[k][j] = temp_board[k][j];
		        }
		    }
			availableMoves.clear();
//			System.out.println("============befArx==============");
//			System.out.println(move);
//			for (int i = 0; i < rows; i++) {
//				for (int j = 0; j < columns; j++) {
//					if(Character.toString(board[i][j].charAt(0)).equals("P"))
//						System.out.print("|P|");
//					else if(!((Character.toString(board[i][j].charAt(0)).equals("B")) || Character.toString(board[i][j].charAt(0)).equals("W")))
//						System.out.print("|-|");
//					else
//						System.out.print("|"+board[i][j]+"|");
//				}
//				System.out.println("");
//			}
//			System.out.println("=============befArx=============");
//			System.out.println("Before Move Mine: "+myScore+"\tEnemy: "+enemyScore);
			
			if (myColor == 0) { // I am the white player
				myScore+=calcWhiteScore(move);
				enemyScore+=calcBlackScore(move);
			}	
			else { // I am the black player
				myScore+=calcBlackScore(move);
				enemyScore+=calcWhiteScore(move);
			}
			// Make move
			move(move);
			
//			System.out.println("============afArx==============");
//			System.out.println(move);
//			for (int i = 0; i < rows; i++) {
//				for (int j = 0; j < columns; j++) {
//					if(Character.toString(board[i][j].charAt(0)).equals("P"))
//						System.out.print("|P|");
//					else if(!((Character.toString(board[i][j].charAt(0)).equals("B")) || Character.toString(board[i][j].charAt(0)).equals("W")))
//						System.out.print("|-|");
//					else
//						System.out.print("|"+board[i][j]+"|");
//				}
//				System.out.println("");
//			}
//			System.out.println("=============afArx=============");
//			System.out.println("After Move Mine: "+myScore+"\tEnemy: "+enemyScore);
			eval = minimax(move, minimaxDepth, Integer.MIN_VALUE, Integer.MAX_VALUE, false,myScore,enemyScore);
			
			// Restore score
			myScore 	= curMyScore;
			enemyScore	= curEnemyScore;
			
			// Restore board
			undoMove(temp_board);
//			System.out.println("============afArxRe==============");
//			System.out.println(move);
//			for (int i = 0; i < rows; i++) {
//				for (int j = 0; j < columns; j++) {
//					if(Character.toString(board[i][j].charAt(0)).equals("P"))
//						System.out.print("|P|");
//					else if(!((Character.toString(board[i][j].charAt(0)).equals("B")) || Character.toString(board[i][j].charAt(0)).equals("W")))
//						System.out.print("|-|");
//					else
//						System.out.print("|"+board[i][j]+"|");
//				}
//				System.out.println("");
//			}
//			System.out.println("=============afArxRe=============");
//			System.out.println("After Undo Mine: "+myScore+"\tEnemy: "+enemyScore);
			if (eval > maxEval) {
				maxEval = eval;
				chosenMove = move;
			}
		}
		return chosenMove;
	}

	private int calcWhiteScore(String move) {
		int x1,y1,x2,y2;
		int returningWhiteScore = 0;
		x1 = Integer.parseInt(Character.toString(move.charAt(0)));
		y1 = Integer.parseInt(Character.toString(move.charAt(1)));
		x2 = Integer.parseInt(Character.toString(move.charAt(2)));
		y2 = Integer.parseInt(Character.toString(move.charAt(3)));
//		System.out.println("Move: "+move+"\tx:"+x+" y:"+y+"\t cell: "+board[x][y]);
		if (Character.toString(board[x1][y1].charAt(0)).equals("W")){
			if(Character.toString(board[x2][y2].charAt(0)).equals("B")){
			//String chesspart = Character.toString(board[x2][y2].charAt(1)); // what kind of enemy we are about to capture
				if (Character.toString(board[x2][y2].charAt(1)).equals("P")) {
					returningWhiteScore++;
				} else if(Character.toString(board[x2][y2].charAt(1)).equals("R")) {
					returningWhiteScore += 3;
				} else if(Character.toString(board[x2][y2].charAt(1)).equals("K")) {
					returningWhiteScore += 8;
				}
			}
			else if(Character.toString(board[x2][y2].charAt(0)).equals("P")) {// prize
				if(Math.random() > prizeChance)
					returningWhiteScore++;
			}
			if(Character.toString(board[x1][y1].charAt(1)).equals("P")) {
				if((x1==rows-2 && x2==rows-1) || (x1==1 && x2==0)) { //last row
					returningWhiteScore++;
				}
			}
		}
//		System.out.println("Calc mooooooo \t"+returningScore);
		return returningWhiteScore;
	}
	
	private int calcBlackScore(String move) {
		int x1,y1,x2,y2;
		int returningBlackScore = 0;
		x1 = Integer.parseInt(Character.toString(move.charAt(0)));
		y1 = Integer.parseInt(Character.toString(move.charAt(1)));
		x2 = Integer.parseInt(Character.toString(move.charAt(2)));
		y2 = Integer.parseInt(Character.toString(move.charAt(3)));
//		System.out.println("Move: "+move+"\tx:"+x+" y:"+y+"\t cell: "+board[x][y]);
		if (Character.toString(board[x1][y1].charAt(0)).equals("B")){
			if(Character.toString(board[x2][y2].charAt(0)).equals("W")){
			//String chesspart = Character.toString(board[x2][y2].charAt(1)); // what kind of enemy we are about to capture
				if (Character.toString(board[x2][y2].charAt(1)).equals("P")) {
					returningBlackScore++;
				} else if(Character.toString(board[x2][y2].charAt(1)).equals("R")) {
					returningBlackScore += 3;
				} else if(Character.toString(board[x2][y2].charAt(1)).equals("K")) {
					returningBlackScore += 8;
				}
			}
			else if(Character.toString(board[x2][y2].charAt(0)).equals("P")) {// prize
				if(Math.random() > prizeChance)
					returningBlackScore++;
			}
			if(Character.toString(board[x1][y1].charAt(1)).equals("P")) {
				if((x1==rows-2 && x2==rows-1) || (x1==1 && x2==0)) { //last row
					returningBlackScore++;
				}
			}
		}
//		System.out.println("Calc mooooooo \t"+returningScore);
		return returningBlackScore;
	}

	private Boolean game_over() {
		Boolean white = false;
		Boolean black = false;
		int pawns=0;

		for(int r = 0; r < this.rows; r++) {
			for(int c = 0; c < this.columns; c++) {
//				String chesspart = Character.toString(board[r][c].charAt(1)); 
				if(Character.toString(board[r][c].charAt(0)).equals("W")) {
					if(Character.toString(board[r][c].charAt(1)).equals("K")) {
						white = true;
					}
					else if(Character.toString(board[r][c].charAt(1)).equals("P") 
							|| Character.toString(board[r][c].charAt(1)).equals("R"))
						pawns++;
				}
				else if(Character.toString(board[r][c].charAt(0)).equals("B")) {
					if(Character.toString(board[r][c].charAt(1)).equals("K")) {
						black = true;
					}
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
	
	private ArrayList<String> sortedClone(ArrayList<String> in, boolean b){
		ArrayList<String> ret = new ArrayList<String>(in.size());
		ArrayList<String> retReal = new ArrayList<String>(in.size());
		String[][] tmp_board = new String[rows][columns];
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < columns; j++) {
				tmp_board[i][j] = board[i][j];
			}
		}
		
		for(String s: in) {
			ret.add(s);
		}
		
		
		
		
		if (b==true) { //maximizing 
			
			for(int i=0;i<ret.size();i++) {
				
				String move1=ret.get(i);
				
				move(move1);
				
				int score1 =calcWhiteScore(ret.get(i));
				undoMove(tmp_board);
				
				String move2=ret.get(i+1);
				
				move(move2);
				
				int score2 =calcWhiteScore(ret.get(i+1));
				undoMove(tmp_board);
				
			
					if(score1 >= score2) {
						retReal.add(move1);
					}else {
						retReal.add(move2);
					}
			
			}
		}else{ 	//minimizing 
				for(int i=0;i<ret.size();i++) {
				
					String move1=ret.get(i);
				
					move(move1);
				
					int score1 =calcWhiteScore(ret.get(i));
					undoMove(tmp_board);
				
					String move2=ret.get(i+1);
				
					move(move2);
				
					int score2 =calcWhiteScore(ret.get(i+1));
					undoMove(tmp_board);
				
			
						if(score1 <= score2) {
							retReal.add(move1);
						
						}else {
								retReal.add(move2);
						}
			
		  }
				
		}		
		return retReal;
	}
	
	
	
	
	/**
	 * first call alpha=Integer.MIN_VALUE and beta=Integer.MAX_VALUE and
	 * maximizingPlayer=true
	 */
	
	
	
	
	
	private int minimax(String move, int depth, int alpha, int beta, Boolean maximizingPlayer ,int mScore , int eScore) {
		String[][] temp_board = new String[rows][columns];
		ArrayList<String> children=new ArrayList<String>();
		boolean b=false;
		if (depth == 0 || game_over()) {
//			System.out.println("Depth"+depth);
//			System.out.println("Recursion fin: \tmine: "+myScore+"\tenemy: "+enemyScore);
			return static_eval(mScore,eScore);// (myScore, enemyScore);
		}

		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < columns; j++) {
				temp_board[i][j] = board[i][j];
			}
		}

		if (maximizingPlayer) {
			int maxEval = Integer.MIN_VALUE;
//			ArrayList<String> children;
			availableMoves.clear();
			if (myColor == 0) // I am the white player
				this.whiteMoves();
			else // I am the black player
				this.blackMoves();
			b=true;
			children = customClone(availableMoves);
			
			
//			System.out.println("MINIMAX::maxPlayer"+availableMoves);
			currMyScore=mScore;
			currEnemyScore=eScore;
			for (String child : children) {
				for(int k = 0; k < rows; k++) {
					for(int j = 0; j < columns; j++) {
			           board[k][j] = temp_board[k][j];
			        }
			    }
				
//				System.out.println("============befMax=============");
//				System.out.println(child);
//				for (int i = 0; i < rows; i++) {
//					for (int j = 0; j < columns; j++) {
//						if(Character.toString(board[i][j].charAt(0)).equals("P"))
//							System.out.print("|P|");
//						else if(!((Character.toString(board[i][j].charAt(0)).equals("B")) || Character.toString(board[i][j].charAt(0)).equals("W")))
//							System.out.print("|-|");
//						else
//							System.out.print("|"+board[i][j]+"|");
//					}
//					System.out.println("");
//				}
//				System.out.println("============befMax============");
//				System.out.println("Mine: "+myScore+"\tEnemy: "+enemyScore);
				
				if (myColor == 0) { // I am the white player
					myScore+=calcWhiteScore(child);
					enemyScore+=calcBlackScore(child);
//					myScore=mScore;
//					enemyScore=eScore;
				}	
				else { // I am the black player
					myScore+=calcBlackScore(child);
					enemyScore+=calcWhiteScore(child);
//					myScore=mScore;
//					enemyScore=eScore;
				}
				// Make Move
				move(child);

//				System.out.println("===========afMax============");
//				System.out.println(child);
//				for (int i = 0; i < rows; i++) {
//					for (int j = 0; j < columns; j++) {
//						if(Character.toString(board[i][j].charAt(0)).equals("P"))
//							System.out.print("|P|");
//						else if(!((Character.toString(board[i][j].charAt(0)).equals("B")) || Character.toString(board[i][j].charAt(0)).equals("W")))
//							System.out.print("|-|");
//						else
//							System.out.print("|"+board[i][j]+"|");
//					}
//					System.out.println("");
//				}
//				System.out.println("===========afMax============");
//				System.out.println("Mine: "+myScore+"\tEnemy: "+enemyScore);
				maxEval = Math.max(maxEval, minimax(child, depth - 1, alpha, beta, false, myScore, enemyScore));
				myScore=currMyScore;
				enemyScore=currEnemyScore;
				undoMove(temp_board);
//				System.out.println("===========afMaxRe============");
//				System.out.println(child);
//				for (int i = 0; i < rows; i++) {
//					for (int j = 0; j < columns; j++) {
//						if(Character.toString(board[i][j].charAt(0)).equals("P"))
//							System.out.print("|P|");
//						else if(!((Character.toString(board[i][j].charAt(0)).equals("B")) || Character.toString(board[i][j].charAt(0)).equals("W")))
//							System.out.print("|-|");
//						else
//							System.out.print("|"+board[i][j]+"|");
//					}
//					System.out.println("");
//				}
//				System.out.println("===========afMaxRe============");
//				System.out.println("Mine: "+myScore+"\tEnemy: "+enemyScore);
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
			b=false;
			children = customClone(availableMoves);
//			System.out.println("MINIMAX::minPlayer"+availableMoves);
			currMyScore=mScore;
			currEnemyScore=eScore;
			for (String child : children) {
				for(int k = 0; k < rows; k++) {
					for(int j = 0; j < columns; j++) {
			           board[k][j] = temp_board[k][j];
			        }
			    }
//				availableMoves.clear();

//				System.out.println("===========befMin============");
//				System.out.println(child);
//				for (int i = 0; i < rows; i++) {
//					for (int j = 0; j < columns; j++) {
//						if(Character.toString(board[i][j].charAt(0)).equals("P"))
//							System.out.print("|P|");
//						else if(!((Character.toString(board[i][j].charAt(0)).equals("B")) || Character.toString(board[i][j].charAt(0)).equals("W")))
//							System.out.print("|-|");
//						else
//							System.out.print("|"+board[i][j]+"|");
//					}
//					System.out.println("");
//				}
//				System.out.println("===========befMin============");
//				System.out.println("Mine: "+myScore+"\tEnemy: "+enemyScore);
				
				if (myColor == 0) { // I am the white player
					myScore+=calcWhiteScore(child);
					enemyScore+=calcBlackScore(child);
//					this.myScore=mScore;
//					this.enemyScore=eScore;
				}	
				else { // I am the black player
					myScore+=calcBlackScore(child);
					enemyScore+=calcWhiteScore(child);
//					this.myScore=mScore;
//					this.enemyScore=eScore;
				}
				// Make Move
				move(child);
				
//				System.out.println("===========afMin============");
//				System.out.println(child);
//				for (int i = 0; i < rows; i++) {
//					for (int j = 0; j < columns; j++) {
//						if(Character.toString(board[i][j].charAt(0)).equals("P"))
//							System.out.print("|P|");
//						else if(!((Character.toString(board[i][j].charAt(0)).equals("B")) || Character.toString(board[i][j].charAt(0)).equals("W")))
//							System.out.print("|-|");
//						else
//							System.out.print("|"+board[i][j]+"|");
//					}
//					System.out.println("");
//				}
//				System.out.println("===========afMin============");
//				System.out.println("Mine: "+myScore+"\tEnemy: "+enemyScore);
				minEval = Math.min(minEval, minimax(child, depth - 1, alpha, beta, true, myScore, enemyScore));
				myScore=currMyScore;
				enemyScore=currEnemyScore;
				undoMove(temp_board);
//				System.out.println("===========afMinRe============");
//				System.out.println(child);
//				for (int i = 0; i < rows; i++) {
//					for (int j = 0; j < columns; j++) {
//						if(Character.toString(board[i][j].charAt(0)).equals("P"))
//							System.out.print("|P|");
//						else if(!((Character.toString(board[i][j].charAt(0)).equals("B")) || Character.toString(board[i][j].charAt(0)).equals("W")))
//							System.out.print("|-|");
//						else
//							System.out.print("|"+board[i][j]+"|");
//					}
//					System.out.println("");
//				}
//				System.out.println("===========afMinRe============");
//				System.out.println("Mine: "+myScore+"\tEnemy: "+enemyScore);
				beta = Math.min(beta, minEval);
				if (beta <= alpha)
					break;
			}
//			System.out.println("gurnaw min");
			return minEval;
		}
	}

	private int static_eval(int mScore , int eScore) {
		int whitePawnValue=0;
		int blackPawnValue=0;
		int value=0;
		for(int i = 0; i < rows; i++) {
	         for(int j = 0; j < columns; j++){
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
		value=(mScore+whitePawnValue)-(eScore+blackPawnValue);
		if(myColor == 0)		// I am the white player
			return value;
		else					// I am the black player
			return (mScore+blackPawnValue)-(eScore+whitePawnValue);
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
