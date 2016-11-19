package com.accenture.exec;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Scanner;

/**
 * Created by THINK on 2016/11/19.
 */
public class ProcessExcuter extends Thread {
    private final ProcessBuilder pb;
    private final OutputHandler outputHandler;
    private volatile InputStream in;

    public ProcessExcuter(final ProcessBuilder pb, final OutputHandler outputHandler) {
        this.pb = pb;
        this.outputHandler = outputHandler;
    }

    @Override
    public void run() {
        super.run();
        try {
            startProcess();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void startProcess() throws IOException, InterruptedException {
        pb.redirectErrorStream(true);
        Process p = pb.start();
        in = p.getInputStream();
        try {
            BufferedReader bf = new BufferedReader(new InputStreamReader(in));

            while ((!isInterrupted())) {
                if(bf.ready()){
                    outputHandler.handleLine(bf.readLine());
                }
            }
            bf.close();
        } finally {
            p.destroy();
        }

    }

    @Override
    public void interrupt() {
        if (in != null) {
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        super.interrupt();
    }
}
