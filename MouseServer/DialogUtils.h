#pragma once
#include "framework.h"
#include <string>

class DialogUtils
{
public:
    static std::wstring GetDlgItemTextEx(HWND hDlg, int nItem, int bufferSize);
};

