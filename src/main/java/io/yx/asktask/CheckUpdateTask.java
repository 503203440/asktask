package io.yx.asktask;

import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;

import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * @author YX
 * @date 2022/12/8 10:25
 */
public class CheckUpdateTask {

    private static final Log log = LogFactory.get(CheckUpdateTask.class);

    // 获取更新信息的url
    private static final String updateInfoUrl = "";

    /**
     * 检查更新
     */
    public static void checkUpdate() {
        HttpResponse httpResponse = HttpUtil.createGet(updateInfoUrl, true).execute();
        JSONObject jsonObject = JSONUtil.parseObj(httpResponse.body());

        BigDecimal lastVersion = jsonObject.getBigDecimal("version");
        String updateInfo = jsonObject.getStr("updateInfo");
        String packageUrl = jsonObject.getStr("packageUrl");

        if (lastVersion.compareTo(getCurrentVersion()) > 0) {
            // 提示用户下载更新

        }

    }


    public static BigDecimal getCurrentVersion() {
        String versionJsonStr = ResourceUtil.readStr("currentVersion.json", StandardCharsets.UTF_8);
        log.info("versionJson:{}", JSONUtil.formatJsonStr(versionJsonStr));
        return JSONUtil.parseObj(versionJsonStr).getBigDecimal("version");
    }

    public static void main(String[] args) {
        checkUpdate();
    }

}
