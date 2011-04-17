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

    /**
     * Return all possible words that contain at least on letter from 
     * the board, and one letter from the rack.
     * @param boardLetters
     * @param rackLetters
     * @return
     */
    public ArrayList<String> getPossibleWords(String boardLetters, String rackLetters){
    	return getPossibleWords(new ArrayList<String>(), boardLetters, rackLetters, "");
    }
    
    private ArrayList<String> getPossibleWords(ArrayList<String> words, String boardLetters, String rackLetters, String prefix){
    	
    	ArrayList<String> w = new ArrayList<String>();
        for(int i=0; i<rackLetters.length(); i++){
       		getPossibleWords(w, boardLetters, "", rackLetters.charAt(i));
       		for(String word : w){
       			if(trie.contains(word) && wordContainsLetter(word, boardLetters)){
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
       		}
        }
        
        return words;
    }
    
    /**
     * Return whether the given word contains at least on of the given letters
     *  
     * @param letters
     * @return boolean
     */
    private boolean wordContainsLetter(String word, String letters){
    	for(char c : word.toCharArray()){
    		if(letters.indexOf(c) != -1) return true;
    	}
    	
    	return false;
    }

    private void getPossibleWords(ArrayList<String> words, String letters, String prefix, char requiredLetter){      
        boolean hasReq = (requiredLetter != ' ');
		if (hasReq && letters.indexOf(requiredLetter) == -1 && 
				prefix.indexOf(requiredLetter) == -1)
			return;
    	
    	String word;

        for(int i=0; i<letters.length(); i++){
            word = prefix + letters.charAt(i);

            if((!hasReq || word.indexOf(requiredLetter) != -1) && trie.contains(word)){
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
            
            getPossibleWords(words, remainingLetters, word, requiredLetter);
        }
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
    

//    
//    /**
//     * Get all the possbile words that can be formed with the given letters
//     * @param word
//     * @return ArrayList<String> possible words
//     */
//    public ArrayList<String> getPossibleWords(String letters){
//        return getPossibleWords(letters, 99);
//    }
//    
//    /**
//     * Get all the possible words with max length n that can be formed with the given letters
//     * @param word
//     * @param maxLength
//     * @return ArrayList<String> possible words
//     */
//    public ArrayList<String> getPossibleWords(String letters, int maxLength){
//        ArrayList<String> words = new ArrayList<String>();
//        getPossibleWords(words, letters, "", maxLength, "", "");
//        
//        return words;
//    }
//
//    /**
//     * Find all possible words that end with postfix
//     * @param postfix
//     * @param letters
//     * @return ArrayList<String> words
//     */
//    public ArrayList<String> getPossibleWordsWithPostfix(String postfix, String letters){
//        return getPossibleWordsWithPostfix(postfix, letters, 99);
//    }
//    
//    /**
//     * Find all possible words with max length n that end with postfix
//     * @param postfix
//     * @param letters
//     * @param maxLength
//     * @return ArrayList<String> words
//     */
//    public ArrayList<String> getPossibleWordsWithPostfix(String postfix, String letters, int maxLength){
//        ArrayList<String> words = new ArrayList<String>();
//        getPossibleWords(words, letters, "", maxLength, "", postfix);
//        return words;
//    }
//    
//    /**
//     * Find all possible words that can be formed with the given letters
//     * @param prefix
//     * @param letters
//     * @param maxLength
//     * @return ArrayList<String> words
//     */
//    public ArrayList<String> getPossibleWordsWithPrefix(String prefix, String letters){
//        return getPossibleWordsWithPrefix(prefix, letters, 99);
//    }
//    
//    /**
//     * Find all possible words with max length n that start with prefix
//     * @param prefix
//     * @param letters
//     * @param maxLength
//     * @return ArrayList<String> words
//     */
//    public ArrayList<String> getPossibleWordsWithPrefix(String prefix, String letters, int maxLength){
//        ArrayList<String> words = new ArrayList<String>();
//        getPossibleWords(words, letters, "", maxLength, prefix, "");
//        return words;
//    }
//    
//    
//    public ArrayList<String> getPossibleWords(String letters, String startingWith, String endingWith){
//    	ArrayList<String> words = new ArrayList<String>();
//    	getPossibleWords(words, letters, "", 15, startingWith, endingWith);
//    	return words;
//    }
//    
//    /**
//     * @param words
//     * @param letters
//     * @param prefix
//     * @param maxLength
//     * @param startingWith
//     * @param endingWith
//     */
//    private void getPossibleWords(ArrayList<String> words, String letters, String prefix, int maxLength, String startingWith, String endingWith){      
//        String word;
//        if(startingWith.length() > 0){
//            prefix = startingWith;
//            startingWith = "";
//            if(prefix.length() > maxLength) return;
//        }
//
//        for(int i=0; i<letters.length(); i++){
//            word = prefix + letters.charAt(i);
//
//            if(trie.contains(word + endingWith) && !words.contains(word + endingWith)){
//            	if(word.indexOf(LetterBag.JOKER) == -1){
//                	// no jokers in the word, so we can simply add it
//                	if(!words.contains(word + endingWith)) words.add(word + endingWith);
//                } else {
//                	// the word contains jokers. Add all possible words where jokers
//                	// are replaced with normal letters
//                	ArrayList<String> wordsWithoutJokers = getWordsWithoutJokers(word + endingWith);
//                	for(String tmpWord : wordsWithoutJokers)
//                		if(!words.contains(tmpWord)) words.add(tmpWord);
//                }
//            }
//
//            String remainingLetters = "";
//            for(int j=0; j<letters.length(); j++) if(i != j) remainingLetters += letters.charAt(j);
//            
//            if(word.length() + endingWith.length() + 1 <= maxLength){
//                getPossibleWords(words, remainingLetters, word, maxLength, startingWith, endingWith);
//            }
//        }
//    }
//    
    
//    public int getWordScore(String word){
//        int score = 0;
//        for(char c : word.toCharArray()){
//            score += LetterBag.getLetterScore(c);
//        }
//        
//        return score;
//    }
    
    private boolean wordContainsLettersFromBoth(String word, String boardLetters, String rackLetters){
    	boolean includesBoardLetter = false;
    	boolean includesRackLetter = false;
    	
    	for(char c : word.toCharArray()){
    		if(boardLetters.indexOf(c) != -1){
    			includesBoardLetter = true;
    			if(includesRackLetter) return true;
    		} else if(rackLetters.indexOf(c) != -1 || rackLetters.indexOf(LetterBag.JOKER) != -1){
    			includesRackLetter = true;
    			if(includesBoardLetter) return true;
    		}
    	}
    	
    	return false;
    }
    
    public ArrayList<String> getWordsWithLetters(String boardLetters, String rackLetters){
    	return getWordsWithLetters(new ArrayList<String>(boardLetters.length() * 3), trie.getRoot(), "", boardLetters, rackLetters);
    }
    
	private ArrayList<String> getWordsWithLetters(ArrayList<String> words, Node root, String prefix, String boardLetters, String rackLetters){
		if(root.isWord() && wordContainsLettersFromBoth(prefix, boardLetters, rackLetters)){
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
			if(rackLettersIndex == -1) rackLettersIndex = rackLetters.indexOf(LetterBag.JOKER);
			
			if(boardLettersIndex != -1){
				buf = new StringBuffer( boardLetters.length() - 1 );
				buf.append( boardLetters.substring(0, boardLettersIndex) ).append( boardLetters.substring(boardLettersIndex+1) );
				getWordsWithLetters(words, root.getChild(c), prefix + c, buf.toString(), rackLetters);
			} else if(rackLettersIndex != -1){
				buf = new StringBuffer( rackLetters.length() - 1 );
				buf.append( rackLetters.substring(0, rackLettersIndex) ).append( rackLetters.substring(rackLettersIndex+1) );
				getWordsWithLetters(words, root.getChild(c), prefix + c, rackLetters, buf.toString());
			}
		}
		
		return words;
	}
    
    public boolean contains(String word){
        return trie.contains(word);
    }
       
}
