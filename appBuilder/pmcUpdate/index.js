'use strict';

exports.main = async (event, context) => {
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
    // 定义不同系统的文件ID（在uniCloud控制台获取）
    const fileLinks = {
        win: 'https://mp-f42cc448-2bf2-4edf-9bb8-8f060ec60dd6.cdn.bspapp.com/Perfect Mouse Control 3.0.0.zip',
        mac: 'https://mp-f42cc448-2bf2-4edf-9bb8-8f060ec60dd6.cdn.bspapp.com/Perfect Mouse Control 3.0.0.dmg'
    };
    const fileLink = fileLinks[clientOS];
    // 验证操作系统类型
    if (!fileLink) {
        return {
            code: 400,
            message: `无效的操作系统参数: ${clientOS}`
        };
    }
    try {
        // 构造响应对象
        const latestVersionInfo = {
            version: "3.0.0",
            buildDate: "2025.06.23",
            whatsNew: "版本更新测试",
            downloadLink: fileLink
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