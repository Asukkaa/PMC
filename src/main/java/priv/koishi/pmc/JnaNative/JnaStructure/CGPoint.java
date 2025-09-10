package priv.koishi.pmc.JnaNative.JnaStructure;

import com.sun.jna.Structure;

import java.util.Arrays;
import java.util.List;

/**
 * @author KOISHI
 * Date:2025-09-10
 * Time:17:44
 */
public class CGPoint extends Structure {

    public double x;

    public double y;

    @Override
    protected List<String> getFieldOrder() {
        return Arrays.asList("x", "y");
    }

}
