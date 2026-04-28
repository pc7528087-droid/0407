import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.util.ArrayList;

public class KisaragiSimulator extends JFrame {
    private BufferedImage img;
    private ArrayList<Point> pts = new ArrayList<>();
    private Point vanishingPoint = null;

    public KisaragiSimulator() {
        try {
            // 請確保將如月車站的照片命名為 kisaragi.jpg 並放在同資料夾
            img = ImageIO.read(new File("pic2.jpg")); 
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "找不到 pic2.jpg (如月車站照片)！");
            return;
        }

        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.drawImage(img, 0, 0, null);
                
                // 畫出點擊的點
                g2.setColor(Color.CYAN);
                for (Point p : pts) g2.fillOval(p.x - 5, p.y - 5, 10, 10);

                // 如果點滿四個，畫出延伸線與消失點
                if (vanishingPoint != null) {
                    g2.setStroke(new BasicStroke(2));
                    g2.setColor(Color.YELLOW);
                    // 畫出延伸到消失點的鐵軌線
                    g2.drawLine(pts.get(0).x, pts.get(0).y, vanishingPoint.x, vanishingPoint.y);
                    g2.drawLine(pts.get(2).x, pts.get(2).y, vanishingPoint.x, vanishingPoint.y);
                    
                    // 標註消失點
                    g2.setColor(Color.RED);
                    g2.drawOval(vanishingPoint.x - 10, vanishingPoint.y - 10, 20, 20);
                    g2.drawString("消失點 (無限遠處)", vanishingPoint.x + 15, vanishingPoint.y);
                }
            }
        };

        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (pts.size() < 4) {
                    pts.add(e.getPoint());
                    if (pts.size() == 4) {
                        calculateVanishingPoint();
                    }
                    repaint();
                }
            }
        });

        add(new JScrollPane(panel));
        setTitle("如月車站透視證明 - 請依序點擊左右鐵軌各兩點");
        setSize(1000, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }

    private void calculateVanishingPoint() {
        Point p1 = pts.get(0), p2 = pts.get(1);
        Point p3 = pts.get(2), p4 = pts.get(3);

        double x1 = p1.x, y1 = p1.y, x2 = p2.x, y2 = p2.y;
        double x3 = p3.x, y3 = p3.y, x4 = p4.x, y4 = p4.y;

        double denom = (x1 - x2) * (y3 - y4) - (y1 - y2) * (x3 - x4);
        if (denom != 0) {
            int px = (int) (((x1 * y2 - y1 * x2) * (x3 - x4) - (x1 - x2) * (x3 * y4 - y3 * x4)) / denom);
            int py = (int) (((x1 * y2 - y1 * x2) * (y3 - y4) - (y1 - y2) * (x3 * y4 - y3 * x4)) / denom);
            vanishingPoint = new Point(px, py);
            
            JOptionPane.showMessageDialog(this, 
                "證明成功！\n鐵軌在影像座標 (" + px + ", " + py + ") 匯聚。\n" +
                "這符合針孔相機模型中 Z 趨於無限大時，投影點收斂於一點的特性。");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new KisaragiSimulator());
    }
}
