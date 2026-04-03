package priv.koishi.pmc.Bean.Result;

import java.awt.image.BufferedImage;

/**
 * 图像识别截图记录类
 *
 * @param image   截图图像
 * @param offsetX 截图左上角在屏幕上的 X 坐标（用于计算绝对坐标）
 * @param offsetY 截图左上角在屏幕上的 Y 坐标（用于计算绝对坐标）
 * @author applesaucepenguin
 * Date 2026-04-03
 * time 19:04
 */
public record ScreenCaptureResult(BufferedImage image, int offsetX, int offsetY) {
}
