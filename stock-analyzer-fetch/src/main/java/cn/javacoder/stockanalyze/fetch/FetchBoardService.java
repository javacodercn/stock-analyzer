package cn.javacoder.stockanalyze.fetch;

import cn.javacoder.stockanalyze.common.entity.Board;
import cn.javacoder.stockanalyze.common.entity.Company;
import cn.javacoder.stockanalyze.common.service.BoardService;
import cn.javacoder.stockanalyze.common.service.CompanyService;
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
public class FetchBoardService {

    private String url = "https://datacenter.eastmoney.com/securities/api/data/v1/get?" +
            "reportName=RPT_F10_CORETHEME_BOARDTYPE" +
            "&columns=SECUCODE%2CSECURITY_CODE%2CSECURITY_NAME_ABBR%2CNEW_BOARD_CODE%2CBOARD_NAME%2CSELECTED_BOARD_REASON%2CIS_PRECISE%2CBOARD_RANK%2CBOARD_YIELD%2CDERIVE_BOARD_CODE" +
            "&quoteColumns=f3~05~NEW_BOARD_CODE~BOARD_YIELD&client=APP&source=SECURITIES&pageNumber=1&pageSize=200" +
            "&sortTypes=1&sortColumns=BOARD_RANK&random=0.2633053930239022&v=046824988742590623";
    @Resource
    private BoardService boardService;

    @Resource
    private CompanyService companyService;

    public Board fetchBoardForOneCompany(Company company) throws Exception {
        String stockCode = company.getStockCode() + '.' + company.getExchange();
        String filter = "&filter=(SECUCODE%3D%22" + stockCode + "%22)(IS_PRECISE%3D%221%22)";
        Connection conn = Jsoup.connect(url + filter);
        conn.method(Connection.Method.GET);
        conn.ignoreContentType(true);
        conn.header("accept", "text/html,application/json");
        conn.header("user-agent", "Chrome/96.0.4664.110");
        conn.header("Origin","https://emh5.eastmoney.com");
        conn.header("Accept-Language", "zh-CN,zh;q=0.9");
        String content = conn.execute().body();
        return parse(stockCode, content);
    }

    public void fetchBoard(String exchange) throws Exception {
        List<Company> companyList = companyService.listByExchangeCode(exchange, null);
        for(Company company : companyList) {
            if(boardService.exist(company.getStockCode())){
                continue;
            }

            Board board = fetchBoardForOneCompany(company);
            if(board == null) {
                continue;
            }
            if(board.isLeadingPosition() && !company.isLeadingPosition()){
                company.setLeadingPosition(true);
                companyService.updateById(company);
            }

            boardService.save(board);
        }
    }

    private boolean leadingPosition(String str) {
        if(str != null && str.contains("龙头")) {
            return true;
        }
        return false;
    }

    public Board parse(String _scode, String content) {
        JSONObject context = JSON.parseObject(content);
        if(content == null || context.getJSONObject("result") == null || context.getJSONObject("result").getJSONArray("data") == null) {
            log.warn("result for {} is empty", _scode);
            return null;
        }
        JSONArray arr = JSON.parseObject(content).getJSONObject("result").getJSONArray("data");

        Board board = new Board();
        board.setStockCode(_scode.substring(0, _scode.length()-3));

        List<JSONObject> selected = new ArrayList<>();
        for(int i =0;i< arr.size(); i++) {
            JSONObject obj = arr.getJSONObject(i);
            String desc = obj.getString("SELECTED_BOARD_REASON");
            if(leadingPosition(desc)){
                board.setLeadingPosition(true);
                selected.add(obj);
            }
        }
        if(selected.size() < 3) {
            int i =0;
            while(selected.size() < 3 && i< arr.size()) {
                selected.add(arr.getJSONObject(i));
                i++;
            }
        }

        JSONObject obj = selected.get(0);
        board.setBoard1(obj.getString("BOARD_NAME"));
        String desc = obj.getString("SELECTED_BOARD_REASON");
        if(desc != null && desc.length() > 512) {
            desc = desc.substring(0, 512);
        }
        board.setBoard1Desc(desc);

        if(selected.size() > 1) {
            obj = selected.get(1);
            board.setBoard2(obj.getString("BOARD_NAME"));
            desc = obj.getString("SELECTED_BOARD_REASON");
            if (desc != null && desc.length() > 512) {
                desc = desc.substring(0, 512);
            }
            board.setBoard2Desc(desc);
        }

        if(selected.size() > 2) {
            obj = selected.get(2);
            board.setBoard3(obj.getString("BOARD_NAME"));
            desc = obj.getString("SELECTED_BOARD_REASON");
            if (desc != null && desc.length() > 512) {
                desc = desc.substring(0, 512);
            }
            board.setBoard3Desc(desc);
        }
        return board;
    }
}
