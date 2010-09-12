package it.polimi.dei.dbgroup.pedigree.contextmodel.builder.options;

import it.polimi.dei.dbgroup.pedigree.contextmodel.builder.BuilderConfiguration;
import it.polimi.dei.dbgroup.pedigree.contextmodel.builder.Main;
import it.polimi.dei.dbgroup.pedigree.contextmodel.builder.Option;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Help extends Option {

	public Help() {
		super(0);
	}

	@Override
	public boolean parse(BuilderConfiguration config, String[] args, int offset)
			throws Exception {
		// build options map
		Map<Option, Set<String>> optionsMap = new HashMap<Option, Set<String>>();
		for (String optionArg : Main.options.keySet()) {
			Option option = Main.options.get(optionArg);
			Set<String> optionArgSet = optionsMap.get(option);
			if (optionArgSet == null) {
				optionArgSet = new HashSet<String>();
				optionsMap.put(option, optionArgSet);
			}
			optionArgSet.add(optionArg);
		}
		System.out.println("Usage:");
		System.out.println();
		for (Option option : optionsMap.keySet()) {
			boolean firstArg = true;
			for (String arg : optionsMap.get(option)) {
				if (firstArg)
					firstArg = false;
				else
					System.out.print(", ");
				System.out.print("-" + arg);
				for(int i=0; i < option.getNumArguments(); i++) {
					System.out.print(" [arg" + (i+1) + "]");
				}
			}
			System.out.println(":");
			System.out.println("\t" + option.getDescription());
			System.out.println();
		}
		return false;
	}

	@Override
	public String getDescription() {
		return "shows this help information and exits";
	}

}
