package it.polimi.dei.dbgroup.pedigree.contextmodel.proxy;

import java.util.Collection;

public interface DimensionParent {
	public Collection<? extends Dimension> listChildDimensions();
}
