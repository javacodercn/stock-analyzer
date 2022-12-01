package cn.javacoder.stockanalyze.common.vo;

import lombok.Data;

@Data
public class Overview {
    /**
     * 总市值
     */
    private int marketCapitalization;
    /**
     * 平均pe
     */
    private int avgPe;
}
