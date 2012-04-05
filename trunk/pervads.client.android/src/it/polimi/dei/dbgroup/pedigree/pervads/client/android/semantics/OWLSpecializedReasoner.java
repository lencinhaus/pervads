package it.polimi.dei.dbgroup.pedigree.pervads.client.android.semantics;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import android.content.Context;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.reasoner.InfGraph;
import com.hp.hpl.jena.reasoner.ReasonerException;
import com.hp.hpl.jena.reasoner.rulesys.BuiltinRegistry;
import com.hp.hpl.jena.reasoner.rulesys.FBRuleInfGraph;
import com.hp.hpl.jena.reasoner.rulesys.GenericRuleReasoner;
import com.hp.hpl.jena.reasoner.rulesys.Rule;

public class OWLSpecializedReasoner extends GenericRuleReasoner {
	static {
		BuiltinRegistry.theRegistry.register(new MakeSkolem());
	}
	private static final String RULES_ASSET_NAME = "pervads.rules";

	public OWLSpecializedReasoner(Context context) throws IOException {
		super(Rule.parseRules(Rule.rulesParserFromReader(new BufferedReader(
				new InputStreamReader(context.getAssets()
						.open(RULES_ASSET_NAME))))));
		setMode(HYBRID);
		// no transitive closure caching, we use rules for now
		setTransitiveClosureCaching(false);
		// disabled owl translation of intersections (done in rules, no
		// intersection recognition)
		// setOWLTranslation(true);
	}

	@Override
	public InfGraph bind(Graph data) throws ReasonerException {
		InfGraph graph = super.bind(data);
		((FBRuleInfGraph) graph).setDatatypeRangeValidation(true);
		return graph;
	}
}
