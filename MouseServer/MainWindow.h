#pragma once
#include "framework.h"
#include "SettingUtils.h"
#include "MouseServer.h"
#include <string>
#include <shellapi.h>
#include <Shlwapi.h>

class MainWindow
{
public:
    MainWindow(HINSTANCE hInst);
    ~MainWindow();

    bool Create();
    int RunMessageLoop();

    static INT_PTR CALLBACK MainProc(HWND hDlg, UINT message, WPARAM wParam, LPARAM lParam);

    SettingUtils* GetSettings() { return settings; }

    bool IsSlient = false;
private:
    void LoadSettings();
    void SaveSettings();
    void InitTrayMenu();
    void InitTrayIcon();
    void InitTokenPriviledge();
    void ShowContextMenu();

    HWND hWnd = nullptr;
    HMENU hMenuTray = nullptr, hMenuTrayIn = nullptr;
    HBITMAP hStatusGood = nullptr, hStatusBad = nullptr;

    UINT WM_TaskbarRestart = 0;
    SettingUtils* settings = nullptr;
    HINSTANCE hInstance;
    WCHAR currentIniPath[MAX_PATH];
    WCHAR currentPath[MAX_PATH];

    NOTIFYICONDATA nid = { 0 };//Õ–≈ÃÕº±Í

    bool setAutoRun = true;
    bool setNeedPass = true;
    std::wstring setPass;

    bool OnInitDialog();
    void StartServer();
    void OnDestroy();
    void OnClose();
    INT_PTR OnWmMessage(UINT message, WPARAM wParam, LPARAM lParam);
    void OnNotifyIcon(WPARAM wParam, LPARAM lParam);
    void OnWmCommand(WPARAM wParam, LPARAM lParam, int wmId);
    static void OnServerStatus(bool running, const wchar_t* error, void* data);
    static void OnConnectStatus(bool connected, const wchar_t* ip, void* data);

    MouseServer mouseServer;
};

