import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author H
 */
public class ReptileApplication {

    public static final String JD_BOOK_COMPUTER = "https://list.jd.com/list.html?cat=1713,3287,3797&page=1&sort=sort_rank_asc&trans=1&JL=6_0_0#J_main";

    public static void main(String[] args) {
        HttpClient client = new DefaultHttpClient();
        try {
            List<JDBook> books = new ArrayList<>();
//            for (int i = 0; i < 258; i++) {
//                int page = i + 1;
            int page = 1;
            System.out.println("当前获取第【" + page + "】页数据！");
            String url = "https://list.jd.com/list.html?cat=1713,3287,3797&page=" + page + "&sort=sort_rank_asc&trans=1&JL=6_0_0#J_main";
            books.addAll(HtmlPageFetcher.fetch(client, url));
//            }
            for (JDBook book : books) {
                System.out.println(book);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
