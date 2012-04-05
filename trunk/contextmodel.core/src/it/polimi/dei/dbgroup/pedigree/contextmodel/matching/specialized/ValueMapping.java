package it.polimi.dei.dbgroup.pedigree.contextmodel.matching.specialized;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ValueMapping {
	String[] valueURIs();
}
