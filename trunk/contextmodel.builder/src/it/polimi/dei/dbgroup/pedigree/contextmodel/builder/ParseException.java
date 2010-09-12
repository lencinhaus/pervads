/**
 * 
 */
package it.polimi.dei.dbgroup.pedigree.contextmodel.builder;

public class ParseException extends Exception {
	public static final long serialVersionUID = 0L;
	public ParseException(String message) {
		super(message);
	}
	
	public ParseException(String message, Throwable t) {
		super(message, t);
	}
}