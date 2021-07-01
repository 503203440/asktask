package io.yx.asktask;

import java.util.List;

/**
 * @author YX
 * @date 2021/7/1
 */
//@Data
//@AllArgsConstructor
public class AskConfig {
    private Long ScanningInterval;// 时间间隔毫秒
    private Integer ConnectionTimeOut;// http请求超时毫秒
    private String LocalFolderError;//错误文件存放文件夹
    private List<String> Url;//访问的url

    public AskConfig(Long scanningInterval, Integer connectionTimeOut, String localFolderError, List<String> url) {
        ScanningInterval = scanningInterval;
        ConnectionTimeOut = connectionTimeOut;
        LocalFolderError = localFolderError;
        Url = url;
    }

    public Long getScanningInterval() {
        return ScanningInterval;
    }

    public void setScanningInterval(Long scanningInterval) {
        ScanningInterval = scanningInterval;
    }

    public Integer getConnectionTimeOut() {
        return ConnectionTimeOut;
    }

    public void setConnectionTimeOut(Integer connectionTimeOut) {
        ConnectionTimeOut = connectionTimeOut;
    }

    public String getLocalFolderError() {
        return LocalFolderError;
    }

    public void setLocalFolderError(String localFolderError) {
        LocalFolderError = localFolderError;
    }

    public List<String> getUrl() {
        return Url;
    }

    public void setUrl(List<String> url) {
        Url = url;
    }
}
