package cn.javacoder.stockanalyze.common.service;

import cn.javacoder.stockanalyze.common.entity.Company;
import cn.javacoder.stockanalyze.common.entity.HolderChangeHistory;
import cn.javacoder.stockanalyze.common.mapper.CompanyMapper;
import cn.javacoder.stockanalyze.common.mapper.HolderChangeHistoryMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@Slf4j
public class HolderChangeHistoryService extends ServiceImpl<HolderChangeHistoryMapper, HolderChangeHistory> {

    public boolean holderHistoryExists(String stockCode, Date endDate) {
        int n = baseMapper.selectCount(new QueryWrapper<HolderChangeHistory>().lambda()
        .eq(HolderChangeHistory::getStockCode, stockCode)
        .eq(HolderChangeHistory::getEndDate, endDate));
        return n > 0;
    }
}
