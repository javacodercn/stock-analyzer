package cn.javacoder.stockanalyze.common.service;

import cn.javacoder.stockanalyze.common.entity.Board;
import cn.javacoder.stockanalyze.common.entity.BusinessAnalysis;
import cn.javacoder.stockanalyze.common.mapper.BoardMapper;
import cn.javacoder.stockanalyze.common.mapper.BusinessAnalysisMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class BusinessAnalysisService extends ServiceImpl<BusinessAnalysisMapper, BusinessAnalysis> {

    public boolean exist(String stockCode) {
        int n=  this.baseMapper.selectCount(new QueryWrapper<BusinessAnalysis>().lambda()
                .eq(BusinessAnalysis::getStockCode, stockCode));
        return n > 0;
    }
}
