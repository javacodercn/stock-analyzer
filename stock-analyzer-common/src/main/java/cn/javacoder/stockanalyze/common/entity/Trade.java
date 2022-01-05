package cn.javacoder.stockanalyze.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 交易记录
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Trade {

    @TableId(type = IdType.AUTO)
    private long id;

    private String stockCode;
    private Date buyInDate;
    private Date sellOffDate;
    private int buyInPrice;
    private int sellOffPrice;
    private boolean sellOff;
}
