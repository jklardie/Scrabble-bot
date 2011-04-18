package nl.jeffreyklardie.scrabbleBot.game.engine;

import nl.jeffreyklardie.scrabbleBot.game.objects.Board;
import nl.jeffreyklardie.scrabbleBot.game.objects.LetterBag;
import nl.jeffreyklardie.scrabbleBot.game.objects.Rack;
import nl.jeffreyklardie.scrabbleBot.util.Dictionary;
import nl.jeffreyklardie.scrabbleBot.util.WordPosition;

import java.util.ArrayList;

public abstract class WordFinder {

    
	public static ArrayList<WordPosition> getPossibleWords(Board board, Rack rack){
		Dictionary dict = Dictionary.getInstance();
		ArrayList<WordPosition> words = new ArrayList<WordPosition>();
		ArrayList<String> possibleWords = dict.getWordsWithLetters(board.getLetters(), rack.getLetters());
		
		int row, col;
		char letter;

//	    System.out.println(possibleWords.toString());
		
		for(row=0; row < Board.BOARD_SIZE; row++){
			for(col=0; col < Board.BOARD_SIZE; col++){
				letter = board.get(row, col);
				
				// skip empty squares on the board
				if(letter == LetterBag.EMPTY_LETTER) continue;
				
				words.addAll(getWordPositions(board, row, col, possibleWords, rack));
			}
		}
		
		return words;
	}
	
	/**
	 * Get all words that contain a specific letter
	 * 
	 * @param possibleWords
	 * @param letter
	 * @return
	 */
	private static ArrayList<String> getWordsWithLetter(ArrayList<String> possibleWords, char letter){
		ArrayList<String> wordsWithLetter = new ArrayList<String>();
		for(String word : possibleWords){
			if(word.indexOf(letter) != -1) wordsWithLetter.add(word);
		}
		
		return wordsWithLetter;
	}
	
	/**
	 * Return all the possible positions for a board position given a set of possible
	 * words.
	 * 
	 * @param board
	 * @param row
	 * @param col
	 * @param wordWithLetter
	 * @return
	 */
	private static ArrayList<WordPosition> getWordPositions(Board board, int row, int col, ArrayList<String> possibleWords, Rack rack){
		ArrayList<WordPosition> wordPositions = new ArrayList<WordPosition>();
		
		int letterIndex;
		char letter = board.get(row, col);
		WordPosition wordPos = new WordPosition();
		String word; 
		
		// for each word check the possible positions using the letter from the board
		for(int i=0; i<possibleWords.size(); i++){
			word = possibleWords.get(i);
			if(word.indexOf(letter) == -1) continue;
			
			letterIndex = word.indexOf(letter);
			while(letterIndex != -1){
				// check horizontal
				if(col-letterIndex >= 0 && (col-letterIndex+word.length()-1) < Board.BOARD_SIZE){
					wordPos = new WordPosition(row, col - letterIndex, true, -1, word);
					wordPos.score = board.getWordScore(wordPos, rack);
					if(wordPos.score > 0) wordPositions.add(wordPos);
				}
				
				// check vertical
				if(row-letterIndex >= 0 && (row-letterIndex+word.length()-1) < Board.BOARD_SIZE){
					wordPos = new WordPosition(row - letterIndex, col, false, -1, word);
					wordPos.score = board.getWordScore(wordPos, rack);
					if(wordPos.score > 0) wordPositions.add(wordPos);
				}
				
				letterIndex = word.indexOf(letter, letterIndex+1);
			}
			
		}
		
		return wordPositions;
	}
	
//    public static ArrayList<WordPosition> getPossibleWords(Board board, Rack rack){
//        // For each complete word on the board, try to add some letters to form a new word
//        int row=0, col=0, numEmptyBeforeWord=0;
//        String word;
//        Dictionary dict = Dictionary.getInstance();
//        ArrayList<WordPosition> words = new ArrayList<WordPosition>();
//        ArrayList<String> possibleWords = new ArrayList<String>();
//        
//        for(int i=0; i<2; i++){
//        	boolean reversed = i==1;
//        	row = 0;
//			col = 0;
//	        while(row < Board.BOARD_SIZE){
//	            col = 0;
//	            while(col < Board.BOARD_SIZE){
//	                word = "";
//	                
//	                // skip all empty squares
//	                numEmptyBeforeWord=0;
//	                while(col < Board.BOARD_SIZE && board.get(row, col, reversed) == LetterBag.EMPTY_LETTER){
//	                    col++;
//	                    numEmptyBeforeWord++;
//	                }
//	                
//	                // read word
//	                while(col < Board.BOARD_SIZE && board.get(row, col, reversed) != LetterBag.EMPTY_LETTER){
//	                    word += board.get(row, col, reversed);
//	                    col++;
//	                }
//	                
//	                if(word.length() > 0){
//	                    // Found a letter/word. 
//	                    // Check if we can put some letters in front of the word
//	                    possibleWords = dict.getPossibleWordsWithPostfix(word, rack.toString(), numEmptyBeforeWord + word.length());
//	                    
//	                    // Check if we can put some letters after the word. First find out how many empty squares we 
//	                    // have after the word
//	                    int numEmptyAfterWord = 0;
//	                    for(int j=col; j<Board.BOARD_SIZE; j++){
//	                        if(board.get(row, j, reversed) != LetterBag.EMPTY_LETTER) {
//	                            numEmptyAfterWord--;
//	                            break;
//	                        }
//	                        numEmptyAfterWord++;
//	                    }
//	                    
//	                    // check if we can put some letters after the word
//	                    possibleWords.addAll(dict.getPossibleWordsWithPrefix(word, rack.toString(), numEmptyAfterWord + word.length()) );
//	                    WordPosition wordPos = new WordPosition();
//	                    wordPos.horizontal = !reversed;
//	                    for(String w : possibleWords){
//	                        wordPos.word = w;
//	                        wordPos.row = (reversed) ? col - w.length() : row;
//	                        wordPos.col = (reversed) ? row : col - w.length();
//	                        if(board.validWordPosition(wordPos)){
//	                            wordPos.score = board.getWordScore(wordPos.word, wordPos.row, wordPos.col, wordPos.horizontal);
//	                            words.add(wordPos);
//	                        }
//	                    }
//	                }
//	                
//	                // NumEmpty tells how many squares in front of a word are empty. If we find multiple words per line,
//	                // we need to make sure that we don't put them immediately next to each other. 
//	                numEmptyBeforeWord = -1;
//	            }
//	            
//	            row++;
//	        }
//        }
//        
//	    return words;
//    }
    
    public static ArrayList<WordPosition> getPossibleWordsForEmptyBoard(Rack rack){
    	ArrayList<WordPosition> words = new ArrayList<WordPosition>();
    	Dictionary dict = Dictionary.getInstance();
    	
		ArrayList<String> dictWords = dict.getPossibleWords(rack.toString());

		int boardMiddleIndex = (int)(Board.BOARD_SIZE/2);
		int wordLength, col, score;
		String word;
		
		WordPosition wordPos;
		for(int i=0; i<dictWords.size(); i++){
			word = dictWords.get(i);
	    	// Board is empty, so word must touch the middle square
	        wordLength = word.length();
	        
	        if(wordLength < 5){
	            // it is impossible to hit the times 2 letter bonus, so just put the 
	            // word in the middle of the board.
	            col = (boardMiddleIndex - (int)(wordLength/2));
	            wordPos = new WordPosition(boardMiddleIndex, col, true, -1, word);
	            score = Board.getInstance().getWordScore(wordPos, rack);
	            if(score > 0) {
	            	wordPos.score = score;
	            	words.add(wordPos);
	            }
	        } else {
	            // we know that the word length is max 7, so the current word has a length
	            // of 5, 6 or 7. We can now also hit a 2 letter bonus, so we need to check what
	            // position will bring the highest score.
	            WordPosition bestPos = new WordPosition();
	            
	            int startCol = boardMiddleIndex + 1 - wordLength;
	            while(startCol <= boardMiddleIndex){
	            	wordPos = new WordPosition(boardMiddleIndex, startCol, true, -1, word);
	            	score = Board.getInstance().getWordScore(wordPos, rack);
		            if(score > bestPos.score){
		            	wordPos.score = score;
		            	bestPos = wordPos;
		            }
	                startCol++;
	            }
	
	            if(bestPos.score > 0)
	            	words.add(bestPos);
	        }
		}
		
		return words;
    }
}
