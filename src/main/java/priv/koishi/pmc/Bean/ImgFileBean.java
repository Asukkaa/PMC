package priv.koishi.pmc.Bean;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 图片文件信息类
 *
 * @author KOISHI
 * Date:2025-03-21
 * Time:16:50
 */
@Data
@Accessors(chain = true)
public class ImgFileBean {

    /**
     * 文件名称
     */
    String name;

    /**
     * 文件类型
     */
    String type;

    /**
     * 文件地址
     */
    StringProperty path = new SimpleStringProperty();

    public StringProperty pathProperty() {
        return this.path;
    }

    public String getPath() {
        return path.get();
    }

    public void setPath(String path) {
        this.path.set(path);
    }

}
