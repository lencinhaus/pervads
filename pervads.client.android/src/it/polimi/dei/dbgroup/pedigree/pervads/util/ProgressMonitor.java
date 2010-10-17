package it.polimi.dei.dbgroup.pedigree.pervads.util;

public interface ProgressMonitor {
	public void max(int max);
	
	public void progress(int progress);
	
	public void increment(int by);
	
	public void message(String message);
	
	public void message(int messageId);
	
	public void indeterminate(boolean indeterminate);
}
