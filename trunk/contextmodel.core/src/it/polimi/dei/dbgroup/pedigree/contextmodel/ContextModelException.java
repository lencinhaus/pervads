package it.polimi.dei.dbgroup.pedigree.contextmodel;

public class ContextModelException extends RuntimeException {
	public static final long serialVersionUID = 0L;

	public ContextModelException() {
		super();
	}

	public ContextModelException(String message) {
		super(message);
	}

	public ContextModelException(String message, Throwable t) {
		super(message, t);
	}

	public ContextModelException(Throwable t) {
		super(t);
	}
}
