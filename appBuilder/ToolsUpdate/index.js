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
    const serverVersion = "2.5.4";
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
        win: 'https://mp-f42cc448-2bf2-4edf-9bb8-8f060ec60dd6.cdn.bspapp.com/Tools/win/Tools-2.5.4-win.zip',
        mac: 'https://mp-f42cc448-2bf2-4edf-9bb8-8f060ec60dd6.cdn.bspapp.com/Tools/mac/Tools-2.5.4-mac.zip'
    };
    // 阿里云增量更新文件存储地址
    const aliyunLibLinks = {
        win: 'https://mp-f42cc448-2bf2-4edf-9bb8-8f060ec60dd6.cdn.bspapp.com/Tools/win/lib-2.5.4-win.zip',
        mac: 'https://mp-f42cc448-2bf2-4edf-9bb8-8f060ec60dd6.cdn.bspapp.com/Tools/mac/lib-2.5.4-mac.zip'
    };
    // 支付宝云全量更新文件存储地址
    const alipayAppLinks = {
        win: 'https://env-00jxtp3qdq80.normal.cloudstatic.cn/Tools/win/Tools-2.5.4-win.zip',
        mac: 'https://env-00jxtp3qdq80.normal.cloudstatic.cn/Tools/mac/Tools-2.5.4-mac.zip'
    };
    // 支付宝云增量更新文件存储地址
    const alipayLibLinks = {
        win: 'https://env-00jxtp3qdq80.normal.cloudstatic.cn/Tools/win/lib-2.5.4-win.zip',
        mac: 'https://env-00jxtp3qdq80.normal.cloudstatic.cn/Tools/mac/lib-2.5.4-mac.zip'
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
            message: "无效的操作系统参数"
        };
    }
    try {
        // 构造响应对象
        const latestVersionInfo = {
            version: serverVersion,
            buildDate: "2025.08.13",
            whatsNew: `版本 2.5.4:
        1.批量移动文件工具多文件夹选择器会在视觉上区分文件与文件夹
        2.读取excel相关功能可从下拉框中选择要读取的表
        3.优化了一些ui界面
        4.修复了一些bug`,
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