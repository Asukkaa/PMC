'use strict';

const i18n = require('./i18n.js');

exports.main = async (event) => {
    // 解析请求体
    let requestBody;
    let clientLang;
    try {
        requestBody = JSON.parse(event.body);
        // 获取客户端语言类型
        clientLang = requestBody.lang || 'zh-CN';
    } catch (e) {
        return {
            code: 400,
            message: i18n[clientLang].invalidJson
        };
    }
    // 获取客户端操作系统类型
    const clientOS = requestBody.os;
    // 客户端当前版本
    const clientVersion = requestBody.version;
    // 服务端最新版本
    const serverVersion = "3.1.1";
    // 版本号对比逻辑
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
    // 阿里云全量更新文件存储地址
    const aliyunAppLinks = {
        win: 'https://mp-f42cc448-2bf2-4edf-9bb8-8f060ec60dd6.cdn.bspapp.com/PMC/win/Perfect Mouse Control-3.1.1-win.zip',
        mac: 'https://mp-f42cc448-2bf2-4edf-9bb8-8f060ec60dd6.cdn.bspapp.com/PMC/mac/Perfect Mouse Control-3.1.1-mac.zip'
    };
    // 阿里云增量更新文件存储地址
    const aliyunLibLinks = {
        win: 'https://mp-f42cc448-2bf2-4edf-9bb8-8f060ec60dd6.cdn.bspapp.com/PMC/win/lib-3.1.1-win.zip',
        mac: 'https://mp-f42cc448-2bf2-4edf-9bb8-8f060ec60dd6.cdn.bspapp.com/PMC/mac/lib-3.1.1-mac.zip'
    };
    // 支付宝云全量更新文件存储地址
    const alipayAppLinks = {
        win: 'https://env-00jxtp3qdq80.normal.cloudstatic.cn/PMC/win/Perfect%20Mouse%20Control-3.1.1-win.zip',
        mac: 'https://env-00jxtp3qdq80.normal.cloudstatic.cn/PMC/mac/Perfect%20Mouse%20Control-3.1.1-mac.zip'
    };
    // 支付宝云增量更新文件存储地址
    const alipayLibLinks = {
        win: 'https://env-00jxtp3qdq80.normal.cloudstatic.cn/PMC/win/lib-3.1.1-win.zip',
        mac: 'https://env-00jxtp3qdq80.normal.cloudstatic.cn/PMC/mac/lib-3.1.1-mac.zip'
    };
    let aliyunFileLink, alipayFileLink;
    if (fullUpdate) {
        aliyunFileLink = aliyunAppLinks[clientOS];
        alipayFileLink = alipayAppLinks[clientOS];
    } else {
        aliyunFileLink = aliyunLibLinks[clientOS];
        alipayFileLink = alipayLibLinks[clientOS];
    }
    // 验证操作系统类型
    if (!aliyunFileLink || !alipayFileLink) {
        return {
            code: 400,
            message: i18n[clientLang].invalidOS
        };
    }
    try {
        // 构造响应对象
        const latestVersionInfo = {
            version: serverVersion,
            buildDate: "2025.08.01",
            whatsNew: i18n[clientLang].whatsNew,
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