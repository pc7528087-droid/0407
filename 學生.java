import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.util.ArrayList;

public class HeightTool extends JFrame {
    private BufferedImage displayImg; // 縮放後顯示用的圖
    private ArrayList<Point> pts = new ArrayList<>();
    private final double REF_ACTUAL_HEIGHT = 175.0; // 基準身高
    private Point vanishingPoint = null;
    
    private String[] steps = {
        "1. 點擊左側平行線-起點", "2. 點擊左側平行線-終點", 
        "3. 點擊右側平行線-起點", "4. 點擊右側平行線-終點", 
        "5. 【基準同學 175cm】頭頂", "6. 【基準同學 175cm】腳底", 
        "7. 【目標同學】頭頂", "8. 【目標同學】腳底"
    };

    public HeightTool() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("請選擇步道照片");
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                BufferedImage rawImg = ImageIO.read(fileChooser.getSelectedFile());
                
                // --- 自動縮放邏輯 ---
                int screenWidth = 1000; // 設定顯示寬度為 1000 像素
                double scale = (double) screenWidth / rawImg.getWidth();
                int screenHeight = (int) (rawImg.getHeight() * scale);
                
                displayImg = new BufferedImage(screenWidth, screenHeight, BufferedImage.TYPE_INT_RGB);
                Graphics2D g2d = displayImg.createGraphics();
                // 設定高品質縮放
                g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                g2d.drawImage(rawImg, 0, 0, screenWidth, screenHeight, null);
                g2d.dispose();
                
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "讀取失敗: " + e.getMessage());
                System.exit(0);
            }
        } else { System.exit(0); }

        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (displayImg != null) g.drawImage(displayImg, 0, 0, null);
                
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // 畫出消失線輔助
                if (pts.size() >= 4) {
                    g2.setColor(new Color(0, 255, 255, 100));
                    g2.drawLine(pts.get(0).x, pts.get(0).y, pts.get(1).x, pts.get(1).y);
                    g2.drawLine(pts.get(2).x, pts.get(2).y, pts.get(3).x, pts.get(3).y);
                }

                // 畫出點位
                for (int i = 0; i < pts.size(); i++) {
                    g2.setColor(i < 4 ? Color.CYAN : (i < 6 ? Color.GREEN : Color.RED));
                    g2.fillOval(pts.get(i).x - 5, pts.get(i).y - 5, 10, 10);
                    g2.drawString("P" + (i + 1), pts.get(i).x + 10, pts.get(i).y);
                }
            }
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(displayImg.getWidth(), displayImg.getHeight());
            }
        };

        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (pts.size() < 8) {
                    pts.add(e.getPoint());
                    if (pts.size() == 4) calculateVP();
                    if (pts.size() < 8) setTitle("進度: " + steps[pts.size()]);
                    repaint();
                    if (pts.size() == 8) calculateResult();
                }
            }
        });

        add(new JScrollPane(panel));
        setTitle(steps[0]);
        pack(); // 自動調整視窗大小
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setVisible(true);
    }

    private void calculateVP() {
        double x1=pts.get(0).x, y1=pts.get(0).y, x2=pts.get(1).x, y2=pts.get(1).y;
        double x3=pts.get(2).x, y3=pts.get(2).y, x4=pts.get(3).x, y4=pts.get(3).y;
        double d = (x1-x2)*(y3-y4) - (y1-y2)*(x3-x4);
        if (Math.abs(d) < 0.1) {
            vanishingPoint = new Point(500, -10000); // 近乎平行
        } else {
            int px = (int)(((x1*y2-y1*x2)*(x3-x4)-(x1-x2)*(x3*y4-y3*x4))/d);
            int py = (int)(((x1*y2-y1*x2)*(y3-y4)-(y1-y2)*(x3*y4-y3*x4))/d);
            vanishingPoint = new Point(px, py);
        }
    }

    private void calculateResult() {
        double vy = vanishingPoint.y;
        double h1 = Math.abs(pts.get(5).y - pts.get(4).y);
        double y1 = pts.get(5).y;
        double h2 = Math.abs(pts.get(7).y - pts.get(6).y);
        double y2 = pts.get(7).y;

        // 透視比例公式
        double result = REF_ACTUAL_HEIGHT * (h2 * (y1 - vy)) / (h1 * (y2 - vy));

        JOptionPane.showMessageDialog(this, String.format("計算成功！\n目標預估身高: %.2f cm", result));
        
        // 標註完成後重置最後兩點，方便測量下一個人
        pts.remove(7); pts.remove(6);
        setTitle("請標記下一個目標學生的頭頂");
        repaint();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new HeightTool());
    }
}
