package it.polimi.dei.dbgroup.pedigree.contextmodel.proxy;

import java.util.Collection;

public interface ValueParent {
	public Collection<? extends Value> listChildValues();
}
