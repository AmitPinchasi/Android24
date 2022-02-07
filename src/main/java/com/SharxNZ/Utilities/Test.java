package com.SharxNZ.Utilities;


import java.io.*;
import java.net.*;

public class Test {
    String aFile;
    String aURL;



    public Test(String u, String s) {
        aURL = u;
        aFile = s;
    }

    public void doit() {
        DataInputStream di = null;
        FileOutputStream fo = null;
        byte[] b = new byte[1];

        try {
            System.out.println("Sucking " + aFile);
            System.out.println("   at " + aURL);
            // input
            URL url = new URL(aURL + aFile);
            URLConnection urlConnection = url.openConnection();
            urlConnection.connect();
            di = new DataInputStream(urlConnection.getInputStream());

            // output
            fo = new FileOutputStream(aFile);

            //  copy the actual file
            //   (it would better to use a buffer bigger than this)
            while (-1 != di.read(b, 0, 1))
                fo.write(b, 0, 1);
            di.close();
            fo.close();
        } catch (Exception ex) {
            System.out.println("Oups!!!");
            ex.printStackTrace();
            System.exit(1);
        }
        System.out.println("done.");
    }
}

