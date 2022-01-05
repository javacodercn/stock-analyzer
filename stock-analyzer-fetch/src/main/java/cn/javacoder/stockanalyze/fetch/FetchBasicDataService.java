package cn.javacoder.stockanalyze.fetch;

import cn.javacoder.stockanalyze.common.entity.Company;
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
import java.util.List;

/**
 * 获取市盈率
 */

@Slf4j
@Service
public class FetchBasicDataService {
    //深圳为0.xxxx, 上海为1.xxxx
    String url_format = "https://datacenter.eastmoney.com/securities/api/data/v1/get?" +
            "reportName=RPT_DMSK_NEWINDICATOR&columns=SECUCODE%2CSECURITY_CODE%2CORG_CODE%2CSECURITY_NAME_ABBR%2CEPS%2CBVPS" +
            "%2CTOTAL_OPERATE_INCOME%2COPERATE_INCOME_RATIO%2CNETPROFIT%2CNETPROFIT_RATIO%2CGROSS_PROFIT_RATIO%2CNPR%2CROE" +
            "%2CDEBT%2CCAPITAL_ADEQUACY_RATIO%2CNPL%2CALLOWANCE_NPL%2CCOMMNREVE%2CCOMMNREVE_YOY%2CEARNED_PREMIUM%2CCOMPENSATE_EXPENSE" +
            "%2CSURRENDER_RATE_LIFE%2CSOLVENCY_AR%2CRESEARCH_EXPENSE%2CRSEXPENSE_RATIO%2CRESEARCH_NUM%2CRESEARCH_NUM_RATIO%2CTOTAL_SHARES" +
            "%2CA_SHARES_EQUITY%2CFREE_A_SHARES%2CPLEDGE_RATIO%2CGOODWILL%2CCDR_SHARE%2CCDR_CONVERT_RATIO%2CMARKETCAP_A%2CB_SHARES_EQUITY" +
            "%2CMARKETCAP_B%2CFREE_B_SHARES%2CB_UNIT%2CSECURITYTYPE%2CTRADEMARKET%2CDATE_TYPE%2CIS_PROFIT%2CORG_TYPE%2CIS_VOTE_DIFF" +
            "%2CLISTING_STATE%2CPE_DYNAMIC_SOURCE%2CPB_NOTICE_SOURCE%2CEPS_SOURCE%2CBVPS_SOURCE%2CTOI_SOURCE%2COIR_SOURCE%2CNETPROFIT_SOURCE" +
            "%2CNETPROFIT_RATIO_SOURCE%2CGPR_SOURCE%2CNPR_SOURCE%2CROE_SOURCE%2CDEBT_SOURCE%2CNPL_SOURCE%2CALLOWANCE_NPL_SOURCE%2CCAR_SOURCE" +
            "%2CCOMMNREVE_SOURCE%2CCOMMNREVE_YOY_SOURCE%2CEARNED_PREMIUM_SOURCE%2CCOMPENSATE_EXPENSE_SOURCE%2CSRL_SOURCE%2CSOLVENCY_AR_SOURCE" +
            "%2CRESEARCH_EXPENSE_SOURCE%2CRSEXPENSE_RATIO_SOURCE%2CRESEARCH_NUM_SOURCE%2CRNR_SOURCE%2CTOTAL_SHARES_SOURCE%2CTMC_SOURCE" +
            "%2CCDR_SHARE_SOURCE%2CCCR_SOURCE%2CASE_SOURCE%2CFAS_SOURCE%2CMCFA_SOURCE%2CPLEDGE_RATIO_SOURCE%2CMCA_SOURCE%2CGOODWILL_SOURCE" +
            "%2CBSE_SOURCE%2CMCB_SOURCE%2CFBS_SOURCE%2CMCFB_SOURCE%2CEQUITY_NEW_REPORT&quoteColumns=f9~01~SECURITY_CODE~PE_DYNAMIC" +
            "%2Cf23~01~SECURITY_CODE~PB_NEW_NOTICE%2Cf20~01~SECURITY_CODE~TOTAL_MARKET_CAP%2Cf21~01~SECURITY_CODE~MARKETCAP_FREE_B" +
            "%2Cf114~01~SECURITY_CODE~PE_STATIC%2Cf115~01~SECURITY_CODE~PE_TTM%2Cf21~01~SECURITY_CODE~MARKETCAP_FREE_A%2Cf2~01~SECURITY_CODE~f2" +
            "%2Cf18~01~SECURITY_CODE~f18&pageNumber=1&pageSize=200&v=07571448539876571";

    @Resource
    private CompanyService companyService;


    public void fetchBasicForAllCompanies() throws Exception {
        List<Company> list = companyService.listByExchangeCode("SH");
        for(Company company : list) {
            if(null != getBasic(company)) {
                companyService.updateById(company);
            }
        }

        list = companyService.listByExchangeCode("SZ");
        for(Company company : list) {
            if(null != getBasic(company)) {
                companyService.updateById(company);
            }
        }
    }

    public Company getBasic(Company company) throws Exception {
        String secid= company.getStockCode() + "." + company.getExchange();
        String filter = "&filter=(SECUCODE%3D%22" + secid + "%22)";
        Connection conn = Jsoup.connect(url_format + filter);
        conn.method(Connection.Method.GET);
        conn.header("accept", "text/html,application/json");
        conn.ignoreContentType(true);
        conn.header("user-agent", "Chrome/96.0.4664.110");
        String json = conn.execute().body();
       company = parse(company, secid, json);
       if(company == null) {
           return null;
       }
        log.debug("stockCode:{}, name:{}, pe:{}, roe {}, marketCapital:{}",
                company.getStockCode(), company.getName(), company.getPe(), company.getRoe(), company.getMarketCapital());
        //随机休眠一段时间，防止被被人墙
        Thread.sleep(System.currentTimeMillis()%50);
        return company;
    }

    public Company parse(Company company, String _scode, String content) {
        JSONObject context = JSON.parseObject(content);
        if (content == null || context.getJSONObject("result") == null || context.getJSONObject("result").getJSONArray("data") == null) {
            log.warn("result for {} is empty", _scode);
            return null;
        }
        JSONArray arr = JSON.parseObject(content).getJSONObject("result").getJSONArray("data");
        if(arr.size() == 0){
            return null;
        }
        JSONObject obj = arr.getJSONObject(0);
        company.setPe(MiscUtils.convert2intWith2P(obj.getString("PE_DYNAMIC")));
        company.setRoe(MiscUtils.convert2intWith2P(obj.getString("ROE")));
        company.setDebt(MiscUtils.convert2intWith2P(obj.getString("DEBT")));
        company.setGrossProfitRatio(MiscUtils.convert2intWith2P(obj.getString("GROSS_PROFIT_RATIO")));
        company.setNpr(MiscUtils.convert2intWith2P(obj.getString("NPR")));
        company.setShares(MiscUtils.convert2Million(obj.getString("FREE_A_SHARES")));
        company.setMarketCapital(MiscUtils.convert2Million(obj.getString("MARKETCAP_FREE_A")));
        return company;
    }

}
