package it.polimi.dei.dbgroup.pedigree.contextmodel.util;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.hp.hpl.jena.datatypes.xsd.XSDDateTime;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.RDFNode;

public class ModelUtils {
	private ModelUtils() {
	}

	public static List<String> parseStringLiterals(NodeIterator iterator) {
		List<String> strings = new ArrayList<String>();
		while (iterator.hasNext()) {
			String string = parseStringLiteral(iterator.next());
			if (string != null)
				strings.add(string);
		}
		return strings;
	}

	public static <T> List<T> parseTypedLiterals(NodeIterator iterator,
			Class<T> clazz) {
		List<T> list = new ArrayList<T>();
		while (iterator.hasNext()) {
			T item = parseTypedLiteral(iterator.next(), clazz);
			if (item != null)
				list.add(item);
		}
		return list;
	}

	public static Literal parseLiteral(RDFNode node) {
		if (node == null)
			return null;
		if (!node.isLiteral())
			throw new RuntimeException("node " + node + " is not a Literal");
		return node.as(Literal.class);
	}

	public static Object parseLiteralValue(RDFNode node) {
		Literal literal = parseLiteral(node);
		if (literal == null)
			return null;
		return literal.getValue();
	}

	public static Date parseDateLiteral(RDFNode node) {
		XSDDateTime xsdDateTime = parseTypedLiteral(node, XSDDateTime.class);
		if (xsdDateTime == null)
			return null;
		return xsdDateTime.asCalendar().getTime();
	}

	public static Integer parseIntLiteral(RDFNode node) {
		return parseTypedLiteral(node, Integer.class);
	}

	public static Double parseDoubleLiteral(RDFNode node) {
		return parseTypedLiteral(node, Double.class);
	}

	public static Float parseFloatLiteral(RDFNode node) {
		return parseTypedLiteral(node, Float.class);
	}

	@SuppressWarnings("unchecked")
	public static <T> T parseTypedLiteral(RDFNode node, Class<T> clazz) {
		Object value = parseLiteralValue(node);
		if (value == null)
			return null;
		if (!value.getClass().equals(clazz))
			throw new RuntimeException("Node value " + value + " is not a "
					+ clazz.getName() + " but a " + value.getClass().getName());
		return (T) value;
	}

	public static String parseStringLiteral(RDFNode node) {
		Literal literal = parseLiteral(node);
		if (literal == null)
			return null;
		return literal.getLexicalForm();
	}
}
