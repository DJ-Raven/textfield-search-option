package textfield;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Cursor;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import org.jdesktop.animation.timing.Animator;
import org.jdesktop.animation.timing.TimingTargetAdapter;

public class TextFieldSearchOption extends JTextField {

    public String getHint() {
        return hint;
    }

    public void setHint(String hint) {
        this.hint = hint;
    }

    public Color getColorOverlay1() {
        return colorOverlay1;
    }

    public void setColorOverlay1(Color colorOverlay1) {
        this.colorOverlay1 = colorOverlay1;
    }

    public Color getColorOverlay2() {
        return colorOverlay2;
    }

    public void setColorOverlay2(Color colorOverlay2) {
        this.colorOverlay2 = colorOverlay2;
    }

    private Animator animator;
    private float animate;
    private boolean option = false;
    private Shape shape;
    private boolean mousePressed = false;
    private final List<SearchOption> items = new ArrayList<>();
    private final List<SearchOptinEvent> events = new ArrayList<>();
    private int selectedIndex = -1;
    private int pressedIndex = -1;
    private Color colorOverlay1 = new Color(40, 170, 240);
    private Color colorOverlay2 = new Color(138, 39, 232);
    private String hint = "Search...";

    public TextFieldSearchOption() {
        setBorder(new EmptyBorder(10, 10, 10, 40));
        setSelectionColor(new Color(25, 141, 255));
        MouseAdapter mouseEvent = new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent me) {
                if (isOver(me.getPoint())) {
                    setCursor(new Cursor(Cursor.HAND_CURSOR));
                } else {
                    if (option) {
                        setCursor(new Cursor(Cursor.HAND_CURSOR));
                    } else {
                        setCursor(new Cursor(Cursor.TEXT_CURSOR));
                    }
                }
            }

            @Override
            public void mousePressed(MouseEvent me) {
                if (SwingUtilities.isLeftMouseButton(me)) {
                    mousePressed = isOver(me.getPoint());
                    if (!mousePressed) {
                        pressedIndex = checkPress(me.getPoint());
                    }
                }
            }

            @Override
            public void mouseReleased(MouseEvent me) {
                if (SwingUtilities.isLeftMouseButton(me)) {
                    if (!animator.isRunning()) {
                        if (mousePressed && isOver(me.getPoint())) {
                            startAnimate();
                        } else {
                            int index = checkPress(me.getPoint());
                            if (index != -1) {
                                if (index == pressedIndex) {
                                    selectedIndex = index;
                                    runEvent();
                                    startAnimate();
                                }
                            }
                        }
                    }
                }
            }
        };
        addMouseMotionListener(mouseEvent);
        addMouseListener(mouseEvent);
        addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent fe) {
                if (option) {
                    startAnimate();
                }
            }
        });
        initAnimator();
    }

    public void addOption(SearchOption option) {
        items.add(option);
        if (selectedIndex == -1) {
            selectedIndex = 0;
            runEvent();
        }
    }

    public void addEventOptionSelected(SearchOptinEvent event) {
        events.add(event);
    }

    public SearchOption getSelectedOption() {
        if (selectedIndex == -1) {
            return null;
        } else {
            return items.get(selectedIndex);
        }
    }

    public boolean isSelected() {
        return selectedIndex >= 0;
    }

    public void setSelectedIndex(int index) {
        selectedIndex = index;
        runEvent();
        repaint();
    }

    public int getSelectedIndex() {
        return selectedIndex;
    }

    private void runEvent() {
        for (SearchOptinEvent event : events) {
            event.optionSelected(getSelectedOption(), selectedIndex);
        }
    }

    private void startAnimate() {
        if (animator.isRunning()) {
            float f = animator.getTimingFraction();
            animator.stop();
            animator.setStartFraction(1f - f);
        } else {
            animator.setStartFraction(0f);
        }
        option = !option;
        animator.start();
    }

    private boolean isOver(Point mouse) {
        if (!option) {
            return shape.contains(mouse);
        }
        return false;
    }

    private int checkPress(Point mouse) {
        int index = -1;
        if (!items.isEmpty() && option) {
            double width = getWidth() / items.size();
            for (int i = 0; i < items.size(); i++) {
                if (new Rectangle2D.Double(width * i, 0, width, getHeight()).contains(mouse)) {
                    index = i;
                    break;
                }
            }
        }
        return index;
    }

    private void initAnimator() {
        animator = new Animator(500, new TimingTargetAdapter() {
            @Override
            public void begin() {
                setEditable(!option);
            }

            @Override
            public void timingEvent(float fraction) {
                if (option) {
                    animate = fraction;
                } else {
                    animate = 1f - fraction;
                }
                repaint();
            }
        });
        animator.setResolution(0);
        animator.setDeceleration(0.5f);
        animator.setAcceleration(0.5f);
    }

    @Override
    protected void paintComponent(Graphics grphcs) {
        Graphics2D g2 = (Graphics2D) grphcs.create();
        g2.setColor(new Color(151, 151, 151));
        g2.drawRect(1, 1, getWidth() - 3, getHeight() - 3);
        if (isFocusOwner()) {
            g2.setColor(new Color(60, 158, 255));
            g2.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
        }
        g2.dispose();
        super.paintComponent(grphcs);
    }

    @Override
    public void paint(Graphics grphcs) {
        super.paint(grphcs);
        Graphics2D g2 = (Graphics2D) grphcs.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        paintHint(g2);
        double x = getWidth() - 35;
        double y = 2;
        x -= (x - 2) * animate;
        double height = getHeight() - 4;
        double round = height - height * animate;
        Area area = new Area(new RoundRectangle2D.Double(x, y, height, height, round, round));
        Path2D p = new Path2D.Double();
        p.moveTo(x + height / 2, y);
        p.lineTo(getWidth() - 2, y);
        p.lineTo(getWidth() - 2, y + height);
        p.lineTo(x + height / 2, y + height);
        area.add(new Area(p));
        g2.setPaint(new GradientPaint(new Point2D.Double(x, 0), colorOverlay1, new Point2D.Double(getWidth(), 0), colorOverlay2));
        g2.fill(area);
        shape = area;
        drawItem(g2, x, y, getWidth() - 2, height);
        g2.dispose();
    }

    private void paintHint(Graphics2D g2) {
        if (getText().length() == 0) {
            int h = getHeight();
            Insets ins = getInsets();
            FontMetrics fm = g2.getFontMetrics();
            int c0 = getBackground().getRGB();
            int c1 = getForeground().getRGB();
            int m = 0xfefefefe;
            int c2 = ((c0 & m) >>> 1) + ((c1 & m) >>> 1);
            g2.setColor(new Color(c2, true));
            g2.drawString(hint, ins.left, h / 2 + fm.getAscent() / 2 - 2);
        }
    }

    private void drawItem(Graphics2D g2, double x, double y, double width, double height) {
        double w = width - x;
        double per = w / items.size();
        for (int i = 0; i < items.size(); i++) {
            drawIcon(g2, x + i * per, y, per, height, i);
        }
    }

    private void drawIcon(Graphics2D g2, double x, double y, double width, double height, int index) {
        Composite oldComposite = g2.getComposite();
        if (index != selectedIndex) {
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, animate));
        } else {
            width = (width <= 35 ? 35 : width);
            x = (x > getWidth() - 34 ? getWidth() - 34 : x);
        }
        ImageIcon image = toImage(index);
        double ix = x + ((width - image.getIconWidth()) / 2);
        double iy = y + ((height - image.getIconHeight()) / 2);
        g2.drawImage(image.getImage(), (int) ix, (int) iy, null);
        g2.setComposite(oldComposite);
    }

    private ImageIcon toImage(int index) {
        return (ImageIcon) items.get(index).getIcon();
    }
}
