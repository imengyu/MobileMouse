#pragma once
#include "resource.h"
#include <WinSock2.h>
#include <Ws2tcpip.h> 
#include <string>
#include <intrin.h>
#include "NetUtils.h"

#define PORT 2166//�˿ں� 
#define DISCOVER_PORT 2167//�˿ں� 
#define CONTROL_PORT 2166
#define BACKLOG 5/*��������*/ 
#define BUFFER_SIZE 128//���շ��ͻ�������С 

#define PACKET_PREFIX '$'
#define PACKET_TYPE_SEARCH_DEVICE_REQ 0x10
#define PACKET_TYPE_SEARCH_DEVICE_RSP 0x11;
#define SEARCH_DEVICE_TIMES 3

typedef void(* MouseServerRunStatusCallback)(bool running, const wchar_t*error, void*data);
typedef void(* MouseServerConnectionStatusCallback)(bool connected, const wchar_t* ip, void* data);

class MouseServer
{
public:

    bool Start();
    bool Stop();

    void SetCallback(MouseServerRunStatusCallback callback, void* data);
    void SetConnectStatusCallback(MouseServerConnectionStatusCallback callback, void* data);

    void SetNeedPass(bool needPass);
    void SetPass(const WCHAR* pass);
    bool GetIsRunning() {
        return running;
    }
    const wchar_t* GetLastError() {
        return lastError;
    }

private:

    static DWORD WINAPI WorkThread(LPVOID lpThreadParameter);
    static DWORD WINAPI SearchRespThread(LPVOID lpThreadParameter);
    static DWORD WINAPI ReceiveControlCommandThread(LPVOID lpThreadParameter);
    bool Work();
    void SearchResp();
    void ReceiveControlCommand();
    int PackSearchRespData(char* data);
    bool VerifySearchData(int recvbytes, char* recvbuf);
    void NotifyCallback(bool running, const wchar_t* error);
    void NotifyConnectionCallback(bool connected, const wchar_t* ip);

    MouseServerRunStatusCallback callback = nullptr;
    void* callbackData = nullptr;
    MouseServerConnectionStatusCallback callbackConnectStatus = nullptr;
    void* callbackConnectStatusData = nullptr;

    std::string pass;
    bool needPass;

    HANDLE hThread = nullptr;
    wchar_t lastError[128] = { 0 };
    bool running = false;

    SOCKET sockfd, new_fd; /*socket����ͽ������Ӻ�ľ��*/
    SOCKET sockReceiveCommandListen;
    SOCKET sockSearchListen;

    struct sockaddr_in my_addr; /*������ַ��Ϣ�ṹ�壬�����о�������Ը�ֵ*/
    struct sockaddr_in their_addr; /*�Է���ַ��Ϣ*/
    int sin_size;

    int timeout = 300;
    int buf = 10240;

    bool connected = false;
    char clientKey[17];

    bool CheckKey(const char* recv_buf, char* send_buf);
    bool CheckPass(const char* recv_buf, char* send_buf);
    bool HandleCtl(const char* recv_buf);
    bool HandleKeyCtl(const char* recv_buf);
    bool HandleActionCtl(const char* recv_buf);
};
