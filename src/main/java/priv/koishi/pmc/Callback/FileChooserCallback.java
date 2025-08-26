package priv.koishi.pmc.Callback;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * 文件选择回调接口
 *
 * @author KOISHI
 * Date:2025-08-05
 * Time:14:45
 */
public interface FileChooserCallback {

    /**
     * 文件选择回调
     *
     * @param selectedItems 选择的文件列表
     * @throws IOException io异常
     */
    void onFileChooser(List<File> selectedItems) throws IOException;

}
