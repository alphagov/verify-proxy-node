package uk.gov.ida.notification.helpers;

import com.gargoylesoftware.htmlunit.StringWebResponse;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HTMLParser;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.htmlcleaner.CleanerProperties;
import org.htmlcleaner.DomSerializer;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.w3c.dom.Document;
import uk.gov.ida.Base64;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.net.URL;

import static org.assertj.core.api.Assertions.assertThat;

public class HtmlHelpers {
    public static String getValueFromForm(String html, String inputName) throws IOException {
        StringWebResponse webResponse = new StringWebResponse(html, new URL("http://localhost"));
        try (final WebClient client = new WebClient()) {
            client.getOptions().setJavaScriptEnabled(false);
            HtmlPage page = HTMLParser.parseHtml(webResponse, client.getCurrentWindow());
            HtmlForm samlForm = page.getForms().get(0);
            String encodedEidasResponse = samlForm.getInputByName(inputName).getValueAttribute();
            return Base64.decodeToString(encodedEidasResponse);
        }
    }

    public static void assertXPath(String htmlString, String xPathExpression) throws XPathExpressionException, ParserConfigurationException {
        TagNode tagNode = new HtmlCleaner().clean(htmlString);
        Document doc = new DomSerializer(new CleanerProperties()).createDOM(tagNode);
        XPath xpath = XPathFactory.newInstance().newXPath();

        assertThat((Boolean)xpath.evaluate(xPathExpression, doc, XPathConstants.BOOLEAN)).isTrue();
    }
}
