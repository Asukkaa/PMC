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
    // 阿里云文件存储地址
    const aliyunFileLinks = {
        win: 'https://mp-f42cc448-2bf2-4edf-9bb8-8f060ec60dd6.cdn.bspapp.com/Perfect Mouse Control 3.1.0-win.zip',
        mac: 'https://mp-f42cc448-2bf2-4edf-9bb8-8f060ec60dd6.cdn.bspapp.com/Perfect Mouse Control 3.1.0-mac.zip'
    };
    const aliyunFileLink = aliyunFileLinks[clientOS];
    // 支付宝云文件存储地址
    const alipayFileLinks = {
        win: 'https://env-00jxtp3qdq80.normal.cloudstatic.cn/Perfect Mouse Control 3.1.0-win.zip?expire_at=1751365797&er_sign=2f512eccc9a6dec591427e19a16c930a',
        mac: 'https://env-00jxtp3qdq80.normal.cloudstatic.cn/Perfect Mouse Control 3.1.0-mac.zip?expire_at=1751365811&er_sign=3cc02f9d7e7752efd2411504d1ebd904'
    };
    const alipayFileLink = alipayFileLinks[clientOS];
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
            version: "3.1.0",
            buildDate: "2025.07.01",
            whatsNew: "版本更新测试",
            aliyunFileLink: aliyunFileLink,
            alipayFileLink: alipayFileLink
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