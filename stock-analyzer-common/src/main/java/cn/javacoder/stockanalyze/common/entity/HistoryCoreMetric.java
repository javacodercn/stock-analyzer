package cn.javacoder.stockanalyze.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.*;

import java.util.Date;

/**
 * 历年核心指标
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode(of = {"stockCode", "endDate"})
public class HistoryCoreMetric {

    @TableId(type=IdType.AUTO)
    private long id;

    private String stockCode;

    private Date endDate;

    private int roekcjq;

    private int zcfzl;
}
