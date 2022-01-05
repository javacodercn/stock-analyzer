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


/**
 * 获取上证总体市值，深圳总体市值， GDP
 */
@Slf4j
public class FetchOverviewService {

    private String shUrl = "http://www.sse.com.cn/market/stockdata/statistic/";

    private String szUrl = "http://www.szse.cn/api/report/ShowReport/data?" +
            "SHOWTYPE=JSON&CATALOGID=1803_after&TABKEY=tab1&random=0.3767021739561307";

    private String gdpUrl = "https://data.stats.gov.cn/search.htm?s=GDP&m=searchdata&db=&p=0";

    /**
     * 抓取的为 https://data.stats.gov.cn/search.htm?s=GDP 页面数据
     */
    public long fetchGdpInfo() throws IOException {
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

    public Overview getSZOverview() throws Exception {
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
    public Overview getSHOverview() throws Exception {
        Connection conn = Jsoup.connect(shUrl);
        Document doc = conn.get();
        Elements elements = doc.select(".sse_home_in_table2").get(0).select("table>tbody>tr>td");
        Overview overview = new Overview();
        if(elements != null && !elements.isEmpty()) {
            for(Element td : elements) {
                if(td.html().contains("总市值/亿元")) {
                    String str = td.child(1).text();
                    overview.setMarketCapitalization(MiscUtils.convert2intWith2P(str));
                } else if(td.html().contains("平均市盈率/倍")) {
                    String str = td.child(1).text();
                    overview.setAvgPe(MiscUtils.convert2intWith2P(str));
                }
            }
        }
        return overview;
    }

    public static void main(String[] args) throws Exception {
        FetchOverviewService service = new FetchOverviewService();
        Overview sz = service.getSZOverview();
        Overview sh = service.getSHOverview();
        log.info("sh:{}, sz:{}", sh, sz);
        log.info("gdp:{} sh+sz:{}", service.fetchGdpInfo(), sh.getMarketCapitalization() + sz.getMarketCapitalization()) ;
    }


}
