package com.del;

import com.fazecast.jSerialComm.SerialPort;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.Scanner;

/**
 * Unit test for simple App.
 */
public class AppTest {

    public static void main(String[] args) {
        try {
            SerialPort[] commPorts = SerialPort.getCommPorts();
            for (SerialPort commPort : commPorts) {
                System.out.println(commPort.getDescriptivePortName()); // отображаемое имя
                System.out.println(commPort.getSystemPortName()); // имя для инициализации
                System.out.println(commPort.getBaudRate());
            }
            Scanner scanner = new Scanner(System.in);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
