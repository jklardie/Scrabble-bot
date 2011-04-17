package nl.jeffreyklardie.scrabbleBot;
import java.util.ArrayList;

import nl.jeffreyklardie.scrabbleBot.game.engine.WordFinder;
import nl.jeffreyklardie.scrabbleBot.game.objects.Board;
import nl.jeffreyklardie.scrabbleBot.game.objects.Rack;
import nl.jeffreyklardie.scrabbleBot.util.Dictionary;
import nl.jeffreyklardie.scrabbleBot.util.WordPosition;


public class ScrabbleBot {

    public static final String SMALL_DICTIONARY_PATH    = "/home/jeffrey/workspace/java/scrabblebot/src/WRDSMALL.DIC";
    public static final String FULL_DICTIONARY_PATH     = "/home/jeffrey/workspace/java/scrabblebot/src/WRDMED.DIC";
    
    public static final String DICTIONARY_PATH = FULL_DICTIONARY_PATH;
    public static final int MAX_SEQUENTIAL_EXCHANGES = 2;
    
    public static final int GAME_STATE_RUNNING 					= 0;
    public static final int GAME_STATE_OUT_OF_LETTERS 			= 1;
    public static final int GAME_STATE_EXCHANGED_LIMIT_REACHED 	= 2;
    
    private Rack rack;
    private Dictionary dict;
    private Board board;
    
    private int turn = 0;
    private int numSequentialExchanges = 0;
    private long turnStart;
    
	public ScrabbleBot(){
        Dictionary.setDictionaryFile(DICTIONARY_PATH);
        dict = Dictionary.getInstance();
        dict.printNumWords();
        
        board = Board.getInstance();
    }
	
	public void startGame(){
		rack = new Rack();
		
		int gameState = GAME_STATE_RUNNING;
		while(gameState == GAME_STATE_RUNNING){
			gameState = takeTurn();
		}
		
		switch(gameState){
			case GAME_STATE_EXCHANGED_LIMIT_REACHED:
				System.out.println("No more possibilities. Exchange limit reached. \n\nGame ended.");
				break;
			case GAME_STATE_OUT_OF_LETTERS:
				System.out.println("Ran out of letters. \n\nGame ended.");
				break;
		}
		
		rack.printScore();
	}
    
    private int takeTurn(){
        turn++;
    	turnStart = System.currentTimeMillis();

    	ArrayList<WordPosition> possibleWords = (board.isEmpty())
    		? WordFinder.getPossibleWordsForEmptyBoard(rack)
    		: WordFinder.getPossibleWords(board, rack);
    		
    	rack.printRack();
    		
        if(possibleWords.size() == 0){
            return exchangeLetters();
        } else {
            WordPosition bestWordPos = new WordPosition();
            for(WordPosition wordPos : possibleWords){
                if(wordPos.score > bestWordPos.score){
                    bestWordPos = wordPos;
                }
            }
            
            // We found the best word for the board, so lets put it down
            playWord(bestWordPos);
        }
        
    	printTurnTime(turnStart);
    	System.out.println("");
    	
    	board.printBoard();
    	rack.printScore();
    	if(!board.validBoardState()){
    	    System.out.println("Error. Board state is invalid at the end of the turn.");
    	    System.exit(1);
    	}
    	
    	// turn ended and we randomly picked new letters for our rack. If the
    	// rack is empty at this point, then the game ends
    	return (rack.getNumLetters() == 0) ? GAME_STATE_OUT_OF_LETTERS : GAME_STATE_RUNNING;
    }
    
    private void playWord(WordPosition wordPos){
    	numSequentialExchanges = 0;
    	
    	System.out.println("Playing word " + wordPos.word + " with score: " + wordPos.score);
        
        rack.addScore(wordPos.score);
        rack.removeLetters(wordPos.fromRack);
        
        board.putWord(wordPos, false);
    }
    
    private int exchangeLetters(){
        if(++numSequentialExchanges > MAX_SEQUENTIAL_EXCHANGES) return GAME_STATE_EXCHANGED_LIMIT_REACHED;
        
    	System.out.println("No words possible. Trying to exchange " + Rack.NUM_LETTERS_TO_EXCHANGE + " letters");
        
        // No possibilities. Randomly change n letters
        int numExchangedLetters = rack.exchangeLetters(Rack.NUM_LETTERS_TO_EXCHANGE);
        if(numExchangedLetters > 0){
            System.out.println("Exchanged " + numExchangedLetters + " letters");
        } else {
            System.out.println("Not enough letters remaining to exchange. Pass. :(");
        }
        
        return GAME_STATE_RUNNING;
    }
    
    private void printTurnTime(long turnStart){
        System.out.println(String.format("Turn %d took %.3f seconds", turn, (System.currentTimeMillis()-turnStart) / 1000f));
    }
    
    public static void main(String[] args){
        ScrabbleBot bot = new ScrabbleBot();
        bot.startGame();
    }
}
