package cn.javacoder.stockanalyze.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BusinessAnalysis {
    @TableId(type = IdType.AUTO)
    private long id;
    private String stockCode;
    private Date reportDate;
    private int mainopType;
    private String itemName;
    private long mainBusinessIncome;
    private int mbiRatio;
    private long mainBusinessCost;
    private int mbcRatio;
    private long mainBusinessProfit;
    private int mbpRatio;
    private int grossProfitRatio;
    private int irank;
}