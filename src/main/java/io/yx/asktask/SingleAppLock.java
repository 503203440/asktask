package io.yx.asktask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;

/**
 * 避免多次运行同一个java程序
 */
public class SingleAppLock {

    private static final Logger log = LoggerFactory.getLogger(SingleAppLock.class);

    static FileLock fileLock = null;
    static FileChannel fileChannel = null;
    static RandomAccessFile raf = null;

    public static boolean lock(String key) {
        String tmpdir = System.getProperty("java.io.tmpdir");
        if (!tmpdir.endsWith(File.separator)) {
            tmpdir = tmpdir + File.separator;
        }
        File file = new File(tmpdir + key + ".lock");

        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            // 尝试获取文件锁            raf = new RandomAccessFile(file, "rw");
            fileChannel = raf.getChannel();
            fileLock = fileChannel.tryLock();
            if (fileLock != null) {
                log.info("获取锁成功");
                return true;
            }
        } catch (Exception e) {
            log.error("ERROR", e);
        }

        return false;
    }

}
