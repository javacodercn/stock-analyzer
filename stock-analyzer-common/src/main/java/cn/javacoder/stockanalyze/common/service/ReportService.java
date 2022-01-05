package cn.javacoder.stockanalyze.common.service;

import cn.javacoder.stockanalyze.common.entity.Board;
import cn.javacoder.stockanalyze.common.entity.Report;
import cn.javacoder.stockanalyze.common.mapper.BoardMapper;
import cn.javacoder.stockanalyze.common.mapper.ReportMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class ReportService extends ServiceImpl<ReportMapper, Report> {

    public boolean exist(String stockCode) {
        int n=  this.baseMapper.selectCount(new QueryWrapper<Report>().lambda()
                .eq(Report::getStockCode, stockCode));
        return n > 0;
    }
}
