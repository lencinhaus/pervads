package it.polimi.dei.dbgroup.pedigree.contextmodel.builder;

import java.util.HashMap;
import java.util.Map;

public class LocalizedAttribute {
	private Map<String, String> localizedValues = new HashMap<String, String>();
	private String defaultValue;
	public String getDefaultValue() {
		return defaultValue;
	}
	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}
	public Map<String, String> getLocalizedValues() {
		return localizedValues;
	}
	
	public String get(String lang) {
		return localizedValues.get(lang);
	}
	
	public void set(String lang, String value) {
		if(value == null || value.length() == 0) throw new IllegalArgumentException("value cannot be empty");
		if(lang == null || lang.length() == 0) setDefaultValue(value);
		else localizedValues.put(lang, value);
	}
	@Override
	public String toString() {
		return getDefaultValue();
	}
}
