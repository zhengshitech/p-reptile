import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

/**
 * @author H
 */
public class JDBookParser {
    public static List<JDBook> getJDBook(String html) {
        List<JDBook> books = new ArrayList<>(16);
        Document doc = Jsoup.parse(html);
        Elements elements = doc.select("ul[class=gl-warp clearfix]")
                .select("li[class=gl-item]");
        for (Element ele : elements) {
            Elements bookEle = ele.select("div[class=gl-i-wrap j-sku-item]");
            if (bookEle.size()==1) {
                Element element = bookEle.get(0);
                books.add(element2book(element));
            } if (bookEle.size()==0){
                //套装书
                Elements booksEle = ele.select("div[class=gl-i-wrap]")
                        .select("div[class=gl-i-tab]")
                        .select("div[class=gl-i-tab-content]");
                for (int i = 0; i < booksEle.size(); i++) {
                    Element element = booksEle.get(i);
                    Elements children = element.children();
                    for (Element singleEle : children) {
                        books.add(element2book(singleEle));
                    }
                }
            }
        }
        //返回数据
        return books;

    }

    private static JDBook element2book(Element bookEle) {
        String bookID = bookEle.attr("data-sku");
        String bookPrice = bookEle.select("div[class=p-price]").select("strong[class=J_price]").select("i").text();
        String bookName = bookEle.select("div[class=p-name]").select("em").text();
        return new JDBook(bookID, bookName, bookPrice);
    }
}
