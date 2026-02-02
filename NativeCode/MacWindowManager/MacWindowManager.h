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

// 窗口信息结构体
typedef struct {
    int pid;                // 进程ID
    int windowId;           // 窗口ID
    char title[256];        // 窗口标题
    int x;                  // 窗口X坐标
    int y;                  // 窗口Y坐标
    int width;              // 窗口宽度
    int height;             // 窗口高度
    int layer;              // 窗口层级
    char processName[256];  // 进程名称
    char processPath[1024]; // 进程路径
} WindowInfo;

// 函数声明
bool moveWindow(int pid, int x, int y);
bool resizeWindow(int pid, int width, int height);
WindowInfo getFocusedWindowInfo(void);
WindowInfo* getAllWindows(int* count);
void freeWindowList(WindowInfo* windows);

#ifdef __cplusplus
}
#endif

#endif /* MacWindowManager_h */
