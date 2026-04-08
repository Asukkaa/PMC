package priv.koishi.pmc.Bean;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;
import lombok.experimental.Accessors;
import priv.koishi.pmc.Bean.Interface.Indexable;

/**
 * OCR 数据结果类
 *
 * @author applesaucepenguin
 * Date 2026-04-03
 * time 16:08
 */
@Data
@Accessors(chain = true)
public class OCRDataBean implements Indexable {

    /**
     * 序号
     */
    private Integer index;

    /**
     * 识别到的文本
     */
    private String text;

    /**
     * 文本 X 坐标
     */
    private String x;

    /**
     * 文本 Y 坐标
     */
    private String y;

    /**
     * 文字识别置信度
     */
    private float confidence;

    /**
     * 文字识别置信度百分比
     */
    @Setter(AccessLevel.NONE)
    private String confidenceString;

    /**
     * 获取文字识别置信度百分比
     *
     * @return 文字识别置信度百分比
     */
    @SuppressWarnings("unused")
    public String getConfidenceString() {
        return String.format("%.2f%%", confidence / 100.0);
    }

    /**
     * 为列表数据设置序号接口
     *
     * @param index 要设置的序号
     */
    @Override
    public void setIndex(int index) {
        this.index = index;
    }

}
