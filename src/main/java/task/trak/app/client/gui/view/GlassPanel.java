package task.trak.app.client.gui.view;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import javax.swing.*;

/**
 * JPanel with rounded corners, gradient background, and optional drop shadow.
 */
public class GlassPanel extends JPanel {

    private final int cornerRadius;
    private final boolean paintShadow;
    private Color gradientTop;
    private Color gradientBottom;

    public GlassPanel(int cornerRadius, boolean paintShadow) {
        this.cornerRadius = cornerRadius;
        this.paintShadow = paintShadow;
        this.gradientTop = TrakTheme.BG_SURFACE;
        this.gradientBottom = TrakTheme.BG_DARK;
        setOpaque(false);
    }

    public GlassPanel(int cornerRadius) {
        this(cornerRadius, false);
    }

    public void setGradient(Color top, Color bottom) {
        this.gradientTop = top;
        this.gradientBottom = bottom;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();
        int shadowOffset = paintShadow ? 4 : 0;
        int drawW = w - shadowOffset;
        int drawH = h - shadowOffset;

        // Shadow layer
        if (paintShadow) {
            g2.setColor(new Color(0, 0, 0, 40));
            g2.fill(new RoundRectangle2D.Float(shadowOffset, shadowOffset, drawW, drawH, cornerRadius, cornerRadius));
            g2.setColor(new Color(0, 0, 0, 20));
            g2.fill(new RoundRectangle2D.Float(shadowOffset / 2f, shadowOffset / 2f, drawW, drawH, cornerRadius, cornerRadius));
        }

        // Gradient fill
        GradientPaint gradient = new GradientPaint(0, 0, gradientTop, 0, drawH, gradientBottom);
        g2.setPaint(gradient);
        g2.fill(new RoundRectangle2D.Float(0, 0, drawW, drawH, cornerRadius, cornerRadius));

        // Subtle top edge highlight (glass reflection)
        g2.setColor(new Color(255, 255, 255, 8));
        g2.fill(new RoundRectangle2D.Float(0, 0, drawW, Math.min(drawH / 3f, 40), cornerRadius, cornerRadius));

        // Border
        g2.setColor(new Color(255, 255, 255, 12));
        g2.setStroke(new BasicStroke(1f));
        g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, drawW - 1, drawH - 1, cornerRadius, cornerRadius));

        g2.dispose();
        super.paintComponent(g);
    }
}
