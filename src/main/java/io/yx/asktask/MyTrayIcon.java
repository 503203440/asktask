package io.yx.asktask;

import com.formdev.flatlaf.FlatIntelliJLaf;
import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Objects;

/**
 * @author YX
 * @date 2022/12/26 16:32
 */
public class MyTrayIcon extends TrayIcon {

    private static final Font defaultFont = new Font("微软雅黑", Font.BOLD, 15);

    static {
        FlatIntelliJLaf.setup();
    }

    public MyTrayIcon(Image image, String tooltip) {
        super(image, tooltip);
        setImageAutoSize(true);
    }

    public static final MyTrayIcon getInstance(Image imageIcon, int width, int height, String tooltip) {
        JDialog jDialog = new JDialog();
        // 无边框
        jDialog.setUndecorated(true);
        // 置顶
        jDialog.setAlwaysOnTop(true);
        // 设置大小
        jDialog.setSize(width, height);
        // 窗体失去焦点监听
        jDialog.addWindowFocusListener(new WindowAdapter() {
            @Override
            public void windowLostFocus(WindowEvent e) {
                jDialog.setVisible(false);
            }
        });

        JPanel jPanel = new JPanel();
        jPanel.setLayout(new BorderLayout());

        // 退出按钮
        JButton exitBtn = new JButton("退出");
        exitBtn.setFont(defaultFont);
        exitBtn.setForeground(Color.decode("#9b59b6"));
        exitBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int dialog = JOptionPane.showConfirmDialog(null, "确定要退出吗?", "提示", JOptionPane.YES_NO_OPTION);
                if (dialog == 0) { // 确定
                    System.exit(0);
                }
            }
        });
        jPanel.add(exitBtn);


        // 面板设置背景色
//        jPanel.setBackground(Color.getColor("#9b59b6"));
        // 面板设置边框颜色
        jPanel.setBorder(new LineBorder(Color.gray, 1));
        jDialog.add(jPanel);
//        jDialog.setOpacity(0.8f);


        MyTrayIcon myTrayIcon = new MyTrayIcon(imageIcon, tooltip);

        int realHeight = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDisplayMode().getHeight();
        int screenHeight = Toolkit.getDefaultToolkit().getScreenSize().height;
        // 判断缩放比例
        double ratio = 1.0d;
        if (!Objects.equals(realHeight, screenHeight)) {
            ratio = (double) realHeight / screenHeight;
        }
        double finalRatio = ratio;


        myTrayIcon.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);

                if (e.getButton() == 3) {
                    int eX = e.getX();
                    int eY = e.getY();
                    // 获取缩放比例下的坐标位置
                    int eXLocation = (int) (eX / finalRatio);
                    int eYLocation = (int) (eY / finalRatio);
                    // 右键
                    jDialog.setLocation(eXLocation, eYLocation - jDialog.getHeight());
                    jDialog.setVisible(true);
                }
            }
        });
        return myTrayIcon;
    }


}
