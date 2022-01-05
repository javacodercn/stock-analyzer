package cn.javacoder.stockanalyze.fetch;

import cn.javacoder.stockanalyze.common.entity.Company;
import cn.javacoder.stockanalyze.common.entity.HolderChangeHistory;
import cn.javacoder.stockanalyze.common.service.CompanyService;
import cn.javacoder.stockanalyze.common.service.HolderChangeHistoryService;
import cn.javacoder.stockanalyze.utils.MiscUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * 获取持有者
 */
@Service
@Slf4j
public class FetchHolderService {
    String  url = "https://datacenter.eastmoney.com/securities/api/data/v1/get?" +
            "reportName=RPT_F10_EH_FREEHOLDERS" +
            "&columns=SECUCODE%2CEND_DATE%2CHOLDER_NAME%2CHOLDER_CODE%2CHOLDER_CODE_OLD%2CHOLD_NUM%2CFREE_HOLDNUM_RATIO%2CFREE_RATIO_QOQ%2CIS_HOLDORG%2CHOLDER_RANK%2CHOLDER_NEW" +
            "&client=APP&source=SECURITIES&pageNumber=1&pageSize=200" +
            "&sr=1&st=HOLDER_RANK&v=028323234219786175";
    //(SECUCODE="002001.SZ")(END_DATE='2021-08-19')
    String FILTER_FORMAT = "(SECUCODE=\"%s\")(END_DATE='%s')";


    @Resource
    private HolderChangeHistoryService holderChangeHistoryService;

    @Resource
    private CompanyService companyService;

    private List<HolderChangeHistory> fetchHolderForOneCompany(Company company,  String endDate) throws Exception {
        log.info("fetchHolderForOneCompany stockCode:{}, endDate:{}", company.getStockCode(), endDate);
        String stockCode = company.getStockCode() + "." + company.getExchange();
        String filter = String.format(FILTER_FORMAT, stockCode, endDate);
        filter = UriUtils.encode(filter, "utf-8");
        filter ="&filter=" + filter;
        String requestUrl = url + filter;

        Connection conn = Jsoup.connect(requestUrl);
        conn.method(Connection.Method.GET);
        conn.ignoreContentType(true);
        conn.header("accept", "text/html,application/json");
        conn.header("user-agent", "Chrome/96.0.4664.110");
        conn.header("Origin","https://emh5.eastmoney.com");
        conn.header("Accept-Language", "zh-CN,zh;q=0.9");
        String content = conn.execute().body();
        return parse(stockCode, content);
    }


    public void fetchHolder(String exchange, String endDate) throws Exception {
        List<Company> companyList = companyService.listByExchangeCode(exchange);
        for(Company company : companyList) {
            if(holderChangeHistoryService.holderHistoryExists(company.getStockCode(), MiscUtils.parseDate(endDate))) {
                log.info("skip stockCode:{}, endDate:{}", company.getStockCode(), endDate);
                continue;
            }
            List<HolderChangeHistory> histories = fetchHolderForOneCompany(company, endDate);
            if(histories != null && !histories.isEmpty()) {
                holderChangeHistoryService.saveBatch(histories);
            }
        }
    }

    private List<HolderChangeHistory> parse(String _scode, String content) {
        List<HolderChangeHistory> result = new ArrayList<>();
        try {
            JSONObject context = JSON.parseObject(content);
            if(content == null || context.getJSONObject("result") == null || context.getJSONObject("result").getJSONArray("data") == null) {
                log.warn("result for {} is empty", _scode);
                return result;
            }
            JSONArray arr = JSON.parseObject(content).getJSONObject("result").getJSONArray("data");
            for (int i = 0; i < arr.size(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                String stockCode = obj.getString("SECUCODE");
                stockCode = stockCode.substring(0, stockCode.length() - 3);
                HolderChangeHistory history = HolderChangeHistory.builder()
                        .stockCode(stockCode)
                        .endDate(obj.getDate("END_DATE"))
                        .holderName(obj.getString("HOLDER_NAME"))
                        .holdCount(obj.getLongValue("HOLD_NUM"))
                        .holderRank(obj.getIntValue("HOLDER_RANK"))
                        .isHolderOrg(obj.getBooleanValue("IS_HOLDORG"))
                        .freeHolderRatio(MiscUtils.convert2intWith2P(obj.getString("FREE_HOLDNUM_RATIO")))
                        .freeRatioQoq(MiscUtils.convert2intWith2P(obj.getString("FREE_RATIO_QOQ")))
                        .incCount(MiscUtils.convert2Long(obj.getString("HOLDER_NEW")))
                        .build();
                result.add(history);
            }
        }catch (Exception e) {
            log.error(content, e);
            throw e;
        }
        return result;
    }
}
