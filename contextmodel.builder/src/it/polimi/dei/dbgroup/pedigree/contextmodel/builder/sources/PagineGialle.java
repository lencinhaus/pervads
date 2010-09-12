package it.polimi.dei.dbgroup.pedigree.contextmodel.builder.sources;

import it.polimi.dei.dbgroup.pedigree.contextmodel.builder.Category;
import it.polimi.dei.dbgroup.pedigree.contextmodel.builder.ICategorySource;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

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
	private URL baseUrl;
	private WebConversation conv;

	@Override
	public String getName() {
		return "pagine_gialle";
	}

	@Override
	public List<Category> getCategories() throws Exception {
		baseUrl = new URL("http://www.paginegialle.it/cat/");
		conv = new WebConversation();

		List<Category> categories = new ArrayList<Category>();
		HttpUnitOptions.setExceptionsThrownOnScriptError(false);
		WebRequest request = new GetMethodWebRequest(baseUrl,
				"pagine_gialle_naviga.html");
		WebResponse response = conv.getResponse(request);
		System.out.println("First response: " + response.getText());
		for (HTMLElement el : response.getElementsByTagName("div")) {
			if (el.getClassName().contains("category-box ")) {
				Element e = (Element) el.getNode();
				NodeList as = e.getElementsByTagName("a");
				if (as.getLength() != 1)
					throw new Exception("found a categorybox div with "
							+ as.getLength() + " as");
				Element a = (Element) as.item(0);
				String category = ((Text) a.getChildNodes().item(0)).getData()
						.trim();
				String link = a.getAttribute("href");
				parseCategory(categories, category, link);
			}
		}

		return categories;
	}

	private void parseCategory(List<Category> parent, String category,
			String nextUrl) throws Exception {
		System.out.println("Category: " + category + ", link: " + nextUrl);
		Category cat = new Category(category);
		cat.setLang("it");
		parent.add(cat);
		if (!nextUrl.startsWith("/naviga/")) {
			// wait 10 seconds before request
			long sleepTime = 5000 + (int) Math.round(10000D * Math.random());
			System.out.println("Sleeping " + sleepTime
					+ " ms before request...");
			Thread.sleep(sleepTime);
			WebRequest req = new GetMethodWebRequest(baseUrl, nextUrl);
			System.out.println("Requesting " + req.getURL().toString());
			WebResponse resp = conv.getResponse(req);
			HTMLElement[] ols = resp.getElementsByTagName("ol");
			if (ols.length == 0)
				throw new Exception("no ols in url " + nextUrl + "\n"
						+ resp.getText());
			int foundCats = 0;
			for (HTMLElement el : ols) {
				if (el.getClassName().startsWith("cat_col_")) {
					Element e = (Element) el.getNode();
					NodeList lis = e.getElementsByTagName("li");
					for (int i = 0; i < lis.getLength(); i++) {
						Element li = (Element) lis.item(i);
						NodeList as = li.getElementsByTagName("a");
						if (as.getLength() != 1)
							throw new Exception("found a li with "
									+ as.getLength() + " as");
						Element a = (Element) as.item(0);
						String subCat = ((Text) a.getChildNodes().item(0))
								.getData().trim();
						String subLink = a.getAttribute("href");
						Category childCategory = new Category("Tipo di " + cat.getName().substring(0, 1).toLowerCase() + cat.getName().substring(1));
						childCategory.setLang("it");
						cat.getSubCategories().add(childCategory);
						parseCategory(childCategory.getSubCategories(), subCat, subLink);
						foundCats++;
					}
				}
			}

			if (foundCats == 0)
				throw new Exception("No as found at " + nextUrl);
			System.out.println("Found " + foundCats + " categories at "
					+ req.getURL().toString());
		}
	}
}
