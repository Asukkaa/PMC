package priv.koishi.pmc.Bean;

import lombok.Data;
import lombok.experimental.Accessors;
import priv.koishi.pmc.Bean.Interface.FilePath;
import priv.koishi.pmc.Bean.Interface.Indexable;

import java.io.File;

import static priv.koishi.pmc.Utils.FileUtils.getExistsFileName;

/**
 * tessdata 文件展示类
 *
 * @author applesaucepenguin
 * Date 2026-03-30
 * time 14:39
 */
@Data
@Accessors(chain = true)
public class TessdataBean implements Indexable, FilePath {

    /**
     * 序号
     */
    Integer index;

    /**
     * tessdata 文件名称（不带拓展名）
     */
    String name;

    /**
     * tessdata 文件地址
     */
    String path;

    /**
     * tessdata 文件备注
     */
    String remark;

    /**
     * 启用状态文本
     */
    String state;

    /**
     * 启用状态(true 启用模型)
     */
    boolean active = true;

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
     * 有参构造
     *
     * @param file 需要读取的文件
     */
    public TessdataBean(File file) {
        name = getExistsFileName(file);
        path = file.getPath();
    }

    /**
     * 无参构造
     */
    public TessdataBean() {
    }

    /**
     * 设置启用状态
     *
     * @param active true 启用模型
     */
    @SuppressWarnings("unused")
    public void setActive(boolean active) {
        this.active = active;
        state = active ? "启用" : "禁用";
    }

    /**
     * 查询启用状态
     *
     * @return 启用状态文本
     */
    @SuppressWarnings("unused")
    public String getState() {
        state = active ? "启用" : "禁用";
        return state;
    }

}
