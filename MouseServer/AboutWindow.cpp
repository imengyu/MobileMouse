#include "AboutWindow.h"
#include "Resource.h"

extern HINSTANCE hInst;

int AboutWindow::Show(HWND parent)
{
    return (int)DialogBox(hInst, MAKEINTRESOURCE(IDD_ABOUTBOX), parent, About);
}

// “关于”框的消息处理程序。
INT_PTR CALLBACK AboutWindow::About(HWND hDlg, UINT message, WPARAM wParam, LPARAM lParam)
{
    UNREFERENCED_PARAMETER(lParam);
    switch (message)
    {
    case WM_INITDIALOG:
        SendMessage(hDlg, WM_SETICON, ICON_BIG, (LPARAM)LoadIcon(hInst, MAKEINTRESOURCE(IDI_LOGO)));
        SendMessage(hDlg, WM_SETICON, ICON_SMALL, (LPARAM)LoadIcon(hInst, MAKEINTRESOURCE(IDI_LOGO_SMALL)));
        return (INT_PTR)TRUE;
    case WM_COMMAND:
        if (LOWORD(wParam) == IDOK || LOWORD(wParam) == IDCANCEL)
        {
            EndDialog(hDlg, LOWORD(wParam));
            return (INT_PTR)TRUE;
        }
        break;
    }
    return (INT_PTR)FALSE;
}
