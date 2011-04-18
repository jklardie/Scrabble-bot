package nl.jeffreyklardie.scrabbleBot.util;

public class WordPosition {

	public int row, col, score;
	public String word;
	public boolean horizontal;
	public String fromRack;
	public String scoreCalculation;
	
	public WordPosition(int row, int col, boolean horizontal, int score, String word){
		this.row = row;
		this.col = col;
		this.horizontal = horizontal;
		this.score = score;
		this.word = word;
		this.fromRack = word; 
		scoreCalculation = "";
	}
	
	public WordPosition(){
		this(0, 0, true, 0, null);
	}
	
	public void addScoreLine(String bonusText){
		scoreCalculation += "\n " + bonusText;
	}
	
}
