package it.polimi.dei.dbgroup.pedigree.contextmodel.builder;

import java.util.List;

public interface ICategorySource {
	public String getName();
	public List<Category> getCategories() throws Exception;
}
