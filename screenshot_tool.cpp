#include <windows.h>
#include <string>
#include <sstream>
#include <ctime>

LRESULT CALLBACK WindowProc(HWND hwnd, UINT uMsg, WPARAM wParam, LPARAM lParam);
bool CaptureScreen(const wchar_t* filename);
std::wstring GetTimestampFilename();

HWND hButtonFullscreen, hStatusText;
std::wstring lastStatus = L"Ready to capture screenshot";

int WINAPI WinMain(HINSTANCE hInstance, HINSTANCE hPrevInstance, LPSTR lpCmdLine, int nCmdShow) {
    const wchar_t CLASS_NAME[] = L"ScreenshotToolClass";
    
    WNDCLASS wc = {};
    wc.lpfnWndProc = WindowProc;
    wc.hInstance = hInstance;
    wc.lpszClassName = CLASS_NAME;
    wc.hbrBackground = (HBRUSH)(COLOR_WINDOW + 1);
    wc.hCursor = LoadCursor(NULL, IDC_ARROW);
    
    RegisterClass(&wc);
    
    HWND hwnd = CreateWindowEx(
        0, CLASS_NAME, L"Screenshot Tool",
        WS_OVERLAPPEDWINDOW,
        CW_USEDEFAULT, CW_USEDEFAULT, 450, 300,
        NULL, NULL, hInstance, NULL
    );
    
    if (hwnd == NULL) return 0;
    
    CreateWindow(L"STATIC", L"Screenshot Tool - Capture your screen",
        WS_VISIBLE | WS_CHILD,
        20, 20, 400, 30, hwnd, NULL, hInstance, NULL);
    
    hButtonFullscreen = CreateWindow(L"BUTTON", L"Capture Fullscreen",
        WS_VISIBLE | WS_CHILD | BS_PUSHBUTTON,
        50, 70, 350, 50, hwnd, (HMENU)1, hInstance, NULL);
    
    hStatusText = CreateWindow(L"STATIC", lastStatus.c_str(),
        WS_VISIBLE | WS_CHILD | SS_CENTER,
        20, 150, 400, 30, hwnd, (HMENU)3, hInstance, NULL);
    
    ShowWindow(hwnd, nCmdShow);
    
    MSG msg = {};
    while (GetMessage(&msg, NULL, 0, 0)) {
        TranslateMessage(&msg);
        DispatchMessage(&msg);
    }
    
    return 0;
}

LRESULT CALLBACK WindowProc(HWND hwnd, UINT uMsg, WPARAM wParam, LPARAM lParam) {
    switch (uMsg) {
        case WM_DESTROY:
            PostQuitMessage(0);
            return 0;
            
        case WM_COMMAND:
            if (LOWORD(wParam) == 1) {
                std::wstring filename = GetTimestampFilename();
                if (CaptureScreen(filename.c_str())) {
                    lastStatus = L"Screenshot saved: " + filename;
                    MessageBox(hwnd, (L"Screenshot saved as:\n" + filename).c_str(), 
                              L"Success", MB_OK | MB_ICONINFORMATION);
                } else {
                    lastStatus = L"Failed to capture screenshot";
                    MessageBox(hwnd, L"Failed to capture screenshot", L"Error", MB_OK | MB_ICONERROR);
                }
                SetWindowText(hStatusText, lastStatus.c_str());
            }
            return 0;
            
        case WM_PAINT: {
            PAINTSTRUCT ps;
            HDC hdc = BeginPaint(hwnd, &ps);
            
            HBRUSH blueBrush = CreateSolidBrush(RGB(0, 120, 215));
            RECT iconRect = {180, 230, 270, 250};
            FillRect(hdc, &iconRect, blueBrush);
            DeleteObject(blueBrush);
            
            EndPaint(hwnd, &ps);
            return 0;
        }
    }
    return DefWindowProc(hwnd, uMsg, wParam, lParam);
}

bool CaptureScreen(const wchar_t* filename) {
    int screenWidth = GetSystemMetrics(SM_CXSCREEN);
    int screenHeight = GetSystemMetrics(SM_CYSCREEN);
    
    HDC hScreenDC = GetDC(NULL);
    HDC hMemoryDC = CreateCompatibleDC(hScreenDC);
    
    HBITMAP hBitmap = CreateCompatibleBitmap(hScreenDC, screenWidth, screenHeight);
    HBITMAP hOldBitmap = (HBITMAP)SelectObject(hMemoryDC, hBitmap);
    
    BitBlt(hMemoryDC, 0, 0, screenWidth, screenHeight, hScreenDC, 0, 0, SRCCOPY);
    hBitmap = (HBITMAP)SelectObject(hMemoryDC, hOldBitmap);
    
    BITMAP bmp;
    GetObject(hBitmap, sizeof(BITMAP), &bmp);
    
    BITMAPINFOHEADER bi;
    bi.biSize = sizeof(BITMAPINFOHEADER);
    bi.biWidth = bmp.bmWidth;
    bi.biHeight = bmp.bmHeight;
    bi.biPlanes = 1;
    bi.biBitCount = 24;
    bi.biCompression = BI_RGB;
    bi.biSizeImage = 0;
    bi.biXPelsPerMeter = 0;
    bi.biYPelsPerMeter = 0;
    bi.biClrUsed = 0;
    bi.biClrImportant = 0;
    
    DWORD dwBmpSize = ((bmp.bmWidth * bi.biBitCount + 31) / 32) * 4 * bmp.bmHeight;
    
    HANDLE hDIB = GlobalAlloc(GHND, dwBmpSize);
    char* lpbitmap = (char*)GlobalLock(hDIB);
    
    GetDIBits(hMemoryDC, hBitmap, 0, (UINT)bmp.bmHeight, lpbitmap, 
              (BITMAPINFO*)&bi, DIB_RGB_COLORS);
    
    HANDLE hFile = CreateFile(filename, GENERIC_WRITE, 0, NULL, CREATE_ALWAYS, 
                              FILE_ATTRIBUTE_NORMAL, NULL);
    
    bool success = false;
    if (hFile != INVALID_HANDLE_VALUE) {
        DWORD dwBytesWritten;
        
        BITMAPFILEHEADER bmfHeader;
        bmfHeader.bfType = 0x4D42; // "BM"
        bmfHeader.bfSize = dwBmpSize + sizeof(BITMAPFILEHEADER) + sizeof(BITMAPINFOHEADER);
        bmfHeader.bfReserved1 = 0;
        bmfHeader.bfReserved2 = 0;
        bmfHeader.bfOffBits = sizeof(BITMAPFILEHEADER) + sizeof(BITMAPINFOHEADER);
        
        WriteFile(hFile, (LPSTR)&bmfHeader, sizeof(BITMAPFILEHEADER), &dwBytesWritten, NULL);
        WriteFile(hFile, (LPSTR)&bi, sizeof(BITMAPINFOHEADER), &dwBytesWritten, NULL);
        WriteFile(hFile, (LPSTR)lpbitmap, dwBmpSize, &dwBytesWritten, NULL);
        
        CloseHandle(hFile);
        success = true;
    }
    
    GlobalUnlock(hDIB);
    GlobalFree(hDIB);
    DeleteObject(hBitmap);
    DeleteDC(hMemoryDC);
    ReleaseDC(NULL, hScreenDC);
    
    return success;
}

std::wstring GetTimestampFilename() {
    time_t now = time(0);
    tm ltm;
    localtime_s(&ltm, &now);
    
    wchar_t buffer[256];
    swprintf_s(buffer, 256, L"screenshot_%04d%02d%02d_%02d%02d%02d.bmp",
              1900 + ltm.tm_year, 1 + ltm.tm_mon, ltm.tm_mday,
              ltm.tm_hour, ltm.tm_min, ltm.tm_sec);
    
    return std::wstring(buffer);
}
