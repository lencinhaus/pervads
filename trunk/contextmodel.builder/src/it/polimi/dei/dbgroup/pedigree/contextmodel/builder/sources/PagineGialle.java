package it.polimi.dei.dbgroup.pedigree.contextmodel.builder.sources;

import it.polimi.dei.dbgroup.pedigree.contextmodel.builder.Category;
import it.polimi.dei.dbgroup.pedigree.contextmodel.builder.ICategorySource;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.HTMLElement;
import com.meterware.httpunit.HttpUnitOptions;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;

public class PagineGialle implements ICategorySource {
	private class NameAndLink {
		public String name;
		public String link;

		protected NameAndLink(String name, String link) {
			super();
			this.name = name;
			this.link = link;
		}

	}

	private static class TranslationInfo {
		public String lang;
		public String url;
		public String kindFormat;
		public boolean firstLower;
		public boolean isDefault;

		public TranslationInfo(String lang, String url, String kindFormat) {
			this(lang, url, kindFormat, true);
		}

		public TranslationInfo(String lang, String url, String kindFormat,
				boolean firstLower) {
			this(lang, url, kindFormat, firstLower, false);
		}

		public TranslationInfo(String lang, String url, String kindFormat,
				boolean firstLower, boolean isDefault) {
			super();
			this.lang = lang;
			this.url = url;
			this.kindFormat = kindFormat;
			this.firstLower = firstLower;
			this.isDefault = isDefault;
		}

	}

	private WebConversation conv;
	private List<Category> categories;
	private Map<String, List<Category>> finalCategories = new HashMap<String, List<Category>>();
	private URL baseUrl;
	private static final TranslationInfo mainTranslation = new TranslationInfo(
			"it", "pagine_gialle_naviga.html", "Tipo di %s");
	private static final TranslationInfo[] translations = new TranslationInfo[] {
			new TranslationInfo("es", "pagine_gialle_naviga----.html",
					"Tipo de %s"),
			new TranslationInfo("fr", "pagine_gialle_naviga---.html",
					"Genre de %s"),
			new TranslationInfo("en", "pagine_gialle_naviga-.html",
					"Kind of %s", true, true),
			new TranslationInfo("de", "pagine_gialle_naviga--.html",
					"%s sorte", false) };

	@Override
	public String getName() {
		return "pagine_gialle";
	}

	@Override
	public List<Category> getCategories() throws Exception {
		baseUrl = new URL("http://www.paginegialle.it/cat/");
		conv = new WebConversation();
		HttpUnitOptions.setExceptionsThrownOnScriptError(false);

		createCategories();

		translateCategories();

		return categories;
	}

	private void createCategories() throws Exception {
		categories = new ArrayList<Category>();
		System.out.println("Creating main categories");
		for (NameAndLink item : parseFirstPage(mainTranslation.url)) {
			parseCategory(null, categories, item.name, item.link);
		}
	}

	private void translateCategories() throws Exception {
		for (TranslationInfo translation : translations) {
			System.out.println("Translating in " + translation.lang);
			for (NameAndLink item : parseFirstPage(translation.url)) {
				translateCategory(item.name, item.link, translation);
			}
		}
	}

	private Category translateCategory(String name, String url,
			TranslationInfo translation) throws Exception {
		Category child = null;
		Set<Category> parents = null;
		List<NameAndLink> items = parseDeeperPage(url);
		for (NameAndLink item : items) {
			String finalId = extractFinalId(item.link);
			if (finalId != null) {
				if (finalCategories.containsKey(finalId)) {
					Set<Category> temp = new HashSet<Category>();
					for (Category c : finalCategories.get(finalId)) {
						if (translation.isDefault)
							c.getName().setDefaultValue(item.name);
						c.getName().set(translation.lang, item.name);
						temp.add(c.getParent());
					}
					if (parents == null)
						parents = temp;
					else if (parents.size() > 1)
						parents.retainAll(temp);
				} else
					System.err.println("Final id " + finalId + " not found");
			} else {
				Category temp = translateCategory(item.name, item.link,
						translation);
				if (temp == null)
					throw new Exception(
							"translateCategory returned null category");
				if (child != null && child != temp)
					throw new Exception(
							"returned two different parent categories. Current: "
									+ child.getName().get(translation.lang)
									+ ", Returned: "
									+ temp.getName().get(translation.lang));
				if (child == null)
					child = temp;
			}
		}
		if (parents != null) {
			if (parents.size() > 1) {
				int count = items.size();
				for (Iterator<Category> it = parents.iterator(); it.hasNext();) {
					Category c = it.next();
					if (c.getSubCategories().size() != count)
						it.remove();
				}
			}
			if (parents.size() != 1)
				throw new Exception("Ambiguous category for " + name
						+ ", found " + parents.size() + " possible categories");
			child = parents.iterator().next();
		}
		String kindName = createKindName(name, translation);
		if (translation.isDefault)
			child.getName().setDefaultValue(kindName);
		child.getName().set(translation.lang, kindName);
		Category cat = child.getParent();
		if (translation.isDefault)
			cat.getName().setDefaultValue(name);
		cat.getName().set(translation.lang, name);
		return cat.getParent();
	}

	private String extractFinalId(String url) {
		if (url.startsWith("/naviga/")) {
			int start = "/naviga/".length();
			int slashPos = url.indexOf('/', start);
			return url.substring(start, slashPos);
		}
		return null;
	}

	private void parseCategory(Category parent, List<Category> parentChildren,
			String category, String url) throws Exception {
//		System.out.println("Category: " + category + ", link: " + url);
		Category cat = new Category();
		if (mainTranslation.isDefault)
			cat.getName().setDefaultValue(category);
		cat.getName().set(mainTranslation.lang, category);
		cat.setParent(parent);
		parentChildren.add(cat);
		String finalId = extractFinalId(url);

		if (finalId != null) {
			cat.setId(finalId);
			List<Category> lst = finalCategories.get(finalId);
			if (lst == null) {
				lst = new ArrayList<Category>();
				finalCategories.put(finalId, lst);
			} else {
				for (Category c : lst) {
					if (!c.getName().get(mainTranslation.lang).equals(
							cat.getName().get(mainTranslation.lang)))
						throw new Exception("Ambiguous final category "
								+ finalId);
				}
			}
			lst.add(cat);
		} else {
			List<NameAndLink> items = parseDeeperPage(url);
			if (items.size() > 0) {
				String childName = createKindName(category, mainTranslation);
				Category childCategory = new Category();
				if (mainTranslation.isDefault)
					childCategory.getName().setDefaultValue(childName);
				childCategory.getName().set(mainTranslation.lang, childName);
				cat.getSubCategories().add(childCategory);
				childCategory.setParent(cat);
				for (NameAndLink item : items) {
					parseCategory(childCategory, childCategory
							.getSubCategories(), item.name, item.link);
				}
			}
		}
	}

	private String createKindName(String name, TranslationInfo translation) {
		if (translation.firstLower)
			name = name.substring(0, 1).toLowerCase() + name.substring(1);
		return String.format(translation.kindFormat, name);
	}

	private List<NameAndLink> parseFirstPage(String url) throws Exception {
		List<NameAndLink> result = new ArrayList<NameAndLink>();
		WebRequest request = new GetMethodWebRequest(baseUrl, url);
		WebResponse response = conv.getResponse(request);
		// System.out.println("First response: " + response.getText());
		for (HTMLElement el : response.getElementsByTagName("div")) {
			if (el.getClassName().contains("category-box ")) {
				Element e = (Element) el.getNode();
				NodeList as = e.getElementsByTagName("a");
				if (as.getLength() != 1)
					throw new Exception("found a categorybox div with "
							+ as.getLength() + " as");
				Element a = (Element) as.item(0);
				String name = ((Text) a.getChildNodes().item(0)).getData()
						.trim();
				String link = a.getAttribute("href");
				result.add(new NameAndLink(name, link));
			}
		}

		return result;
	}

	private List<NameAndLink> parseDeeperPage(String url) throws Exception {
		List<NameAndLink> result = new ArrayList<NameAndLink>();
		// wait 10 seconds before request
		// long sleepTime = 5000 + (int) Math.round(10000D * Math.random());
		// System.out.println("Sleeping " + sleepTime +
		// " ms before request...");
		// Thread.sleep(sleepTime);
		WebRequest req = new GetMethodWebRequest(baseUrl, url);
		System.out.println("Requesting " + req.getURL().toString());
		WebResponse resp = conv.getResponse(req);
		HTMLElement[] ols = resp.getElementsByTagName("ol");
		if (ols.length == 0)
			throw new Exception("no ols in url " + url + "\n" + resp.getText());
		int foundCats = 0;
		for (HTMLElement el : ols) {
			if (el.getClassName().startsWith("cat_col_")) {
				Element e = (Element) el.getNode();
				NodeList lis = e.getElementsByTagName("li");
				for (int i = 0; i < lis.getLength(); i++) {
					Element li = (Element) lis.item(i);
					NodeList as = li.getElementsByTagName("a");
					if (as.getLength() != 1)
						throw new Exception("found a li with " + as.getLength()
								+ " as");
					Element a = (Element) as.item(0);
					String name = ((Text) a.getChildNodes().item(0)).getData()
							.trim();
					String link = a.getAttribute("href");
					result.add(new NameAndLink(name, link));
					foundCats++;
				}
			}
		}

		if (foundCats == 0)
			throw new Exception("No as found at " + url);
		System.out.println("Found " + foundCats + " categories at "
				+ req.getURL().toString());

		return result;
	}
}
