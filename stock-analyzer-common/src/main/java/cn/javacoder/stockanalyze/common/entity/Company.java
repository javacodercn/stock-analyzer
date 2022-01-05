package cn.javacoder.stockanalyze.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import jdk.nashorn.internal.objects.annotations.Constructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Company {
    /**
     * 股票代碼
     */
    @TableId(value = "stock_code", type = IdType.INPUT)
    String stockCode;
    /**
     * 名稱
     */
    String name;
    /**
     * 首字母
     */
    String var3;

    Date ipoDate;

    String exchange;

    /**
     * f162
     */
    private int pe;

    /**
     * f173
     */
    private int roe;

    private int debt;

    private int npr;

    private int grossProfitRatio;

    private int shares;

    /**
     * 12.34转换为1234存放
     */
    private int marketCapital;

    private boolean leadingPosition;

    private boolean watchOn;
}
