import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author H
 */
public class HtmlPageFetcher {
    public static List<JDBook> fetch(HttpClient client, String url) throws IOException {
        List<JDBook> books = new ArrayList<>(16);
        //获取网站响应的html
        HttpResponse response = getRawHtml(client, url);
        //获取响应状态码
        int status = response.getStatusLine().getStatusCode();
        //如果状态响应码为200，则获取html实体内容或者json文件
        HttpEntity httpEntity = response.getEntity();
        if (status ==  HttpStatus.SC_OK) {
            String entityString = EntityUtils.toString(httpEntity, "utf-8");
            books.addAll(JDBookParser.getJDBook(entityString));
            EntityUtils.consume(httpEntity);
        } else {
            //否则，消耗掉实体
            EntityUtils.consume(httpEntity);
        }
        return books;
    }


    public static HttpResponse getRawHtml(HttpClient client, String personalUrl) {
        //获取响应文件，即html，采用get方法获取响应数据
        HttpGet getMethod = new HttpGet(personalUrl);
        getMethod.addHeader("authority","list.jd.com");
        getMethod.addHeader("scheme","https");
        getMethod.addHeader("accept","text/javascript, application/javascript, application/ecmascript, application/x-ecmascript, */*; q=0.01");
//        getMethod.addHeader("accept-encoding","gzip, deflate, br");
        getMethod.addHeader("accept-language","zh-CN,zh;q=0.9,en;q=0.8");
        getMethod.addHeader("cookie","pinId=PtFc4_gZkHqYb9Bk97dkzLV9-x-f3wj7; __jdu=739100586; TrackID=121_rQKcQc-ilBXEJXqRISv-2LQSu3iywUAyycv8q6BHFadkZlp-LNsKUyj2UPVh9BylpcJHSY3MVn2AKN-7szCDUHS8zxgHPRUSpGtcH3iNo2_-SsEDHM8LQid5marp_; shshshfpa=3a9fc695-42fd-ec02-5440-64ff2e9ee2da-1531137445; shshshfpb=1b72c285b76a24d94ad485001f3333b3f82abe03ea64a26f85abbc02c1; __jdc=122270672; __jdv=122270672|direct|-|none|-|1538109296489; PCSYCityID=1930; ipLoc-djd=1-72-4137-0; areaId=1; listck=640a5712b818b1eed26cd60730bccfae; shshshfp=707f100f752791e8acce9222836257ed; ipLocation=%u5317%u4EAC; 3AB9D23F7A4B3C9B=UN3DQJBUHOAVQJL5J7RN26EMAEGZEY6RGAAWT6TOTMJOXOLTONNQDJJLSNK5M5MGODUWSGI54J2WXAUEGU2SZO4HQU; __jda=122270672.739100586.1472284842.1538114686.1538117273.42; __jdb=122270672.3.739100586|42.1538117273");
        getMethod.addHeader("user-agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/69.0.3497.100 Safari/537.36");
        getMethod.addHeader("x-requested-with","XMLHttpRequest");
        HttpResponse response = new BasicHttpResponse(HttpVersion.HTTP_1_1, HttpStatus.SC_OK, "OK");
        try {
            //执行get方法
            response = client.execute(getMethod);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response;
    }
}
