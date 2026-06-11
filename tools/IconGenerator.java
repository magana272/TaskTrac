import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Generates Trak application icons: .png (multiple sizes) and .ico
 * Run: java tools/IconGenerator.java
 */
public class IconGenerator {

    // Trak brand colors
    static final Color BG_DARK = new Color(30, 32, 40);
    static final Color ACCENT = new Color(99, 102, 241);   // indigo
    static final Color ACCENT_LIGHT = new Color(139, 142, 255);
    static final Color WHITE = new Color(240, 240, 245);
    static final Color CHECK_GREEN = new Color(74, 222, 128);

    public static void main(String[] args) throws Exception {
        String outDir = "src/main/resources/icons";

        int[] sizes = {16, 32, 48, 64, 128, 256, 512};
        BufferedImage[] images = new BufferedImage[sizes.length];

        for (int i = 0; i < sizes.length; i++) {
            images[i] = render(sizes[i]);
            ImageIO.write(images[i], "png",
                    new File(outDir + "/trak-" + sizes[i] + ".png"));
        }

        // Main icon (256px)
        ImageIO.write(images[5], "png", new File(outDir + "/trak.png"));

        // Write .ico (contains 16, 32, 48, 64, 128, 256)
        writeIco(new BufferedImage[]{images[0], images[1], images[2], images[3], images[4], images[5]},
                new File(outDir + "/trak.ico"));

        // macOS .icns needs 512 — jpackage can use the 512 PNG directly
        System.out.println("Icons generated in " + outDir);
    }

    static BufferedImage render(int size) {
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

        float s = size;
        float pad = s * 0.08f;
        float r = s * 0.18f; // corner radius

        // Background: rounded rectangle with gradient
        RoundRectangle2D bg = new RoundRectangle2D.Float(pad, pad, s - 2 * pad, s - 2 * pad, r, r);
        GradientPaint bgGrad = new GradientPaint(0, 0, new Color(35, 37, 48), s, s, new Color(22, 24, 32));
        g.setPaint(bgGrad);
        g.fill(bg);

        // Subtle border
        g.setPaint(new Color(99, 102, 241, 40));
        g.setStroke(new BasicStroke(Math.max(1, s * 0.01f)));
        g.draw(bg);

        // Draw "T" letter — bold, modern
        float tLeft = s * 0.22f;
        float tRight = s * 0.78f;
        float tTop = s * 0.22f;
        float tBottom = s * 0.72f;
        float barH = s * 0.10f;
        float stemW = s * 0.13f;
        float stemCenter = s * 0.46f;

        // T horizontal bar with gradient
        RoundRectangle2D bar = new RoundRectangle2D.Float(
                tLeft, tTop, tRight - tLeft, barH, barH * 0.5f, barH * 0.5f);
        GradientPaint tGrad = new GradientPaint(tLeft, tTop, ACCENT_LIGHT, tRight, tTop + barH, ACCENT);
        g.setPaint(tGrad);
        g.fill(bar);

        // T vertical stem
        RoundRectangle2D stem = new RoundRectangle2D.Float(
                stemCenter - stemW / 2, tTop + barH * 0.6f,
                stemW, tBottom - tTop - barH * 0.6f,
                stemW * 0.3f, stemW * 0.3f);
        g.fill(stem);

        // Small checkmark at bottom-right (sprint tracking motif)
        if (size >= 32) {
            float cx = s * 0.70f;
            float cy = s * 0.68f;
            float cs = s * 0.14f;

            // Check circle background
            g.setPaint(CHECK_GREEN);
            Ellipse2D circle = new Ellipse2D.Float(cx - cs / 2, cy - cs / 2, cs, cs);
            g.fill(circle);

            // Checkmark stroke
            g.setPaint(BG_DARK);
            float sw = Math.max(1.2f, s * 0.02f);
            g.setStroke(new BasicStroke(sw, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            Path2D check = new Path2D.Float();
            check.moveTo(cx - cs * 0.22, cy + cs * 0.02);
            check.lineTo(cx - cs * 0.05, cy + cs * 0.20);
            check.lineTo(cx + cs * 0.25, cy - cs * 0.18);
            g.draw(check);
        }

        g.dispose();
        return img;
    }

    /** Writes a minimal .ico file containing the given images (must be <=256px each). */
    static void writeIco(BufferedImage[] images, File out) throws Exception {
        // Convert each image to PNG bytes
        byte[][] pngData = new byte[images.length][];
        for (int i = 0; i < images.length; i++) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(images[i], "png", baos);
            pngData[i] = baos.toByteArray();
        }

        // ICO header: 6 bytes
        // ICO entry: 16 bytes each
        // Then PNG data
        int headerSize = 6 + 16 * images.length;
        int totalSize = headerSize;
        for (byte[] d : pngData) totalSize += d.length;

        ByteBuffer buf = ByteBuffer.allocate(totalSize);
        buf.order(ByteOrder.LITTLE_ENDIAN);

        // ICONDIR header
        buf.putShort((short) 0);          // reserved
        buf.putShort((short) 1);          // type: 1=ICO
        buf.putShort((short) images.length);

        // ICONDIRENTRY for each image
        int offset = headerSize;
        for (int i = 0; i < images.length; i++) {
            int w = images[i].getWidth();
            int h = images[i].getHeight();
            buf.put((byte) (w >= 256 ? 0 : w));   // width (0 means 256)
            buf.put((byte) (h >= 256 ? 0 : h));   // height
            buf.put((byte) 0);   // color palette
            buf.put((byte) 0);   // reserved
            buf.putShort((short) 1);   // color planes
            buf.putShort((short) 32);  // bits per pixel
            buf.putInt(pngData[i].length);  // size of PNG data
            buf.putInt(offset);             // offset to PNG data
            offset += pngData[i].length;
        }

        // PNG data
        for (byte[] d : pngData) {
            buf.put(d);
        }

        try (FileOutputStream fos = new FileOutputStream(out)) {
            fos.write(buf.array());
        }
    }
}