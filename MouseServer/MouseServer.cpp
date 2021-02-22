// MouseServer.cpp : 定义应用程序的入口点。
//

#include "framework.h"
#include "MouseServer.h"
#include "SettingUtils.h"
#include "RandomUtils.h"
#include "MainWindow.h"
#pragma comment(lib, "shlwapi.lib")
#pragma comment(lib, "ws2_32.lib")
#pragma comment(lib, "netapi32.lib")
#pragma comment(linker,"\"/manifestdependency:type='win32' \
name='Microsoft.Windows.Common-Controls' version='6.0.0.0' \
processorArchitecture='*' publicKeyToken='6595b64144ccf1df' language='*'\"")

HINSTANCE hInst;
LPWSTR* appArgList = nullptr;
int appArgCount = 0;


int APIENTRY wWinMain(_In_ HINSTANCE hInstance, _In_opt_ HINSTANCE hPrevInstance, _In_ LPWSTR lpCmdLine, _In_ int       nCmdShow)
{
    UNREFERENCED_PARAMETER(hPrevInstance);
    UNREFERENCED_PARAMETER(lpCmdLine);

    hInst = hInstance;

    //命令行
    appArgList = CommandLineToArgvW(GetCommandLine(), &appArgCount);
    if (appArgList == NULL)
    {
        MessageBox(NULL, L"Unable to parse command line", L"Error", MB_OK);
        return -1;
    }


    MainWindow mainWnd(hInst);

    if (appArgCount > 1 && (wcscmp(appArgList[1], L"-silent") == 0 || wcscmp(appArgList[1], L"/silent") == 0))
        mainWnd.IsSlient = true;

    if (!mainWnd.Create()) {
        MessageBox(NULL, L"Create window error!", L"ERROR", MB_ICONERROR);
        return -1;
    }

    int result = mainWnd.RunMessageLoop();


    LocalFree(appArgList);

    return result;
}

//启停和回调

bool MouseServer::Start()
{
    WSADATA wsaData;
    if (WSAStartup(MAKEWORD(2, 0), &wsaData) != 0)
    {
        swprintf_s(lastError, L"WSAStartup failed: %d", WSAGetLastError());
        return false;
    }

    sockfd = socket(AF_INET, SOCK_STREAM, IPPROTO_TCP);//建立socket 
    if (sockfd == SOCKET_ERROR) {
        swprintf_s(lastError, L"socket failed: %d", errno);
        return false;
    }

    my_addr.sin_family = AF_INET;/*该属性表示接收本机或其他机器传输*/
    my_addr.sin_port = htons(PORT);/*端口号*/
    my_addr.sin_addr.s_addr = htonl(INADDR_ANY);/*IP，括号内容表示本机IP*/
    ZeroMemory(&my_addr.sin_zero, sizeof(my_addr.sin_zero));/*将其他属性置0*/

    if (bind(sockfd, (struct sockaddr*)&my_addr, sizeof(struct sockaddr)) < 0) {//绑定地址结构体和socket
        swprintf_s(lastError, L"bind error");
        return false;
    }
    
    if (listen(sockfd, BACKLOG) == SOCKET_ERROR)//开启监听 ，第二个参数是最大监听数 
    {
        swprintf_s(lastError, L"listen error");
        return false;
    }

    int recvTimeout = 10 * 1000;
    int sendTimeout = 10 * 1000;

    setsockopt(sockfd, SOL_SOCKET, SO_RCVTIMEO, (char*)&recvTimeout, sizeof(int));
    setsockopt(sockfd, SOL_SOCKET, SO_SNDTIMEO, (char*)&sendTimeout, sizeof(int));

    running = true;
    hThread = CreateThread(NULL, 0, WorkThread, this, 0, NULL);
    CreateThread(NULL, 0, SearchRespThread, this, 0, NULL);
    CreateThread(NULL, 0, ReceiveControlCommandThread, this, 0, NULL);

    NotifyCallback(true, nullptr);
    return true;
}
bool MouseServer::Stop()
{
    if (running) {
        running = false;
    }
    if (sockfd > 0) {
        shutdown(sockfd, SD_BOTH);
        closesocket(sockfd);
        sockfd = 0;
    }
    if (sockReceiveCommandListen > 0) {
        closesocket(sockReceiveCommandListen);
        sockReceiveCommandListen = 0;
    }
    if (sockSearchListen > 0) {
        closesocket(sockSearchListen);
        sockSearchListen = 0;
    }
    WSACleanup();
    return false;
}

void MouseServer::SetCallback(MouseServerRunStatusCallback callback, void* data)
{
    this->callback = callback;
    this->callbackData = data;
}
void MouseServer::SetConnectStatusCallback(MouseServerConnectionStatusCallback callback, void* data)
{
    this->callbackConnectStatus = callback;
    this->callbackConnectStatusData = data;
}

void MouseServer::SetNeedPass(bool needPass) { this->needPass = needPass; }
void MouseServer::SetPass(const WCHAR* p) { 
    char pass[32];
    sprintf_s(pass, "%ws", p);
    this->pass = pass; 
}

//线程入口

DWORD WINAPI MouseServer::WorkThread(LPVOID lpThreadParameter)
{
    auto _this = (MouseServer*)lpThreadParameter;
    if(!_this->Work() && _this->running)
        _this->NotifyCallback(false, _this->lastError);
    else
        _this->NotifyCallback(false, nullptr);
    return 0;
}
DWORD WINAPI MouseServer::SearchRespThread(LPVOID lpThreadParameter)
{
    auto _this = (MouseServer*)lpThreadParameter;
    _this->SearchResp();
    return 0;
}
DWORD WINAPI MouseServer::ReceiveControlCommandThread(LPVOID lpThreadParameter)
{
    auto _this = (MouseServer*)lpThreadParameter;
    _this->ReceiveControlCommand();
    return 0;
}

//发现设备接收工作函数
void MouseServer::SearchResp() {

    int set = 1;
    struct sockaddr_in recvAddr;

    if ((sockSearchListen = socket(AF_INET, SOCK_DGRAM, 0)) == -1) {
        OutputDebugStringA("SearchResp socket fail\n");
        return;
    }

    setsockopt(sockSearchListen, SOL_SOCKET, SO_REUSEADDR, (const char*)&set, sizeof(int));
    memset(&recvAddr, 0, sizeof(struct sockaddr_in));
    recvAddr.sin_family = AF_INET;
    recvAddr.sin_port = htons(DISCOVER_PORT);
    recvAddr.sin_addr.s_addr = INADDR_ANY;

    if (bind(sockSearchListen, (struct sockaddr*)&recvAddr, sizeof(struct sockaddr)) == -1) {
        OutputDebugStringA("SearchResp bind fail\n");
        return;
    }

    while (running) {

        int recvbytes;
        char recvbuf[BUFFER_SIZE];
        int addrLen = sizeof(struct sockaddr_in);
        if ((recvbytes = recvfrom(sockSearchListen, recvbuf, BUFFER_SIZE, 0,
            (struct sockaddr*)&recvAddr, &addrLen)) != -1) {
            recvbuf[recvbytes] = '\0';
            
            if (VerifySearchData(recvbytes, recvbuf)) {
                char sendbuf[128];
                int len = PackSearchRespData(sendbuf);
                
                int sendBytes;
                if ((sendBytes = sendto(sockSearchListen, sendbuf, len, 0,
                    (struct sockaddr*)&recvAddr, sizeof(struct sockaddr))) == -1) {
                    char err[64];
                    sprintf_s(err, "SendSearchResp sendto fail, errno=%d\n", errno);
                    OutputDebugStringA(err);
                }
            }
        }
        else {
            OutputDebugStringA("SearchResp recvfrom fail\n");
            break;
        }
    }

    closesocket(sockSearchListen);
    sockSearchListen = 0;
}
int MouseServer::PackSearchRespData(char* data) {
    int offset = 0;
    data[offset++] = PACKET_PREFIX;
    data[offset++] = PACKET_TYPE_SEARCH_DEVICE_RSP;

    int cpuInfo[4] = { 0 };
    char str[64] = { 0 };
    for (int index = 1; index <= 1; index++) {
        __cpuid(cpuInfo, index);
        sprintf_s(str, "%08d\t%08X\t%08X\t%08X\t%08X\n", index, cpuInfo[0], cpuInfo[1], cpuInfo[2], cpuInfo[3]);
    }
    int len = (int)strlen(str);
    data[offset++] = len;
    for(int i = 0; i < len; i++)
        data[offset++] = str[i];
    return offset;
}
bool MouseServer::VerifySearchData(int recvbytes, char *recvbuf) {

    if (recvbytes < 6) 
        return false;

    int offset = 0;
    int sendSeq;
    if (recvbuf[offset++] != '$' || recvbuf[offset++] != PACKET_TYPE_SEARCH_DEVICE_REQ) 
        return false;

    sendSeq = recvbuf[offset++] & 0xFF;
    sendSeq |= (recvbuf[offset++] << 8) & 0xFF00;
    sendSeq |= (recvbuf[offset++] << 16) & 0xFF0000;
    sendSeq |= (recvbuf[offset++] << 24) & 0xFF000000;
    if (sendSeq < 1 || sendSeq > SEARCH_DEVICE_TIMES)
        return false;
 
    return true;
}

//控制指令接收工作函数
void MouseServer::ReceiveControlCommand() {

    int set = 1;
    struct sockaddr_in recvAddr;

    if ((sockReceiveCommandListen = socket(AF_INET, SOCK_DGRAM, 0)) == -1) {
        OutputDebugStringA("ReceiveControlCommand socket fail\n");
        return;
    }

    setsockopt(sockReceiveCommandListen, SOL_SOCKET, SO_REUSEADDR, (const char*)&set, sizeof(int));
    memset(&recvAddr, 0, sizeof(struct sockaddr_in));
    recvAddr.sin_family = AF_INET;
    recvAddr.sin_port = htons(CONTROL_PORT);
    recvAddr.sin_addr.s_addr = INADDR_ANY;

    if (bind(sockReceiveCommandListen, (struct sockaddr*)&recvAddr, sizeof(struct sockaddr)) == -1) {
        OutputDebugStringA("ReceiveControlCommand bind fail\n");
        return;
    }

    while (running) {
        int recvbytes;
        char recvbuf[BUFFER_SIZE];
        int addrLen = sizeof(struct sockaddr_in);
        if ((recvbytes = recvfrom(sockReceiveCommandListen, recvbuf, BUFFER_SIZE, 0,
            (struct sockaddr*)&recvAddr, &addrLen)) != -1) {
            recvbuf[recvbytes] = '\0';

            if (strcmp(recvbuf, "") != 0) {
                if (strncmp(recvbuf, "mtl", 3) == 0) {
                    if (CheckKey(recvbuf, nullptr))
                        HandleCtl(recvbuf);
                }
                else if (strncmp(recvbuf, "ktl", 3) == 0) {
                    if (CheckKey(recvbuf, nullptr))
                        HandleKeyCtl(recvbuf);
                }
            }
        }
        else {
            OutputDebugStringA("ReceiveControlCommand recvfrom fail\n");
            break;
        }
    }

    closesocket(sockReceiveCommandListen);
    sockReceiveCommandListen = 0;
}

//主指令接收发送工作函数
bool MouseServer::Work()
{
    NotifyConnectionCallback(false, nullptr);

    char recv_buf[BUFFER_SIZE];
    char send_buf[BUFFER_SIZE];

    OutputDebugStringA("Server work start\n");

    int recvTimeout = 30 * 1000;
    int sendTimeout = 30 * 1000;

    int len = sizeof(SOCKADDR);
    new_fd = accept(sockfd, (SOCKADDR*)&their_addr, &len);
    //在这里阻塞 挂起等待

    if (new_fd == SOCKET_ERROR)  //
    {
        swprintf_s(lastError, L"receive failed");
        OutputDebugStringA("Server receive failed\n");
        closesocket(sockfd);
        return false;
    }

    //设置超时时间
    setsockopt(new_fd, SOL_SOCKET, SO_RCVTIMEO, (char*)&recvTimeout, sizeof(int));
    setsockopt(new_fd, SOL_SOCKET, SO_SNDTIMEO, (char*)&sendTimeout, sizeof(int));

    std::wstring ipStr = NetUtils::IPToStringW(&their_addr);
    char str[128];
    sprintf_s(str, "New connection: %ws\n", ipStr.c_str());
    OutputDebugStringA(str);
    NotifyConnectionCallback(true, ipStr.c_str());

    while (running) {

        memset(recv_buf, 0, sizeof(recv_buf));

        int recv_len = recv(new_fd, recv_buf, BUFFER_SIZE, 0);
        if (recv_len <= 0) //这里 即使链接断开 也能继续监听 服务端不关闭
        {
            closesocket(new_fd);
            OutputDebugStringA("Connection close\n");
            NotifyConnectionCallback(false, nullptr);
            connected = false;

            sin_size = sizeof(struct sockaddr_in);
            new_fd = accept(sockfd, (struct sockaddr*)&their_addr, &sin_size);//在这里阻塞知道接收到消息，参数分别是socket句柄，接收到的地址信息以及大小 
            if (new_fd == SOCKET_ERROR) {
                swprintf_s(lastError, L"receive failed");
                OutputDebugStringA("Server receive failed\n");
                return false;
            }
            else {

                //设置超时时间
                setsockopt(new_fd, SOL_SOCKET, SO_RCVTIMEO, (char*)&recvTimeout, sizeof(int));
                setsockopt(new_fd, SOL_SOCKET, SO_SNDTIMEO, (char*)&sendTimeout, sizeof(int));

                char str[128];
                ipStr = NetUtils::IPToStringW(&their_addr);
                sprintf_s(str, "New connection: %ws\n", ipStr.c_str());
                OutputDebugStringA(str);
                NotifyConnectionCallback(true, ipStr.c_str());
                continue;
            }
        }

        bool shouldSendBack = false;

        if (strcmp(recv_buf, "") != 0) {

            if (strcmp(recv_buf, "coon") == 0) {
                if (needPass) 
                    strcpy_s(send_buf, "needpass");
                else {
                    connected = true;
                    strcpy_s(send_buf, "ok");
                }
                shouldSendBack = true;
            }
            else if (strncmp(recv_buf, "pas", 3) == 0) shouldSendBack = CheckPass(recv_buf, send_buf);
            else if (strncmp(recv_buf, "end", 3) == 0) {
                if (!CheckKey(recv_buf, send_buf))
                    goto SEND;
                connected = false;
                strcpy_s(clientKey, "");
                shouldSendBack = true;
                strcpy_s(send_buf, "ok");
            }
            else if (strncmp(recv_buf, "het", 3) == 0) {
                shouldSendBack = true;
                strcpy_s(send_buf, "ok");
            }
            else if (strncmp(recv_buf, "ifo", 3) == 0) {

                strcpy_s(send_buf, "ok");
                strcat_s(send_buf, "$win$");

                char sbuf[30];
                sprintf_s(sbuf, "%ws", NetUtils::GetCanonname().c_str());
                strcat_s(send_buf, sbuf);

                strcat_s(send_buf, "$k");

                shouldSendBack = true;
            }
            else if (strncmp(recv_buf, "num", 3) == 0) {

                //小键盘状态
                if((GetKeyState(VK_NUMLOCK) & 0x01))
                    strcpy_s(send_buf, "ok$on");
                else 
                    strcpy_s(send_buf, "ok$off");

                shouldSendBack = true;
            }
            else if (strncmp(recv_buf, "cap", 3) == 0) {

                //Caps lock状态
                if ((GetKeyState(VK_CAPITAL) & 0x01))
                    strcpy_s(send_buf, "ok$on");
                else
                    strcpy_s(send_buf, "ok$off");

                shouldSendBack = true;
            }
            else if (strncmp(recv_buf, "act", 3) == 0) {
                if (CheckKey(recv_buf, send_buf)) shouldSendBack = HandleActionCtl(recv_buf);
                else shouldSendBack = true;
            }
        }
SEND:
        if (shouldSendBack) {

            if(send(new_fd, send_buf, BUFFER_SIZE, 0) == -1)
                OutputDebugStringA("Send failed! \n");
        }
    }

    OutputDebugStringA("Server Quit\n");

    closesocket(new_fd);
    closesocket(sockfd);
    return true;
}
void MouseServer::NotifyCallback(bool running, const wchar_t* error)
{
    if (callback)
        callback(running, error, callbackData);
}
void MouseServer::NotifyConnectionCallback(bool connected, const wchar_t* ip)
{
    if (callbackConnectStatus)
        callbackConnectStatus(connected, ip, callbackConnectStatusData);
}

/*

send coon -> ok / needpass

   send needpass -> pasxxxxxxxxxx -> ok / badpass

send ctlkey16cmd... -> ok / badkey / ...

send end -> ok

*/

bool MouseServer::CheckKey(const char* recv_buf, char* send_buf)
{
    if (!needPass || strlen(clientKey) == 0)
        return true;

    char key[17];
    strncpy_s(key, recv_buf + 3, 16);
    key[16] = '\0';

    if (strcmp(key, clientKey) == 0)
        return true;

    if(send_buf)
        strcpy_s(send_buf, BUFFER_SIZE, "badkey");
    return false;
}
bool MouseServer::CheckPass(const char* recv_buf, char* send_buf)
{
    recv_buf += 3;
    if (strcmp(recv_buf, pass.c_str()) == 0) {
        connected = true;
        strcpy_s(clientKey, RandomUtils::GenRandomStringA(16).c_str());
        clientKey[16] = '\0';
        strcpy_s(send_buf, BUFFER_SIZE, "ok ");
        strcat_s(send_buf, BUFFER_SIZE, clientKey);
    }
    else strcpy_s(send_buf, BUFFER_SIZE, "badpass");
    return true;
}

//鼠标控制函数

const int MOUSE_EMPTY = 0;
const int MOUSE_DOWN = 1;
const int MOUSE_UP = 2;
const int MOUSE_MOVE = 3;
const int MOUSE_OUT = 4;
const int MOUSE_SCROLL = 5;

const int BUTTON_NONE = 0;
const int BUTTON_LEFT = 0x1;
const int BUTTON_RIGHT = 0x2;
const int BUTTON_MIDDLE = 0x4;

bool MouseServer::HandleCtl(const char* recv_buf)
{
    const char* cmd = recv_buf + 19;

    char type = cmd[0];
    char buttom = cmd[1];
    int x = 0, y = 0;
    for (int i = 0; i < 4; i++) {
        int shift = (3 - i) * 8;
        x += (cmd[2 + i] & 0xFF) << shift;
    }
    for (int i = 0; i < 4; i++) {
        int shift = (3 - i) * 8;
        y += (cmd[6 + i] & 0xFF) << shift;
    }

    INPUT input = { 0 };

    switch (type)
    {
    case MOUSE_DOWN:
        input.type = INPUT_MOUSE;
        if ((buttom & BUTTON_LEFT) == BUTTON_LEFT) input.mi.dwFlags = MOUSEEVENTF_LEFTDOWN;
        else if ((buttom & BUTTON_RIGHT) == BUTTON_RIGHT) input.mi.dwFlags = MOUSEEVENTF_RIGHTDOWN;
        else if ((buttom & BUTTON_MIDDLE) == BUTTON_MIDDLE) input.mi.dwFlags = MOUSEEVENTF_MIDDLEDOWN;

        SendInput(1, &input, sizeof(input));
        break;
    case MOUSE_UP:

        input.type = INPUT_MOUSE;
        if ((buttom & BUTTON_LEFT) == BUTTON_LEFT) input.mi.dwFlags = MOUSEEVENTF_LEFTUP;
        else if ((buttom & BUTTON_RIGHT) == BUTTON_RIGHT) input.mi.dwFlags = MOUSEEVENTF_RIGHTUP;
        else if ((buttom & BUTTON_MIDDLE) == BUTTON_MIDDLE) input.mi.dwFlags = MOUSEEVENTF_MIDDLEUP;

        SendInput(1, &input, sizeof(input));
        break;
    case MOUSE_MOVE: {

        int currX = x, currY = y;
        int deltaX = x / 2, deltaY = y / 2;
        bool xSmoothNotDone = true, ySmoothNotDone = true;

        do {

            xSmoothNotDone = deltaX != 0 && (x < 0 ? (currX < 0) : (currX > 0));
            ySmoothNotDone = deltaY != 0 && (y < 0 ? (currY < 0) : (currY > 0));

            input.type = INPUT_MOUSE;
            input.mi.dwFlags = MOUSEEVENTF_MOVE;
            if (xSmoothNotDone) {
                currX -= deltaX;
                input.mi.dx = -deltaX;
            }
            else
                input.mi.dx = 0;

            if (ySmoothNotDone) {
                currY -= deltaY;
                input.mi.dy = -deltaY;
            }
            else
                input.mi.dy = 0;

            SendInput(1, &input, sizeof(input));
            Sleep(2);

        } while (xSmoothNotDone || ySmoothNotDone);

        break;
    }
    case MOUSE_SCROLL: {

        input.type = INPUT_MOUSE;
        input.mi.dwFlags = MOUSEEVENTF_WHEEL;
        input.mi.mouseData = y > 0 ? WHEEL_DELTA : -WHEEL_DELTA;

        int yt = (abs(y) / x);
        if (yt > 0) {
            for (int i = 0; i < yt && i < 32; i++) {
                SendInput(1, &input, sizeof(input));
                Sleep(2);
            }
        }
        else
            SendInput(1, &input, sizeof(input));

        break;
    }
    default:
        break;
    }

    return false;
}

//键盘控制函数

const int KEY_EMPTY = 0;
const int KEY_DOWN = 1;
const int KEY_UP = 2;

char MyKeyCodeToMsVK(char code) {

    if ((code >= VK_NUMPAD0 && code <= VK_NUMPAD9) || code == VK_DECIMAL) {
        if ((GetKeyState(VK_NUMLOCK) & 0x01)) {
            switch (code)
            {
            case VK_NUMPAD0: return VK_INSERT;
            case VK_NUMPAD1: return VK_END;
            case VK_NUMPAD2: return VK_DOWN;
            case VK_NUMPAD3: return VK_NEXT;
            case VK_NUMPAD4: return VK_LEFT;
            case VK_NUMPAD5: return 0;
            case VK_NUMPAD6: return VK_RIGHT;
            case VK_NUMPAD7: return VK_HOME;
            case VK_NUMPAD8: return VK_UP;
            case VK_NUMPAD9: return VK_PRIOR;
            case VK_DECIMAL: return VK_DELETE;
            }
        }
    }


    return code;
}

bool MouseServer::HandleKeyCtl(const char* recv_buf)
{
    const char* cmd = recv_buf + 19;

    char type = cmd[0];
    char key = MyKeyCodeToMsVK(cmd[1]);
    if (key == 0)
        return false;

    INPUT input = { 0 };

    switch (type)
    {
    case KEY_DOWN:
        input.type = INPUT_KEYBOARD;
        input.ki.dwFlags = 0;
        input.ki.wVk = key;

        SendInput(1, &input, sizeof(input));
        break;
    case KEY_UP:
        input.type = INPUT_KEYBOARD;
        input.ki.wVk = key;
        input.ki.dwFlags = KEYEVENTF_KEYUP;

        SendInput(1, &input, sizeof(input));
        break;
    }


    return false;
}

//电脑控制函数

const int ACTION_SHUTDOWN = 4;
const int ACTION_REBOOT = 5;
const int ACTION_LOGOFF = 6;

bool MouseServer::HandleActionCtl(const char* recv_buf)
{
    const char* cmd = recv_buf + 19;

    char act = cmd[0];
    switch (act)
    {
    case ACTION_SHUTDOWN:
        ExitWindowsEx(EWX_FORCE | EWX_SHUTDOWN, 0);
        break;
    case ACTION_REBOOT:
        ExitWindowsEx(EWX_FORCE | EWX_REBOOT, 0);
        break;
    case ACTION_LOGOFF:
        ExitWindowsEx(EWX_FORCE | EWX_LOGOFF, 0);
        break;
    default:
        break;
    }


    return false;
}
