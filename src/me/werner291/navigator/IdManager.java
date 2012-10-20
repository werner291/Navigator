package me.werner291.navigator;

import java.util.ArrayList;
import java.util.Random;

public class IdManager {

	static ArrayList<Integer> ids = new ArrayList<Integer>();
	static Random rand = new Random();
	
	
	static void registerId(int id) throws IdConflictException{
		if (ids.contains(id)) throw new IdConflictException();
		ids.add(id);
	}
	
	
	public static int createId(){
		
		int newId;
		
		do { 
			newId = rand.nextInt();
		} while (ids.contains(newId));
		
		try {
			registerId(newId);
		} catch (IdConflictException e) {
			e.printStackTrace();
		}
		
		rand.setSeed(rand.nextInt());
		
		return newId;
	}
	
	@SuppressWarnings("serial")
	public static class IdConflictException extends Exception{
		IdConflictException(){super();}
	}

	public static void clear() {
		ids.clear();
	}
	
}
