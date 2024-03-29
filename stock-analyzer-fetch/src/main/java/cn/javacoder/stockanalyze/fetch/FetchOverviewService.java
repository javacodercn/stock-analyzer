package cn.javacoder.stockanalyze.fetch;

import cn.javacoder.stockanalyze.common.vo.Overview;
import cn.javacoder.stockanalyze.utils.MiscUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;


/**
 * 获取上证总体市值，深圳总体市值， GDP
 */
@Slf4j
public class FetchOverviewService {

    private static String shUrl = "http://query.sse.com.cn/commonQuery.do?jsonCallBack=jsonpCallback15022355" +
            "&sqlId=COMMON_SSE_SJ_GPSJ_GPSJZM_TJSJ_L&PRODUCT_NAME=%E8%82%A1%E7%A5%A8%2C%E4%B8%BB%E6%9D%BF%2C%E7%A7%91%E5%88%9B%E6%9D%BF" +
            "&type=inParams&TRADE_DATE=&_=";

    private static String szUrl = "http://www.szse.cn/api/report/ShowReport/data?" +
            "SHOWTYPE=JSON&CATALOGID=1803_after&TABKEY=tab1&random=0.3767021739561307";

    private static String gdpUrl = "https://data.stats.gov.cn/search.htm?s=GDP&m=searchdata&db=&p=0";

    /**
     * 抓取的为 https://data.stats.gov.cn/search.htm?s=GDP 页面数据
     */
    public static long fetchGdpInfo() throws IOException {
        int year = Calendar.getInstance().get(Calendar.YEAR);
        Connection conn = Jsoup.connect(gdpUrl);
        conn.method(Connection.Method.GET);
        conn.header("accept", "text/html,application/json");
        conn.ignoreContentType(true);
        conn.header("user-agent", "Chrome/96.0.4664.110");
        String json = conn.execute().body();
        String report = "cn=C01&zb=A0201&sj=" + (year-1);
        JSONArray arr = JSON.parseObject(json).getJSONArray("result");
        for(int i =0 ;i< arr.size(); i++) {
            JSONObject obj = arr.getJSONObject(i);
            if(report.equals(obj.getString("report"))){
                String str = obj.getString("data");
                log.info("url:{}, gdp:{}" ,gdpUrl, str);
                return MiscUtils.convert2intWith2P(str);
            }
        }
        return 0;
    }

    public static Overview getSZOverview() throws Exception {
        String requestUrl = szUrl + "&txtQueryDate=" + MiscUtils.formatYesterday();
        Connection conn = Jsoup.connect(requestUrl);
        conn.method(Connection.Method.GET);
        conn.header("accept", "text/html,application/json");
        conn.ignoreContentType(true);
        conn.header("user-agent", "Chrome/96.0.4664.110");

        String json = conn.execute().body();
        JSONArray ctx = JSON.parseArray(json);
        JSONArray arr = ctx.getJSONObject(0).getJSONArray("data");

        Overview overview = new Overview();
        for(int i =0 ;i < arr.size(); i++) {
            JSONObject obj = arr.getJSONObject(i);
            if(obj.getString("zbmc").contains("股票总市值")){
                String str = obj.getString("brsz");
                str = str.trim().replaceAll(",", "");
                overview.setMarketCapitalization(MiscUtils.convert2intWith2P(str));
            } else if(obj.getString("zbmc").contains("股票平均市盈率")){
                String str = obj.getString("brsz");
                overview.setAvgPe(MiscUtils.convert2intWith2P(str));
            }
        }
        return overview;
    }
    public static  Overview getSHOverview() throws Exception {
        Connection.Response response = Jsoup.connect(shUrl + "1669814045757")
                .header("Accept", "*/*")
                .header("Referer", "http://www.sse.com.cn/")
                .ignoreContentType(true)
                .method(Connection.Method.GET)
                .execute();
        String json = response.body().replaceFirst("jsonpCallback15022355\\(", "");
        json = json.substring(0, json.length() -1);
        JSONObject ctx = JSON.parseObject(json).getJSONArray("result").getJSONObject(0);
        Overview overview = new Overview();
        overview.setAvgPe(MiscUtils.convert2intWith2P(ctx.getString("AVG_PE_RATIO")));
        overview.setMarketCapitalization(MiscUtils.convert2intWith2P(ctx.getString("TOTAL_VALUE")));
        return overview;
    }

    /**
     * 10年期国债收益率
     * https://www.chinamoney.com.cn/chinese/sddsint/
     */
    public static  void fetchBond10Y() throws IOException {
        String url = "https://www.chinamoney.com.cn/r/cms/www/chinamoney/data/currency/sdds-intr-rate.json?t=" + System.currentTimeMillis();
        Connection.Response response = Jsoup.connect(url)
        .header("Accept", "application/json, text/javascript, */*; q=0.01")
        .ignoreContentType(true)
        .method(Connection.Method.POST)
                .execute();
        JSONObject ctx = JSON.parseObject(response.body());
        log.info(ctx.getJSONObject("data").getDouble("bond10Y").toString());
    }

    public static void main(String[] args) throws Exception {
        System.out.println(fetchGdpInfo());
        Overview overview = getSHOverview();
        System.out.println(overview.getAvgPe() + "--->" + overview.getMarketCapitalization());
        overview = getSZOverview();
        System.out.println(overview.getAvgPe() + "--->" + overview.getMarketCapitalization());
    }


}
