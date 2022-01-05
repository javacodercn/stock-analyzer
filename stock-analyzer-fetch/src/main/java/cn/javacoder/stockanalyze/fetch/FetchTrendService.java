package cn.javacoder.stockanalyze.fetch;

import cn.javacoder.stockanalyze.common.entity.Company;
import cn.javacoder.stockanalyze.common.entity.Trade;
import cn.javacoder.stockanalyze.common.service.CompanyService;
import cn.javacoder.stockanalyze.common.service.TradeService;
import cn.javacoder.stockanalyze.common.vo.DailyInfo;
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
import java.util.Date;
import java.util.List;
import java.util.OptionalLong;

@Service
@Slf4j
public class FetchTrendService {

    private String url = "https://push2his.eastmoney.com/api/qt/stock/kline/get?" +
            "klt=101&fqt=1&lmt=66&end=20500000&iscca=1" +
            "&fields1=f1%2Cf2%2Cf3%2Cf4%2Cf5%2Cf6%2Cf7%2Cf8" +
            "&fields2=f51%2Cf52%2Cf53%2Cf54%2Cf55%2Cf56%2Cf57%2Cf58%2Cf59%2Cf60%2Cf61%2Cf62%2Cf63%2Cf64" +
            "&ut=f057cbcbce2a86e2866ab8877db1d059&forcect=1" +
            "&secid=";

    @Resource
    private CompanyService companyService;

    @Resource
    private TradeService tradeService;

    private List<DailyInfo> fetchOneCompany(Company company) throws Exception {
        String secid = "SH".equals(company.getExchange()) ? "1." : "0.";
        secid = secid + company.getStockCode();
        Connection conn = Jsoup.connect(url + secid);
        conn.method(Connection.Method.GET);
        conn.header("accept", "text/html,application/json");
        conn.ignoreContentType(true);
        conn.header("user-agent", "Chrome/96.0.4664.110");
        String json = conn.execute().body();
        List<DailyInfo> dailyInfos = parse(company.getStockCode(), json);
        return dailyInfos;
    }

    public boolean isTop(List<DailyInfo>  dailyInfos, int index) {
        DailyInfo curr = dailyInfos.get(index);
        if(curr.isRed()) {
            return false;
        }
        List<DailyInfo> subList = dailyInfos.subList(Math.max(index-7, 0), index);
        if(subList.size() < 2)
            return false;
        if(!isVolumeScale(subList)) {
            return  false;
        }

        //必须要大于平均收盘价的8%
        OptionalLong optional = subList.stream().mapToLong(DailyInfo::getClosePrice).min();
        if(!optional.isPresent()) {
            return false;
        }
        double avgPrice = optional.getAsLong();
        return Math.max(curr.getOpenPrice(),curr.getClosePrice()) > avgPrice * (1+0.05);
    }

    private boolean isVolumeScale(List<DailyInfo>  subList) {
        for(int j = 0; j< subList.size();  j++ ) {
            DailyInfo curr = subList.get(j);
            if(j+1 >= subList.size())
                continue;
            DailyInfo next = subList.get(j+1);
            if(next.getTradeVolume() > curr.getTradeVolume() *2) {
                return true;
            }
        }
        //找最大值和比平均值小的元素的平均值
        if(subList.isEmpty()) {
            return false;
        }
        double avg = subList.stream().mapToLong(DailyInfo::getTradeVolume).average().getAsDouble();
        double minAvg = subList.stream().mapToLong(DailyInfo::getTradeVolume).filter(l-> l <= avg).average().getAsDouble();
        return subList.stream().mapToLong(DailyInfo::getTradeVolume).max().getAsLong() > minAvg * 2;
    }


    private boolean isBottom(List<DailyInfo>  dailyInfos, int index) {
        DailyInfo curr = dailyInfos.get(index);
        if(!curr.isCross() && !curr.isUnderHachure()) {
            return false;
        }

        if(isVolumeScale(dailyInfos.subList(Math.max(index-7, 0), index))) {
            return false;
        }

        List<DailyInfo> seesawList = new ArrayList<>();
        seesawList.add(curr);
        int  minPrice = curr.getClosePrice();
        for(int j= index-1; j> 0; j--) {
            DailyInfo item = dailyInfos.get(j);
            /*
             * 股价在2%间波动， 量在一倍间波动，
             */
            if(Math.abs(item.getClosePrice() - curr.getOpenPrice()) < minPrice*0.04) {
                seesawList.add(item);
            } else {
                break;
            }
        }
        return seesawList.size() >= 3;
    }

    public List<DailyInfo> parse(String _scode, String content) {
        JSONObject context = JSON.parseObject(content);
        if (content == null || context.getJSONObject("data") == null || context.getJSONObject("data").getJSONArray("klines") == null) {
            log.warn("result for {} is empty", _scode);
            return null;
        }
        JSONArray arr = JSON.parseObject(content).getJSONObject("data").getJSONArray("klines");
        List<DailyInfo> list = new ArrayList<>();
        if (arr.size() == 0) {
            return list;
        }
        for(int i=0; i < arr.size(); i++) {
            String info = arr.getString(i);
            String[] daily = info.split(",");
            DailyInfo dailyInfo = DailyInfo.builder()
                    .date(MiscUtils.parseDate(daily[0]))
                    .openPrice(MiscUtils.convert2intWith2P(daily[1]))
                    .closePrice(MiscUtils.convert2intWith2P(daily[2]))
                    .highestPrice(MiscUtils.convert2intWith2P(daily[3]))
                    .lowestPrice(MiscUtils.convert2intWith2P(daily[4]))
                    .tradeVolume(MiscUtils.convert2Integer(daily[5]))
                    .tradeSum(MiscUtils.d2i(daily[6]))
                    .build();
            list.add(dailyInfo);
        }
        return list;
    }


    public  List<String> fetchTrend() throws Exception {
        List<Company> watchOn =  companyService.listNeedWatchOn();
        if(watchOn.isEmpty()) {
            return new ArrayList<>();
        }
        List<String> result = new ArrayList<>();
        for(Company company : watchOn) {
            StringBuilder sb = new StringBuilder();
            sb.append("{ company :" + company.getStockCode() + "." +  company.getName() + ":");
            List<DailyInfo> list = fetchOneCompany(company);
            List<Trade> trades = new ArrayList<>();
            Trade trade = tradeService.selectLastTrade(company.getStockCode());
            Date lastDate = new Date(System.currentTimeMillis() - (long)100 *24 * 3600 * 1000);
            if(trade != null) {
                lastDate = trade.isSellOff() ? trade.getSellOffDate() : trade.getBuyInDate();
            }
            for(int i=0; i< list.size(); i++) {
                DailyInfo curr = list.get(i);
                if(curr.getDate().before(lastDate)){
                    continue;
                }
                if(isBottom(list, i)  ) {
                    if(trade == null || trade.isSellOff()) {
                        trade = Trade.builder()
                                .stockCode(company.getStockCode())
                                .buyInDate(curr.getDate())
                                .buyInPrice(curr.getAvgPrice())
                                .build();
                        log.info("is bottom date:{}, price: {}", MiscUtils.format(curr.getDate()), curr.getClosePrice());
                    } else {
                        if(trade.getBuyInPrice() > curr.getAvgPrice()*(1+0.03)) {
                            trade.setBuyInPrice((trade.getBuyInPrice() + curr.getAvgPrice()) / 2);
                            log.info("is bottom date:{}, price: {}", MiscUtils.format(curr.getDate()), curr.getClosePrice());
                        }
                    }

                }

                if(isTop(list, i) && trade != null && !trade.isSellOff()) {
                    if(curr.getAvgPrice() > trade.getBuyInPrice() *(1+0.03)) {
                        trade.setSellOff(true);
                        trade.setSellOffDate(curr.getDate());
                        trade.setSellOffPrice(curr.getAvgPrice());
                        trades.add(trade);
                        trade = null;
                        log.info("is top date:{}, price: {}", MiscUtils.format(curr.getDate()), curr.getClosePrice());
                    }
                }
            }

            this.tradeService.saveBatch(trades);

            int total = trades.stream().filter(Trade::isSellOff)
                    .mapToInt(vo-> vo.getSellOffPrice()-vo.getBuyInPrice()).sum();
            DailyInfo temp  = list.get(list.size()-1);
            int ref = temp.getAvgPrice();
            temp  = list.get(0);
            ref = ref - temp.getAvgPrice();
            sb.append("total:" + total + ", ref:" + ref);
            sb.append("}");
            result.add(sb.toString());
        }
        return result;
    }
}
