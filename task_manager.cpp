#include <windows.h>
#include <pdh.h>
#include <pdhmsg.h>
#include <string>
#include <sstream>
#include <iomanip>

#pragma comment(lib, "pdh.lib")

PDH_HQUERY cpuQuery;
PDH_HCOUNTER cpuCounter;

LRESULT CALLBACK WindowProc(HWND hwnd, UINT uMsg, WPARAM wParam, LPARAM lParam);

void InitCPU() {
    PdhOpenQuery(NULL, NULL, &cpuQuery);
    PdhAddEnglishCounter(cpuQuery, L"\\Processor(_Total)\\% Processor Time", NULL, &cpuCounter);
    PdhCollectQueryData(cpuQuery);
}

double GetCPUUsage() {
    PDH_FMT_COUNTERVALUE counterVal;
    PDH_STATUS status = PdhCollectQueryData(cpuQuery);
    if (status == ERROR_SUCCESS) {
        status = PdhGetFormattedCounterValue(cpuCounter, PDH_FMT_DOUBLE, NULL, &counterVal);
        if (status == ERROR_SUCCESS) {
            return counterVal.doubleValue;
        }
    }
    return 0.0;
}

void GetRAMUsage(DWORDLONG& totalRAM, DWORDLONG& usedRAM, double& usagePercent) {
    MEMORYSTATUSEX memInfo;
    memInfo.dwLength = sizeof(MEMORYSTATUSEX);
    GlobalMemoryStatusEx(&memInfo);
    
    totalRAM = memInfo.ullTotalPhys / (1024ULL * 1024ULL); // Convert to MB
    DWORDLONG availRAM = memInfo.ullAvailPhys / (1024ULL * 1024ULL);
    usedRAM = totalRAM - availRAM;
    usagePercent = ((double)usedRAM / (double)totalRAM) * 100.0;
}

int WINAPI WinMain(HINSTANCE hInstance, HINSTANCE hPrevInstance, LPSTR lpCmdLine, int nCmdShow) {
    const wchar_t CLASS_NAME[] = L"SystemMonitorClass";
    
    WNDCLASS wc = {};
    wc.lpfnWndProc = WindowProc;
    wc.hInstance = hInstance;
    wc.lpszClassName = CLASS_NAME;
    wc.hbrBackground = (HBRUSH)(COLOR_WINDOW + 1);
    wc.hCursor = LoadCursor(NULL, IDC_ARROW);
    
    RegisterClass(&wc);
    
    HWND hwnd = CreateWindowEx(
        0, CLASS_NAME, L"System Monitor - Task Manager",
        WS_OVERLAPPEDWINDOW,
        CW_USEDEFAULT, CW_USEDEFAULT, 500, 400,
        NULL, NULL, hInstance, NULL
    );
    
    if (hwnd == NULL) return 0;
    
    InitCPU();
    ShowWindow(hwnd, nCmdShow);
    
    for (int i = 0; i < 3; i++) {
        PdhCollectQueryData(cpuQuery);
        Sleep(100);
    }
    
    SetTimer(hwnd, 1, 500, NULL);
    
    MSG msg = {};
    while (GetMessage(&msg, NULL, 0, 0)) {
        TranslateMessage(&msg);
        DispatchMessage(&msg);
    }
    
    PdhCloseQuery(cpuQuery);
    return 0;
}

LRESULT CALLBACK WindowProc(HWND hwnd, UINT uMsg, WPARAM wParam, LPARAM lParam) {
    switch (uMsg) {
        case WM_DESTROY:
            KillTimer(hwnd, 1);
            PostQuitMessage(0);
            return 0;
            
        case WM_TIMER:
            InvalidateRect(hwnd, NULL, TRUE);
            return 0;
            
        case WM_PAINT: {
            PAINTSTRUCT ps;
            HDC hdc = BeginPaint(hwnd, &ps);
            
            HFONT hFont = CreateFont(20, 0, 0, 0, FW_BOLD, FALSE, FALSE, FALSE,
                DEFAULT_CHARSET, OUT_DEFAULT_PRECIS, CLIP_DEFAULT_PRECIS,
                DEFAULT_QUALITY, DEFAULT_PITCH | FF_SWISS, L"Arial");
            HFONT hOldFont = (HFONT)SelectObject(hdc, hFont);
            
            double cpuUsage = GetCPUUsage();
            DWORDLONG totalRAM, usedRAM;
            double ramPercent;
            GetRAMUsage(totalRAM, usedRAM, ramPercent);
            
            int y = 30;
            std::wstringstream ss;
            
            SetTextColor(hdc, RGB(0, 102, 204));
            TextOut(hdc, 20, y, L"SYSTEM MONITOR", (int)wcslen(L"SYSTEM MONITOR"));
            y += 50;
            
            SetTextColor(hdc, RGB(0, 0, 0));
            ss << L"CPU Usage: " << std::fixed << std::setprecision(1) << cpuUsage << L"%";
            TextOut(hdc, 20, y, ss.str().c_str(), (int)ss.str().length());
            
            RECT rect = {20, y + 30, 450, y + 50};
            FrameRect(hdc, &rect, (HBRUSH)GetStockObject(BLACK_BRUSH));
            rect.right = 20 + (int)((430 * cpuUsage) / 100);
            HBRUSH cpuBrush = CreateSolidBrush(RGB(0, 200, 0));
            FillRect(hdc, &rect, cpuBrush);
            DeleteObject(cpuBrush);
            y += 70;
            
            ss.str(L"");
            ss << L"RAM: " << usedRAM << L" MB / " << totalRAM << L" MB (" 
               << std::fixed << std::setprecision(1) << ramPercent << L"%)";
            TextOut(hdc, 20, y, ss.str().c_str(), (int)ss.str().length());
            
            rect = {20, y + 30, 450, y + 50};
            FrameRect(hdc, &rect, (HBRUSH)GetStockObject(BLACK_BRUSH));
            rect.right = 20 + (int)((430 * ramPercent) / 100);
            HBRUSH ramBrush = CreateSolidBrush(RGB(255, 165, 0));
            FillRect(hdc, &rect, ramBrush);
            DeleteObject(ramBrush);
            y += 70;

            
            SelectObject(hdc, hOldFont);
            DeleteObject(hFont);
            EndPaint(hwnd, &ps);
            return 0;
        }
    }
    return DefWindowProc(hwnd, uMsg, wParam, lParam);
}
