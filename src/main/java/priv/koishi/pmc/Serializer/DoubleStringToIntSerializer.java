package priv.koishi.pmc.Serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

/**
 * 自定义 jackson 序列化器
 *
 * @author KOISHI
 * Date:2025-04-01
 * Time:13:21
 */
public class DoubleStringToIntSerializer extends JsonSerializer<String> {

    /**
     * json 序列化时将非整数字符串转为整数
     *
     * @param value    待序列化的字符串
     * @param gen      json 生成器
     * @param provider 序列化提供者
     */
    @Override
    public void serialize(String value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        if (value != null) {
            try {
                // 将字符串转为 double 后再转为 int
                int intValue = (int) Double.parseDouble(value);
                gen.writeString(String.valueOf(intValue));
            } catch (NumberFormatException e) {
                // 非数字字符串保持原样
                gen.writeString(value);
            }
        }
    }

}