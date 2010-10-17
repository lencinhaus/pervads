package it.polimi.dei.dbgroup.pedigree.pervads;

public final class EventTypes {
	private EventTypes()
	{
		
	}
	
	public static final int Started = 0;
	public static final int Completed = 1;
	public static final int Failed = 2;
	
	public static String describe(int eventType)
	{
		switch(eventType)
		{
			case Started:
				return "Started";
			case Completed:
				return "Completed";
			case Failed:
				return "Failed";
			default:
				throw new IllegalArgumentException("event type " + eventType + " not recognized");
		}
	}
}
