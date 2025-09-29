package priv.koishi.pmc.Bean.Config;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 文件选择器配置类
 *
 * @author KOISHI
 * Date:2025-08-05
 * Time:15:51
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class FileChooserConfig extends FileConfig {

    /**
     * 要更新的路径配置 key
     */
    String pathKey;

    /**
     * 要更新的路径配置配置文件地址
     */
    String configPath;

    /**
     * 文件选择器窗口标题
     */
    String title;

}
