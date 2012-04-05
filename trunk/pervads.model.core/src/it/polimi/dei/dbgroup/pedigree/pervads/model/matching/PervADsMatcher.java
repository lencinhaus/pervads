package it.polimi.dei.dbgroup.pedigree.pervads.model.matching;

import java.util.Collection;

import it.polimi.dei.dbgroup.pedigree.contextmodel.matching.SourceMatcher;
import it.polimi.dei.dbgroup.pedigree.contextmodel.matching.specialized.SpecializedMatcherResolver;
import it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.DimensionAssignment;
import it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.DimensionAssignmentSource;
import it.polimi.dei.dbgroup.pedigree.contextmodel.proxy.ParameterAssignment;
import it.polimi.dei.dbgroup.pedigree.pervads.model.matching.specialized.AgeMatcher;
import it.polimi.dei.dbgroup.pedigree.pervads.model.matching.specialized.ScalarMatcher;
import it.polimi.dei.dbgroup.pedigree.pervads.model.matching.specialized.SpaceMatcher;
import it.polimi.dei.dbgroup.pedigree.pervads.model.matching.specialized.TimeMatcher;

public class PervADsMatcher<S extends DimensionAssignment<? extends ParameterAssignment>, T extends DimensionAssignment<? extends ParameterAssignment>> extends SourceMatcher<S, T> {
	public PervADsMatcher(LocationService locationService) {
		this(locationService, null, null);
	}
	
	public PervADsMatcher(LocationService locationService, Collection<S> sourceDimensionAssignments) {
		this(locationService, sourceDimensionAssignments, null);
	}

	public PervADsMatcher(LocationService locationService, Collection<S> sourceDimensionAssignments,
			DimensionAssignmentSource<T> source) {
		super(sourceDimensionAssignments, source);
		if(locationService == null) throw new NullPointerException("locationService cannot be null");
		initSpecializedMatchers(locationService);
	}
	
	private void initSpecializedMatchers(LocationService locationService) {
		SpecializedMatcherResolver<S, T> resolver = getSpecializedMatcherResolver();
		resolver.addMatcher(new AgeMatcher<S, T>());
		resolver.addMatcher(new TimeMatcher<S, T>());
		resolver.addMatcher(new SpaceMatcher<S, T>(locationService));
	}
}
