#include "SettingUtils.h"

SettingUtils::SettingUtils(LPCWSTR setttingFilePath)
{
	wcscpy_s(this->setttingFilePath, setttingFilePath);
}
SettingUtils::~SettingUtils()
{
}
bool SettingUtils::GetSettingBool(LPCWSTR key, bool defaultValue, LPCWSTR appKey)
{
	WCHAR w[16];
	GetPrivateProfileString(appKey, key, defaultValue ? L"TRUE" : L"FALSE", w, 16, setttingFilePath);
	return wcscmp(w, L"TRUE") == 0;
}
int SettingUtils::GetSettingInt(LPCWSTR key, int defaultValue, LPCWSTR appKey)
{
	WCHAR w[32];
	swprintf_s(w, L"%d", defaultValue);
	GetPrivateProfileString(appKey, key, w, w, 32, setttingFilePath);
	return _wtoi(w);
}
std::wstring SettingUtils::GetSettingStr(LPCWSTR key, LPCWSTR defaultValue, size_t bufferSize, LPCWSTR appKey)
{
	std::wstring tmp;
	tmp.resize(bufferSize + 1);
	GetPrivateProfileString(appKey, key, defaultValue, (wchar_t*)tmp.data(), (DWORD)(bufferSize + 1), setttingFilePath);
	return tmp;
}

bool SettingUtils::SetSettingBool(LPCWSTR key, bool value, LPCWSTR appKey)
{
	return WritePrivateProfileString(appKey, key, value ? L"TRUE" : L"FALSE", setttingFilePath);
}
bool SettingUtils::SetSettingInt(LPCWSTR key, int value, LPCWSTR appKey)
{
	WCHAR w[30]; 
	_itow_s(value, w, 10);
	return WritePrivateProfileString(appKey, key, w, setttingFilePath);
}
bool SettingUtils::SetSettingStr(LPCWSTR key, LPCWSTR value, LPCWSTR appKey)
{
	return WritePrivateProfileString(appKey, key, value, setttingFilePath);
}
bool SettingUtils::SetSettingStr(LPCWSTR key, std::wstring value, LPCWSTR appKey)
{
	return SetSettingStr(key, value.c_str(), appKey);
}