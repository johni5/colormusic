package com.del.flc;

import com.del.flc.view.MainFrame;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.Field;

import static javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE;

public class Launcher {

    final static Logger logger = Logger.getLogger(Launcher.class);

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.
                        getSystemLookAndFeelClassName());
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
            JFrame.setDefaultLookAndFeelDecorated(true);
            MainFrame mainFrame = new MainFrame();
            mainFrame.setVisible(true);
        });
    }

}
