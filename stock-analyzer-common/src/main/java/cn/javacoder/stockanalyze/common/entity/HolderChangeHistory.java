package cn.javacoder.stockanalyze.common.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class HolderChangeHistory {
    /**
     *   `id` bigint NOT NULL AUTO_INCREMENT,
     *   `stock_code` char(6) NOT NULL COMMENT '股票代码',
     *   `end_date` date NOT NULL COMMENT '报告时间',
     *   `holder_name` varchar(64) NOT NULL COMMENT '持有者姓名',
     *   `free_holder_ratio` varchar(255) DEFAULT NULL COMMENT '占流通股比例',
     *   `free_ratio_qoq` varchar(255) DEFAULT NULL COMMENT '变化率',
     *   `inc_count` varchar(255) DEFAULT NULL COMMENT '增加的股数',
     *   `hold_count` varchar(255) DEFAULT NULL COMMENT '持有数量',
     *   `is_holder_org` varchar(255) DEFAULT NULL COMMENT '是否是机构',
     *   `holder_rank` varchar(255) DEFAULT NULL COMMENT '排名',
     */
    @TableId
    private long id;
    private String stockCode;
    private Date endDate;
    private String holderName;
    private int freeHolderRatio;
    private int freeRatioQoq;
    private long incCount;
    private long holdCount;
    private boolean isHolderOrg;
    private int holderRank;
}
