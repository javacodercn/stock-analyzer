package cn.javacoder.stockanalyze.common.service;

import cn.javacoder.stockanalyze.common.entity.Company;
import cn.javacoder.stockanalyze.common.entity.HistoryCoreMetric;
import cn.javacoder.stockanalyze.common.mapper.CompanyMapper;
import cn.javacoder.stockanalyze.common.mapper.HistoryCoreMetricMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class HistoryCoreMetricService extends ServiceImpl<HistoryCoreMetricMapper, HistoryCoreMetric> {

    public List<HistoryCoreMetric > getByStockCode(String stockCode) {
        List<HistoryCoreMetric > list = this.list(new QueryWrapper<HistoryCoreMetric>().lambda()
        .eq(HistoryCoreMetric::getStockCode, stockCode));
        return list == null ? new ArrayList<>() : list;
    }
}
