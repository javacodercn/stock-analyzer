package cn.javacoder.stockanalyze.fetch;

import cn.javacoder.stockanalyze.common.entity.Company;
import cn.javacoder.stockanalyze.common.entity.Report;
import cn.javacoder.stockanalyze.common.service.CompanyService;
import cn.javacoder.stockanalyze.common.service.ReportService;
import cn.javacoder.stockanalyze.utils.MiscUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class FetchReportService {

    /**
     * 东方财富，wap端的 研报  入口
     */
    private static  final String url = "https://np-areport-wap.eastmoney.com/api/security/rep?" +
            "client_source=wap&page_index=1&page_size=20";
    @Resource
    private ReportService reportService;

    @Resource
    private CompanyService companyService;


    public void fetchReports(String exchange) throws Exception {
        List<Company> companies = companyService.listByExchangeCode(exchange, null);
        for(Company company : companies) {
            if(reportService.exist(company.getStockCode())){
                log.info("report for {} exist , ignore", company.getStockCode());
                continue;
            }
            List<Report> reports = fetchOneCompany(company);
            if(reports == null || reports.isEmpty()){
                continue;
            }
            reportService.saveBatch(reports);
        }
    }

    private List<Report> fetchOneCompany(Company company) throws Exception {
        log.info("company:{}, name:{}" , company.getStockCode(), company.getName());
        String stockCode = "&stock_list=" + (company.getExchange().equals("SZ")? "0." : "1.") + company.getStockCode();
        Connection conn = Jsoup.connect(url + stockCode);
        conn.method(Connection.Method.GET);
        conn.header("accept", "text/html,application/json");
        conn.ignoreContentType(true);
        conn.header("user-agent", "Chrome/96.0.4664.110");
        String json = conn.execute().body();
        List<Report> reports = parse(company.getStockCode(), json);
        return reports;
    }

    public List<Report> parse(String _sCode, String json) {
        JSONObject context = JSON.parseObject(json);
        if (json == null || context.getJSONObject("data") == null || context.getJSONObject("data").getJSONArray("list") == null) {
            log.warn("result for {} is empty", _sCode);
            return null;
        }
        JSONArray arr = JSON.parseObject(json).getJSONObject("data").getJSONArray("list");
        if(arr.size() == 0){
            return new ArrayList<>();
        }
        List<Report> reports = new ArrayList<>();
        for(int i=0; i< arr.size(); i++) {
            JSONObject obj = arr.getJSONObject(i);
            Report report = Report.builder()
                    .author(concatAuthors(obj.getJSONArray("author_items")))
                    .publishDate(MiscUtils.parseDate(obj.getString("publish_time")))
                    .rateCode(obj.getString("em_rating_code"))
                    .rateName(obj.getString("em_rating_name"))
                    .source(obj.getString("source"))
                    .stockCode(_sCode)
                    .title(obj.getString("title"))
                    .aimPrice(MiscUtils.convert2intWith2P(obj.getString("aim_price")))
                    .build();
            if(report.getTitle().contains("深度报告")){
                report.setDeepReport(true);
            }
            reports.add(report);
        }
        return reports;
    }

    private String concatAuthors(JSONArray authors) {
        if(authors == null || authors.isEmpty()){
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for(int i =0; i< authors.size(); i++) {
            JSONObject obj = authors.getJSONObject(i);
            sb.append(obj.get("author_name")).append(',');
        }
        String str = sb.substring(0, sb.length() -1);
        return str.length() > 32 ? str.substring(0,32) : str;
    }
}
