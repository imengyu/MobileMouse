#include "RandomUtils.h"
#include <time.h>

std::wstring RandomUtils::GenRandomStringW(int len)
{
	int flag, i;
	std::wstring output;
	output.resize(len);
	srand((unsigned)time(NULL));
	for (i = 0; i < len - 1; i++)
	{
		flag = rand() % 3;
		switch (flag)
		{
		case 0:
			output[i] = L'A' + rand() % 26;
			break;
		case 1:
			output[i] = L'a' + rand() % 26;
			break;
		case 2:
			output[i] = L'0' + rand() % 10;
			break;
		default:
			output[i] = L'x';
			break;
		}
	}
    return output;
}
std::string RandomUtils::GenRandomStringA(int len)
{
	int flag, i;
	std::string output;
	output.resize(len);
	srand((unsigned)time(NULL));
	for (i = 0; i < len - 1; i++)
	{
		flag = rand() % 3;
		switch (flag)
		{
		case 0:
			output[i] = 'A' + rand() % 26;
			break;
		case 1:
			output[i] = 'a' + rand() % 26;
			break;
		case 2:
			output[i] = '0' + rand() % 10;
			break;
		default:
			output[i] = 'x';
			break;
		}
	}
	return output;
}
