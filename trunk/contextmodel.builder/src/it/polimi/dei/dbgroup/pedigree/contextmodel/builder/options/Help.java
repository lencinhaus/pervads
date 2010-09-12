package it.polimi.dei.dbgroup.pedigree.contextmodel.builder.options;

import it.polimi.dei.dbgroup.pedigree.contextmodel.builder.BuilderConfiguration;
import it.polimi.dei.dbgroup.pedigree.contextmodel.builder.Main;
import it.polimi.dei.dbgroup.pedigree.contextmodel.builder.Option;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Help extends Option {

	public Help() {
		super(0);
	}

	@Override
	public boolean parse(BuilderConfiguration config, String[] args, int offset)
			throws Exception {
		// build options map
		Map<Option, List<String>> optionsMap = new HashMap<Option, List<String>>();
		for (String optionArg : Main.options.keySet()) {
			Option option = Main.options.get(optionArg);
			List<String> optionArgSet = optionsMap.get(option);
			if (optionArgSet == null) {
				optionArgSet = new ArrayList<String>();
				optionsMap.put(option, optionArgSet);
			}
			optionArgSet.add(optionArg);
		}
		System.out.println("Usage:");
		System.out.println();
		Comparator<String> argsComparator = new Comparator<String>() {
			
			@Override
			public int compare(String o1, String o2) {
				return o1.length() - o2.length();
			}
		};
		for (Option option : optionsMap.keySet()) {
			boolean firstArg = true;
			List<String> optionArgs = optionsMap.get(option);
			Collections.sort(optionArgs, argsComparator);
			for (String arg : optionArgs) {
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
