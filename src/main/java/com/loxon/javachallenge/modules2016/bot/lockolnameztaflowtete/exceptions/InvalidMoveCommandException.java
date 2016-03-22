package com.loxon.javachallenge.modules2016.bot.lockolnameztaflowtete.exceptions;

/**
 * Exception for invalid move commands.
 * 
 * @author ihorvath
 *
 */
public class InvalidMoveCommandException extends Exception {
	
	public InvalidMoveCommandException(String msg) {
		super(msg);
	}
	
	public InvalidMoveCommandException(Exception e) {
		super(e);
	}
}
