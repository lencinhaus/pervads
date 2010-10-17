package it.polimi.dei.dbgroup.pedigree.contextmodel.proxy;

import java.util.Collection;

public interface ContextModelProxy extends DimensionParent {
	public Dimension findDimension(String uri);

	public Value findValue(String uri);

	public Collection<? extends Dimension> findAllDimensions();

	public Collection<? extends Value> findAllValues();
}
