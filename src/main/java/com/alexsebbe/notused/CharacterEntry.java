package com.alexsebbe.notused;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

@JsonIdentityInfo(generator=ObjectIdGenerators.IntSequenceGenerator.class, property="@id")
@JsonIgnoreProperties(ignoreUnknown = true)
public class CharacterEntry {
	private CharacterEntry parent;
	private List<Integer> lengths;
	private Character character;
	private List<CharacterEntry> children = new ArrayList<CharacterEntry>();

	private String characterSequenceUpToNowCache = null;

	public CharacterEntry() {}
	
	public CharacterEntry(List<Integer> lengths, Character character) {
		this(lengths, character, null);
	}
	
	public CharacterEntry(List<Integer> lengths, Character character, CharacterEntry parent) {
		this.lengths = lengths;
		this.character = character;
		this.parent = parent;
	}
	
	public List<Integer> getLengths() {
		return lengths;
	}
	
	public Character getCharacter() {
		return character;
	}
	
	public CharacterEntry getParent() {
		return parent;
	}
	
	public List<CharacterEntry> getChildren() {
		return children;
	}
	
	public String getCharacterSequenceUpToNow() {
		if(characterSequenceUpToNowCache == null) {
			StringBuilder sb = new StringBuilder();
			CharacterEntry current = this.getParent();
			
			while(current != null){
				if(current.getCharacter() != null) {
					sb.append(current.getCharacter());
				}
				current = current.getParent();
			}
			sb.reverse();
			
			characterSequenceUpToNowCache = sb.toString();
		}
		
		return characterSequenceUpToNowCache;
	}
	
	public List<List<Integer>> getLengthsUpToAndIncludingNow() {
		CharacterEntry currentEntry = this;
		List<List<Integer>> lengths = new ArrayList<List<Integer>>();
		do {
			lengths.add(currentEntry.getLengths());
			currentEntry = currentEntry.getParent();
		} while(currentEntry.getParent() != null && currentEntry.getParent().getCharacter() != null);
		
		Collections.reverse(lengths);
		
		return lengths;
	}
	
	public String getCharaceterSequenceUpToAndIncludingThis() {
		return getCharacterSequenceUpToNow() + (getCharacter() != null? getCharacter() : "");
	}
	
	public List<String> childrenToString() {
		List<String> result = new ArrayList<String>();
		childrenToStringHelper(this, result);
		return result;
	}
	
	public int getNumberOfParents() {
		CharacterEntry current = this;
		int result = 0;
		while(current.parent != null && current.parent.character != null) {
			result++;
			current = current.parent;
		}
		
		return result;
	}
	
	@Override
	public String toString() {
		return character + " : " + lengths;
	}
	
	private void childrenToStringHelper(CharacterEntry currentEntry, List<String> result) {
		for(CharacterEntry child : currentEntry.getChildren()) {
			result.add(child.getCharacterSequenceUpToNow() + child.getCharacter() + " : " + child.getLengths());
			childrenToStringHelper(child, result);
		}
	}
}
