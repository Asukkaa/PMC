package priv.koishi.pmc.Properties;

import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Properties;

/**
 * 可以读取Properties中带有‘\’路径的公共类
 *
 * @author KOISHI
 * Date:2024-10-28
 * Time:下午5:01
 */
public class CommonProperties extends Properties {

    /**
     * 重写Properties的load方法，更换配置文件中的‘\’为‘/’
     *
     * @param reader Properties输入流
     */
    @Override
    public synchronized void load(Reader reader) throws IOException {
        try (BufferedReader bufferedReader = new BufferedReader(reader)) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                // 注意: properties属性类文件存在第一个隐藏字符,需要删除掉，否则第一个数据以key查找不存在
                if (line.startsWith("\uFEFF")) {
                    line = line.substring(1);
                }
                // 如果是#注释内容，则不做操作
                if (!line.startsWith("#") && !line.isEmpty()) {
                    // 限制分割次数避免多等号问题
                    String[] parts = line.split("=", 2);
                    if (parts.length == 2) {
                        String key = parts[0].trim();
                        String value = parts[1].trim();
                        // value为空则不处理转义符
                        if (StringUtils.isNotBlank(value)) {
                            // 由于‘\’在Java中表示转义字符，需要将读取的路径进行转换为‘/’符号,这里“\\\\”代表一个‘\’
                            value = value.replaceAll("\\\\", "/");
                        }
                        put(key, value);
                    }
                }
            }
        }
    }

}
