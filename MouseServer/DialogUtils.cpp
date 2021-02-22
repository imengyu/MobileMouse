#include "DialogUtils.h"

std::wstring DialogUtils::GetDlgItemTextEx(HWND hDlg, int nItem, int bufferSize)
{
    std::wstring buffer;
    buffer.resize(bufferSize);
    GetDlgItemText(hDlg, nItem, (LPWSTR)buffer.data(), bufferSize);
    return buffer;
}
