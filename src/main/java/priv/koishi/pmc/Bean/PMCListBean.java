package priv.koishi.pmc.Bean;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import priv.koishi.pmc.Bean.AbstractBean.BaseCopyBean;
import priv.koishi.pmc.Bean.Interface.FilePath;
import priv.koishi.pmc.Bean.Interface.Indexable;
import priv.koishi.pmc.Bean.VO.ClickPositionVO;

import java.util.List;
import java.util.UUID;

/**
 * 批量执行 PMC 流程数据类
 *
 * @author KOISHI
 * Date:2026-01-19
 * Time:15:25
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class PMCListBean extends BaseCopyBean implements Indexable, FilePath {

    /**
     * 唯一标识符
     */
    @JsonIgnore
    String uuid = UUID.randomUUID().toString();

    /**
     * 序号
     */
    @JsonIgnore
    Integer index;

    /**
     * PMC 文件名称
     */
    String name;

    /**
     * PMC 文件地址
     */
    String path;

    /**
     * 运行次数
     */
    String runNum = "1";

    /**
     * 每个文件执行前等待时间
     */
    String waitTime = "0";

    /**
     * PMC 文件解析后的数据
     */
    @JsonIgnore
    List<ClickPositionVO> clickPositionVOS;

    /**
     * 为列表数据设置序号接口
     *
     * @param index 要设置的序号
     */
    @Override
    public void setIndex(int index) {
        this.index = index;
    }

    /**
     * 更新 UUID
     */
    @Override
    public void updateUuid() {
        uuid = UUID.randomUUID().toString();
    }

}
