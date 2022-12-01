package cn.javacoder.stockanalyze.common.service;

import cn.javacoder.stockanalyze.common.entity.Company;
import cn.javacoder.stockanalyze.common.mapper.CompanyMapper;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CompanyService extends ServiceImpl<CompanyMapper, Company>  {

    public List<String> selectAllStockCode() {

        List<Company> result = this.baseMapper.selectList( new QueryWrapper<Company>().lambda()
                .select(Company::getStockCode));
        return result.stream().map(Company::getStockCode).collect(Collectors.toList());
    }

    public List<Company> listByExchangeCode(String exchange, Date date){
        Wrapper<Company> queryWrapper = new QueryWrapper<Company>().lambda()
                .eq(Company::getExchange, exchange)
                .lt(date != null, Company::getUpdateTime, date);
        return this.baseMapper.selectList(queryWrapper);
    }

    public List<Company> listNeedWatchOn(){
        return this.baseMapper.selectList(new QueryWrapper<Company>().lambda()
                .eq(Company::isWatchOn, true));
    }
}
