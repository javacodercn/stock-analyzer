package cn.javacoder.stockanalyze.controller;

import cn.javacoder.stockanalyze.common.entity.Company;
import cn.javacoder.stockanalyze.fetch.*;
import com.alibaba.druid.util.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.jws.WebParam;
import java.util.Arrays;
import java.util.List;

@Controller
public class FetchController {

    @Resource
    private FetchStockCodeService service;

    @Resource
    private FetchBasicDataService fetchBasicDataService;

    @Resource
    private FetchHolderService fetchHolderService;

    @Resource
    private FetchCoreMetricService fetchCoreMetricService;

    @Resource
    private FetchBoardService fetchBoardService;


    @Resource
    private FetchReportService fetchReportService;

    @Resource
    private FetchBusinessAnalysisService fetchBusinessAnalysisService;

    @Resource
    private FetchTrendService fetchTrendService;

    @RequestMapping("/fetch")
    @ResponseBody
    public String fetch() throws Exception {
        List<Company> companyList = service.fetchSHStockCode();
        service.saveOrUpdate(companyList);
        companyList = service.fetchSZStockCode();
        service.saveOrUpdate(companyList);
        return "companyList.size:" + companyList.size();
    }

    @RequestMapping("/fetchBasic")
    @ResponseBody
    public String fetchBasic() throws Exception {
        fetchBasicDataService.fetchBasicForAllCompanies();
        return "fetchBasic finished:";
    }


    @RequestMapping("/fetchHolder")
    @ResponseBody
    public String fetchHolder(@RequestParam("endDate") String endDate,
                              @RequestParam("exchange") String exchange) throws Exception {
        if(StringUtils.isEmpty(endDate) || StringUtils.isEmpty(exchange)) {
            return "endDate  or exchange is empty";
        }
        fetchHolderService.fetchHolder(exchange, endDate);
        return "fetchHolder finished:";
    }


    @RequestMapping("/fetchCoreMetric")
    @ResponseBody
    public String fetchCoreMetric(@RequestParam("exchange") String exchange) throws Exception {
        if(StringUtils.isEmpty(exchange)) {
            return " exchange is empty";
        }
        fetchCoreMetricService.fetchCoreMetric(exchange);
        return "fetchCoreMetric finished:";
    }



    @RequestMapping("/fetchBoard")
    @ResponseBody
    public String fetchBoard(@RequestParam("exchange") String exchange) throws Exception {
        if(StringUtils.isEmpty(exchange)) {
            return " exchange is empty";
        }
        fetchBoardService.fetchBoard(exchange);
        return "fetchBoard finished:";
    }


    @RequestMapping("/fetchReport")
    @ResponseBody
    public String fetchReport(@RequestParam("exchange") String exchange) throws Exception {
        if(StringUtils.isEmpty(exchange)) {
            return " exchange is empty";
        }
        fetchReportService.fetchReports(exchange);
        return "fetchReport finished:";
    }

    @RequestMapping("/fetchBusinessAnalysis")
    @ResponseBody
    public String fetchBusinessAnalysis(@RequestParam("exchange") String exchange) throws Exception {
        if(StringUtils.isEmpty(exchange)) {
            return " exchange is empty";
        }
        fetchBusinessAnalysisService.fetch(exchange);
        return "fetchReport finished:";
    }


    @RequestMapping("/fetchTrend")
    public String fetchTrend(Model model) throws Exception {
        List<String> result = fetchTrendService.fetchTrend();
        model.addAttribute("list", result);
        return "trendList";
    }


    @RequestMapping("/forecastRally")
    @ResponseBody
    public Object forecastRally() throws Exception {
        List<String> result = fetchTrendService.forecastRally();
        return result;
    }

}
