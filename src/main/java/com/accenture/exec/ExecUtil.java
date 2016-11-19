package com.accenture.exec;

import java.io.*;
import java.util.*;
import java.util.concurrent.CountDownLatch;

/**
 * Created by THINK on 2016/11/19.
 */
public class ExecUtil {

    public static void exec(File workDir, OutputHandler handler, String... cmds) {

    }

    public static Thread execNonBlock(File workDir, OutputHandler handler, String... cmds) {
        final ProcessBuilder process = createProcess(workDir, cmds);
        Thread thread = new ProcessExcuter(process, handler);
        thread.start();
        return thread;
    }

    private static ProcessBuilder createProcess(File workDir, String... cmds) {
        List<String> cmdLine = new ArrayList<String>();
        String property = System.getProperty("os.name");
        if (property.toLowerCase().contains("windows")) {
            System.out.println(property);
            cmdLine.add("cmd");
            cmdLine.add("/c");
        }
        cmdLine.addAll(Arrays.asList(cmds));
        ProcessBuilder pb = new ProcessBuilder(cmdLine);
        pb.directory(workDir);
        return pb;
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        File directory = new File("E:\\kafka_2.11-0.10.1.0\\");
        final CountDownLatch zookeeperLatch = new CountDownLatch(1);

        Thread startZookeeper = execNonBlock(directory,
                new OutputHandler() {
                    @Override
                    public void handleLine(final String line) {
                        System.out.println(line);
                        if (line.toLowerCase().contains("binding to port 0.0.0.0/0.0.0.0:2181")) {
                            zookeeperLatch.countDown();
                        }else if(line.contains("ERROR Unexpected exception, exiting abnormally")){
                            System.exit(1);
                        }
                    }
                }, "start /b bin\\windows\\zookeeper-server-start.bat",
                "config\\zookeeper.properties");
        zookeeperLatch.await();
        final CountDownLatch kafkaLatch = new CountDownLatch(1);
        Thread startKafka = execNonBlock(directory,
                new OutputHandler() {
                    @Override
                    public void handleLine(final String line) {
                        System.out.println(line);
                        if (line.contains("started (kafka.server.KafkaServer)")) {
                            kafkaLatch.countDown();
                        }
                    }
                }, "bin\\windows\\kafka-server-start.bat",
                "config\\server.properties");
        kafkaLatch.await();
        System.out.println("done");
    }
}
