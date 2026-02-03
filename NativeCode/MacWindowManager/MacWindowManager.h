//
//  MacWindowManager.h
//  MacWindowManager
//
//  Created by 苹果酱企鹅 on 2026/1/30.
//

#ifndef MacWindowManager_h
#define MacWindowManager_h

#include <stdbool.h>

#ifdef __cplusplus
extern "C" {
#endif

/**
 * @brief 窗口信息结构体
 *
 * 包含窗口的各种属性信息，如位置、大小、所属进程等。
 */
typedef struct {
    
    // 进程 ID
    int pid;
    
    // 窗口 ID
    int windowId;
    
    // 窗口标题
    char title[256];
    
    // 窗口 X 坐标
    int x;
    
    // 窗口 Y 坐标
    int y;
    
    // 窗口宽度
    int width;
    
    // 窗口高度
    int height;
    
    // 窗口层级
    int layer;
    
    // 进程名称
    char processName[256];
    
    // 进程路径
    char processPath[1024];
    
} WindowInfo;

/**
 * @brief 移动指定进程的窗口到新位置
 *
 * @param pid 目标进程ID
 * @param x 新位置的X坐标（屏幕坐标）
 * @param y 新位置的Y坐标（屏幕坐标）
 * @return true 移动成功
 * @return false 移动失败（进程不存在、窗口不存在或权限不足）
 */
bool moveWindow(int pid, int x, int y);

/**
 * @brief 调整指定进程的窗口大小
 *
 * @param pid 目标进程ID
 * @param width 新宽度（像素）
 * @param height 新高度（像素）
 * @return true 调整成功
 * @return false 调整失败（进程不存在、窗口不存在或权限不足）
 */
bool resizeWindow(int pid, int width, int height);

/**
 * @brief 获取当前获得焦点的窗口信息
 *
 * 优先返回层级为0（普通应用窗口）的窗口信息。
 * 如果没有找到焦点窗口，返回第一个非桌面元素的窗口信息。
 *
 * @return WindowInfo 窗口信息结构体，如果失败则所有字段为0
 */
WindowInfo getFocusedWindowInfo(void);

/**
 * @brief 根据进程路径获取窗口信息
 *
 * 支持两种路径格式：
 * 1. 应用程序路径（以.app结尾）：匹配包含该路径的进程
 * 2. 可执行文件路径：精确匹配进程路径
 *
 * 注意：当存在多个匹配窗口时，返回第一个找到的窗口。
 *
 * @param processPath 进程路径或应用程序路径
 * @return WindowInfo 窗口信息结构体，如果未找到则所有字段为0
 */
WindowInfo getMacWindowInfo(const char* processPath);

/**
 * @brief 获取所有可见窗口的信息
 *
 * 不包括桌面元素（如桌面图标、Dock等）。
 * 返回的数组需要调用freeWindowList()释放内存。
 *
 * @param[out] count 窗口数量
 * @return WindowInfo* 窗口信息数组指针，失败返回NULL
 */
WindowInfo* getAllWindows(int* count);

/**
 * @brief 释放getAllWindows分配的窗口列表内存
 *
 * @param windows 由getAllWindows返回的窗口数组指针
 */
void freeWindowList(WindowInfo* windows);

#ifdef __cplusplus
}
#endif

#endif /* MacWindowManager_h */
