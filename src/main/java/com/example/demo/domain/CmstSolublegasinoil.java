package com.example.demo.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 *
 * @TableName cmst_solublegasinoil
 */
@TableName(value ="cmst_solublegasinoil")
@Data
public class CmstSolublegasinoil implements Serializable {
    /**
     * 对象的唯一标识符
     */
    @TableId
    private String objId;

    /**
     * 对象的显示标识符
     */
    private BigDecimal objDispidx;

    /**
     * 对象的说明标识符
     */
    private String objCaption;

    /**
     * 被监测设备代码是指监测装置所监测设备的唯一标识。该代码采用生产管理信息系统（PMS）中通行的 17 位编码。
     */
    private String linkeddevice;

    /**
     * 数据采监测装置代码是指监测装置的唯一标识。该代码在网省侧主站中产生，生成后将保持不变，该代码的使用范
     */
    private String devicecode;

    /**
     * 被监测设备相别:A相 A相  B相 B相  C相 C相 ABC相 ABC相
     */
    private String phase;

    /**
     * 数据采集时间，日期类型，格式如下yyyy-MM-dd HH:mm:ss
     */
    private Date acquisitiontime;

    /**
     *
     */
    private BigDecimal h2;

    /**
     *
     */
    private BigDecimal ch4;

    /**
     *
     */
    private BigDecimal c2h4;

    /**
     *
     */
    private BigDecimal c2h2;

    /**
     *
     */
    private BigDecimal co;

    /**
     *
     */
    private BigDecimal co2;

    /**
     *
     */
    private BigDecimal o2;

    /**
     *
     */
    private BigDecimal n2;

    /**
     *
     */
    private BigDecimal totalhydrocarbon;

    /**
     * 是否处理:T处理，F未处理
     */
    private String ishandled;

    /**
     *
     */
    private BigDecimal c2h6;
    /**
     * 是否检测
     */
    @TableField("ISDETECT")
    private Boolean isDetect;

    /**
     * 氢气告警等级
     */
    @TableField(exist = false)
    private String h2Level;
    /**
     * 乙炔告警等级
     */
    @TableField(exist = false)
    private String c2H2Level;
    /**
     * 总烃告警等级
     */
    @TableField(exist = false)
    private String tYLevel;

    /**
     * 生产厂商
     */
    @TableField(exist = false)
    private String factory;
    /**
     * 装置名称
     */
    @TableField(exist = false)
    private String deviceName;
    /**
     * 运维单位
     */
    @TableField(exist = false)
    private String linkedDeptName;
    /**
     * 数据入库时间，日期类型，格式如下yyyy-MM-dd HH:mm:ss
     */
    private Date resaveTime;

    // ========== 新增字段：增量数据 ==========
    /**
     * 乙炔周绝对增量
     */
    private BigDecimal c2h2WkAbsInc;

    /**
     * 氢气周绝对增量
     */
    private BigDecimal h2WkAbsInc;

    /**
     * 总烃周绝对增量
     */
    private BigDecimal totHydWkAbsInc;

    /**
     * 总烃周相对增长速率
     */
    private BigDecimal totHydWkRelRate;

    /**
     * 乙炔日绝对增量
     */
    private BigDecimal c2h2DyAbsInc;

    /**
     * 乙炔每4h增量
     */
    private BigDecimal c2h2FhAbsInc;

    /**
     * 乙炔每2h增量
     */
    private BigDecimal c2h2ThAbsInc;

    // ========== 新增字段：告警信息 ==========
    /**
     * 乙炔告警
     */
    private String c2h2Alm;

    /**
     * 氢气告警
     */
    private String h2Alm;

    /**
     * 总烃浓度报警
     */
    private String totHydAlm;

    /**
     * 乙炔突增
     */
    private String c2h2UPrush;

    /**
     * 氢气突增
     */
    private String h2UPrush;

    /**
     * 总烃突增
     */
    private String tHyUPrush;

    /**
     * 氢气告警
     */
    private String h2InsAlm;

    /**
     * 乙炔告警
     */
    private String c2h2InsAlm;

    /**
     * 总烃告警
     */
    private String tHyInsAlm;

    /**
     * 氢气绝对增量告警
     */
    private String h2AbsInsAlm;

    /**
     * 总烃绝对增量告警
     */
    private String thyAbsInsAlm;

    /**
     * 乙炔绝对增量告警
     */
    private String c2h2AbsInsAlm;

    /**
     * 总烃相对增长速率告警
     */
    private String tHyRelAlm;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
    /**
     * 运维站
     */
    @TableField(exist = false)
    private String linkedStationName;

    /**
     * 电压等级
     */
    @TableField(exist = false)
    private Integer voltageGrade;

    /**
     * 告警类型
     */
    @TableField(exist = false)
    private String dataType;
    /**
     * 告警等级
     */
    @TableField(exist = false)
    private String alarmlevel;
    /**
     * 因子
     */
    @TableField(exist = false)
    private String param;


}
