package nl.jeffreyklardie.scrabbleBot.game.objects;


import nl.jeffreyklardie.scrabbleBot.util.Dictionary;
import nl.jeffreyklardie.scrabbleBot.util.WordPosition;

public class Board {

    public static final int BOARD_SIZE = 15;
    
    public static final int BONUS_LETTER_TIMES_TWO      = 1;
    public static final int BONUS_LETTER_TIMES_THREE    = 2;
    public static final int BONUS_WORD_TIMES_TWO        = 3;
    public static final int BONUS_WORD_TIMES_THREE      = 4;
    public static final int BONUS_USE_ALL_LETTERS		= 50;
    
    public static final int INVALID_WORD				= -1;
    public static final int VALID_WORD_NOT_NEW			= -2;
    
    /**
     * Bonus points per field. Fields match with above constants.
     */
    public static byte[][] BONUS_FIELDS = {
        {4,0,0,1,0,0,0,4,0,0,0,1,0,0,4},
        {0,3,0,0,0,2,0,0,0,2,0,0,0,3,0},
        {0,0,3,0,0,0,1,0,1,0,0,0,3,0,0},
        {0,0,0,3,0,0,0,1,0,0,0,3,0,0,0},
        {0,0,0,0,3,0,0,0,0,0,3,0,0,0,0},
        {0,2,0,0,0,2,0,0,0,2,0,0,0,2,0},
        {0,0,1,0,0,0,1,0,1,0,0,0,1,0,0},
        {4,0,0,1,0,0,0,3,0,0,0,1,0,0,4},
        {0,0,1,0,0,0,1,0,1,0,0,0,1,0,0},
        {0,2,0,0,0,2,0,0,0,2,0,0,0,2,0},
        {0,0,0,0,3,0,0,0,0,0,3,0,0,0,0},
        {0,0,0,3,0,0,0,1,0,0,0,3,0,0,0},
        {0,0,3,0,0,0,1,0,1,0,0,0,3,0,0},
        {0,3,0,0,0,2,0,0,0,2,0,0,0,3,0},
        {4,0,0,1,0,0,0,4,0,0,0,1,0,0,4},
    };
        
    private static Board instance; 
    private char[][] board;
    private boolean boardIsEmpty = true;

    public static Board getInstance(){
        if(instance == null) instance = new Board();
        return instance;
    }
    
    private Board(){
        board = new char[BOARD_SIZE][BOARD_SIZE];
        
        for(int row=0; row < BOARD_SIZE; row++){
            for(int col=0; col < BOARD_SIZE; col++){
                board[row][col] = LetterBag.EMPTY_LETTER;
            }
        }
    }
    
    /**
     * Return the letters that are on the board as a string
     * 
     * @return
     */
    public String getLetters(){
    	String letters = "";
    	for(int row=0; row < BOARD_SIZE; row++){
            for(int col=0; col < BOARD_SIZE; col++){
                if(board[row][col] != LetterBag.EMPTY_LETTER){
                	letters += board[row][col];
                }
            }
        }
    	
    	return letters;
    }
    
    public boolean isEmpty(){
        return boardIsEmpty;
    }
       
    /**
     * Get the number of points a word would score on a specific position.
     * Return -1 if the position is invalid.
     * 
     * @param wordPos
     * @return
     */
    public int getWordScore(WordPosition wordPos, Rack rack){
    	// check if the word fits the board
    	int wordStartIndex = (wordPos.horizontal) ? wordPos.col : wordPos.row;
    	if(wordStartIndex < 0 && (wordStartIndex + wordPos.word.length() - 1) >= Board.BOARD_SIZE){
    		return INVALID_WORD;
    	}
    	
    	int score = 0;
    	
    	int tmpScore;
    	if(wordPos.horizontal){
    		// get the score for the horizontal word, or return if the position is invalid
    		tmpScore = getHorizontalWordPoints(wordPos, wordPos.row, rack);
    		if(tmpScore == INVALID_WORD || tmpScore == VALID_WORD_NOT_NEW) return INVALID_WORD;
    		score += tmpScore;
    		wordPos.addScoreLine(tmpScore + " " + wordPos.word);
    		
    		if(wordPos.fromRack.length() == Rack.NUM_LETTERS_ON_RACK){
    		    score += 50;
                wordPos.addScoreLine(" + " + 50 + " Letter bonus");
    		}
    		
    		// get the score for the vertical created words, or return if the position is invalid
    		for(int c=wordPos.col; c<wordPos.col + wordPos.word.length(); c++){
    			tmpScore = getVerticalWordPoints(wordPos, c, rack);
    			if(tmpScore == INVALID_WORD) return INVALID_WORD;
    			else if(tmpScore != VALID_WORD_NOT_NEW) score += tmpScore;
    		}
    	} else {
    		// get the score for the vertical word, or return if the position is invalid
    		tmpScore = getVerticalWordPoints(wordPos, wordPos.col, rack);
    		if(tmpScore == INVALID_WORD || tmpScore == VALID_WORD_NOT_NEW) return INVALID_WORD;
    		score += tmpScore;
    		wordPos.addScoreLine(tmpScore + " " + wordPos.word);
    		
    		if(wordPos.fromRack.length() == Rack.NUM_LETTERS_ON_RACK) {
    		    score += 50;
    		    wordPos.addScoreLine(" + " + 50 + " Letter bonus");
    		}
    		
    		// get the score for the horizontal created words, or return if the position is invalid
    		for(int r=wordPos.row; r<wordPos.row + wordPos.word.length(); r++){
    			tmpScore = getHorizontalWordPoints(wordPos, r, rack);
    			if(tmpScore == INVALID_WORD) return INVALID_WORD;
    			else if(tmpScore != VALID_WORD_NOT_NEW) score += tmpScore;
    		}
    	}
    	
    	return score;
    	
    }
    
    /**
     * Get the number of points for the horizontal word
     * 
     * @return int score
     */
    private int getHorizontalWordPoints(WordPosition wordPos, int row, Rack rack){
    	int c;
    	for(c=wordPos.col; c>=0; c--){
    		if(c < wordPos.col && board[row][c] == LetterBag.EMPTY_LETTER){
    			c++; 
    			break;
    		}
    	}
    	
    	if(c < 0) c = 0;
    	
    	// c now points to the first letter in the word
    	if(wordPos.horizontal) wordPos.fromRack = "";
    	String fullWord = "";
    	boolean newWord = false;
    	int wordIndex, letterScore, wordScore = 0, wordMultiplier = 1;
    	char letter;
    	
    	String remainingRack = rack.getLetters();
    	int index;
    	int wordLength = wordPos.word.length();
    	boolean useJokerForLetter = false;
    	
    	for(; c<BOARD_SIZE; c++){
    		if(wordPos.horizontal){
    			wordIndex = c - wordPos.col;
    		} else {
    			wordIndex = (c == wordPos.col) ? row - wordPos.row : -1;
    		}

    		if(wordIndex >= 0 && wordIndex < wordLength){
    			letter = wordPos.word.charAt(wordIndex);
    			
    			if(board[row][c] == LetterBag.EMPTY_LETTER){
    				// board square is empty, so we are creating a new word
    				newWord = true;
    				
    				if(wordPos.horizontal){
    					// we are testing the horizontal word and the current letter is not
    					// on the board, so it should be available on our rack
    					index = remainingRack.indexOf(letter);
    					
    					// the letter is not available on the rack, so return invalid if we don't have a joker
    		    		if(index == -1) {
    		    			int jokerIndex = remainingRack.indexOf(LetterBag.JOKER); 
    		    			if(jokerIndex == -1){
    		    				return INVALID_WORD;
    		    			}
    		    			index = jokerIndex;
    		    			useJokerForLetter = true;
    		    		}
    		    		remainingRack = (new StringBuffer(remainingRack).deleteCharAt(index).toString());
    		    		
    		    		wordPos.fromRack += letter;
    				}
    			} else if(board[row][c] != letter){
    				// board square is not empty, and is different than our letter. This is not valid.
    				return 
    				INVALID_WORD;
    			}
    				
    		} else if(board[row][c] == LetterBag.EMPTY_LETTER) {
    			break;
    		} else {
    			letter = board[row][c];
    		}
    		
    		fullWord += letter;
    		letterScore = (useJokerForLetter) ? 0 : LetterBag.getLetterScore(letter);
    		switch (BONUS_FIELDS[row][c]) {
				case BONUS_LETTER_TIMES_TWO:
					letterScore *= 2;
					break;
				case BONUS_LETTER_TIMES_THREE:
					letterScore *= 3;
					break;
				case BONUS_WORD_TIMES_TWO:
					wordMultiplier = 2;
					break;
				case BONUS_WORD_TIMES_THREE:
					wordMultiplier = 3;
					break;
				default:
					break;
			}
    		
    		wordScore += letterScore;
    	}
    	
    	if(fullWord.length() > 1){
    		// word contains more than one character, so check if the word exists,
    		// and return the number of points
    		if(!Dictionary.getInstance().contains(fullWord)) 
    			return INVALID_WORD;

    		// Word is invalid if it is not a new word on the board
    		if(!newWord) 
    			return VALID_WORD_NOT_NEW;

//    		
//    		if(fullWord.length() >= wordLength){
//    			String direction = (wordPos.horizontal) ? "horizontal" : "vertical";
//    			System.out.println("Check Horizontal word '"+fullWord+"' created by laying " + direction + " word '"+wordPos.word+"'");
//    		}
    		
    		if(!wordPos.horizontal) wordPos.addScoreLine("+" + wordScore * wordMultiplier + " " + fullWord);
    		
    		return wordScore * wordMultiplier;
    	}
    	
    	
		// the word only contains one character, so we did not create a new word.
		// we return 0, which means this is a valid move, but does not score extra points
		return 0;
    }
    
    /**
     * Get the number of points for the vertical word
     * 
     * @return int score
     */
    private int getVerticalWordPoints(WordPosition wordPos, int col, Rack rack){
    	int r;
    	for(r=wordPos.row; r>=0; r--){
    		if(r < wordPos.row && board[r][col] == LetterBag.EMPTY_LETTER){
    			r++; 
    			break;
    		}
    	}
    	
    	if(r < 0) r = 0;
    	
    	// r now points to the first letter in the word
    	if(!wordPos.horizontal) wordPos.fromRack = "";
    	String fullWord = "";
    	boolean newWord = false;
    	int wordIndex, letterScore, wordScore = 0, wordMultiplier = 1;
    	char letter;
    	
    	String remainingRack = rack.getLetters();
    	int index;
    	int wordLength = wordPos.word.length();
    	
    	for(; r<BOARD_SIZE; r++){
    		if(!wordPos.horizontal){
    			wordIndex = r - wordPos.row;
    		} else {
    			wordIndex = (r == wordPos.row) ? col-wordPos.col : -1;
    		}

    		if(wordIndex >= 0 && wordIndex < wordLength){
    			letter = wordPos.word.charAt(wordIndex);
    			
    			if(board[r][col] == LetterBag.EMPTY_LETTER){
    				// board square is empty, so we are creating a new word
    				newWord = true;
    				
    				if(!wordPos.horizontal){
    					// we are testing the horizontal word and the current letter is not
    					// on the board, so it should be available on our rack
    					index = remainingRack.indexOf(letter);
    					
    					// the letter is not available on the rack, so return invalid if we do not have a joker
    		    		if(index == -1){
    		    			int jokerIndex = remainingRack.indexOf(LetterBag.JOKER); 
    		    			if(jokerIndex == -1){
    		    				return INVALID_WORD;
    		    			}
    		    			index = jokerIndex;
    		    		}
    		    		remainingRack = (new StringBuffer(remainingRack).deleteCharAt(index).toString());
    		    		
    		    		wordPos.fromRack += letter;
    				}
    			} else if(board[r][col] != letter){
    				// board square is not empty, and is different than our letter. This is not valid.
    				return INVALID_WORD;
    			}
    			
    		} else if(board[r][col] == LetterBag.EMPTY_LETTER) {
    			break;
    		} else {
    			letter = board[r][col];
    		}
    		
    		fullWord += letter;
    		letterScore = LetterBag.getLetterScore(letter);
    		switch (BONUS_FIELDS[r][col]) {
				case BONUS_LETTER_TIMES_TWO:
					letterScore *= 2;
					break;
				case BONUS_LETTER_TIMES_THREE:
					letterScore *= 3;
					break;
				case BONUS_WORD_TIMES_TWO:
					wordMultiplier = 2;
					break;
				case BONUS_WORD_TIMES_THREE:
					wordMultiplier = 3;
					break;
				default:
					break;
			}
    		
    		wordScore += letterScore;
    	}
    	
    	if(fullWord.length() > 1){
    		// word contains more than one character, so check if the word exists,
    		// and return the number of points
    		if(!Dictionary.getInstance().contains(fullWord)) return INVALID_WORD;

    		// Word is invalid if it is not a new word on the board
    		if(!newWord) return VALID_WORD_NOT_NEW;
    		
//    		if(fullWord.length() > wordLength){
//    			String direction = (wordPos.horizontal) ? "horizontal" : "vertical";
//    			System.out.println("Check Vertical word '"+fullWord+"' created by laying " + direction + " word '"+wordPos.word+"'");
//    		}
    		
    		if(wordPos.horizontal) wordPos.addScoreLine("+" + wordScore * wordMultiplier + " " + fullWord);
    		
    		return wordScore * wordMultiplier;
    	} 

		// the word only contains one character, so we did not create a new word.
		// we return 0, which means this is a valid move, but does not score extra points
		return 0;
    }
    
    public boolean putWord(WordPosition wordPos){
        boardIsEmpty = false;
        
        boolean newWord = false;
        
        char letter, newLetter;
        int r, c;
        for(int i=0; i<wordPos.word.length(); i++){
        	newLetter = wordPos.word.charAt(i);
        	
        	r = (wordPos.horizontal) ? wordPos.row : wordPos.row+i;
        	c = (wordPos.horizontal) ? wordPos.col+i : wordPos.col;
        	
        	letter = board[r][c];
        	if(letter != LetterBag.EMPTY_LETTER && letter != newLetter){
        		System.out.println("Unable to play word: " + wordPos.word);
        		System.out.println("Row: " + wordPos.row + ", col: " + wordPos.col + ", horizontal: "+ wordPos.horizontal);
        		System.exit(1);
        	} 
        	
        	// Remove the bonus field so it can't be used again.
        	BONUS_FIELDS[r][c] = 0;
        	
            board[r][c] = newLetter;
            
            if(newLetter != letter)
            	newWord = true;
        }
        
        return newWord;
    }
    
    /**
     * Check if the given board state is valid. This is very inefficient, 
     * only use for testing purposes only!
     * 
     * @param board
     * @return true if the board state is valid
     */
    public boolean validBoardState(){
        int row, col, r, c;
        String word;
        Dictionary dict = Dictionary.getInstance();

        boolean reversed;
        for(int i=0; i<2; i++){
        	reversed = (i==1);
        	
	        for(row=0; row < BOARD_SIZE; row++){
	            col = 0;
	            while(col < BOARD_SIZE){
	                word = "";
	                
	                r = (reversed) ? col : row;
	                c = (reversed) ? row : col;
	                
	                // skip all empty squares
	                while(c < BOARD_SIZE && r < BOARD_SIZE && board[r][c] == LetterBag.EMPTY_LETTER){
	                    if(reversed) r++;
	                    else c++;
	                }
	                
	                // read word
	                while(c < BOARD_SIZE && r < BOARD_SIZE && board[r][c] != LetterBag.EMPTY_LETTER){
	                    word += board[r][c];
	                    if(reversed) r++;
	                    else c++;
	                }
	                
	                if(word.length() > 1 && !dict.contains(word)) return false;
	                
	                col = (reversed) ? r : c;
	            }
	        }
        }

        return true;
    }
    
    public char get(int row, int col){
        return board[row][col];
    }
    
    
    public void set(int row, int col, char c){
    	set(row, col, c, false);
    }
    
    public void set(int row, int col, char c, boolean reversed){
    	if(reversed)
    		board[col][row] = c;
    	else
    		board[row][col] = c;
    }
    
    public void printBoard(){
        System.out.println("     0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 ");
        System.out.println("   +++++++++++++++++++++++++++++++++");
        for(int row=0; row<BOARD_SIZE; row++){
        	String r = (row < 10) ? " " + row : row + "";
        	r+= " + ";
            for(int col=0; col<BOARD_SIZE; col++){
                r += board[row][col] + " ";
            }
            r += "+";
            System.out.println(r);
        }
        System.out.println("   +++++++++++++++++++++++++++++++++");
    }
    
    
    
}
