package cn.javacoder.stockanalyze.fetch;

import cn.javacoder.stockanalyze.common.entity.Company;
import cn.javacoder.stockanalyze.common.entity.HistoryCoreMetric;
import cn.javacoder.stockanalyze.common.service.CompanyService;
import cn.javacoder.stockanalyze.common.service.HistoryCoreMetricService;
import cn.javacoder.stockanalyze.utils.MiscUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class FetchCoreMetricService {

    private String url = "https://datacenter.eastmoney.com/securities/api/data/get?" +
            "client=APP&source=HSF10&type=RPT_F10_FINANCE_MAINFINADATA" +
            "&sty=APP_F10_MAINFINADATA&ps=5&sr=-1&st=REPORT_DATE";
    @Resource
    private HistoryCoreMetricService historyCoreMetricService;

    @Resource
    private CompanyService companyService;


    public void fetchCoreMetric(String exchange) throws Exception {
        List<Company> companies = companyService.listByExchangeCode(exchange, null);
        for(Company company: companies) {
            List<HistoryCoreMetric> exists = historyCoreMetricService.getByStockCode(company.getStockCode());
            List<HistoryCoreMetric> list = fetchCoreMetricForOneCompany(company);
            if(list != null && !list.isEmpty()) {
                list.removeIf(exists::contains);
                if(!list.isEmpty()) {
                    historyCoreMetricService.saveBatch(list);
                }
            }
        }
    }

    public List<HistoryCoreMetric> fetchCoreMetricForOneCompany(Company company) throws Exception {
        log.info("process stockCode:{}", company.getStockCode());
        String stockCode = company.getStockCode() + "." + company.getExchange();
        String filter="&filter=(SECUCODE%3D%22" + stockCode + "%22)(REPORT_TYPE%3D%22%E5%B9%B4%E6%8A%A5%22)";
        Connection conn = Jsoup.connect(url + filter);
        conn.method(Connection.Method.GET);
        conn.ignoreContentType(true);
        conn.header("accept", "text/html,application/json");
        conn.header("user-agent", "Chrome/96.0.4664.110");
        conn.header("Origin","https://emh5.eastmoney.com");
        conn.header("Accept-Language", "zh-CN,zh;q=0.9");
        String content = conn.execute().body();
        return parse(company.getStockCode(), content);
    }

    private List<HistoryCoreMetric> parse(String _scode, String content) {
        List<HistoryCoreMetric> result = new ArrayList<>();
        JSONObject context = JSON.parseObject(content);
        if(content == null || context.getJSONObject("result") == null || context.getJSONObject("result").getJSONArray("data") == null) {
            log.warn("result for {} is empty", _scode);
            return result;
        }
        JSONArray arr = JSON.parseObject(content).getJSONObject("result").getJSONArray("data");
        for(int i=0; i< arr.size(); i++) {
            JSONObject obj = arr.getJSONObject(i);
            String stockCode = obj.getString("SECUCODE");
            stockCode = stockCode.substring(0, stockCode.length() - 3);
            HistoryCoreMetric metric = HistoryCoreMetric.builder()
                    .stockCode(stockCode)
                    .endDate(obj.getDate("REPORT_DATE"))
                    .roekcjq(MiscUtils.convert2intWith2P(obj.getString("ROEKCJQ")))
                    .zcfzl(MiscUtils.convert2intWith2P(obj.getString("ZCFZL")))
                    .build();
            result.add(metric);
        }
        return result;
    }
}
