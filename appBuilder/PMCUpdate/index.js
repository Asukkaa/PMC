'use strict';

const i18n = require('./i18n.js');

exports.main = async (event) => {
    // 解析请求体
    let requestBody;
    let clientLang = 'zh-CN';
    try {
        requestBody = JSON.parse(event.body);
        // 获取客户端语言类型
        clientLang = requestBody.lang || 'zh-CN';
    } catch (e) {
        return {
            code: 400,
            message: i18n[clientLang]?.invalidJson || 'Invalid JSON request body'
        };
    }
    // 获取客户端操作系统类型和版本
    const clientOS = requestBody.os;
    // 客户端当前版本
    const clientVersion = requestBody.version;
    // 验证操作系统类型
    if (!clientOS || (clientOS !== 'win' && clientOS !== 'mac')) {
        return {
            code: 400,
            message: i18n[clientLang]?.invalidOS || 'Invalid OS parameter'
        };
    }
    // win 版本设置
    const winVersion = "4.4.1";
    const winBuildDate = "2026.07.14";
    // mac 版本设置
    const macVersion = "4.4.0";
    const macBuildDate = "2026.05.12";
    // 根据客户端操作系统选择对应的版本信息
    let serverVersion, buildDate;
    if (clientOS === 'win') {
        serverVersion = winVersion;
        buildDate = winBuildDate;
    } else {
        serverVersion = macVersion;
        buildDate = macBuildDate;
    }
    // 从 i18n 中获取当前语言和平台对应的更新说明
    const langData = i18n[clientLang] || i18n['zh-CN'];
    const versionPrefix = langData.versionPrefix;
    const whatsNewItems = langData.whatsNew?.[clientOS] || '';
    // 拼接带序号的更新内容
    const lines = whatsNewItems.trim().split('\n').map(line => line.trim()).filter(Boolean);
    const numberedLines = lines.map((line, index) => `\n${index + 1}. ${line}`);
    const whatsNew = `${versionPrefix} ${serverVersion} :\n${numberedLines.join('\n')}`;
    // 版本号对比逻辑（使用对应平台的 serverVersion）
    let fullUpdate = false;
    if (clientVersion) {
        try {
            // 只版本号相同时为增量更新
            if (serverVersion !== clientVersion) {
                fullUpdate = true;
            }
        } catch (e) {
            // 版本号格式错误时按全量更新处理
            fullUpdate = true;
        }
    } else {
        // 未提供版本号时按全量更新处理
        fullUpdate = true;
    }
    // 阿里云全量更新
    const aliyunAppLinks = {
        win: `https://mp-f42cc448-2bf2-4edf-9bb8-8f060ec60dd6.cdn.bspapp.com/PMC/win/Perfect Mouse Control-${winVersion}-win.zip`,
        mac: `https://mp-f42cc448-2bf2-4edf-9bb8-8f060ec60dd6.cdn.bspapp.com/PMC/mac/Perfect Mouse Control-${macVersion}-mac.zip`
    };
    // 阿里云增量更新
    const aliyunLibLinks = {
        win: `https://mp-f42cc448-2bf2-4edf-9bb8-8f060ec60dd6.cdn.bspapp.com/PMC/win/lib-${winVersion}-win.zip`,
        mac: `https://mp-f42cc448-2bf2-4edf-9bb8-8f060ec60dd6.cdn.bspapp.com/PMC/mac/lib-${macVersion}-mac.zip`
    };
    // 支付宝云全量更新
    const alipayAppLinks = {
        win: `https://env-00jxtp3qdq80.normal.cloudstatic.cn/PMC/win/Perfect%20Mouse%20Control-${winVersion}-win.zip`,
        mac: `https://env-00jxtp3qdq80.normal.cloudstatic.cn/PMC/mac/Perfect%20Mouse%20Control-${macVersion}-mac.zip`
    };
    // 支付宝云增量更新
    const alipayLibLinks = {
        win: `https://env-00jxtp3qdq80.normal.cloudstatic.cn/PMC/win/lib-${winVersion}-win.zip`,
        mac: `https://env-00jxtp3qdq80.normal.cloudstatic.cn/PMC/mac/lib-${macVersion}-mac.zip`
    };
    let aliyunFileLink, alipayFileLink;
    if (fullUpdate) {
        aliyunFileLink = aliyunAppLinks[clientOS];
        alipayFileLink = alipayAppLinks[clientOS];
    } else {
        aliyunFileLink = aliyunLibLinks[clientOS];
        alipayFileLink = alipayLibLinks[clientOS];
    }
    // 二次校验（理论上不会触发，因为已提前校验 OS）
    if (!aliyunFileLink || !alipayFileLink) {
        return {
            code: 400,
            message: i18n[clientLang]?.invalidOS || 'Invalid OS parameter'
        };
    }
    try {
        // 构造响应对象
        const latestVersionInfo = {
            version: serverVersion,
            buildDate: buildDate,
            whatsNew: whatsNew,
            aliyunFileLink: aliyunFileLink,
            alipayFileLink: alipayFileLink,
            fullUpdate: fullUpdate
        };
        // 返回结果
        return {
            code: 200,
            data: latestVersionInfo
        };
    } catch (error) {
        return {
            code: 500,
            message: `${error.message}`
        };
    }
};
