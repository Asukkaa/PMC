//
//  MacWindowManager.c
//  MacWindowManager
//
//  Created by 苹果酱企鹅 on 2026/1/30.
//

#include <stdio.h>
#include "MacWindowManager.h"
#include <ApplicationServices/ApplicationServices.h>
#include <CoreFoundation/CoreFoundation.h>
#include <unistd.h>
#include <string.h>
#include <stdlib.h>
#include <libproc.h>
#include <pwd.h>
#include <stdint.h>

/**
 * @brief 将 CFString 复制到 C 字符串
 *
 * @param cfStr 源 CFString 字符串
 * @param buffer 目标 C 字符串缓冲区
 * @param bufferSize 缓冲区大小
 */
static void copyCFStringToCString(CFStringRef cfStr, char* buffer, size_t bufferSize) {
    if (!cfStr || !buffer || bufferSize == 0) {
        buffer[0] = '\0';
        return;
    }
    CFIndex length = CFStringGetLength(cfStr);
    CFIndex maxSize = CFStringGetMaximumSizeForEncoding(length, kCFStringEncodingUTF8) + 1;
    if (maxSize > bufferSize) {
        maxSize = bufferSize;
    }
    Boolean success = CFStringGetCString(cfStr, buffer, maxSize, kCFStringEncodingUTF8);
    if (!success) {
        buffer[0] = '\0';
    }
}

/**
 * @brief 获取进程信息（名称和路径）
 *
 * @param pid 进程 ID
 * @param[out] processName 进程名称缓冲区（可选，可为 NULL）
 * @param[out] processPath 进程路径缓冲区（可选，可为 NULL）
 * @param pathSize 进程路径缓冲区大小
 */
static void getProcessInfo(pid_t pid, char* processName, char* processPath, size_t pathSize) {
    // 初始化字符串
    if (processName) {
        processName[0] = '\0';
    }
    if (processPath) {
        processPath[0] = '\0';
    }
    // 安全检查
    if (pid <= 0) {
        if (processName) {
            strncpy(processName, "Invalid PID", 255);
        }
        return;
    }
    // 获取进程名称
    if (processName) {
        int nameResult = proc_name(pid, processName, 255);
        if (nameResult <= 0) {
            snprintf(processName, 255, "Unknown");
        } else {
            // 确保终止
            processName[255] = '\0';
        }
    }
    // 获取进程路径
    if (processPath && pathSize > 0) {
        int pathResult = proc_pidpath(pid, processPath, (uint32_t)MIN(pathSize, UINT32_MAX));
        if (pathResult <= 0) {
            // 尝试其他方法
            struct proc_bsdinfo procInfo;
            if (proc_pidinfo(pid, PROC_PIDTBSDINFO, 0, &procInfo, sizeof(procInfo)) > 0) {
                if (processName) {
                    snprintf(processName, 255, "%s", procInfo.pbi_name);
                }
            }
            // 使用默认路径
            snprintf(processPath, pathSize, "/usr/bin/unknown");
        } else {
            // 确保终止
            processPath[pathSize - 1] = '\0';
        }
    }
}

/**
 * @brief 根据进程路径获取窗口信息
 *
 * 支持两种路径格式：
 * 1. 应用程序路径（以 .app 结尾）：匹配包含该路径的进程
 * 2. 可执行文件路径：精确匹配进程路径
 *
 * @param processPath 进程路径或应用程序路径
 * @return WindowInfo 窗口信息结构体，如果未找到则所有字段为 0
 */
WindowInfo getMacWindowInfo(const char* processPath) {
    WindowInfo info = {0};
    if (!processPath || strlen(processPath) == 0) {
        return info;
    }
    // 检查传入的路径是否以 .app 结尾
    int isAppPath = 0;
    const char* appExtension = ".app";
    size_t pathLen = strlen(processPath);
    size_t appExtLen = strlen(appExtension);
    if (pathLen >= appExtLen) {
        if (strcmp(processPath + pathLen - appExtLen, appExtension) == 0) {
            isAppPath = 1;
        }
    }
    // 获取所有窗口
    CFArrayRef windowList = CGWindowListCopyWindowInfo(
        kCGWindowListOptionOnScreenOnly | kCGWindowListExcludeDesktopElements,
        kCGNullWindowID
    );
    if (!windowList) {
        return info;
    }
    CFIndex count = CFArrayGetCount(windowList);
    for (CFIndex i = 0; i < count; i++) {
        CFDictionaryRef window = CFArrayGetValueAtIndex(windowList, i);
        // 获取进程ID
        CFNumberRef pidRef = CFDictionaryGetValue(window, kCGWindowOwnerPID);
        if (!pidRef) {
            continue;
        }
        pid_t pid;
        CFNumberGetValue(pidRef, kCFNumberIntType, &pid);
        if (pid <= 0) {
            continue;
        }
        // 获取该进程的完整路径
        char currentProcessPath[1024] = {0};
        getProcessInfo(pid, NULL, currentProcessPath, sizeof(currentProcessPath));
        // 匹配逻辑
        int match = 0;
        if (isAppPath) {
            // 如果传入的路径以 .app 结尾，则检查当前进程路径是否包含该 .app 路径，例如：传入 "/Applications/Safari.app" 应该匹配 "/Applications/Safari.app/Contents/MacOS/Safari"
            if (strstr(currentProcessPath, processPath) != NULL) {
                match = 1;
            }
        } else {
            // 否则进行完整路径匹配（保持向后兼容）
            if (strcmp(currentProcessPath, processPath) == 0) {
                match = 1;
            }
        }
        if (match) {
            // 获取窗口 ID
            CFNumberRef windowIdRef = CFDictionaryGetValue(window, kCGWindowNumber);
            if (windowIdRef) {
                CFNumberGetValue(windowIdRef, kCFNumberIntType, &info.windowId);
            }
            // 获取窗口标题
            CFStringRef titleRef = CFDictionaryGetValue(window, kCGWindowName);
            if (titleRef) {
                copyCFStringToCString(titleRef, info.title, sizeof(info.title));
            }
            // 获取窗口位置和大小
            CFDictionaryRef boundsRef = CFDictionaryGetValue(window, kCGWindowBounds);
            if (boundsRef) {
                CGRect bounds;
                if (CGRectMakeWithDictionaryRepresentation(boundsRef, &bounds)) {
                    info.x = (int)bounds.origin.x;
                    info.y = (int)bounds.origin.y;
                    info.width = (int)bounds.size.width;
                    info.height = (int)bounds.size.height;
                }
            }
            // 获取窗口层级
            CFNumberRef layerRef = CFDictionaryGetValue(window, kCGWindowLayer);
            if (layerRef) {
                CFNumberGetValue(layerRef, kCFNumberIntType, &info.layer);
            }
            // 设置进程信息
            info.pid = pid;
            getProcessInfo(pid, info.processName, info.processPath, sizeof(info.processPath));
            CFRelease(windowList);
            return info;
        }
    }
    CFRelease(windowList);
    return info;
}

/**
 * @brief 移动指定进程的窗口到新位置
 *
 * 使用 macOS Accessibility API 移动窗口。
 * 注意：应用需要获取辅助功能权限才能调用此函数。
 *
 * @param pid 目标进程 ID
 * @param x 新位置的 X 坐标（屏幕坐标）
 * @param y 新位置的 Y 坐标（屏幕坐标）
 * @return true 移动成功
 * @return false 移动失败（进程不存在、窗口不存在或权限不足）
 */
bool moveWindow(int pid, int x, int y) {
    bool success = false;
    AXUIElementRef appElement = NULL;
    CFArrayRef windows = NULL;
    // 创建应用元素
    appElement = AXUIElementCreateApplication(pid);
    if (!appElement) {
        goto cleanup;
    }
    // 获取应用的所有窗口
    CFTypeRef windowsValue;
    AXError error = AXUIElementCopyAttributeValue(appElement, kAXWindowsAttribute, &windowsValue);
    if (error != kAXErrorSuccess || !windowsValue) {
        goto cleanup;
    }
    windows = (CFArrayRef)windowsValue;
    CFIndex windowCount = CFArrayGetCount(windows);
    if (windowCount == 0) {
        goto cleanup;
    }
    // 获取第一个窗口（假设是主窗口）
    AXUIElementRef window = (AXUIElementRef)CFArrayGetValueAtIndex(windows, 0);
    // 设置窗口位置
    CGPoint newPosition = CGPointMake(x, y);
    AXValueRef positionValue = AXValueCreate(kAXValueCGPointType, &newPosition);
    if (positionValue) {
        error = AXUIElementSetAttributeValue(window, kAXPositionAttribute, positionValue);
        success = (error == kAXErrorSuccess);
        CFRelease(positionValue);
    }
    
cleanup:
    if (windows) CFRelease(windows);
    if (appElement) CFRelease(appElement);
    return success;
}

/**
 * @brief 调整指定进程的窗口大小
 *
 * 使用 macOS Accessibility API 调整窗口大小。
 * 注意：应用需要获取辅助功能权限才能调用此函数。
 *
 * @param pid 目标进程 ID
 * @param width 新宽度（像素）
 * @param height 新高度（像素）
 * @return true 调整成功
 * @return false 调整失败（进程不存在、窗口不存在或权限不足）
 */
bool resizeWindow(int pid, int width, int height) {
    bool success = false;
    AXUIElementRef appElement = NULL;
    CFArrayRef windows = NULL;
    appElement = AXUIElementCreateApplication(pid);
    if (!appElement) {
        goto cleanup;
    }
    CFTypeRef windowsValue;
    AXError error = AXUIElementCopyAttributeValue(appElement, kAXWindowsAttribute, &windowsValue);
    if (error != kAXErrorSuccess || !windowsValue) {
        goto cleanup;
    }
    windows = (CFArrayRef)windowsValue;
    CFIndex windowCount = CFArrayGetCount(windows);
    if (windowCount == 0) {
        goto cleanup;
    }
    AXUIElementRef window = (AXUIElementRef)CFArrayGetValueAtIndex(windows, 0);
    CGSize newSize = CGSizeMake(width, height);
    AXValueRef sizeValue = AXValueCreate(kAXValueCGSizeType, &newSize);
    if (sizeValue) {
        error = AXUIElementSetAttributeValue(window, kAXSizeAttribute, sizeValue);
        success = (error == kAXErrorSuccess);
        CFRelease(sizeValue);
    }
    
cleanup:
    if (windows) CFRelease(windows);
    if (appElement) CFRelease(appElement);
    return success;
}

/**
 * @brief 获取当前获得焦点的窗口信息
 *
 * 优先返回层级为 0（普通应用窗口）的窗口信息。
 * 如果没有找到焦点窗口，返回第一个非桌面元素的窗口信息。
 *
 * @return WindowInfo 窗口信息结构体，如果失败则所有字段为 0
 */
WindowInfo getFocusedWindowInfo(void) {
    WindowInfo info = {0};
    // 获取所有窗口
    CFArrayRef windowList = CGWindowListCopyWindowInfo(
        kCGWindowListOptionOnScreenOnly | kCGWindowListExcludeDesktopElements,
        kCGNullWindowID
    );
    if (!windowList) {
        return info;
    }
    // 查找焦点窗口（通常是最前面的窗口）
    CFIndex count = CFArrayGetCount(windowList);
    for (CFIndex i = 0; i < count; i++) {
        CFDictionaryRef window = CFArrayGetValueAtIndex(windowList, i);
        // 获取窗口层级
        CFNumberRef layer = CFDictionaryGetValue(window, kCGWindowLayer);
        if (layer) {
            int layerValue;
            CFNumberGetValue(layer, kCFNumberIntType, &layerValue);
            info.layer = layerValue;
            // 通常应用窗口的层级是 0，我们优先查找层级为 0 的窗口
            if (layerValue == 0) {
                // 获取进程 ID
                CFNumberRef pidRef = CFDictionaryGetValue(window, kCGWindowOwnerPID);
                if (pidRef) {
                    CFNumberGetValue(pidRef, kCFNumberIntType, &info.pid);
                }
                // 获取窗口 ID
                CFNumberRef windowIdRef = CFDictionaryGetValue(window, kCGWindowNumber);
                if (windowIdRef) {
                    CFNumberGetValue(windowIdRef, kCFNumberIntType, &info.windowId);
                }
                // 获取窗口标题
                CFStringRef titleRef = CFDictionaryGetValue(window, kCGWindowName);
                if (titleRef) {
                    copyCFStringToCString(titleRef, info.title, sizeof(info.title));
                }
                // 获取窗口位置和大小
                CFDictionaryRef boundsRef = CFDictionaryGetValue(window, kCGWindowBounds);
                if (boundsRef) {
                    CGRect bounds;
                    if (CGRectMakeWithDictionaryRepresentation(boundsRef, &bounds)) {
                        info.x = (int)bounds.origin.x;
                        info.y = (int)bounds.origin.y;
                        info.width = (int)bounds.size.width;
                        info.height = (int)bounds.size.height;
                    }
                }
                // 获取进程信息
                if (info.pid > 0) {
                    getProcessInfo(info.pid, info.processName, info.processPath, sizeof(info.processPath));
                }
                break;
            }
        }
    }
    // 如果没有找到层级为 0 的窗口，使用第一个窗口
    if (info.pid == 0 && count > 0) {
        CFDictionaryRef firstWindow = CFArrayGetValueAtIndex(windowList, 0);
        // 获取窗口层级
        CFNumberRef layer = CFDictionaryGetValue(firstWindow, kCGWindowLayer);
        if (layer) {
            CFNumberGetValue(layer, kCFNumberIntType, &info.layer);
        }
        CFNumberRef pidRef = CFDictionaryGetValue(firstWindow, kCGWindowOwnerPID);
        if (pidRef) {
            CFNumberGetValue(pidRef, kCFNumberIntType, &info.pid);
            CFNumberRef windowIdRef = CFDictionaryGetValue(firstWindow, kCGWindowNumber);
            if (windowIdRef) {
                CFNumberGetValue(windowIdRef, kCFNumberIntType, &info.windowId);
            }
            CFStringRef titleRef = CFDictionaryGetValue(firstWindow, kCGWindowName);
            if (titleRef) {
                copyCFStringToCString(titleRef, info.title, sizeof(info.title));
            }
            CFDictionaryRef boundsRef = CFDictionaryGetValue(firstWindow, kCGWindowBounds);
            if (boundsRef) {
                CGRect bounds;
                if (CGRectMakeWithDictionaryRepresentation(boundsRef, &bounds)) {
                    info.x = (int)bounds.origin.x;
                    info.y = (int)bounds.origin.y;
                    info.width = (int)bounds.size.width;
                    info.height = (int)bounds.size.height;
                }
            }
            // 获取进程信息
            if (info.pid > 0) {
                getProcessInfo(info.pid, info.processName, info.processPath, sizeof(info.processPath));
            }
        }
    }
    CFRelease(windowList);
    return info;
}

/**
 * @brief 获取所有可见窗口的信息
 *
 * 不包括桌面元素（如桌面图标、Dock 等）。
 * 返回的数组需要调用 freeWindowList() 释放内存。
 *
 * @param[out] count 窗口数量
 * @return WindowInfo* 窗口信息数组指针，失败返回 NULL
 */
WindowInfo* getAllWindows(int* count) {
    *count = 0;
    // 获取所有窗口
    CFArrayRef windowList = CGWindowListCopyWindowInfo(
        kCGWindowListOptionOnScreenOnly | kCGWindowListExcludeDesktopElements,
        kCGNullWindowID
    );
    if (!windowList) {
        return NULL;
    }
    CFIndex totalCount = CFArrayGetCount(windowList);
    // 分配内存
    WindowInfo* windows = (WindowInfo*)malloc(sizeof(WindowInfo) * totalCount);
    if (!windows) {
        CFRelease(windowList);
        return NULL;
    }
    // 填充数据
    CFIndex validCount = 0;
    for (CFIndex i = 0; i < totalCount; i++) {
        CFDictionaryRef window = CFArrayGetValueAtIndex(windowList, i);
        WindowInfo* current = &windows[validCount];
        memset(current, 0, sizeof(WindowInfo));
        // 获取进程 ID
        CFNumberRef pidRef = CFDictionaryGetValue(window, kCGWindowOwnerPID);
        if (pidRef) {
            CFNumberGetValue(pidRef, kCFNumberIntType, &current->pid);
        }
        // 获取窗口ID
        CFNumberRef windowIdRef = CFDictionaryGetValue(window, kCGWindowNumber);
        if (windowIdRef) {
            CFNumberGetValue(windowIdRef, kCFNumberIntType, &current->windowId);
        }
        // 获取窗口标题
        CFStringRef titleRef = CFDictionaryGetValue(window, kCGWindowName);
        if (titleRef) {
            copyCFStringToCString(titleRef, current->title, sizeof(current->title));
        }
        // 获取窗口位置和大小
        CFDictionaryRef boundsRef = CFDictionaryGetValue(window, kCGWindowBounds);
        if (boundsRef) {
            CGRect bounds;
            if (CGRectMakeWithDictionaryRepresentation(boundsRef, &bounds)) {
                current->x = (int)bounds.origin.x;
                current->y = (int)bounds.origin.y;
                current->width = (int)bounds.size.width;
                current->height = (int)bounds.size.height;
            }
        }
        // 获取窗口层级
        CFNumberRef layerRef = CFDictionaryGetValue(window, kCGWindowLayer);
        if (layerRef) {
            CFNumberGetValue(layerRef, kCFNumberIntType, &current->layer);
        }
        // 获取进程信息
        if (current->pid > 0) {
            getProcessInfo(current->pid, current->processName, current->processPath, sizeof(current->processPath));
        }
        validCount++;
    }
    CFRelease(windowList);
    *count = (int)validCount;
    return windows;
}

/**
 * @brief 释放 getAllWindows 分配的窗口列表内存
 *
 * @param windows 由 getAllWindows 返回的窗口数组指针
 */
void freeWindowList(WindowInfo* windows) {
    if (windows) {
        free(windows);
    }
}
