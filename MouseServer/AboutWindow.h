#pragma once
#include "framework.h"

class AboutWindow
{
public:
    int Show(HWND parent);

private:
    static INT_PTR CALLBACK About(HWND hDlg, UINT message, WPARAM wParam, LPARAM lParam);
};

