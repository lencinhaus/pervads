package it.polimi.dei.dbgroup.pedigree.contextmodel.util;

public final class StringUtils {
	private StringUtils() {}
	
	public static final void indent(StringBuilder sb, int indent) {
		if(indent == 0) return;
		for(int i=0; i < indent; i++) {
			sb.append("\t");
		}
	}
	
	public static final void indent(StringBuilder sb, int indent, String s) {
		indent(sb, indent, s, true);
	}
	
	public static final void indent(StringBuilder sb, int indent, String s, boolean newline) {
		indent(sb, indent);
		sb.append(s);
		if(newline) sb.append("\n");
	}
}
