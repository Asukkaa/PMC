package priv.koishi.pmc.Bean;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author KOISHI
 * Date:2025-08-26
 * Time:18:25
 */
@Data
@Accessors(chain = true)
public class FileBean {

    /**
     * 文件列表展示名称
     */
    String name;

    /**
     * 文件列表展示路径
     */
    String path;

    /**
     * 文件列表展示文件拓展名
     */
    String fileType;

    /**
     * 文件列表展示文件大小
     */
    String size;

    /**
     * 文件创建时间
     */
    String creatDate;

    /**
     * 文件修改时间
     */
    String updateDate;

    /**
     * 文件是否隐藏
     */
    String showStatus;

}
