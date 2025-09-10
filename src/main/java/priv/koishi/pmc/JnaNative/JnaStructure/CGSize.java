package priv.koishi.pmc.JnaNative.JnaStructure;

import com.sun.jna.Structure;

import java.util.Arrays;
import java.util.List;

/**
 * @author KOISHI
 * Date:2025-09-10
 * Time:17:46
 */
public class CGSize extends Structure {

    public double width;

    public double height;

    @Override
    protected List<String> getFieldOrder() {
        return Arrays.asList("width", "height");
    }

}
