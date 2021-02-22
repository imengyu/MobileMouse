#pragma once
#include "framework.h"
#include <winsock2.h>
#include <windef.h>   
#include <Nb30.h>
#include <string>

class NetUtils
{
public:
    static std::wstring GetCanonname();
    static std::wstring GetIP();
    static std::string IPToStringA(sockaddr_in* addr);
    static std::wstring IPToStringW(sockaddr_in* addr);
};

