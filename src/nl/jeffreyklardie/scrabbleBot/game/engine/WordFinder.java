package nl.jeffreyklardie.scrabbleBot.game.engine;

import nl.jeffreyklardie.scrabbleBot.game.objects.Board;
import nl.jeffreyklardie.scrabbleBot.game.objects.LetterBag;
import nl.jeffreyklardie.scrabbleBot.game.objects.Rack;
import nl.jeffreyklardie.scrabbleBot.util.Dictionary;
import nl.jeffreyklardie.scrabbleBot.util.WordPosition;

import java.util.ArrayList;

import javax.swing.plaf.basic.BasicOptionPaneUI;



public abstract class WordFinder {

    
	public static ArrayList<WordPosition> getPossibleWords(Board board, Rack rack){
		ArrayList<WordPosition> words = new ArrayList<WordPosition>();
		ArrayList<String> tmpWords = new ArrayList<String>();
		
		Dictionary dict = Dictionary.getInstance();
		
		int row, col, r, c, letterIndex, startRow, startCol;
		String tmpWord;
		char letter;
		WordPosition wordPos;
		boolean reversed;
		for(int i=0; i<2; i++){
			reversed = (i==1);
			
			for(row=0; row < Board.BOARD_SIZE; row++){
				tmpWord = "";
				for(col=0; col < Board.BOARD_SIZE; col++){
					r = (reversed) ? col : row;
					c = (reversed) ? row : col;
					
					letter = board.get(r, c);
					if(letter != LetterBag.EMPTY_LETTER){
						tmpWord += letter;
						
						// find all words containing the letter on the board
						tmpWords = dict.getPossibleWords(rack.toString() + letter, letter);
						
						for(String word : tmpWords){
							for(letterIndex = word.indexOf(letter); letterIndex != -1; letterIndex = word.indexOf(letter, letterIndex+1)){
								startRow = (reversed) ? r-letterIndex : r;
								startCol = (reversed) ? c : c-letterIndex;
								
								if(startRow < 0 || startCol < 0 || 
										(!reversed && startCol+word.length() >= Board.BOARD_SIZE) || 
										(reversed && startRow+word.length() >= Board.BOARD_SIZE)) continue;
								
								wordPos = new WordPosition(startRow, startCol, !reversed, -1, word);
								if(board.validWordPosition(wordPos)){
									// word position results in a valid board, so add it to the result
									wordPos.score = board.getWordScore(wordPos.word, wordPos.row, wordPos.col, wordPos.horizontal);
									words.add(wordPos);
								}
							}
						}
					} else {
						// empty tile. Check if the previous tiles contained a word
						if(tmpWord.length() > 1){
							// previous tiles contained a word, so check if we can create a new word with that
							tmpWords = dict.getPossibleWordsWithPrefix(tmpWord, rack.toString());
							
							for(String word : tmpWords){
								startRow = (reversed) ? r-word.length() : r;
								startCol = (reversed) ? c : c-word.length();
								
								if(startRow < 0 || startCol < 0 || 
										(!reversed && startCol+word.length() >= Board.BOARD_SIZE) ||
										(reversed && startRow+word.length() >= Board.BOARD_SIZE)) continue;
								
								wordPos = new WordPosition(startRow, startCol, !reversed, -1, word);
								if(board.validWordPosition(wordPos)){
									// word position results in a valid board, so add it to the result
									wordPos.score = board.getWordScore(wordPos.word, wordPos.row, wordPos.col, wordPos.horizontal);
									words.add(wordPos);
								}
							}
							
							tmpWord = "";
						}
					}
				}
			}
		}
		
		return words;
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
		
		for(String word : dictWords){
	    	// Board is empty, so word must touch the middle square
	        wordLength = word.length();
	        
	        if(wordLength < 5){
	            // it is impossible to hit the times 2 letter bonus, so just put the 
	            // word in the middle of the board.
	            col = (boardMiddleIndex - (int)(wordLength/2));
	            score = Board.getInstance().getWordScore(word, boardMiddleIndex, boardMiddleIndex, true);
	            words.add(new WordPosition(boardMiddleIndex, col, true, score, word));
	        } else {
	            // we know that the word length is max 7, so the current word has a length
	            // of 5, 6 or 7. We can now also hit a 2 letter bonus, so we need to check what
	            // position will bring the highest score.
	            WordPosition bestPos = new WordPosition();
	            bestPos.word = word;
	            bestPos.fromRack = word;
	            bestPos.horizontal = true;
	            
	            int tmpScore;
	            int startCol = boardMiddleIndex + 1 - wordLength;
	            while(startCol <= boardMiddleIndex){
	                tmpScore = Board.getInstance().getWordScore(word, boardMiddleIndex, startCol, true);
	                if(tmpScore > bestPos.score) {
	                    bestPos.score = tmpScore;
	                    bestPos.col = startCol;
	                    bestPos.row = boardMiddleIndex;
	                }
	                startCol++;
	            }
	
	            words.add(bestPos);
	        }
		}
		
		return words;
    }
}
