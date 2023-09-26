package io.yx.asktask;

import java.util.Set;

/**
 * @author YX
 * @date 2021/7/1
 */
//@Data
//@AllArgsConstructor
public class AskConfig {
    private long ScanningInterval;// 时间间隔毫秒
    private int ConnectionTimeOut;// http请求超时毫秒
    private String LocalFolderError;//错误文件存放文件夹
    private Set<String> Url;//访问的url

    public AskConfig(long scanningInterval, int connectionTimeOut, String localFolderError, Set<String> url) {
        ScanningInterval = scanningInterval;
        ConnectionTimeOut = connectionTimeOut;
        LocalFolderError = localFolderError;
        Url = url;
    }

    public long getScanningInterval() {
        return ScanningInterval;
    }

    public void setScanningInterval(long scanningInterval) {
        ScanningInterval = scanningInterval;
    }

    public int getConnectionTimeOut() {
        return ConnectionTimeOut;
    }

    public void setConnectionTimeOut(int connectionTimeOut) {
        ConnectionTimeOut = connectionTimeOut;
    }

    public String getLocalFolderError() {
        return LocalFolderError;
    }

    public void setLocalFolderError(String localFolderError) {
        LocalFolderError = localFolderError;
    }

    public Set<String> getUrl() {
        return Url;
    }

    public void setUrl(Set<String> url) {
        Url = url;
    }
}
