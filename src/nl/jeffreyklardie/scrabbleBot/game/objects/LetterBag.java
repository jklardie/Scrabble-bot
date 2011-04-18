package nl.jeffreyklardie.scrabbleBot.game.objects;
import java.util.Random;


public class LetterBag {

    /**
     * All the possible letters in the game. 
     * 
     * {6, 1} is the first item, so letter A. It says: there are 6 A's, each worth 1 point
     * {2, 3} is the second item, so letter B. There are 2 B's, each worth 3 points
     * The last item is the joker. There are 2 jokers, worth 0 points.
     */
    public static final byte[][] LETTERS = {{6,1},{2,3},{2,5},{5,2},{18,1},{1,4},{3,3},{2,4},{4,1},{2,4},{3,3},
        {3,3},{3,3},{10,1},{6,1},{2,3},{1,10},{5,2},{4,2},{5,2},{3,4},{2,4},{2,5},{1,8},{1,8},{2,4},{2,0}
    }; 
    
    public static final int TOTAL_LETTERS_IN_GAME = 100;
    
    /**
     * Minimum number of letters left in the bag for exchange to be possible
     */
    public static final int MIN_LETTERS_IN_BAG_FOR_EXCHANGE = 7;
    
    /**
     * The character for the jokers. The [ character has value 91, which is the first after Z. This way, 
     * we can simply use the above matrix of letters to find the value.
     */
    public static final char JOKER = '*';
    
    /**
     * Placeholder for 'empty letters' (e.g. an empty square on the board, or a missing letter on the rack)
     */
    public static final char EMPTY_LETTER = '.';
    
    /**
     * The letters left in the bag during the current game.
     */
    private static byte[][] lettersInGame = LETTERS;
    
    private static LetterBag instance;
    private static int numLettersInGame = TOTAL_LETTERS_IN_GAME;
    private static final Random generator = new Random();
    
    
    public static LetterBag getInstance(){
        if(instance == null) instance = new LetterBag();
        return instance;
    }
    
    private LetterBag(){
        
    }
    
    public static char takeRandomLetterFromBag(){
        int letterIndex = generator.nextInt(LETTERS.length);
        while(lettersInGame[letterIndex][0] == 0){
            letterIndex = generator.nextInt(LETTERS.length);
        }
        
        lettersInGame[letterIndex][0]--;
        numLettersInGame--;
        return getCharForInt(letterIndex);
    }
    
    public static boolean letterExchangePossible(){
        return numLettersInGame > MIN_LETTERS_IN_BAG_FOR_EXCHANGE;
    }
    
    public static char exchangeLetter(char letter){
        if(!hasMoreLetters()) return letter;
        
        char newLetter = takeRandomLetterFromBag();
        putLetterInBag(letter);
        
        return newLetter;
    }
    
    private static void putLetterInBag(char letter){
        lettersInGame[getCharIndex(letter)][0]++;
        numLettersInGame++;
    }
    
    public static boolean hasMoreLetters(){
        return numLettersInGame > 0;
    }
        
    public static char getCharForInt(int index){
        if(index == LETTERS.length-1) return JOKER;
        return (char)('A' + index);
    }
    
    public static int getCharIndex(char letter){
        if(letter == JOKER) return LETTERS.length-1;
        return (int)(letter - 'A');
    }
    
    public static int getLetterScore(char letter){
    	return LETTERS[getCharIndex(letter)][1];
    }

        
}
