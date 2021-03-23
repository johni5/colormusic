package com.del.flc.view;


import com.del.flc.ColorMusic;
import com.del.flc.rxtx.*;
import com.del.flc.utils.Logger;
import com.del.flc.utils.Utils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


/**
 * Created by DodolinEL
 * date: 02.07.2019
 */
public class MainFrame extends JFrame implements ConnectionListener, Logger {

    private JTextPane logArea;
    private static SimpleDateFormat LOG_SDF = new SimpleDateFormat("dd/MM/yy HH:mm:ss");
    private ColorMusic colorMusic;
    private Connection connectionManager;

    private JLabel state1;
    private JLabel state2;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.
                        getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            JFrame.setDefaultLookAndFeelDecorated(true);
            JFrame mainFrame = new MainFrame();
            mainFrame.setVisible(true);
        });
    }

    public MainFrame() {
        setTitle(String.format("Версия %s", Utils.getInfo().getString("version.info")));
        setIconImage(Toolkit.getDefaultToolkit().getImage(this.getClass().getResource("/img/ico_64x64.png")));
        setBounds(100, 100, 600, 600);
        setResizable(false);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        connectionManager = new ConnectionManager2(this, this);
        colorMusic = new ColorMusic(connectionManager);
        addWindowListener(new MainFrameActions(() -> connectionManager.exit()));

        JMenuBar menuBar = new JMenuBar();
        setJMenuBar(menuBar);
        JMenu menuFile = new JMenu("Файл");
        menuBar.add(menuFile);
        JMenuItem menuItemRestart = new JMenuItem("Рестарт");
        menuItemRestart.addActionListener( e -> connectionManager.reset());
        JMenuItem menuItemExit = new JMenuItem("Выход");
        menuItemExit.addActionListener(arg0 -> dispatchEvent(new WindowEvent(MainFrame.this, WindowEvent.WINDOW_CLOSING)));
        menuFile.add(new JSeparator());
        menuFile.add(menuItemRestart);
        menuFile.add(menuItemExit);

        // Область логирования
        logArea = new JTextPane();
        logArea.setContentType("text/html");
        logArea.setBackground(Color.DARK_GRAY);
        logArea.setEditable(false);
//        logArea.setPreferredSize(new Dimension(logArea.getWidth(), 100));
        JScrollPane logScroll = new JScrollPane();
        logScroll.setViewportView(logArea);

        // Область настроек
        JPanel topPanel = new JPanel();

        // Область статуса
        JPanel footerPanel = new JPanel(new GridLayout(1, 0));
        state1 = new JLabel("Статус 1");
        JSeparator sep = new JSeparator(SwingConstants.VERTICAL);
        state2 = new JLabel("Статус 2");
        footerPanel.add(state1);
        footerPanel.add(sep);
        footerPanel.add(state2);

        // Область кнопок
        JPanel mainPanel = new JPanel();
        mainPanel.setPreferredSize(new Dimension(400, 400));

        topPanel.add(colorMusic.getStateON().getComponent());
        topPanel.add(colorMusic.getMode().getComponent());
        topPanel.add(colorMusic.getAutoTune().getComponent());

        mainPanel.add(colorMusic.getLightMode().getComponent());
        mainPanel.add(colorMusic.getFreqStrobeMode().getComponent());
        mainPanel.add(colorMusic.getBrightness().getComponent());
        mainPanel.add(colorMusic.getEmptyBright().getComponent());

        mainPanel.add(colorMusic.getSmooth().getComponent());
        mainPanel.add(colorMusic.getRainbowStep().getComponent());
        mainPanel.add(colorMusic.getSmoothFreq().getComponent());
        mainPanel.add(colorMusic.getMaxCoefFreq().getComponent());
        mainPanel.add(colorMusic.getStrobeSmooth().getComponent());
        mainPanel.add(colorMusic.getStrobePeriod().getComponent());
        mainPanel.add(colorMusic.getLightColor().getComponent());
        mainPanel.add(colorMusic.getLightSat().getComponent());
        mainPanel.add(colorMusic.getColorSpeed().getComponent());
        mainPanel.add(colorMusic.getRainbowPeriod().getComponent());
        mainPanel.add(colorMusic.getRainbowStep2().getComponent());
        mainPanel.add(colorMusic.getRunningSpeed().getComponent());
        mainPanel.add(colorMusic.getHueStep().getComponent());
        mainPanel.add(colorMusic.getHueStart().getComponent());

        JSplitPane sp1 = new JSplitPane(JSplitPane.VERTICAL_SPLIT, mainPanel, logScroll);
        sp1.setBorder(null);

        getContentPane().add(topPanel, BorderLayout.NORTH);
        getContentPane().add(sp1, BorderLayout.CENTER);
        getContentPane().add(footerPanel, BorderLayout.SOUTH);

        setLocale(new Locale("RU"));
//        pack();
        connectionManager.start();

    }

    @Override
    public void info(String m) {
        setStatusText(m);
    }

    @Override
    public void error(String m) {
        setStatusError(m);
    }

    @Override
    public void error(String m, Throwable t) {
        setStatusError(String.format("%s [%s]", m, t.getMessage()));
    }

    @Override
    public void connectionChangeState(ConnectionEvent e) {
        switch (e.getEvent()) {
            case READY:
                state1.setText("Подключение...");
                state2.setText("");
                colorMusic.update(-1, -1);
                break;
            case CONNECTED:
                state1.setText("Подключено: " + e.getPortName());
                state2.setText("");
                colorMusic.readState();
                break;
            case WAIT:
                state2.setText("");
                break;
            case RECEIVE:
                state2.setText("Получение");
                break;
            case TRANSMIT:
                state2.setText("Отправка");
                break;
            case BREAK:
                state1.setText("Отключился");
                state2.setText("");
                break;
        }
    }

    public void setStatusText(String message) {
        log(message, "white");
    }

    public void setStatusError(String message) {
        log(message, "red");
    }

    private void log(String message, String color) {
        String f = "<font color=\"gray\" size=\"3\" face=\"Tahoma\">%s: </font><font color=\"%s\" size=\"3\" face=\"Tahoma\">%s</font><br/>%s";
        logArea.setText(String.format(f, LOG_SDF.format(new Date()), color, message, logArea.getText()));
        logArea.setCaretPosition(0);
    }

}
