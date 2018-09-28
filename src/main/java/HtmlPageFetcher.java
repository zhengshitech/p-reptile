import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author H
 */
public class HtmlPageFetcher {

    public static List<JDBook> fetch(HttpClient client, String url) {
        List<JDBook> books = new ArrayList<>(16);
        HttpResponse response = HttpUtil.httpGetRequest(client, url);
        int status = response.getStatusLine().getStatusCode();
        HttpEntity httpEntity = response.getEntity();
        try {
            if (status == HttpStatus.SC_OK) {
                String entityString = EntityUtils.toString(httpEntity, Constant.UTF_8);
                books.addAll(JDBookParser.getJDBook(entityString));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (httpEntity != null) {
                try {
                    EntityUtils.consume(httpEntity);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return books;
    }


}
