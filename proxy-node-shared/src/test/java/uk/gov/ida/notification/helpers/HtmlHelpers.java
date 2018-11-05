package uk.gov.ida.notification.helpers;

import com.gargoylesoftware.htmlunit.StringWebResponse;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HTMLParser;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.glassfish.jersey.internal.util.Base64;

import java.io.IOException;
import java.net.URL;

public class HtmlHelpers {
    public static String getValueFromForm(String html, String formName, String inputName) throws IOException {
        StringWebResponse webResponse = new StringWebResponse(html, new URL("http://localhost"));
        try (final WebClient client = new WebClient()) {
            client.getOptions().setJavaScriptEnabled(false);
            HtmlPage page = HTMLParser.parseHtml(webResponse, client.getCurrentWindow());
            HtmlForm samlForm = page.getFormByName(formName);
            String encodedEidasResponse = samlForm.getInputByName(inputName).getValueAttribute();
            return Base64.decodeAsString(encodedEidasResponse);
        }
    }
}
