package priv.koishi.pmc.Bean;

import lombok.Data;
import lombok.experimental.Accessors;
import org.bytedeco.opencv.opencv_core.Point;

/**
* @author KOISHI
* Date:2025-03-27
* Time:17:00
*/
@Data
@Accessors(chain = true)
public class MatchPoint {

    Point point;

    int matchThreshold;

}
