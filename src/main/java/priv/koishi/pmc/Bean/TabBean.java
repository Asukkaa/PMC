package priv.koishi.pmc.Bean;

import javafx.scene.control.CheckBox;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author KOISHI
 * Date:2025-01-03
 * Time:14:55
 */
@Data
@Accessors(chain = true)
public class TabBean {

    /**
     * 功能名称
     */
    String tabName;

    /**
     * 功能Id
     */
    String tabId;

    /**
     * 启用状态开关
     */
    CheckBox activationCheckBox;

}
