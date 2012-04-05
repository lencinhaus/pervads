package it.polimi.dei.dbgroup.pedigree.contextmodel.builder;

import java.util.Iterator;
import java.util.List;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.reasoner.InfGraph;
import com.hp.hpl.jena.reasoner.Reasoner;
import com.hp.hpl.jena.reasoner.ReasonerException;
import com.hp.hpl.jena.reasoner.ReasonerFactory;
import com.hp.hpl.jena.reasoner.rulesys.BasicForwardRuleInfGraph;
import com.hp.hpl.jena.reasoner.rulesys.BuiltinRegistry;
import com.hp.hpl.jena.reasoner.rulesys.FBRuleInfGraph;
import com.hp.hpl.jena.reasoner.rulesys.GenericRuleReasoner;
import com.hp.hpl.jena.reasoner.rulesys.RETERuleInfGraph;
import com.hp.hpl.jena.reasoner.rulesys.Rule;
import com.hp.hpl.jena.reasoner.rulesys.RulePreprocessHook;
import com.hp.hpl.jena.reasoner.rulesys.GenericRuleReasoner.RuleMode;

public class OWLSpecializedReasoner extends GenericRuleReasoner {
	static {
		BuiltinRegistry.theRegistry.register(new MakeSkolem());
	}

	public OWLSpecializedReasoner() {
		super(Rule.rulesFromURL("rulesets/owl-specialized.rules"));
		setMode(HYBRID);
		setTransitiveClosureCaching(false);
		// disabled owl translation of intersections (done in rules, no
		// intersection recognition)
		setOWLTranslation(false);
		// setFunctorFiltering(true);
	}

	protected OWLSpecializedReasoner(List<Rule> rules, Graph schemaGraph,
			ReasonerFactory factory, RuleMode mode) {
		super(rules, schemaGraph, factory, mode);
	}
}
