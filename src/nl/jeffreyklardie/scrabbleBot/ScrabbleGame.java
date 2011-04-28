package nl.jeffreyklardie.scrabbleBot;


public class ScrabbleGame {

    public static final int GAME_STATE_RUNNING 					= 0;
    public static final int GAME_STATE_OUT_OF_LETTERS 			= 1;
    public static final int GAME_STATE_EXCHANGED_LIMIT_REACHED 	= 2;
	
	private static int turn = 0;
	private static ScrabbleBot[] players;
	
	public ScrabbleGame(int numPlayers){
		players = new ScrabbleBot[numPlayers];
		
		for(int i=0; i<numPlayers; i++){
			players[i] = new ScrabbleBot("Player " + (i+1));
			players[i].startGame();
		}
	}
	
	private void startGame(){
		int gameState = GAME_STATE_RUNNING;
		while(gameState == GAME_STATE_RUNNING){
			turn++;
			System.out.println();
			gameState = players[getCurrentPlayer()].takeTurn();
			System.out.println();
		}
		
		switch(gameState){
			case GAME_STATE_EXCHANGED_LIMIT_REACHED:
				// last turn does not count
				System.out.println("No more possibilities. Exchange limit reached. \n\nGame ended.");
				break;
			case GAME_STATE_OUT_OF_LETTERS:
				System.out.println("Ran out of letters. \n\nGame ended.");
				break;
		}
		
		for(ScrabbleBot player : players){
			player.printFinalScore();
			turn++;
		}
	}
	
	public static void printLine(String line){
		if(getCurrentPlayer() % 2 == 0){
			System.out.println(players[getCurrentPlayer()].getTag() + "    |    " + line);
		} else {
			System.out.println(String.format("%80s    |    %s", players[getCurrentPlayer()].getTag(), line));
		}
	}
	
	private static int getCurrentPlayer(){
		return turn % players.length;
	}
	
	public static void main(String[] args){
        ScrabbleGame game = new ScrabbleGame(2);
        game.startGame();
    }
}
