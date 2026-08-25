/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Framework.GraphicalAssets;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.VolatileImage;

/**
 * Represents an image used to be rendered in the world
 *
 * @author Joseph
 */
public class Sprite implements Graphic {

    private BufferedImage image;
    private VolatileImage volatileImage;
    private String signuature;

    private double scale = 1;
    private double opacity = 1;
    /**
     * Anchor in current image pixels, or NaN to keep using the middle of the image.
     *
     * A sprite's image is replaced in place by scaling and mirroring, so this is maintained by
     * every operation that changes the image rather than being expressed against some original
     * size that no longer exists.
     */
    private double anchorX = Double.NaN, anchorY = Double.NaN;

    @Override
    public double getAnchorX() {
        return Double.isNaN(anchorX) ? (image == null ? 0 : image.getWidth() / 2.0) : anchorX;
    }

    @Override
    public double getAnchorY() {
        return Double.isNaN(anchorY) ? (image == null ? 0 : image.getHeight() / 2.0) : anchorY;
    }

    /**
     * Sets the anchor, in current image pixels from the top left. Scaling or mirroring the sprite
     * moves the anchor with the image, so it keeps pointing at the same feature.
     */
    public void setAnchor(double x, double y) {
        anchorX = x;
        anchorY = y;
    }

    /** Restores the default anchor at the middle of the image. */
    public void clearAnchor() {
        anchorX = Double.NaN;
        anchorY = Double.NaN;
    }

    /** Anchors the sprite at the bottom centre of its non-transparent pixels. */
    public void setAnchorToOpaqueBottomCentre() {
        if (image == null) return;
        int minX = image.getWidth(), maxX = -1, maxY = -1;
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                if ((image.getRGB(x, y) >>> 24) == 0) continue;
                if (x < minX) minX = x;
                if (x > maxX) maxX = x;
                if (y > maxY) maxY = y;
            }
        }
        if (maxX < 0) return;   // fully transparent: leave the default alone
        setAnchor((minX + maxX + 1) / 2.0, maxY + 1);
    }

    /**
     * gets raw underlying image
     *
     * @return underlying image
     */
    public BufferedImage getImage() {
        return image;
    }

    /**
     * sets underlying image
     *
     * @param b new underlying image
     */
    public void setImage(BufferedImage b) {
        // an anchor describes a position within the old image; it only carries over to a
        // replacement of the same size, and is otherwise dropped rather than left pointing
        // somewhere arbitrary
        if (image != null && b != null
                && (image.getWidth() != b.getWidth() || image.getHeight() != b.getHeight())) {
            clearAnchor();
        }
        image = b;
        volatileImage = Graphic.getVolatileFromBuffered(b);
    }

    public Sprite(BufferedImage bi) {
        image = bi;
        volatileImage = Graphic.getVolatileFromBuffered(image);
    }
    
    public Sprite(BufferedImage bi, String signature) {
        image = bi;
        volatileImage = Graphic.getVolatileFromBuffered(image);
        this.signuature = signature;
    }

    public Sprite(BufferedImage bi, double inputScale) {
        image = bi;
        this.scale = inputScale;
        volatileImage = Graphic.getVolatileFromBuffered(image);
    }
    
    public Sprite(BufferedImage bi, double inputScale, String signature) {
        image = bi;
        this.scale = inputScale;
        volatileImage = Graphic.getVolatileFromBuffered(image);
        this.signuature = signature;
    }

    @Override
    public VolatileImage getCurrentVolatileImage() {
        if (volatileImage != null && image != null) {
            volatileImage = Graphic.getValidatedVolatileImage(volatileImage, image);
            return volatileImage;
        } else {
            return null;
        }
    }

    @Override
    public Sprite copy() {
        Sprite output = new Sprite(image);
        output.scale = scale;
        output.signuature = this.signuature;
        output.anchorX = anchorX;
        output.anchorY = anchorY;
        return output;
    }

    @Override
    public double getScale() {
        return scale;
    }

    /**
     * image by a given amount
     *
     * @param d multiplier to scale by
     */
    @Override
    public void scale(double d) {
        image = Graphic.scaleImage(image, d);
        volatileImage = Graphic.getVolatileFromBuffered(image);
        scale *= d;
        // the anchor is in image pixels, so it scales with the image to stay on the same feature
        if (!Double.isNaN(anchorX)) {
            anchorX *= d;
            anchorY *= d;
        }
    }

    @Override
    public void mirrorHorizontal() {
        image = Graphic.mirrorHorizontal(image);
        volatileImage = Graphic.getVolatileFromBuffered(image);
        // the anchor is a position in the image, so it flips with the pixels
        if (!Double.isNaN(anchorX)) anchorX = image.getWidth() - anchorX;
    }

    @Override
    public void mirrorVertical() {
        image = Graphic.mirrorVertical(image);
        volatileImage = Graphic.getVolatileFromBuffered(image);
        if (!Double.isNaN(anchorY)) anchorY = image.getHeight() - anchorY;
    }

    /**
     * gets height of image in px
     *
     * @return number of pixels tall
     */
    public int getHeight() {
        return image.getHeight();
    }

    /**
     * gets width of image in px
     *
     * @return number of pixels wide
     */
    public int getWidth() {
        return image.getWidth();
    }

    /**
     * sets the scale of image to a given scale
     *
     * @param d new value to be scale, relative to the default scale of image
     */
    @Override
    public void scaleTo(double d) {
        double factor = d / scale;
        image = Graphic.scaleImage(image, factor);
        volatileImage = Graphic.getVolatileFromBuffered(image);
        scale = d;
        if (!Double.isNaN(anchorX)) {
            anchorX *= factor;
            anchorY *= factor;
        }
    }

    /**
     * destroys this sprite and releases memory do not call unless you know what
     * your doing
     */
    @Override
    public void destroy() {
        if (volatileImage != null) {
            volatileImage.flush(); // release native/VRAM memory now rather than waiting for finalization
        }
        image = null;
        volatileImage = null;
    }

    @Override
    public BufferedImage getCurrentImage() {
        return getImage();
    }

    @Override
    public boolean isAnimated() {
        return false;
    }

    @Override
    public String getSignature() {
        return signuature;
    }

    @Override
    public void setSignature(String s) {
        signuature = s;
    }
    
    @Override
    public void setOpacity(double amount) {
        // Ensure opacity amount is within the range [0, 1]
        if (amount < 0) amount = 0;
        if (amount > 1) amount = 1;

        // Create a new image with the same width, height, and type as the input image
        BufferedImage outputImage = new BufferedImage(image.getWidth(), 
                                                     image.getHeight(), 
                                                     BufferedImage.TYPE_INT_ARGB);
        
        // Get a Graphics2D object to modify the new image
        Graphics2D g2d = outputImage.createGraphics();
        
        // Set the opacity using AlphaComposite
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) amount));
        
        // Draw the original image onto the new image with the adjusted opacity
        g2d.drawImage(image, 0, 0, null);
        
        // Dispose the Graphics2D object to release system resources
        g2d.dispose();
        image = outputImage;
        volatileImage = Graphic.getVolatileFromBuffered(image);
        opacity = amount;
    }

    /**
     * generates a topdown shadow sprite based on given input
     * @param input image to make shadow of
     * @param opacity opacity of shadow
     * @return new sprite from gnerated image
     */
    public static Sprite generateShadowSprite(BufferedImage input, double opacity) {
        opacity = Math.max(0, Math.min(1, opacity));

        // Create a new BufferedImage with the same dimensions and type as the input
        BufferedImage shadowImage = new BufferedImage(input.getWidth(), input.getHeight(), BufferedImage.TYPE_INT_ARGB);

        // Iterate over every pixel in the input image
        for (int x = 0; x < input.getWidth(); x++) {
            for (int y = 0; y < input.getHeight(); y++) {
                // Get the original pixel color
                int argb = input.getRGB(x, y);

                // Extract the alpha value of the pixel
                int originalAlpha = (argb >> 24) & 0xff;

                // Create a dark gray color (e.g., RGB 30, 30, 30) and apply the input opacity and original alpha
                int shadowAlpha = (int) (originalAlpha * opacity); // Adjust alpha based on opacity
                int gray = 30; // Dark gray color
                int shadowColor = (shadowAlpha << 24) | (gray << 16) | (gray << 8) | gray;

                // Set the pixel in the shadow image
                shadowImage.setRGB(x, y, shadowColor);
            }
        }

        return new Sprite(shadowImage);
    }
     
    @Override
     public void applyAlphaEdgeBlurSelf (int strength) {
        setImage(Graphic.applyAlphaEdgeBlur(this.image, strength));
    }

    @Override
    public void setBrightness(double brightness) {
        setImage(Graphic.applyBrightness(this.image, brightness));
    }

    @Override
    public void setSaturation(double saturation) {
        setImage(Graphic.applySaturation(this.image, saturation));
    }
    

    @Override
    public double getOpacity() {
        return opacity;
    }
}
