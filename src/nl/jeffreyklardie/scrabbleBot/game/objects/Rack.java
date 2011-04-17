package nl.jeffreyklardie.scrabbleBot.game.objects;

import java.util.Random;


public class Rack {

    public static final int NUM_LETTERS_ON_RACK = 7;
    
    /**
     * Number of random letters to exchange when no words are possible
     */
    public static final int NUM_LETTERS_TO_EXCHANGE = 4;
    
    
    private char[] rack = new char[NUM_LETTERS_ON_RACK];
    private int numLettersOnRack = 0;
    private int score = 0;
    private Random generator;
    
    public Rack(){
        for(int i=0; i<NUM_LETTERS_ON_RACK; i++){
        	if(LetterBag.hasMoreLetters()){
        		rack[i] = LetterBag.takeRandomLetterFromBag();
        		numLettersOnRack++;
        	}
        }
        
        generator = new Random();
    }
    
    public void printRack(){
        String rackLine = " ";
        for(char letter : rack){
            rackLine += letter + " ";
        }
        
        System.out.println("\nRack: [" + rackLine + "]\n");
    }
    
    /**
     * FIXME: currently random letters are exchanged. So it's possible we exchange the same letter three times. 
     * That should be fixed!
     * 
     * Exchange n letters for new ones, if possible. 
     * @param numLetters
     * @return int number of exchanged letters
     */
    public int exchangeLetters(int numLetters){
        if(numLetters == 0 || !LetterBag.letterExchangePossible()) return 0;
        
        int letterIndex = getLetterIndexToExchange();
        for(int i=numLetters; i>0; i--){
        	letterIndex = (i+3) % 7;
        	rack[letterIndex] = LetterBag.exchangeLetter(rack[letterIndex]);
        }
        
        return numLetters;
    }
    
    /**
     * The letters in the word are removed from the rack. 
     * @param letters
     */
    public void removeLetters(String word){
        int numRemoved = 0;
        int i;
        
        StringBuffer remainingLetters = new StringBuffer(word);
        char[] letters = word.toCharArray();
        
    	removeLetter:
        for(i=0; i<letters.length; i++){
            for(int j=0; j<NUM_LETTERS_ON_RACK; j++){
                if(letters[i] == rack[j]) {
                    rack[j] = LetterBag.EMPTY_LETTER;
                    remainingLetters.deleteCharAt(remainingLetters.indexOf(letters[i] + ""));
                    numRemoved++;
                    continue removeLetter;
                }
            }
        }
    
    	if(letters.length > numRemoved){
    		for(int j=0; j<NUM_LETTERS_ON_RACK; j++){
    			if(rack[j] == LetterBag.JOKER){
    				rack[j] = LetterBag.EMPTY_LETTER;
    				
    				System.out.println("Used joker for letter " + remainingLetters.charAt(0));
    				remainingLetters.deleteCharAt(0);
    				if(++numRemoved == letters.length) break;
    			}
    		}
    	}
    	
    	numLettersOnRack -= numRemoved;
    
    	if(word.length() != numRemoved){
    		System.out.println("Error. Could not play word from rack");
    		System.exit(1);
    	}
    	
        fillRackWithLetters();
    }
    
    private void fillRackWithLetters(){
        for(int i=0; i<NUM_LETTERS_ON_RACK; i++){
            if(!LetterBag.hasMoreLetters()) break;
            
            if(rack[i] == LetterBag.EMPTY_LETTER && LetterBag.hasMoreLetters()){
                rack[i] = LetterBag.takeRandomLetterFromBag();
                numLettersOnRack++;
            }
        }
    }
    
    
    
    /**
     * TODO: create an algorithm for this.
     * Determine which letter on the rack to exchange.
     * @return int letterIndex
     */
    private int getLetterIndexToExchange(){
        return generator.nextInt(NUM_LETTERS_ON_RACK);
    }
    
    public int getNumLetters(){
    	return numLettersOnRack;
    }
    
    public String getLetters(){
    	return new String(rack);
    }
    
    public void addScore(int score){
    	this.score += score;
    }
    
    public void printScore(){
    	System.out.println("Total score: " + score);
    }
    
    @Override
    public String toString() {
        return new String(rack);
    }
    
}
