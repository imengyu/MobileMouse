#include "framework.h"
#include "MainWindow.h"
#include "SettingUtils.h"
#include "AboutWindow.h"
#include "Resource.h"
#include "MouseServer.h"
#include "DialogUtils.h"
#include "NetUtils.h"

#define WM_NOTIFYICON WM_USER+5  

MainWindow::MainWindow(HINSTANCE hInst)
{
    hInstance = hInst;

    //Current path
    GetModuleFileName(NULL, currentPath, MAX_PATH);
    PathRemoveFileSpec(currentPath);

    //Settings path
    
    GetModuleFileName(NULL, currentIniPath, MAX_PATH);
    PathRenameExtension(currentIniPath, L".ini");
    settings = new SettingUtils(currentIniPath);
}
MainWindow::~MainWindow()
{
    delete settings;
    settings = nullptr;
}

#define GWL_USERDATA        (-21)

bool MainWindow::Create()
{
    hWnd = CreateDialog(hInstance, MAKEINTRESOURCE(IDD_MAIN), NULL, MainProc);
    if (!hWnd)
        return false;

    SetWindowLongPtr(hWnd, GWL_USERDATA, (LONG_PTR)this);
    OnInitDialog();
    UpdateWindow(hWnd);

    if(!IsSlient)
        ShowWindow(hWnd, SW_SHOW);

    return true;
}
int MainWindow::RunMessageLoop()
{
    HACCEL hAccelTable = LoadAccelerators(hInstance, MAKEINTRESOURCE(IDC_MOUSESERVER));
    MSG msg;

    // 主消息循环:
    while (GetMessage(&msg, nullptr, 0, 0))
    {
        if (!TranslateAccelerator(msg.hwnd, hAccelTable, &msg))
        {
            TranslateMessage(&msg);
            DispatchMessage(&msg);
        }
    }

    return (int)msg.wParam;
}

INT_PTR CALLBACK MainWindow::MainProc(HWND hDlg, UINT message, WPARAM wParam, LPARAM lParam)
{
    auto*wnd = (MainWindow*)(LONG_PTR)GetWindowLongPtr(hDlg, GWL_USERDATA);
    switch (message)
    {
    case WM_INITDIALOG: {
        return true;
    }
    case WM_COMMAND: {
        int wmId = LOWORD(wParam);
        wnd->OnWmCommand(wParam, lParam, wmId);
        break;
    }
    case WM_NOTIFYICON: {
        wnd->OnNotifyIcon(wParam, lParam);
        return FALSE;
    }
    case WM_CLOSE: {
        wnd->OnClose();
        break;
    }
    case WM_DESTROY: {
        wnd->OnDestroy();
        PostQuitMessage(0);
        break;
    }
    default: if(wnd) return wnd->OnWmMessage(message, wParam, lParam);
    }
    return 0;
}

void MainWindow::LoadSettings() {
    setAutoRun = false;
    setNeedPass = settings->GetSettingBool(L"NeedPass");
    setPass = settings->GetSettingStr(L"Pass");

    SetDlgItemText(hWnd, IDC_EDIT_PASS, setPass.c_str());
    EnableWindow(GetDlgItem(hWnd, IDC_EDIT_PASS), setNeedPass);
    CheckDlgButton(hWnd, IDC_CHECK_NEED_PASSWORD, setNeedPass);

    mouseServer.SetNeedPass(setNeedPass);
    mouseServer.SetPass(setPass.c_str());

    //检测有没有开机运行
    HKEY hKey;
    LPCWSTR strRegPath = L"SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\Run";
    if (RegOpenKeyEx(HKEY_CURRENT_USER, strRegPath, 0, KEY_ALL_ACCESS, &hKey) == ERROR_SUCCESS)
    {
        TCHAR strDir[MAX_PATH] = {};
        DWORD nLength = MAX_PATH;
        long result = RegGetValue(hKey, nullptr, L"IMengyuMouseServer", RRF_RT_REG_SZ, 0, strDir, &nLength);

        if (result == ERROR_SUCCESS)
            setAutoRun = true;

        RegCloseKey(hKey);
    }


    CheckDlgButton(hWnd, IDC_CHECK_AUTO_RUN, setAutoRun);
}
void MainWindow::SaveSettings() {

    settings->SetSettingBool(L"AutoRun", IsDlgButtonChecked(hWnd, IDC_CHECK_AUTO_RUN));
    settings->SetSettingBool(L"NeedPass", IsDlgButtonChecked(hWnd, IDC_CHECK_NEED_PASSWORD));
    settings->SetSettingStr(L"Pass", DialogUtils::GetDlgItemTextEx(hWnd, IDC_EDIT_PASS, 32));
}
void MainWindow::InitTrayMenu() {
    hMenuTray = LoadMenu(hInstance, MAKEINTRESOURCE(IDR_MENU_TRAY));
    hMenuTrayIn = GetSubMenu(hMenuTray, 0);
}
void MainWindow::InitTrayIcon() {

    nid.cbSize = sizeof(NOTIFYICONDATA);
    nid.hWnd = hWnd;
    nid.uID = IDI_LOGO_SMALL;
    nid.hIcon = ::LoadIcon(hInstance, MAKEINTRESOURCE(IDI_LOGO_SMALL));
    nid.uCallbackMessage = WM_NOTIFYICON;
    nid.uFlags = NIF_ICON | NIF_MESSAGE | NIF_TIP;
    wcscpy_s(nid.szTip, _T("MouseServer"));
    Shell_NotifyIcon(NIM_ADD, &nid);
}
void MainWindow::InitTokenPriviledge() {
    HANDLE hToken;
    TOKEN_PRIVILEGES tkp;
    if (!OpenProcessToken(GetCurrentProcess(), TOKEN_ADJUST_PRIVILEGES | TOKEN_QUERY, &hToken))
        return;
    LookupPrivilegeValue(NULL, SE_SHUTDOWN_NAME, &tkp.Privileges[0].Luid);
    tkp.PrivilegeCount = 1;
    tkp.Privileges[0].Attributes = SE_PRIVILEGE_ENABLED;
    AdjustTokenPrivileges(hToken, FALSE, &tkp, 0, NULL, NULL);
}
void MainWindow::ShowContextMenu() {
    POINT pos;
    GetCursorPos(&pos);
    int id = TrackPopupMenu(hMenuTrayIn, TPM_RETURNCMD | TPM_LEFTALIGN | TPM_TOPALIGN, pos.x + 10, pos.y + 10, NULL, hWnd, NULL);
    SendMessage(hWnd, WM_COMMAND, id, NULL);
}
void MainWindow::StartServer() {
RETRY:
    if (!mouseServer.Start()) {
        std::wstring err = mouseServer.GetLastError();
        err = L"无法启动服务，错误信息：" + err;

        int rs = MessageBox(hWnd, err.c_str(), L"错误", MB_ICONERROR | MB_RETRYCANCEL);
        if (rs == IDCANCEL) SendMessage(hWnd, WM_COMMAND, IDM_QUIT, NULL);
        else if (rs == IDRETRY) goto RETRY;
    }
}

bool MainWindow::OnInitDialog()
{
    SendMessage(hWnd, WM_SETICON, ICON_BIG, (LPARAM)LoadIcon(hInstance, MAKEINTRESOURCE(IDI_LOGO)));
    SendMessage(hWnd, WM_SETICON, ICON_SMALL, (LPARAM)LoadIcon(hInstance, MAKEINTRESOURCE(IDI_LOGO_SMALL)));

    hStatusGood = LoadBitmap(hInstance, MAKEINTRESOURCE(IDB_STATUS_GOOD));
    hStatusBad = LoadBitmap(hInstance, MAKEINTRESOURCE(IDB_STATUS_BAD));

    WM_TaskbarRestart = RegisterWindowMessage(TEXT("TaskbarCreated"));

    InitTrayMenu();
    InitTrayIcon();
    LoadSettings();
    InitTokenPriviledge();

    std::wstring ip = NetUtils::GetIP();
    std::wstring str = L"本机名称：";
    str += NetUtils::GetCanonname();
    str += L"\n本机地址：\n" + ip;
    SetDlgItemText(hWnd, IDC_STATIC_CURRENT_IP, str.c_str());

    mouseServer.SetCallback(OnServerStatus, this);
    mouseServer.SetConnectStatusCallback(OnConnectStatus, this);
    StartServer();

    return true;
}
void MainWindow::OnDestroy()
{
    mouseServer.Stop();
    SaveSettings();
    Shell_NotifyIcon(NIM_DELETE, &nid);
    DestroyMenu(hMenuTray);
}
void MainWindow::OnClose()
{
    
}
INT_PTR MainWindow::OnWmMessage(UINT message, WPARAM wParam, LPARAM lParam)
{
    if (message == WM_TaskbarRestart)
        InitTrayIcon();
    return 0;
}
void MainWindow::OnNotifyIcon(WPARAM wParam, LPARAM lParam)
{
    switch (lParam)
    {
    case WM_LBUTTONDBLCLK:
        ShowWindow(hWnd, SW_SHOWNORMAL);
        break;
    case WM_RBUTTONDOWN:
    case WM_CONTEXTMENU:
        ShowContextMenu();
        break;
    default:
        break;
    }
}
void MainWindow::OnWmCommand(WPARAM wParam, LPARAM lParam, int wmId)
{
    if (HIWORD(wParam) == EN_CHANGE) {
        if (LOWORD(wParam) == IDC_EDIT_PASS) {
            setPass = DialogUtils::GetDlgItemTextEx(hWnd, IDC_EDIT_PASS, 32);
            mouseServer.SetPass(setPass.c_str());
        }
    }
    else if (HIWORD(wParam) == STN_CLICKED && LOWORD(wParam) == IDC_STATIC_STATUS_ICON) {
        if (mouseServer.GetIsRunning()) {
            if (MessageBox(hWnd, L"你希望立即停止服务吗？", L"提示", MB_YESNO) == IDYES)
                mouseServer.Stop();
        }
        else {
            if (MessageBox(hWnd, L"你希望立即启动服务吗？", L"提示", MB_YESNO) == IDYES)
                StartServer();
        }
    }
    else {
        switch (wmId)
        {
        case IDCANCEL:
            ShowWindow(hWnd, SW_HIDE);
            break;
        case IDC_CHECK_AUTO_RUN: {
            bool newRun = IsDlgButtonChecked(hWnd, IDC_CHECK_AUTO_RUN);
            if (newRun != setAutoRun) {
                setAutoRun = newRun;
                if (setAutoRun) {
                    HKEY hKey;
                    LPCWSTR strRegPath = L"SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\Run";
                    if (RegOpenKeyEx(HKEY_CURRENT_USER, strRegPath, 0, KEY_ALL_ACCESS, &hKey) == ERROR_SUCCESS)       
                    {
                        TCHAR strExeFullDir[MAX_PATH + 10];
                        GetModuleFileName(NULL, strExeFullDir, MAX_PATH);
                        wcscat_s(strExeFullDir, L" -silent");

                        TCHAR strDir[MAX_PATH] = {};
                        DWORD nLength = MAX_PATH;
                        long result = RegGetValue(hKey, nullptr, L"IMengyuMouseServer", RRF_RT_REG_SZ, 0, strDir, &nLength);

                        if (result != ERROR_SUCCESS || _tcscmp(strExeFullDir, strDir) != 0)
                        {
                            RegSetValueEx(hKey, L"IMengyuMouseServer", 0, REG_SZ, (LPBYTE)strExeFullDir,
                                (lstrlen(strExeFullDir) + 1) * sizeof(TCHAR));
                        }

                        RegCloseKey(hKey);
                    }
                }
                else {
                    HKEY hKey;
                    LPCWSTR strRegPath = L"SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\Run";
                    if (RegOpenKeyEx(HKEY_CURRENT_USER, strRegPath, 0, KEY_ALL_ACCESS, &hKey) == ERROR_SUCCESS)       
                    {
                        RegDeleteKey(hKey, L"IMengyuMouseServer");
                        RegCloseKey(hKey);
                    }
                }
            }
            break;
        }
        case IDC_CHECK_NEED_PASSWORD:
            setNeedPass = IsDlgButtonChecked(hWnd, IDC_CHECK_NEED_PASSWORD);
            EnableWindow(GetDlgItem(hWnd, IDC_EDIT_PASS), setNeedPass);
            mouseServer.SetNeedPass(setNeedPass);
            break;

        case IDC_ABOUT:
            AboutWindow about;
            about.Show(hWnd);
            break;
        case IDM_QUIT:
        case IDC_QUIT:
            DestroyWindow(hWnd);
            break;
        case IDM_SHOW_MAIN:
            ShowWindow(hWnd, SW_SHOW);
            break;
        }
    }
}
void MainWindow::OnServerStatus(bool running, const wchar_t* error, void* data) {
    auto _this = (MainWindow*)data;
    if (running) {
        SetDlgItemText(_this->hWnd, IDC_STATIC_STATUS, L"服务已启动");
        SendMessage(GetDlgItem(_this->hWnd, IDC_STATIC_STATUS_ICON), 
            STM_SETIMAGE, IMAGE_BITMAP, LPARAM(_this->hStatusGood));
    }
    else {
        SendMessage(GetDlgItem(_this->hWnd, IDC_STATIC_STATUS_ICON), 
            STM_SETIMAGE, IMAGE_BITMAP, LPARAM(_this->hStatusBad));

        if (error) {
            std::wstring err = L"服务运行遇到了错误而停止，您可以选择“重试”重新启动服务。\n错误信息：";
            err += error;
            int rs = MessageBox(_this->hWnd, err.c_str(), L"错误", MB_ICONEXCLAMATION | MB_RETRYCANCEL);
            if(rs == IDRETRY)
                _this->StartServer();

            SetDlgItemText(_this->hWnd, IDC_STATIC_STATUS, L"服务因为错误而停止");
        }
        else SetDlgItemText(_this->hWnd, IDC_STATIC_STATUS, L"服务未启动");
    }
}
void MainWindow::OnConnectStatus(bool connected, const wchar_t* ip, void* data) {
    auto _this = (MainWindow*)data;
    if (connected) {
        WCHAR string[100];
        swprintf_s(string, L"%s 已连接", ip);
        SetDlgItemText(_this->hWnd, IDC_STATIC_CONNECTED, string);
    }
    else {
        SetDlgItemText(_this->hWnd, IDC_STATIC_CONNECTED, L"暂无已连接的设备");
    }
}
