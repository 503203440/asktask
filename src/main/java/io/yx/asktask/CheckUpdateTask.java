package io.yx.asktask;

import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * @author YX
 * @date 2022/12/8 10:25
 */
public class CheckUpdateTask {

    private static final Log log = LogFactory.get(CheckUpdateTask.class);

    // 获取更新信息的url
    private static final String updateInfoUrl = "https://gitee.com/YX503203440/asktask/raw/master/src/main/resources/updateVersion.json";

    /**
     * 检查更新
     */
    public static void checkUpdate() {
        try {
            HttpResponse httpResponse = HttpUtil.createGet(updateInfoUrl, true).execute();
            JSONObject jsonObject = JSONUtil.parseObj(httpResponse.body());

            BigDecimal lastVersion = jsonObject.getBigDecimal("version");
            String updateInfo = jsonObject.getStr("updateInfo");
            String packageUrl = jsonObject.getStr("packageUrl");

            if (lastVersion.compareTo(getCurrentVersion()) > 0) {
                // 提示用户下载更新
                int i = JOptionPane.showConfirmDialog(null, updateInfo,
                        "发现新版本", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
                if (i == 0) {
                    // 打开下载url
                    Desktop desktop = Desktop.getDesktop();
                    desktop.browse(URI.create(packageUrl));
                }

            }
        } catch (Exception ignore) {
        }

    }


    public static BigDecimal getCurrentVersion() {
        String versionJsonStr = ResourceUtil.readStr("currentVersion.json", StandardCharsets.UTF_8);
        log.info("versionJson:\n{}", JSONUtil.formatJsonStr(versionJsonStr));
        return JSONUtil.parseObj(versionJsonStr).getBigDecimal("version");
    }

    public static void main(String[] args) {
        checkUpdate();
    }

}
