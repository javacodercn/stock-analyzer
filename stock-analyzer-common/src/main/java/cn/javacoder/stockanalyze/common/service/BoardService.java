package cn.javacoder.stockanalyze.common.service;

import cn.javacoder.stockanalyze.common.entity.Board;
import cn.javacoder.stockanalyze.common.entity.Company;
import cn.javacoder.stockanalyze.common.mapper.BoardMapper;
import cn.javacoder.stockanalyze.common.mapper.CompanyMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class BoardService  extends ServiceImpl<BoardMapper, Board> {

    public boolean exist(String stockCode) {
        int n=  this.baseMapper.selectCount(new QueryWrapper<Board>().lambda()
                .eq(Board::getStockCode, stockCode));
        return n > 0;
    }
}
