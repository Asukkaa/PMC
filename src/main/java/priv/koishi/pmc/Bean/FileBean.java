package priv.koishi.pmc.Bean;

import lombok.Data;
import lombok.experimental.Accessors;
import priv.koishi.pmc.Bean.Interface.FilePath;

/**
 * @author KOISHI
 * Date:2025-08-26
 * Time:18:25
 */
@Data
@Accessors(chain = true)
public class FileBean implements FilePath {

    /**
     * 文件列表展示名称
     */
    private String name;

    /**
     * 文件列表展示路径
     */
    private String path;

    /**
     * 文件列表展示文件拓展名
     */
    private String fileType;

    /**
     * 文件列表展示文件大小
     */
    private String size;

    /**
     * 文件创建时间
     */
    private String creatDate;

    /**
     * 文件修改时间
     */
    private String updateDate;

    /**
     * 文件是否隐藏
     */
    private String showStatus;

}
