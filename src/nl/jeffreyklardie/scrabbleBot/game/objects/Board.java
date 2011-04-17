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
    
    public char[] getLettersOnBoard(){
    	String letters = "";
    	for(int row=0; row < BOARD_SIZE; row++){
            for(int col=0; col < BOARD_SIZE; col++){
                if(board[row][col] != LetterBag.EMPTY_LETTER && letters.indexOf(board[row][col]) != -1){
                	letters += board[row][col];
                }
            }
        }
    	
    	return letters.toCharArray();
    }
    
    public boolean isEmpty(){
        return boardIsEmpty;
    }
       
    /**
     * FIXME: this only counts the squares that the word takes up, but does not
     * look at surrounding squares!
     */
    public int getWordScore(String word, int row, int col, boolean horizontal){
    	int wordLength = word.length();
    	int wordMultiplier = 1;
    	int score = (wordLength == Rack.NUM_LETTERS_ON_RACK) ? BONUS_USE_ALL_LETTERS : 0;
    	
    	int letterScore;
    	int squareBonus;
    	for(int i=0; i<wordLength; i++){
    		letterScore = LetterBag.getLetterScore(word.charAt(i));
    		squareBonus = (horizontal) ? BONUS_FIELDS[row][col+i] :  BONUS_FIELDS[row+i][col];
    		
    		switch (squareBonus) {
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
    		
    		score += letterScore;
    	}
    	
    	return score * wordMultiplier;
    	
    }
    
    public boolean putWord(WordPosition wordPos, boolean test){
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
        		if(test) return false;
        		System.out.println("Unable to play word: " + wordPos.word);
        		System.out.println("Row: " + wordPos.row + ", col: " + wordPos.col + ", horizontal: "+ wordPos.horizontal);
        		System.exit(1);
        	} else if(letter == newLetter){
        		// we are using a letter from the board, so remove it from the 
        		// list of letters we use from the rack
        		wordPos.useLetterFromBoard(i);
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
     * Return whether the given word position results in a correct board state.
     * 
     * @param wordPos
     * @return true if placing the word on the given position results in a valid board state
     */
    public boolean validWordPosition(WordPosition wordPos){
        char[][] currentBoard = new char[BOARD_SIZE][BOARD_SIZE];
        for(int row=0; row < BOARD_SIZE; row++){
            for(int col=0; col < BOARD_SIZE; col++){
                currentBoard[row][col] = board[row][col];
            }
        }
        
        if(!putWord(wordPos, true)){
        	board = currentBoard;
        	return false;
        }
        
        boolean validBoardState = validBoardState();
        board = currentBoard;
        
        return validBoardState;
    }
    
    public boolean validWordPosition(WordPosition wordPos){
    	int dCol = (wordPos.horizontal) ? 1 : 0;
    	int dRow = (wordPos.horizontal) ? 0 : 1;
    	int col, row;
    	int wordLength = wordPos.word.length();
    	char letter;
    	
    	// check if we will not create any incorrect word in the same direction
		if(wordPos.horizontal && !correctHorizontalWord(wordPos, wordPos.row)) { 
			return false; }
		if(!wordPos.horizontal && !correctVerticalWord(wordPos, wordPos.col)){ 
			return false; }
    	
		boolean newWord = false;
    	for(int i=0; i<wordLength; i++){
    		row = wordPos.row+(dRow*i);
    		col = wordPos.col+(dCol*i);
    		letter = board[row][col];
    		
    		// check if we don't override letters that are already on the board
    		if(letter != LetterBag.EMPTY_LETTER && letter != wordPos.word.charAt(i)) {
    			return false;
    		} else if(letter == LetterBag.EMPTY_LETTER){
    			newWord = true;
    		}
    		
    		// check if we will not create any incorrect word in the other direction
    		if(wordPos.horizontal && !correctVerticalWord(wordPos, col)){ 
    			return false; }
    		if(!wordPos.horizontal && !correctHorizontalWord(wordPos, row)){ 
    			return false; }
    	}
    	
    	return newWord;
    }
    
    public boolean correctHorizontalWord(WordPosition wordPos, int row){
        int col;
        for(col=wordPos.col; col > 0 && board[row][col] != LetterBag.EMPTY_LETTER; col--);
        
        
        // make sure col points at the first letter of the word we need to check
        if(board[row][col] == LetterBag.EMPTY_LETTER) col++;
        
        String word = "";
        int wordIndex = (wordPos.horizontal)
            ? col-wordPos.col
            : row-wordPos.row;
        
        for(int i=col; i<BOARD_SIZE; i++){
            if(wordIndex > 0 && wordIndex < wordPos.word.length()-1){
                word += wordPos.word.charAt(wordIndex);
            }
        }
    }
    
    public boolean correctHorizontalWord(WordPosition wordPos, int row){
    	int col;
		for(col=wordPos.col-1; col>0 && board[row][col] != LetterBag.EMPTY_LETTER; col--);
		
		if(col < wordPos.col-1 && board[row][col] == LetterBag.EMPTY_LETTER) col++;
		if(col < 0) col++;
    			
		String word = "";
		char letter;
		int wordIndex;
		for(; col<BOARD_SIZE; col++){
			wordIndex = (wordPos.horizontal) ? row-wordPos.row : col-wordPos.col;
			
			letter = (col >= wordPos.col && col < wordPos.col + wordPos.word.length() 
					&& (wordPos.horizontal || col-wordPos.col == 0))
				? wordPos.word.charAt(wordIndex)
				: board[row][col];
				
			if(letter == LetterBag.EMPTY_LETTER && wordIndex < 0){
				continue;
			} else if(letter == LetterBag.EMPTY_LETTER){
				break;
			}
			
			word += letter;
		}
//		
//		for(; col<BOARD_SIZE; col++){
//			wordIndex = (wordPos.horizontal) ? col-wordPos.col : row-wordPos.row;
//			
//			letter = (col >= wordPos.col && col < wordPos.col + wordPos.word.length() 
//					&& (wordPos.horizontal || col-wordPos.col == 0))
//				? wordPos.word.charAt(wordIndex)
//				: board[row][col];
//				
//			if(letter == LetterBag.EMPTY_LETTER && wordIndex < 0){
//				continue;
//			} else if(letter == LetterBag.EMPTY_LETTER){
//				break;
//			}
//			
//			word += letter;
//		}
		
		if(word.length() == 1) return true; 
		
		return (Dictionary.getInstance().contains(word));
    }
    
    public boolean correctVerticalWord(WordPosition wordPos, int col){
    	int row;
		for(row=wordPos.row-1; col>0 && board[row][col] != LetterBag.EMPTY_LETTER; row--);
		
		if(row < wordPos.row-1 && board[row][col] == LetterBag.EMPTY_LETTER) row++;
		if(row < 0) col++;
		
		String word = "";
		char letter;
		int wordIndex;
		for(; row<BOARD_SIZE; row++){
			wordIndex = (wordPos.horizontal) ? col-wordPos.col : row-wordPos.row;
			
			letter = (row >= wordPos.row && row < wordPos.row + wordPos.word.length() 
					&& (!wordPos.horizontal || row-wordPos.row == 0))
				? wordPos.word.charAt(wordIndex)
				: board[row][col];
				
			if(letter == LetterBag.EMPTY_LETTER && wordIndex < 0){
				continue;
			} else if(letter == LetterBag.EMPTY_LETTER){
				break;
			}
			
			word += letter;
		}
		
		if(word.length() == 1) return true; 
		
		return (Dictionary.getInstance().contains(word));
    }
    
    /**
     * Check if the given board state is valid.
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
        System.out.println("Board state:");
        System.out.println("+++++++++++++++++++++++++++++++++");
        for(int row=0; row<BOARD_SIZE; row++){
            String r = "+ ";
            for(int col=0; col<BOARD_SIZE; col++){
                r += board[row][col] + " ";
            }
            r += "+";
            System.out.println(r);
        }
        System.out.println("+++++++++++++++++++++++++++++++++");
    }
    
    
    
}
