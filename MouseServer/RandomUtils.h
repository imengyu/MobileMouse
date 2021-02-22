#pragma once
#include "framework.h"
#include <string>

class RandomUtils
{
public:
    static std::wstring GenRandomStringW(int len);
    static std::string GenRandomStringA(int len);
};

