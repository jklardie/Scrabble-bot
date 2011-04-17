package nl.jeffreyklardie.scrabbleBot.util;

public class WordPosition {

	public int row, col, score;
	public String word;
	public boolean horizontal;
	public String fromRack;
	public String bonus;
	
	public WordPosition(int row, int col, boolean horizontal, int score, String word){
		this.row = row;
		this.col = col;
		this.horizontal = horizontal;
		this.score = score;
		this.word = word;
		this.fromRack = word; 
		bonus = "";
	}
	
	public WordPosition(){
		this(0, 0, true, 0, null);
	}
	
	public void addBonus(String bonusText){
		bonus += "\n " + bonusText;
	}
	
}
