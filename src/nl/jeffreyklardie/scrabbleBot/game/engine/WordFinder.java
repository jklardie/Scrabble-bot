package nl.jeffreyklardie.scrabbleBot.game.engine;

import java.util.ArrayList;

import nl.jeffreyklardie.scrabbleBot.game.objects.Board;
import nl.jeffreyklardie.scrabbleBot.game.objects.Rack;
import nl.jeffreyklardie.scrabbleBot.util.Dictionary;
import nl.jeffreyklardie.scrabbleBot.util.WordPosition;

public abstract class WordFinder {

	/**
	 * The best word position as calculated in the previous turn. 
	 * This word position is not yet played. Using this, we can 
	 */
	private static WordPosition bestWordPos = new WordPosition();
    
	public static WordPosition getBestWordPos(Board board, Rack rack){
		WordPosition bestWordPos = new WordPosition();
		
		int row, col;
		WordPosition wordPos;
		
		for(int linear : board.getLinearBoard()){
			row = board.getRowFromLinear(linear);
			col = board.getColFromLinear(linear);
			
			wordPos = getBestWordPosition(board, row, col, rack);
			if(wordPos != null && wordPos.score > bestWordPos.score) bestWordPos = wordPos;
		}
		
		if(bestWordPos.score > 0)
			return bestWordPos;
		
		return null;
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
	private static WordPosition getBestWordPosition(Board board, int row, int col, Rack rack){
		// we use the best word position to return immediately, and play on the board. 
		// the second best word position is used to quickly determine what we will
		// play in the next turn (so we only have to check the changed rows/columns).
		WordPosition bestWordPos = new WordPosition();
		WordPosition secondBestWordPos = new WordPosition();
		
		int letterIndex;
		char letter = board.get(row, col);
		WordPosition wordPos = new WordPosition();
		String word;
		String rackLetters = rack.getLetters();

		// check horizontal words
		ArrayList<String> possibleHorizontalWords = Dictionary.getInstance().getWordsWithLetters(
				board.getLettersOnCol(col), rackLetters);
		
		for(int i=0; i<possibleHorizontalWords.size(); i++){
			word = possibleHorizontalWords.get(i);
			if(word.indexOf(letter) == -1) continue;
			
			letterIndex = word.indexOf(letter);
			while(letterIndex != -1){
				if(col-letterIndex >= 0 && (col-letterIndex+word.length()-1) < Board.BOARD_SIZE){
					wordPos = new WordPosition(row, col - letterIndex, true, -1, word);
					wordPos.score = board.getWordScore(wordPos, rack);
					if(wordPos.score > bestWordPos.score) {
						secondBestWordPos = bestWordPos;
						bestWordPos = wordPos;
					} else if(wordPos.score > secondBestWordPos.score){
						secondBestWordPos = wordPos;
					}
				}
				letterIndex = word.indexOf(letter, letterIndex+1);
			}
		}

		// check vertical words
		ArrayList<String> possibleVerticalWords = Dictionary.getInstance().getWordsWithLetters(
				board.getLettersOnRow(row), rackLetters);
		
		for(int i=0; i<possibleVerticalWords.size(); i++){
			word = possibleVerticalWords.get(i);
			if(word.indexOf(letter) == -1) continue;
			
			letterIndex = word.indexOf(letter);
			while(letterIndex != -1){
				if(row-letterIndex >= 0 && (row-letterIndex+word.length()-1) < Board.BOARD_SIZE){
					wordPos = new WordPosition(row - letterIndex, col, false, -1, word);
					wordPos.score = board.getWordScore(wordPos, rack);
					if(wordPos.score > bestWordPos.score) {
						secondBestWordPos = bestWordPos;
						bestWordPos = wordPos;
					} else if(wordPos.score > secondBestWordPos.score){
						secondBestWordPos = wordPos;
					}
				}
				letterIndex = word.indexOf(letter, letterIndex+1);
			}
		}

		if(secondBestWordPos.score > 0){
			bestWordPos = secondBestWordPos;
		}
		
		if(bestWordPos.score > 0)
			return bestWordPos;
		
		return null;
	}
	
    public static WordPosition getBestWordPosForEmptyBoard(Rack rack){
    	WordPosition bestWordPos = new WordPosition();
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
	            if(score > bestWordPos.score) {
	            	wordPos.score = score;
	            	bestWordPos = wordPos;
	            }
	        } else {
	            // we know that the word length is max 7, so the current word has a length
	            // of 5, 6 or 7. We can now also hit a 2 letter bonus, so we need to check what
	            // position will bring the highest score.
	            int startCol = boardMiddleIndex + 1 - wordLength;
	            while(startCol <= boardMiddleIndex){
	            	wordPos = new WordPosition(boardMiddleIndex, startCol, true, -1, word);
	            	score = Board.getInstance().getWordScore(wordPos, rack);
		            if(score > bestWordPos.score){
		            	wordPos.score = score;
		            	bestWordPos = wordPos;
		            }
	                startCol++;
	            }
	
	        }
		}
		
		if(bestWordPos.score > 0)
			return bestWordPos;
		
		return null;
    }
}
