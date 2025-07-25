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
    const serverVersion = "3.1.0";
    // 版本号对比逻辑
    let fullUpdate = false;
    if (clientVersion) {
        try {
            // 拆分版本号为数字数组
            const client = clientVersion.split('.').map(Number);
            const server = serverVersion.split('.').map(Number);
            // 检查前两位版本号是否不同
            if (client[0] !== server[0] || client[1] !== server[1]) {
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
        win: 'https://mp-f42cc448-2bf2-4edf-9bb8-8f060ec60dd6.cdn.bspapp.com/Perfect Mouse Control 3.1.0-win.zip',
        mac: 'https://mp-f42cc448-2bf2-4edf-9bb8-8f060ec60dd6.cdn.bspapp.com/Perfect Mouse Control 3.1.0-mac.zip'
    };
    // 阿里云增量更新文件存储地址
    const aliyunLibLinks = {
        win: 'https://mp-f42cc448-2bf2-4edf-9bb8-8f060ec60dd6.cdn.bspapp.com/lib-3.1.0-win.zip',
        mac: 'https://mp-f42cc448-2bf2-4edf-9bb8-8f060ec60dd6.cdn.bspapp.com/lib-3.1.0-mac.zip'
    };
    // 支付宝云全量更新文件存储地址
    const alipayAppLinks = {
        win: 'https://env-00jxtp3qdq80.normal.cloudstatic.cn/Perfect Mouse Control 3.1.0-win.zip?expire_at=1753435054&er_sign=37346409499ef6fa340b102c4517a413',
        mac: 'https://env-00jxtp3qdq80.normal.cloudstatic.cn/Perfect Mouse Control 3.1.0-mac.zip?expire_at=1753435070&er_sign=f90f90c60ee28b50cb55c4e72f6d268a'
    };
    // 支付宝云增量更新文件存储地址
    const alipayLibLinks = {
        win: 'https://env-00jxtp3qdq80.normal.cloudstatic.cn/lib-3.1.0-win.zip?expire_at=1753435080&er_sign=e88ad16768bbe4ec74948b690bc1e64e',
        mac: 'https://env-00jxtp3qdq80.normal.cloudstatic.cn/lib-3.1.0-mac.zip?expire_at=1753435093&er_sign=5a3ab69631a2f575678de03cd65f8682'
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
            buildDate: "2025.07.25",
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