package it.polimi.dei.dbgroup.pedigree.contextmodel.matching.specialized;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target( {})
public @interface ValueParameterMapping {
	String valueUri();

	String parameterUri();

	boolean optional() default false;
}
