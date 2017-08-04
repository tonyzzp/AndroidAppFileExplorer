package me.izzp.androidappfileexplorer.demo;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by zzp on 2017-08-04.
 */

public class Util {
    public static String readText(InputStream in) {
        String s = null;
        try {
            ByteArrayOutputStream boas = new ByteArrayOutputStream();
            byte[] buff = new byte[1024 * 4];
            int len = 0;
            while ((len = in.read(buff)) > 0) {
                boas.write(buff, 0, len);
            }
            buff = boas.toByteArray();
            s = new String(buff, "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return s;
    }
}
