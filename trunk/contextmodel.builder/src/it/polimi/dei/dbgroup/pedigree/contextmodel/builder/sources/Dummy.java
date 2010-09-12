package it.polimi.dei.dbgroup.pedigree.contextmodel.builder.sources;

import it.polimi.dei.dbgroup.pedigree.contextmodel.builder.Category;
import it.polimi.dei.dbgroup.pedigree.contextmodel.builder.ICategorySource;

import java.util.ArrayList;
import java.util.List;

public class Dummy implements ICategorySource {

	@Override
	public List<Category> getCategories() throws Exception {
		List<Category> cats = new ArrayList<Category>();
		Category cat1 = new Category("cat1");
		cats.add(cat1);
		Category cat2 = new Category("cat2");
		cats.add(cat2);
		Category cat11 = new Category("cat11");
		cat1.getSubCategories().add(cat11);
		Category cat12 = new Category("cat12");
		cat1.getSubCategories().add(cat12);
		Category cat21 = new Category("cat21");
		cat2.getSubCategories().add(cat21);
		Category cat22 = new Category("cat22");
		cat2.getSubCategories().add(cat22);
		cat11.getSubCategories().add(new Category("cat111"));
		cat22.getSubCategories().add(new Category("cat221"));
		return cats;
	}

	@Override
	public String getName() {
		return "dummy";
	}
}
