package nl.jeffreyklardie.scrabbleBot;
import nl.jeffreyklardie.scrabbleBot.game.engine.WordFinder;
import nl.jeffreyklardie.scrabbleBot.game.objects.Board;
import nl.jeffreyklardie.scrabbleBot.game.objects.Rack;
import nl.jeffreyklardie.scrabbleBot.util.Dictionary;
import nl.jeffreyklardie.scrabbleBot.util.WordPosition;


public class ScrabbleBot {

    public static final String SMALL_DICTIONARY_PATH    = "/home/jeffrey/workspace/scrabblebot/src/WRDSMALL.DIC";
    public static final String FULL_DICTIONARY_PATH     = "/home/jeffrey/workspace/scrabblebot/src/WRDMED.DIC";
    
    public static final String DICTIONARY_PATH = FULL_DICTIONARY_PATH;
    public static final int MAX_SEQUENTIAL_EXCHANGES = 2;
    
    public static final int GAME_STATE_RUNNING 					= 0;
    public static final int GAME_STATE_OUT_OF_LETTERS 			= 1;
    public static final int GAME_STATE_EXCHANGED_LIMIT_REACHED 	= 2;
    
    private Rack rack;
    private Dictionary dict;
    private Board board;
    
    private String tag;
    
    private int turn = 0;
    private int numExchanges = 0;
    private int numSequentialExchanges = 0;
    private long totalTurnTime = 0;
    private long turnStart;
    
	public ScrabbleBot(String tag){
        Dictionary.setDictionaryFile(DICTIONARY_PATH);
        dict = Dictionary.getInstance();
        dict.printNumWords();
        
        board = Board.getInstance();
        this.tag = tag;
    }
	
	public void startGame(){
		rack = new Rack();
	}
    
    public int takeTurn(){
    	turn++;
    	turnStart = System.currentTimeMillis();

    	WordPosition bestWordPos = (board.isEmpty())
    		? WordFinder.getBestWordPosForEmptyBoard(rack)
    		: WordFinder.getBestWordPos(board, rack);
    		
    	rack.printRack();
    		
    	if(bestWordPos == null){
    		return exchangeLetters();
    	} 
    	
    	playWord(bestWordPos);

    	printTurnTime(turnStart);
    	ScrabbleGame.printLine("");
    	
    	board.printBoard();
    	ScrabbleGame.printLine("");
    	rack.printScore();
    	
//    	if(!board.validBoardState()){
//    	    ScrabbleGame.printLine("Error. Board state is invalid at the end of the turn.");
//    	    System.exit(1);
//    	}
    	
    	// turn ended and we randomly picked new letters for our rack. If the
    	// rack is empty at this point, then the game ends
    	return (rack.getNumLetters() == 0) ? GAME_STATE_OUT_OF_LETTERS : GAME_STATE_RUNNING;
    }
    
    private void playWord(WordPosition wordPos){
    	numSequentialExchanges = 0;
    	
    	String direction = (wordPos.horizontal) ? "horizontal" : "vertical";
    	ScrabbleGame.printLine("Playing " + direction + " word " + wordPos.word + "(r:"+wordPos.row+"-c:"+wordPos.col+") with score: " + wordPos.score);
    	for(String scoreLine : wordPos.scoreCalculation.split("\\n"))
    		ScrabbleGame.printLine(scoreLine);
    	
    	ScrabbleGame.printLine("------------");
    	ScrabbleGame.printLine(" " + wordPos.score + " total");
    	ScrabbleGame.printLine("");
        
        rack.addScore(wordPos.score);
        board.putWord(wordPos);
        rack.removeLetters(wordPos.fromRack);
    }
    
    private int exchangeLetters(){
        if(++numSequentialExchanges > MAX_SEQUENTIAL_EXCHANGES) return GAME_STATE_EXCHANGED_LIMIT_REACHED;
        
        numExchanges++;
        
    	ScrabbleGame.printLine("No words possible. Trying to exchange " + Rack.NUM_LETTERS_TO_EXCHANGE + " letters");
        
        // No possibilities. Randomly change n letters
        int numExchangedLetters = (numSequentialExchanges == 2)
        	? rack.exchangeLetters(Rack.NUM_LETTERS_ON_RACK) 
        	: rack.exchangeLetters(Rack.NUM_LETTERS_TO_EXCHANGE);
        if(numExchangedLetters > 0){
            ScrabbleGame.printLine("Exchanged " + numExchangedLetters + " letters");
        } else {
            ScrabbleGame.printLine("Not enough letters remaining to exchange. Pass. :(");
        }
        
        return GAME_STATE_RUNNING;
    }
    
    private void printTurnTime(long turnStart){
    	long turnTime = System.currentTimeMillis()-turnStart;
    	totalTurnTime += turnTime;
        ScrabbleGame.printLine(String.format("Turn %d took %.3f seconds", turn, (turnTime / 1000f)));
        ScrabbleGame.printLine(String.format("Average turn time: %.3f seconds", (totalTurnTime / turn / 1000f)));
    }

	public void printFinalScore(){
		rack.printScore();
		ScrabbleGame.printLine(
			String.format("Played %d words in %.3f seconds. Avg: %.3f seconds", turn - numExchanges, 
			(totalTurnTime / 1000f), (totalTurnTime / turn / 1000f)));
		ScrabbleGame.printLine("Swapped " + numExchanges + " times");
	}
	
	public String getTag(){
		return tag;
	}
    
}
