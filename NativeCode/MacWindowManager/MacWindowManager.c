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

// 工具函数：从 CFString 复制到 C 字符串
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

// 工具函数：获取进程名称和路径
static void getProcessInfo(pid_t pid, char* processName, char* processPath, size_t pathSize) {
    // 初始化字符串
    processName[0] = '\0';
    processPath[0] = '\0';
    // 获取进程名称
    int nameResult = proc_name(pid, processName, 256);
    if (nameResult <= 0) {
        snprintf(processName, 256, "Unknown");
    }
    // 获取进程路径 - 使用显式类型转换
    int pathResult = proc_pidpath(pid, processPath, (uint32_t)pathSize);
    if (pathResult <= 0) {
        // 使用 proc_pidinfo 获取进程信息
        struct proc_bsdinfo procInfo;
        if (proc_pidinfo(pid, PROC_PIDTBSDINFO, 0, &procInfo, sizeof(procInfo)) > 0) {
            snprintf(processName, 256, "%s", procInfo.pbi_name);
        }
        // 构建可能的路径
        char pidStr[32];
        snprintf(pidStr, sizeof(pidStr), "%d", pid);
        // 尝试从 /proc 读取
        char procPath[1024];
        snprintf(procPath, sizeof(procPath), "/proc/%s/exe", pidStr);
        char resolvedPath[1024];
        if (readlink(procPath, resolvedPath, sizeof(resolvedPath) - 1) > 0) {
            strncpy(processPath, resolvedPath, pathSize - 1);
            processPath[pathSize - 1] = '\0';
        } else {
            // 最后回退方案
            snprintf(processPath, pathSize, "/usr/bin/unknown");
        }
    }
}

// 主函数：移动窗口
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

// 主函数：调整窗口大小
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

// 主函数：获取焦点窗口信息
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
            // 通常应用窗口的层级是 0，我们优先查找层级为0的窗口
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
    
    // 如果没有找到层级为0的窗口，使用第一个窗口
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

// 主函数：获取所有窗口
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
        // 获取进程ID
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

// 主函数：释放窗口列表
void freeWindowList(WindowInfo* windows) {
    if (windows) {
        free(windows);
    }
}
