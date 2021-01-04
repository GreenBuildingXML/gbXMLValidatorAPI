package com.bimport.ashrae.common;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ImageUtil {
    private static BufferedImage updateImage(BufferedImage originalImage,
                                             Integer imgWidth, Integer imgHeight) {
        int type = originalImage.getType() == 0
                ? BufferedImage.TYPE_INT_ARGB
                : originalImage.getType();

        BufferedImage resizedImage = new BufferedImage(imgWidth, imgHeight, type);
        Graphics2D g = resizedImage.createGraphics();
        g.drawImage(originalImage, 0, 0, imgWidth, imgHeight, null);
        g.dispose();

        return resizedImage;
    }

    private static boolean saveImage(BufferedImage image, File file) {
        String imageType = FileUtil.getSuffix(file);
        try {
            return ImageIO.write(image, imageType, file);
        } catch (IOException e) {
            return false;
        }
    }

    public static boolean cropImageToSquare(File imageFile, int maxDim) {
        BufferedImage img = capImage(imageFile, maxDim);
        if (img == null) {
            return false;
        }

        int w = img.getWidth();
        int h = img.getHeight();

        if (w > h) {
            // crop middle part
            img = img.getSubimage((w - h) / 2, 0, h, h);
        } else if (w < h) {
            // crop top part
            img = img.getSubimage(0, 0, w, w);
        }

        return saveImage(img, imageFile);
    }

    private static BufferedImage capImage(File imageFile, int maxDim) {
        try {
            BufferedImage img = ImageIO.read(imageFile);
            int w = img.getWidth();
            int h = img.getHeight();

            if (w > maxDim || h > maxDim) {
                if (w > h) {
                    h = maxDim * h / w;
                    w = maxDim;
                } else {
                    w = maxDim * w / h;
                    h = maxDim;
                }

                img = updateImage(img, w, h);
            }
            return img;
        } catch (IOException e) {
            return null;
        }
    }

    public static boolean shrinkImage(File imageFile, int maxDim) {
        BufferedImage img = capImage(imageFile, maxDim);
        if (img == null) {
            return false;
        }

        return saveImage(img, imageFile);
    }
}
