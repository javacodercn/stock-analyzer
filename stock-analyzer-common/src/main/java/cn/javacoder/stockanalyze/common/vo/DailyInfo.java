package cn.javacoder.stockanalyze.common.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 *
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DailyInfo {
    private Date date;
    private int highestPrice;
    private int lowestPrice;
    private int openPrice;
    private int closePrice;
    private long tradeVolume;
    private long tradeSum;

    public boolean isCross() {
        int min = Math.min(openPrice, closePrice);
        int max = Math.max(openPrice, closePrice);

        if((max-min) *3 < (highestPrice - max) &&
                (max-min)*3 < (min-lowestPrice)
                ){
            return true;
        }
        return false;
    }

    public boolean isRed() {
        return closePrice > openPrice;
    }

    public boolean isHachure() {
        int min = Math.min(openPrice, closePrice);
        int max = Math.max(openPrice, closePrice);

        if((max-min) *3 < (highestPrice - max)) {
            return true;
        }
        return false;
    }

    public boolean isUnderHachure() {
        int min = Math.min(openPrice, closePrice);
        int max = Math.max(openPrice, closePrice);

        if((max-min)*3 < (min-lowestPrice)){
            return true;
        }
        return false;
    }

    public int getAvgPrice () {
        return Math.addExact(openPrice, closePrice) / 2;
    }
}
