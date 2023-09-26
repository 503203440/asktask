package io.yx.asktask;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.file.FileReader;
import cn.hutool.core.util.RuntimeUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.cron.CronUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.formdev.flatlaf.FlatLightLaf;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
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

    private static final OkHttpClient okHttpClient = new OkHttpClient();


    static {
        FlatLightLaf.setup();

        // 检测系统是否重复启动
        boolean look = SingleAppLock.lock("asktask");
        if (!look) {
            JOptionPane.showMessageDialog(null, "程序已在运行中,请勿重复运行", "警告", JOptionPane.WARNING_MESSAGE);
            System.exit(-1);
        }

        try {
            askConfig = askConfig();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e.getMessage(), "初始化配置文件错误", JOptionPane.ERROR_MESSAGE);
            System.exit(-1);
        }

        // 处理SSLHandshakeException异常
        System.setProperty("https.protocols", "TLSv1.2,TLSv1.1,SSLv3");
    }

    public static void main(String[] args) throws Exception {

        // 显示系统图标

        MenuItem menuItem = new MenuItem("退出");
        menuItem.addActionListener(e -> System.exit(0));

        PopupMenu popupMenu = new PopupMenu();
        popupMenu.add(menuItem);
        popupMenu.setFont(new Font("微软雅黑", Font.PLAIN, 20));


        MyTrayIcon trayIcon = MyTrayIcon.getInstance(new ImageIcon(Objects.requireNonNull(Main.class.getResource("/fly.png"))).getImage(),
                80, 80, "接口监控系统");

        final SystemTray systemTray = SystemTray.getSystemTray();
        systemTray.add(trayIcon);


        // 3 启动定时任务线程
        startTask(askConfig);

        String msg = String.format("正在定时轮询%s个URL\n访问间隔%s毫秒\n超时时间%s毫秒\n错误日志文件夹%s",
                askConfig.getUrl().size(), askConfig.getScanningInterval(), askConfig.getConnectionTimeOut(), askConfig.getLocalFolderError());
        trayIcon.displayMessage("askTask", msg, TrayIcon.MessageType.INFO);
        trayIcon.addActionListener(e -> {
            // 用户点击了消息
            // 打开文件夹
            try {
                Desktop.getDesktop().browse(new URI("file:///" + askConfig.getLocalFolderError().replaceAll("\\\\", "/")));
            } catch (IOException | URISyntaxException err) {
                log.error("打开文件夹失败", err);
            }
        });
        log.info("JVM进程ID:{}", RuntimeUtil.getPid());

        // 启动定时检查更新的任务
        CronUtil.setMatchSecond(true);
        // 每小时检查一次更新
        CronUtil.schedule("0 * * * *", (Runnable) CheckUpdateTask::checkUpdate);
        CronUtil.start(true);

    }

    /**
     * 初始化AskConfig
     */
    public static AskConfig askConfig() throws Exception {

        File configFile = new File(userDir + "/config.conf");
        if (!configFile.exists()) {
            try (
                    InputStream inputStream = Main.class.getResourceAsStream("/config.conf");
                    FileOutputStream fos = new FileOutputStream(configFile)
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
        Set<String> Url = new HashSet<>();// 访问的url目录


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
        final Set<String> urls = askConfig.getUrl();

        // 有多少个url就需要启动多少个线程
        for (String url : urls) {
//            Thread thread = new Thread(() -> runner(url));
            Thread.startVirtualThread(() -> runner(url));
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
     */
    public static void execHttp(String url) {
        try {
            Request request = new Request.Builder()
                    .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:109.0) Gecko/20100101 Firefox/114.0")
                    .url(url).build();
            try (Response response = okHttpClient.newCall(request).execute()) {
                int status = response.code();
                if (status != 200) {
                    String body = response.body().string();
                    String format = String.format("DATETIME：%s,http状态错误HTTP_STATUS:%s,URL:%s,BODY:%s", DateUtil.now(), status, url, body);
                    log(format, DateUtil.format(new Date(), "yyyyMMddHH"));
                }
            }
//            HttpResponse response = HttpRequest.get(url).timeout(askConfig.getConnectionTimeOut()).execute();
//            final int status = response.getStatus();
//            final String body = response.body();
        } catch (Exception e) {
            log.error(e.getMessage());
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
