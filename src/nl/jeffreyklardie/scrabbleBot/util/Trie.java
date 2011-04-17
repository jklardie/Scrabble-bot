package nl.jeffreyklardie.scrabbleBot.util;


import java.util.Set;

import nl.jeffreyklardie.scrabbleBot.game.objects.LetterBag;

public class Trie {

	private Node root;
	
	public Trie(){
		root = new Node(' ');
	}
	
	public void insert(String word){
		insert(root, word);
	}
	
	private void insert(Node root, String word){
		if(word.length() == 0){
			root.setIsWord(true);
			return;
		}
		
		insert(root.getChild(word.charAt(0)), word.substring(1));
	}
	
	public boolean contains(String word){
		return contains(root, word);
	}
	
	private boolean contains(Node root, String word){
		if(word.length() == 0){
			return root.isWord();
		} else if(root.hasChild(word.charAt(0))){
			return contains(root.getChild(word.charAt(0)), word.substring(1));
		} else if(word.charAt(0) == LetterBag.JOKER){
			// Empty tile, so every letter is possible
			char c;
			Set<Integer> keys = root.getChildren().keySet();
			for(int charIndex : keys){
				c = LetterBag.getCharForInt(charIndex);
				if(root.hasChild(c))
					return contains(root.getChild(c), word.substring(1));
			}
			
		}
		
		return false;
	}
	
	public void printWords(){
		printWords(root, "");
	}
	
	private void printWords(Node root, String wordPrefix){
		if(root.isWord()){
			System.out.println(wordPrefix);
		}
		
		char c;
		Set<Integer> keys = root.getChildren().keySet();
		for(int charIndex : keys){
			c = LetterBag.getCharForInt(charIndex);
			printWords(root.getChild(c), wordPrefix + c);
		}
	}
	
	public Node getRoot(){
		return root;
	}
	
}
