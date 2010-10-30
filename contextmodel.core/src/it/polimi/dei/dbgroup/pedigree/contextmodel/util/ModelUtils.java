package it.polimi.dei.dbgroup.pedigree.contextmodel.util;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import com.hp.hpl.jena.datatypes.xsd.XSDDateTime;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;

public class ModelUtils {
	private ModelUtils() {
	}

	public static List<String> parseStringLiterals(Iterator<RDFNode> iterator) {
		List<String> strings = new ArrayList<String>();
		while (iterator.hasNext()) {
			String string = parseStringLiteral(iterator.next());
			if (string != null)
				strings.add(string);
		}
		return strings;
	}

	public static <T> List<T> parseTypedLiterals(Iterator<RDFNode> iterator,
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
			return null;
		// throw new RuntimeException("node " + node + " is not a Literal");

		return (Literal) node;
	}

	public static List<Literal> parseLiterals(Iterator<RDFNode> it) {
		List<Literal> lits = new ArrayList<Literal>();
		while (it.hasNext()) {
			Literal lit = parseLiteral(it.next());
			if (lit != null)
				lits.add(lit);
		}

		return lits;
	}

	public static Literal getLocalizedLiteral(Iterator<RDFNode> it, String lang) {
		if (lang == null)
			throw new IllegalArgumentException("lang cannot be null");
		Literal found = null;
		while (it.hasNext()) {
			Literal lit = parseLiteral(it.next());
			if (lit != null) {
				String llang = lit.getLanguage();
				if (lang.equalsIgnoreCase(llang)) {
					found = lit;
					break;
				} else if (llang != null && llang.length() > 1
						&& llang.substring(0, 2).equalsIgnoreCase(lang)) {
					found = lit;
				} else if (llang == null && found == null) {
					found = lit;
				}
			}
		}

		return found;
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

	public static <T> T getLocalizedTypedLiteral(Iterator<RDFNode> it,
			Class<T> clazz, String lang) {
		Literal lit = getLocalizedLiteral(it, lang);
		if (lit != null)
			return parseTypedLiteral(lit, clazz);
		return null;
	}

	public static String parseStringLiteral(RDFNode node) {
		Literal literal = parseLiteral(node);
		if (literal == null)
			return null;
		return literal.getLexicalForm();
	}

	public static String getLocalizedStringLiteral(Iterator<RDFNode> it,
			String lang) {
		Literal lit = getLocalizedLiteral(it, lang);
		if (lit != null)
			return lit.getLexicalForm();
		return null;
	}
	
	public static Date getLocalizedDateLiteral(Iterator<RDFNode> it,
			String lang) {
		Literal lit = getLocalizedLiteral(it, lang);
		if (lit != null)
			return parseDateLiteral(lit);
		return null;
	}

	public static Resource getResourceIfExists(Model m, String uri) {
		Resource r = m.createResource(uri);
		return m.containsResource(r) ? r : null;
	}

	public static Resource getResourceWithProperty(Model m, String uri,
			Property p, RDFNode object) {
		Resource r = m.createResource(uri);
		if (m.contains(r, p, object))
			return r;
		return null;
	}

	private static Iterator<RDFNode> getPropertyValueIterator(Resource r,
			Property p) {
		return r.getModel().listObjectsOfProperty(r, p);
	}

	public static <T> List<T> listTypedProperties(Resource r, Property p,
			Class<T> clazz) {
		return parseTypedLiterals(getPropertyValueIterator(r, p), clazz);
	}

	public static List<String> listStringProperties(Resource r, Property p) {
		return parseStringLiterals(getPropertyValueIterator(r, p));
	}

	public static <T> T getTypedProperty(Resource r, Property p, Class<T> clazz) {
		return getTypedProperty(r, p, clazz, null);
	}

	public static <T> T getTypedProperty(Resource r, Property p,
			Class<T> clazz, String lang) {
		Iterator<RDFNode> it = getPropertyValueIterator(r, p);
		if (lang == null) {
			if (it.hasNext())
				return parseTypedLiteral(it.next(), clazz);
			return null;
		} else {
			return getLocalizedTypedLiteral(it, clazz, lang);
		}
	}

	public static String getStringProperty(Resource r, Property p, String lang) {
		Iterator<RDFNode> it = getPropertyValueIterator(r, p);
		if (lang == null) {
			if (it.hasNext())
				return parseStringLiteral(it.next());
			return null;
		} else {
			return getLocalizedStringLiteral(it, lang);
		}
	}
	
	public static String getStringProperty(Resource r, Property p) {
		return getStringProperty(r, p, null);
	}

	public static Date getDateProperty(Resource r, Property p, String lang) {
		Iterator<RDFNode> it = getPropertyValueIterator(r, p);
		if (lang == null) {
			if (it.hasNext())
				return parseDateLiteral(it.next());
			return null;
		} else {
			return getLocalizedDateLiteral(it, lang);
		}
	}
	
	public static Date getDateProperty(Resource r, Property p) {
		return getDateProperty(r, p, null);
	}
}
