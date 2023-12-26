package io.yx.asktask;

import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.formdev.flatlaf.FlatIntelliJLaf;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.Objects;

import static io.yx.asktask.Main.userDir;

/**
 * @author YX
 * @date 2022/12/26 16:32
 */
public class MyTrayIcon extends TrayIcon {

    private static final Log log = LogFactory.get(MyTrayIcon.class);
    private static final Font defaultFont = new Font("微软雅黑", Font.BOLD, 15);

    static {
        FlatIntelliJLaf.setup();
    }

    public MyTrayIcon(Image image, String tooltip) {
        super(image, tooltip);
        setImageAutoSize(true);
    }

    public static MyTrayIcon getInstance(Image imageIcon, int width, int height, String tooltip) {
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
        jPanel.setLayout(new GridLayout(2, 1, 0, 5));
        jPanel.setSize(new Dimension(80, 80));

        // 退出按钮
        JButton exitBtn = new JButton("退出");
        exitBtn.setFont(defaultFont);
        exitBtn.setBorder(new EmptyBorder(0, 0, 2, 0));
        exitBtn.setBackground(Color.decode("#ffffff"));
        exitBtn.setForeground(Color.decode("#2f3542"));
        exitBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int dialog = JOptionPane.showConfirmDialog(null, "确定要退出吗?", "提示", JOptionPane.YES_NO_OPTION);
                if (dialog == 0) { // 确定
                    System.exit(0);
                }
            }
        });

        JButton reloadBtn = new JButton("重启应用");
        reloadBtn.setFont(defaultFont);
        reloadBtn.setBorder(new EmptyBorder(0, 0, 0, 0));
        reloadBtn.setBackground(Color.decode("#ffffff"));
        reloadBtn.setForeground(Color.decode("#2f3542"));
        reloadBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // 无需确认直接重启
                // 使用jpackager打包的应用,有一个jvm参数为jpackage.app-path,就是应用程序执行的路径
                String appCommandLine = System.getProperty("jpackage.app-path");
                Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                    ProcessBuilder processBuilder = new ProcessBuilder(appCommandLine);
                    try {
                        processBuilder.start();
                        log.info("已执行重启操作!");
                    } catch (IOException ex) {
                        log.error("重启失败!", ex);
                    }
                }));
                System.exit(1);
            }
        });

        jPanel.add(exitBtn);
        jPanel.add(reloadBtn);


        // 面板设置背景色
        jPanel.setBackground(Color.decode("#ffffff"));
        // 面板设置边框颜色
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
                log.info("button被点击了,button:{}", e.getButton());
                if (e.getButton() == 3) {
                    ThreadUtil.execute(() -> {
                        int eX = e.getX();
                        int eY = e.getY();
                        // 获取缩放比例下的坐标位置
                        int eXLocation = (int) (eX / finalRatio);
                        int eYLocation = (int) (eY / finalRatio);
                        // 右键
                        jDialog.setLocation(eXLocation, eYLocation - jDialog.getHeight());
                        jDialog.setVisible(true);
                    });
                }
            }
        });
        return myTrayIcon;
    }


}
