package it.polimi.dei.dbgroup.pedigree.contextmodel.builder;

public abstract class Option {
	private int numArguments;

	public abstract boolean parse(BuilderConfiguration config, String[] args,
			int offset) throws Exception;

	public Option(int numArguments) {
		this.numArguments = numArguments;
	}

	public int getNumArguments() {
		return numArguments;
	}
	
	public abstract String getDescription();
}
