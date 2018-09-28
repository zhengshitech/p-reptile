import com.alibaba.fastjson.JSON;
import com.google.common.base.Stopwatch;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author H
 */
public class ReptileApplication {
    public static final int TOTAL_PAGE = 258;
    private final static Logger logger = LoggerFactory.getLogger(ReptileApplication.class);

    public static void main(String[] args) {
        Stopwatch stopwatch = Stopwatch.createStarted();
        getBookInfoFromJD();
        stopwatch.stop();
        logger.info("数据获取完成,总共用时:{}秒", stopwatch.elapsed(TimeUnit.SECONDS));
    }

    private static void getBookInfoFromJD() {
        HttpClient client = HttpClients.createDefault();
        List<JDBook> books = new ArrayList<>();
        for (int i = 0; i < TOTAL_PAGE; i++) {
            int page = i + 1;
            logger.info("当前获取第【{}】页数据！", page);
            String url = "https://list.jd.com/list.html?cat=1713,3287,3797&page=" + page + "&sort=sort_rank_asc&trans=1&JL=6_0_0#J_main";
            books.addAll(HtmlPageFetcher.fetch(client, url));
        }
        List<String> skuIDs = books.stream().map(JDBook::getBookID).distinct().collect(Collectors.toList());
        Map<String, JDPrice> pricesMap = getPricesMap(client, skuIDs);
        int noPriceBook = 0;
        for (JDBook book : books) {
            JDPrice jdPrice = pricesMap.get(Constant.PRICE_ID_PREFIX + book.getBookID());
            if (null == jdPrice) {
                noPriceBook++;
                continue;
            }
            book.setBookPrice(jdPrice.getOp());
        }
        logger.info("总共查询到的图书数量为:{}", books.size());
        logger.info("总共查询价格数量为:{}", pricesMap.size());
        logger.info("没有查询到价格的图书数量:{}", noPriceBook);
//        for (JDBook book : books) {
//            logger.info(book.toString());
//        }
    }

    private static Map<String, JDPrice> getPricesMap(HttpClient client, List<String> skuIDs) {
        if (null == skuIDs) {
            return new HashMap<>(0);
        }
        int size = skuIDs.size();
        logger.info("开始获取总共{}个商品的价格信息!", size);
        Map<String, JDPrice> map = new HashMap<>(size);
        Random random = new Random();
        if (size > 0) {
            int index = 0;
            while (true) {
                boolean breakFlag = false;
                int r = random.nextInt(30);
                if (r < 9) {
                    //too small
                    continue;
                }
                logger.info("已经获取:{}个商品价格，正在获取另外：{}个商品价格数据", index, r);
                int toIndex = index + r;
                if (toIndex >= size) {
                    toIndex = size;
                    breakFlag = true;
                }
                List<String> ids = skuIDs.subList(index, toIndex);
                String queryString = ids.stream().map(x -> Constant.PRICE_ID_PREFIX + x).collect(Collectors.joining(","));
                String url = "http://p.3.cn/prices/mgets?skuIds=" + queryString + "&type=1&_" + System.nanoTime();
                List<JDPrice> prices = getPrices(client, url, 3);
                map.putAll(prices.stream().collect(Collectors.toMap(JDPrice::getId, Function.identity())));
                if (breakFlag) {
                    logger.info("商品价格获取完毕，总共获取价格数量:{}个", toIndex);
                    break;
                }
                index = toIndex;
            }
        }
        return map;
    }

    private static List<JDPrice> getPrices(HttpClient client, String url, int retryTimes) {
        HttpEntity httpEntity = null;
        try {
            HttpResponse response = HttpUtil.httpGetRequest(client, url);
            int status = response.getStatusLine().getStatusCode();
            httpEntity = response.getEntity();
            if (status == HttpStatus.SC_OK) {
                String entityString = EntityUtils.toString(httpEntity, Constant.UTF_8);
                try {
                    return JSON.parseArray(entityString, JDPrice.class);
                } catch (Exception e) {
                    e.printStackTrace();
                    logger.info("获取价格失败,RESONSE STRING:{}", entityString);
                    if (retryTimes >= 1) {
                        return getPrices(client, url, retryTimes - 1);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (null != httpEntity) {
                try {
                    EntityUtils.consume(httpEntity);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return new ArrayList<>(0);
    }
}
