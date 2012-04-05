package it.polimi.dei.dbgroup.pedigree.pervads.model.matching;

import it.polimi.dei.dbgroup.pedigree.pervads.model.matching.specialized.Coordinates;

public interface LocationService {
	public Coordinates geocode(String location) throws Exception;

	public Coordinates getCurrentPosition() throws Exception;
}
