package com.del.flc.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class Configure {

    private static Configure instance;
    private Properties p;

    private Configure() {
        p = new Properties();
    }

    public static Configure getInstance() {
        if (instance == null) {
            instance = new Configure();
        }
        return instance;
    }

    public String getPortName() {
        return p.getProperty("portName");
    }

    public void setPortName(String name) {
        p.setProperty("portName", name);
    }

    public void load() throws IOException, CommonException {
        try (FileInputStream fis = new FileInputStream(getConfigFile())) {
            p.loadFromXML(fis);
        }
    }

    public void save() throws IOException, CommonException {
        try (FileOutputStream fos = new FileOutputStream(getConfigFile());) {
            p.storeToXML(fos, "");
        }
    }

    private File getConfigFile() throws IOException, CommonException {
        File file = new File(Utils.nvl(SystemEnv.APP_HOME_DIR.read(), "") + "\\conf.xml");
        if (!file.exists() && file.createNewFile()) {
            try (FileOutputStream fos = new FileOutputStream(file)) {
                p.storeToXML(fos, "");
            }
        }
        if (file.exists()) {
            return file;
        }
        throw new CommonException("Не могу создать файл конфигурации");
    }

}
