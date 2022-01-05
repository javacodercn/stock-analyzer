package cn.javacoder.stockanalyze.common.service;

import cn.javacoder.stockanalyze.common.entity.Trade;
import cn.javacoder.stockanalyze.common.mapper.TradeMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TradeService extends ServiceImpl<TradeMapper, Trade>  {

    public Trade selectLastTrade(String stockCode) {

        List<Trade> result = this.baseMapper.selectList( new QueryWrapper<Trade>().lambda()
                .eq(Trade::getStockCode, stockCode)
                .orderByDesc(Trade::getId)
                .last( " limit 1"));
        return result == null || result.isEmpty() ? null : result.get(0);
    }

}
