package cn.javacoder.stockanalyze.fetch;

import cn.javacoder.stockanalyze.common.entity.Company;
import cn.javacoder.stockanalyze.common.mapper.CompanyMapper;
import cn.javacoder.stockanalyze.common.service.CompanyService;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class FetchStockCodeService {

    @Resource
    private CompanyMapper companyMapper;

    @Resource
    private CompanyService companyService;

    private String shUrl = "http://www.sse.com.cn/js/common/ssesuggestdata.js?v=2021122110";

    Pattern shPattern = Pattern.compile("(\\{.+\\})");

    public List<Company> fetchSHStockCode() throws Exception {
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHH");
        Connection conn = Jsoup.connect(shUrl + "?v="+df.format(new Date()));
        conn.method(Connection.Method.GET);
        conn.ignoreContentType(true);
        Document doc = conn.get();
        String text = doc.text();
        String arr[] = text.split(";");
        List<Company> result = new ArrayList<>();

        List<String> stockCodeList = companyService.selectAllStockCode();
        for(String str : arr) {
            if(str.contains("_t.push")) {
                Matcher matcher = shPattern.matcher(str);
                matcher.find();
                String json = matcher.group(1);
                JSONObject obj = JSON.parseObject(json);
                Company c = Company.builder()
                        .stockCode(obj.getString("val"))
                        .name(obj.getString("val2"))
                        .var3(obj.getString("val3"))
                        .build();
                if(!stockCodeList.contains(c.getStockCode())) {
                    result.add(c);
                }
            }
        }
        return result;
    }

    public void saveOrUpdate(List<Company> companies) {
        for(Company company : companies) {
            companyMapper.insert(company);
        }
    }

    public List<Company> parseSZStockCode(String fileName) throws Exception {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");

        File myFile = new File(fileName);
        FileInputStream fis = new FileInputStream(myFile);
        XSSFWorkbook myWorkBook = new XSSFWorkbook (fis);
        XSSFSheet mySheet = myWorkBook.getSheetAt(0);
        Iterator<Row> rowIterator = mySheet.iterator();

        List<String> stockCodeList = companyService.selectAllStockCode();
        List<Company> result = new ArrayList<>();
        //skip title
        rowIterator.next();

        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();
            Company c = new Company();
            c.setStockCode(row.getCell(4).getStringCellValue());
            c.setName(row.getCell(5).getStringCellValue());
            String ipoDate = row.getCell(6).getStringCellValue();
            c.setIpoDate(df.parse(ipoDate));
            c.setExchange("SZ");
            if(!stockCodeList.contains(c.getStockCode())) {
                result.add(c);
            }
        }
        return result;
    }

    public List<Company> fetchSZStockCode() throws Exception {
        String fileName="D:\\Downloads\\A股列表.xlsx";
        return parseSZStockCode(fileName);
    }

    public static void main(String[] args) throws Exception {
        FetchStockCodeService service = new FetchStockCodeService();
        service.fetchSHStockCode();
    }
}
