#include "NetUtils.h"
#include <ws2tcpip.h>

WCHAR canonname[32];

std::wstring NetUtils::GetCanonname() {
    return std::wstring(canonname);
}
std::wstring NetUtils::GetIP()
{
    WSADATA wsaData;
    std::wstring strResult;

    if (WSAStartup(MAKEWORD(2, 0), &wsaData) == 0)
    {

        ADDRINFOW* result = nullptr;
        ADDRINFOW* ptr = nullptr;
        ADDRINFOW hints = { 0 };
        hints.ai_flags = AI_CANONNAME;
        hints.ai_family = AF_UNSPEC;
        hints.ai_socktype = SOCK_STREAM;
        hints.ai_protocol = IPPROTO_TCP;
        if (GetAddrInfoW(L"", NULL, &hints, &result) == 0)
        {
            for (ptr = result; ptr != NULL; ptr = ptr->ai_next) {

                switch (ptr->ai_family) {
                case AF_INET:
                case AF_INET6:
                    DWORD ipbufferlength = 128;
                    WCHAR ipstringbuffer[128];
                    if(ptr->ai_canonname != nullptr)
                        wcscpy_s(canonname, ptr->ai_canonname);
                    if (WSAAddressToStringW(ptr->ai_addr, (DWORD)ptr->ai_addrlen, NULL,
                        ipstringbuffer, &ipbufferlength) == 0) {
                        strResult.append(ipstringbuffer);
                        strResult.append(L"\n");
                    }
                    break;
                }
            }
        }
        WSACleanup();
    }
    return strResult;
}

std::string NetUtils::IPToStringA(sockaddr_in* addr)
{
    char sendBuf[100] = { '\0' };
    inet_ntop(AF_INET, (void*)&addr->sin_addr, sendBuf, 100);
    return std::string(sendBuf);
}
std::wstring NetUtils::IPToStringW(sockaddr_in* addr)
{
    wchar_t sendBuf[100] = { L'\0' };
    InetNtop(AF_INET, (void*)&addr->sin_addr, sendBuf, 100);
    return std::wstring(sendBuf);
}
