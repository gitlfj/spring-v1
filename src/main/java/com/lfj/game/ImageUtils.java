package com.lfj.game;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * 图片工具类
 *
 * @author lfj
 * @date 2020/11/17 16:55
 */
class ImageUtils {


    /**
     *  保存图片
     */
    private static Map<String, Image> imageMap = new HashMap<String, Image>(8);

    static {
        imageMap.put("background", getImage(Constants.IMG_PRE + "background.jpg"));
        imageMap.put("zhan", getImage(Constants.IMG_PRE + "zhan.jpg"));
        imageMap.put("fail", getImage(Constants.IMG_PRE + "fail.png"));
        imageMap.put("food", getImage(Constants.IMG_PRE + "food.png"));
        imageMap.put("snake_body", getImage(Constants.IMG_PRE + "snake_body.png"));
        imageMap.put("snake_head", getImage(Constants.IMG_PRE + "snake_head.png"));
    }

    /**
     * 加载图片
     * @param imagePath 图片路径
     * @return 图片
     */
    private static Image getImage(String imagePath) {
        URL url = SnakeGame.class.getClassLoader().getResource(imagePath);
        BufferedImage image = null;
        try {
            assert url != null;
            image = ImageIO.read(url);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return image;
    }

    static Map<String, Image> getImageMap() {
        return imageMap;
    }

    /**
     * 按指定角度旋转图片
     * @param bufferedimage bufferedimage
     * @param degree degree
     * @return 图片
     */
    public static Image rotateImage(final BufferedImage bufferedimage, final int degree) {
        // 得到图片宽度
        int w = bufferedimage.getWidth();
        // 得到图片高度。
        int h = bufferedimage.getHeight();
        // 得到图片透明度。
        int type = bufferedimage.getColorModel().getTransparency();
        // 空的图片。
        BufferedImage img;
        // 空的画笔。
        Graphics2D graphics2d;
        (graphics2d = (img = new BufferedImage(w, h, type)).createGraphics()).setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        // 旋转，degree是整型，度数，比如垂直90度。
        graphics2d.rotate(Math.toRadians(degree), w / 2, h / 2);
        // 从bufferedimagecopy图片至img，0,0是img的坐标。
        graphics2d.drawImage(bufferedimage, 0, 0, null);
        graphics2d.dispose();
        // 返回复制好的图片，原图片依然没有变，没有旋转，下次还可以使用。
        return img;
    }

}
