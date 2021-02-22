#pragma once
#include "framework.h"
#include <string>

#define SETTING_DAFAULT_APP_KEY L"App"

class SettingUtils
{
public:
	SettingUtils(LPCWSTR setttingFilePath);
	~SettingUtils();

	bool GetSettingBool(LPCWSTR key, bool defaultValue = true, LPCWSTR appKey = SETTING_DAFAULT_APP_KEY);
	int GetSettingInt(LPCWSTR key, int defaultValue = 0, LPCWSTR appKey = SETTING_DAFAULT_APP_KEY);
	std::wstring GetSettingStr(LPCWSTR key, LPCWSTR defaultValue = L"", size_t bufferSize = 32, LPCWSTR appKey = SETTING_DAFAULT_APP_KEY);
	std::wstring* GetSettingStrPtr(LPCWSTR key, LPCWSTR defaultValue = L"", size_t bufferSize = 32, LPCWSTR appKey = SETTING_DAFAULT_APP_KEY) {
		return new std::wstring(GetSettingStr(key, defaultValue, bufferSize, appKey));
	}

	bool SetSettingBool(LPCWSTR key, bool value, LPCWSTR appKey = SETTING_DAFAULT_APP_KEY);
	bool SetSettingInt(LPCWSTR key, int value, LPCWSTR appKey = SETTING_DAFAULT_APP_KEY);
	bool SetSettingStr(LPCWSTR key, LPCWSTR value, LPCWSTR appKey = SETTING_DAFAULT_APP_KEY);
	bool SetSettingStr(LPCWSTR key, std::wstring value, LPCWSTR appKey = SETTING_DAFAULT_APP_KEY);
private:
	WCHAR setttingFilePath[MAX_PATH] = { 0 };
};

