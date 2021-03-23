package com.del.flc.view;

import com.del.flc.utils.SystemEnv;
import com.del.flc.utils.Utils;
import org.apache.log4j.Logger;

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

public class MainFrameActions implements WindowListener {

    final static private Logger logger = Logger.getLogger(MainFrameActions.class);

    private AutoCloseable rxtx;

    public MainFrameActions(AutoCloseable rxtx) {
        this.rxtx = rxtx;
    }

    @Override
    public void windowOpened(WindowEvent e) {
        logger.info("================================= WINDOW OPENED =================================");
        logger.info("Version: " + Utils.getInfo().getString("version.info"));
        logger.info("Loading system variables...");
        for (SystemEnv value : SystemEnv.values()) {
            logger.info("\t\t" + value.getName() + "=" + value.read());
        }
        logger.info("... success.");
    }

    @Override
    public void windowClosing(WindowEvent e) {
        logger.info("================================= WINDOW CLOSING =================================");
        try {
            rxtx.close();
        } catch (Exception e1) {
            logger.error("RxTx close error", e1);
        }
        System.exit(0);
    }

    @Override
    public void windowClosed(WindowEvent e) {

    }

    @Override
    public void windowIconified(WindowEvent e) {

    }

    @Override
    public void windowDeiconified(WindowEvent e) {

    }

    @Override
    public void windowActivated(WindowEvent e) {

    }

    @Override
    public void windowDeactivated(WindowEvent e) {

    }
}
