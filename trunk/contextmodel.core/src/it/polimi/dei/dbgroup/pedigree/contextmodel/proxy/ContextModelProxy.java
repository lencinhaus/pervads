package it.polimi.dei.dbgroup.pedigree.contextmodel.proxy;

import java.util.Collection;

import com.hp.hpl.jena.rdf.model.Model;

public interface ContextModelProxy extends DimensionParent {
	public Model getModel();
	
	public Dimension findDimension(String uri);
	
	public Dimension findDimensionByActualDimensionClass(String actualDimensionClassUri);

	public Value findValue(String uri);

	public Collection<? extends Dimension> findAllDimensions();

	public Collection<? extends Value> findAllValues();
}
