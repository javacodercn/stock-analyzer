package cn.javacoder.stockanalyze.fetch;

import cn.javacoder.stockanalyze.common.entity.BusinessAnalysis;
import cn.javacoder.stockanalyze.common.entity.Company;
import cn.javacoder.stockanalyze.common.service.BusinessAnalysisService;
import cn.javacoder.stockanalyze.common.service.CompanyService;
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
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Stream;

/**
 * 从东方财富 wap 经营分析抓取
 */
@Service
@Slf4j
public class FetchBusinessAnalysisService {

    private String url = "https://emweb.securities.eastmoney.com/PC_HSF10/BusinessAnalysis/PageAjax?code=";

    @Resource
    private CompanyService companyService;

    @Resource
    private BusinessAnalysisService businessAnalysisService;


    public void fetch(String exchange) throws Exception {
        List<Company> companies = companyService.listByExchangeCode(exchange, null);
        for(Company company : companies) {
            if (businessAnalysisService.exist(company.getStockCode())) {
                continue;
            }
            List<BusinessAnalysis> list = fetchOneCompany(company);
            if(list != null) {
                businessAnalysisService.saveBatch(list);
            }
        }

    }

    public List<BusinessAnalysis> fetchOneCompany(Company company)  {
        log.info("process " + company.getStockCode());
        try {
            Connection conn = Jsoup.connect(url + company.getExchange() + company.getStockCode());
            conn.method(Connection.Method.GET);
            conn.header("X-Requested-With", "XMLHttpRequest");
            conn.header("accept", "text/html,application/json");
            conn.ignoreContentType(true);
            conn.header("user-agent", "Chrome/96.0.4664.110");
            String json = conn.execute().body();
            return parse(company.getStockCode(), json);
        }catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    private List<BusinessAnalysis> parse(String _scode, String content) {
        List<BusinessAnalysis> result = new ArrayList<>();
        JSONObject context = JSON.parseObject(content);
        //zygcfx 主营构成分析
        if (content == null || context.getJSONArray("zygcfx") == null ) {
            log.warn("result for {} is empty", _scode);
            return result;
        }
        JSONArray arr = JSON.parseObject(content).getJSONArray("zygcfx");
        if(arr.size() == 0){
            return result;
        }
        BusinessAnalysis cmp = null;
        for(int i =0 ; i< arr.size(); i++) {
            JSONObject obj = arr.getJSONObject(i);
            String itemName = obj.getString("ITEM_NAME");
            if(itemName.length() > 60) {
                itemName = itemName.substring(0, 60);
            }
            BusinessAnalysis analysis = BusinessAnalysis.builder()
                    .stockCode(obj.getString("SECURITY_CODE"))
                    .reportDate(MiscUtils.parseDate(obj.getString("REPORT_DATE")))
                    .grossProfitRatio(MiscUtils.d2p(obj.getString("GROSS_RPOFIT_RATIO")))
                    .itemName(itemName)
                    .mainBusinessCost(MiscUtils.convert2Million(obj.getString("MAIN_BUSINESS_COST")))
                    .mainBusinessIncome(MiscUtils.convert2Million(obj.getString("MAIN_BUSINESS_INCOME")))
                    .mainopType(obj.getIntValue("MAINOP_TYPE"))
                    .mbcRatio(MiscUtils.d2p(obj.getString("MBC_RATIO")))
                    .mbiRatio(MiscUtils.d2p(obj.getString("MBI_RATIO")))
                    .mbpRatio(MiscUtils.d2p(obj.getString("MBR_RATIO")))
                    .irank(obj.getIntValue("RANK"))
                    .build();
            if(cmp == null) {
                cmp = analysis;
            } else if(cmp.getReportDate().after(analysis.getReportDate()) || cmp.getMainopType() != analysis.getMainopType()) {
                break;
            }
            result.add(analysis);
        }
        return result;
    }
}
