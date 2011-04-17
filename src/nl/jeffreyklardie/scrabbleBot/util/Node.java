package nl.jeffreyklardie.scrabbleBot.util;

import java.util.HashMap;

import nl.jeffreyklardie.scrabbleBot.game.objects.LetterBag;


public class Node {
		
		private char c;
		private boolean isWord;
		private HashMap<Integer, Node> children;
		
		public Node(char c){
			this.c = c;
			isWord = false;
			children = new HashMap<Integer, Node>();
		}
		
		public Node getChild(char c){
			int charIndex = LetterBag.getCharIndex(c);
			if(!hasChild(c)){
				children.put(charIndex, new Node(c));
			}
			
			return children.get(charIndex);
		}
		
		public boolean hasChild(char c){
			int charIndex = LetterBag.getCharIndex(c);
			return children.containsKey(charIndex);
		}
		
		public HashMap<Integer, Node> getChildren(){
			return children;
		}
		
		public char getChar(){
			return c;
		}
		
		public boolean isWord(){
			return isWord;
		}
			
		public void setIsWord(boolean isWord) {
			this.isWord = isWord;
		}
		
	}