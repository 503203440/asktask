package io.yx.asktask;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.file.FileReader;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;

import javax.swing.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Timer;
import java.util.*;

/**
 * @author YX
 * @date 2021/7/1
 */
//@Slf4j
public class Main {

    private static final Log log = LogFactory.get(Main.class);

    public static final String userDir = System.getProperty("user.dir");
    public static AskConfig askConfig;

    static {
        try {
            askConfig = askConfig();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e.getMessage(), "初始化配置文件错误", JOptionPane.ERROR_MESSAGE);
            System.exit(-1);
        }
    }

    public static void main(String[] args) {

        // 3 启动定时任务线程
        startTask(askConfig);

    }

    /**
     * 初始化AskConfig
     */
    public static AskConfig askConfig() throws Exception {

        File configFile = new File(userDir + "/config.conf");
        if (!configFile.exists()) {
            try (
                    InputStream inputStream = Main.class.getResourceAsStream("/config.conf");
                    FileOutputStream fos = new FileOutputStream(configFile);
            ) {

                final byte[] bytes = inputStream.readAllBytes();
                fos.write(bytes);
            }
        }
        FileReader fileReader = new FileReader(configFile);
        final List<String> lines = fileReader.readLines();


        long ScanningInterval = 1000L;// 默认时间间隔
        int ConnectionTimeOut = 8000;//默认超时时间
        String LocalFolderError = "D:/error";//默认错误文件日志目录
        List<String> Url = new ArrayList<>();// 访问的url目录


        for (String line : lines) {
            if (StrUtil.isEmpty(line)) {
                continue;
            }
            if (line.startsWith("ScanningInterval")) {
                try {
                    final String intervalStr = line.substring(line.indexOf("=") + 1);
                    ScanningInterval = Long.parseLong(intervalStr);
                } catch (Exception e) {
                    throw new RuntimeException("ScanningInterval配置值错误");
                }
            } else if (line.startsWith("ConnectionTimeOut")) {
                try {
                    final String timeoutStr = line.substring(line.indexOf("=") + 1);
                    ConnectionTimeOut = Integer.parseInt(timeoutStr);
                } catch (Exception e) {
                    throw new RuntimeException("ConnectionTimeOut配置值错误");
                }
            } else if (line.startsWith("LocalFolderError")) {
                LocalFolderError = line.substring(line.indexOf("=") + 1);
            } else if (line.startsWith("Url")) {
                final String url = line.substring(line.indexOf("=") + 1);
                if (!url.startsWith("http")) {
                    throw new RuntimeException("Url配置有误，请检查请求协议是否有http或https");
                }
                Url.add(url);
            }
        }

        File dir = new File(LocalFolderError);
        if (!dir.exists() || dir.isFile()) {
            if (!dir.mkdirs()) {
                throw new RuntimeException("指定的错误文件目录无法创建！LocalFolderError:" + LocalFolderError);
            }
        }

        AskConfig askConfig = new AskConfig(ScanningInterval, ConnectionTimeOut, LocalFolderError, Url);
        System.out.println("配置信息" + askConfig);
        return askConfig;

    }

    /**
     * 启动任务
     */
    public static void startTask(AskConfig askConfig) {
        final List<String> urls = askConfig.getUrl();

        // 有多少个url就需要启动多少个线程
        for (String url : urls) {
            Thread thread = new Thread(() -> {
                runner(url);
            });
            thread.start();
        }

    }

    public static void runner(String url) {
        execHttp(url);
        Timer timer = new Timer(false);
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                runner(url);
            }
        };
        timer.schedule(timerTask, askConfig.getScanningInterval());
    }


    /**
     * 执行http请求
     *
     * @return
     */
    public static void execHttp(String url) {
        try {
            HttpResponse response = HttpRequest.get(url).timeout(askConfig.getConnectionTimeOut()).execute();
            final int status = response.getStatus();
            final String body = response.body();
            if (status != 200) {
                String format = String.format("DATETIME：%s,http状态错误HTTP_STATUS:%s,URL:%s,BODY:%s", DateUtil.now(), status, url, body);
//                String format = String.format("DATETIME：%s,http状态错误HTTP_STATUS:%s,URL:%s", DateUtil.now(), status, url);
                log(format, DateUtil.format(new Date(), "yyyyMMddHHmmsss"));
            }
        } catch (Exception e) {
            String format = String.format("DATETIME：%s,请求失败：%s，URL:%s", DateUtil.now(), e.getMessage(), url);
            log(format, DateUtil.format(new Date(), "yyyyMMddHH"));
        }

    }

    /**
     * 向一个文件中写入内容
     */
    public static void log(String content, String fileName) {
        final String baseDir = askConfig.getLocalFolderError();
        if (baseDir.endsWith("/") || baseDir.endsWith("\\")) {
            fileName = fileName + ".log";
        } else {
            fileName = File.separator + fileName + ".log";
        }
        File file = new File(baseDir + fileName);
        if (!file.exists()) {
            try {
                final boolean newFile = file.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException("创建文件失败" + e.getMessage());
            }
        }
        FileUtil.appendUtf8String(content + "\n", file);
    }


}
