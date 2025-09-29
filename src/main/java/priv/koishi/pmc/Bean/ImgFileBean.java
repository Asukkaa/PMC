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

    /**
     * 获取图片地址绑定
     *
     * @return 图片地址绑定
     */
    public StringProperty pathProperty() {
        return path;
    }

    /**
     * 获取图片地址字符串
     *
     * @return 图片地址字符串
     */
    public String getPath() {
        return path.get();
    }

    /**
     * 设置图片地址字符串
     *
     * @param path 图片地址字符串
     */
    public void setPath(String path) {
        this.path.set(path);
    }

}
