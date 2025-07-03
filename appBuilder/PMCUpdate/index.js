'use strict';

exports.main = async (event) => {
    // 解析请求体
    let requestBody;
    try {
        requestBody = JSON.parse(event.body);
    } catch (e) {
        return {
            code: 400,
            message: "无效的JSON请求体"
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
        win: 'https://env-00jxtp3qdq80.normal.cloudstatic.cn/Perfect Mouse Control 3.1.0-win.zip?expire_at=1751528320&er_sign=fe4ee486cf7a216941ee07cdb566b526',
        mac: 'https://env-00jxtp3qdq80.normal.cloudstatic.cn/Perfect Mouse Control 3.1.0-mac.zip?expire_at=1751528341&er_sign=aec442f74edcafcf6b45169d27ca6f59'
    };
    // 支付宝云增量更新文件存储地址
    const alipayLibLinks = {
        win: 'https://env-00jxtp3qdq80.normal.cloudstatic.cn/lib-3.1.0-win.zip?expire_at=1751528309&er_sign=7068873b5a7774f219f1ef782e269794',
        mac: 'https://env-00jxtp3qdq80.normal.cloudstatic.cn/lib-3.1.0-mac.zip?expire_at=1751528286&er_sign=05e9463e66b0a676b5ad40ca999fa963'
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
            message: `无效的操作系统参数: ${clientOS}`
        };
    }
    try {
        // 构造响应对象
        const latestVersionInfo = {
            version: serverVersion,
            buildDate: "2025.07.01",
            whatsNew: "版本更新测试",
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