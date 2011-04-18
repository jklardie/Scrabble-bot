package nl.jeffreyklardie.scrabbleBot.util;


import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Set;

import nl.jeffreyklardie.scrabbleBot.game.objects.LetterBag;


public class Dictionary {

    private Trie trie;
    private int numWords = 0;
    private static String filePath;
    private static Dictionary instance;
    
    public static Dictionary getInstance(){
        if(instance == null) instance = new Dictionary();
        return instance;
    }
    
    public static void setDictionaryFile(String filePath){
        Dictionary.filePath = filePath; 
    }
    
    private Dictionary(){
        readWordsFromFile(filePath);
    }
    
    public void readWordsFromFile(String filePath){
        trie = new Trie();
        
        String word;
        try {
            Scanner scanner = new Scanner(new FileInputStream(filePath));
            while (scanner.hasNextLine()){
                word = scanner.nextLine();
                if(word.length() == 0) continue;
                
                trie.insert(word);
                numWords++;
            }
            scanner.close();
        } catch(FileNotFoundException e){
            e.printStackTrace();
        } 
        
    }
            
    public void printNumWords(){
        System.out.println("The dictionary contains " + numWords + " words.");
    }
    
    public void printWords(){
        trie.printWords();
    }
    
    public ArrayList<String> getPossibleWords(String letters){
    	return getPossibleWords(new ArrayList<String>(), letters, "");
    }
    
    private ArrayList<String> getPossibleWords(ArrayList<String> words, String letters, String prefix){
    	String word;

        for(int i=0; i<letters.length(); i++){
            word = prefix + letters.charAt(i);

            if(trie.contains(word)){
                if(word.indexOf(LetterBag.JOKER) == -1){
                	// no jokers in the word, so we can simply add it
                	if(!words.contains(word)) words.add(word);
                } else {
                	// the word contains jokers. Add all possible words where jokers
                	// are replaced with normal letters
                	ArrayList<String> wordsWithoutJokers = getWordsWithoutJokers(word);
                	for(String tmpWord : wordsWithoutJokers)
                		if(!words.contains(tmpWord)) words.add(tmpWord);
                }
            }

            String remainingLetters = "";
            for(int j=0; j<letters.length(); j++) if(i != j) remainingLetters += letters.charAt(j);
            
            getPossibleWords(words, remainingLetters, word);
        }
        
        return words;
    }

    private ArrayList<String> getWordsWithoutJokers(String word){
    	ArrayList<String> result = new ArrayList<String>();
    	
    	int jokerIndex = word.indexOf(LetterBag.JOKER);
    	if(jokerIndex < 0){
    		result.add(word);
    		return result;
    	}
    	
    	int secondJokerIndex = word.indexOf(LetterBag.JOKER, jokerIndex+1);
    	StringBuffer tmpWord = new StringBuffer(word);
    	for(char c='A'; c<='Z'; c++){
    		tmpWord.setCharAt(jokerIndex, c);
    		if(secondJokerIndex >= 0){
    			// there is a second joker, so do a second loop
    			for(char ch='A'; ch<='Z'; ch++){
    				tmpWord.setCharAt(secondJokerIndex, ch);
    				if(contains(tmpWord.toString()) && !result.contains(tmpWord.toString()))
        				result.add(tmpWord.toString());
    			}
    		} else if(contains(tmpWord.toString()) && !result.contains(tmpWord.toString())){
				result.add(tmpWord.toString());
    		}
    	}
    	
    	return result;
    }
    
    public ArrayList<String> getWordsWithLetters(String boardLetters, String rackLetters){
    	return getWordsWithLetters(new ArrayList<String>(boardLetters.length() * 3), trie.getRoot(), "", boardLetters, rackLetters);
    }
    
	private ArrayList<String> getWordsWithLetters(ArrayList<String> words, Node root, String prefix, String boardLetters, String rackLetters){
		if(root.isWord()){
			words.add(prefix);
		}
		
		char c;
		int boardLettersIndex, rackLettersIndex;
		StringBuffer buf;
		Set<Integer> keys = root.getChildren().keySet();
		for(int charIndex : keys){
			c = LetterBag.getCharForInt(charIndex);
			
			boardLettersIndex = boardLetters.indexOf(c);
			rackLettersIndex = rackLetters.indexOf(c);
			if(rackLettersIndex == -1) rackLettersIndex = (rackLetters.indexOf(LetterBag.JOKER));
			
			if(boardLettersIndex != -1){
				buf = new StringBuffer( boardLetters.length() - 1 );
				buf.append( boardLetters.substring(0, boardLettersIndex) ).append( boardLetters.substring(boardLettersIndex+1) );
				getWordsWithLetters(words, root.getChild(c), prefix + c, buf.toString(), rackLetters);
			} else if(rackLettersIndex != -1){
				buf = new StringBuffer( rackLetters.length() - 1 );
				buf.append( rackLetters.substring(0, rackLettersIndex) ).append( rackLetters.substring(rackLettersIndex+1) );
				getWordsWithLetters(words, root.getChild(c), prefix + c, boardLetters, buf.toString());
			}
		}
		
		return words;
	}
    
    public boolean contains(String word){
        return trie.contains(word);
    }
       
}
