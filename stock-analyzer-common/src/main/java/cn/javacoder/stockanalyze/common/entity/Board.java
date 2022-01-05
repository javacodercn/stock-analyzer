package cn.javacoder.stockanalyze.common.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.beans.Transient;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Board {
    @TableId
    private String stockCode;
    private String board1;
    private String board2;
    private String board3;
    private String board1Desc;
    private String board2Desc;
    private String board3Desc;

    @TableField(exist = false)
    private boolean leadingPosition;
}
