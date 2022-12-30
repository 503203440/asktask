package io.yx.asktask;

import com.formdev.flatlaf.FlatDarkLaf;

import javax.swing.*;

/**
 * @author YX
 * @date 2022/12/28 16:53
 */
public class MyFrame extends JFrame {

    public static void main(String[] args) {

        FlatDarkLaf.setup();

        MyFrame myFrame = new MyFrame();
        myFrame.setTitle("测试");
        myFrame.setSize(600, 400);
        myFrame.setLocation(500, 300);
        myFrame.setVisible(true);
        myFrame.setDefaultCloseOperation(EXIT_ON_CLOSE);

    }

}
