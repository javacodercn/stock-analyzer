package cn.javacoder.stockanalyze.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.sun.tracing.dtrace.ArgsAttributes;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Report {
    @TableId(type = IdType.AUTO)
    private long id;
    private String stockCode;
    private Date publishDate;
    private String source;
    private String author;
    private String rateCode;
    private String rateName;
    private String title;
    private boolean isDeepReport;
    private int aimPrice;
}
