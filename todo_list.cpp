#include <windows.h>
#include <commctrl.h>
#include <string>
#include <vector>
#include <fstream>

#pragma comment(lib, "comctl32.lib")

struct Task {
    std::wstring text;
    bool completed;
};

std::vector<Task> tasks;
HWND hListBox, hEditTask, hButtonAdd, hButtonDelete, hButtonToggle, hButtonSave;
const wchar_t* SAVE_FILE = L"tasks.txt";

LRESULT CALLBACK WindowProc(HWND hwnd, UINT uMsg, WPARAM wParam, LPARAM lParam);
void AddTask(const std::wstring& taskText);
void DeleteSelectedTask();
void ToggleSelectedTask();
void RefreshListBox();
void SaveTasks();
void LoadTasks();

int WINAPI WinMain(HINSTANCE hInstance, HINSTANCE hPrevInstance, LPSTR lpCmdLine, int nCmdShow) {
    const wchar_t CLASS_NAME[] = L"TodoListClass";
    
    WNDCLASS wc = {};
    wc.lpfnWndProc = WindowProc;
    wc.hInstance = hInstance;
    wc.lpszClassName = CLASS_NAME;
    wc.hbrBackground = (HBRUSH)(COLOR_WINDOW + 1);
    wc.hCursor = LoadCursor(NULL, IDC_ARROW);
    
    RegisterClass(&wc);
    
    HWND hwnd = CreateWindowEx(
        0, CLASS_NAME, L"To-Do List",
        WS_OVERLAPPEDWINDOW,
        CW_USEDEFAULT, CW_USEDEFAULT, 500, 550,
        NULL, NULL, hInstance, NULL
    );
    
    if (hwnd == NULL) return 0;
    
    CreateWindow(L"STATIC", L"New Task:",
        WS_VISIBLE | WS_CHILD,
        20, 20, 80, 25, hwnd, NULL, hInstance, NULL);
    
    hEditTask = CreateWindow(L"EDIT", L"",
        WS_VISIBLE | WS_CHILD | WS_BORDER | ES_AUTOHSCROLL,
        110, 20, 280, 25, hwnd, (HMENU)1, hInstance, NULL);
    
    hButtonAdd = CreateWindow(L"BUTTON", L"Add",
        WS_VISIBLE | WS_CHILD | BS_PUSHBUTTON,
        400, 20, 70, 25, hwnd, (HMENU)2, hInstance, NULL);
    
    hListBox = CreateWindow(L"LISTBOX", NULL,
        WS_VISIBLE | WS_CHILD | WS_BORDER | WS_VSCROLL | LBS_NOTIFY,
        20, 60, 450, 350, hwnd, (HMENU)3, hInstance, NULL);
    
    hButtonToggle = CreateWindow(L"BUTTON", L"Mark Complete/Incomplete",
        WS_VISIBLE | WS_CHILD | BS_PUSHBUTTON,
        20, 425, 200, 30, hwnd, (HMENU)4, hInstance, NULL);
    
    hButtonDelete = CreateWindow(L"BUTTON", L"Delete Task",
        WS_VISIBLE | WS_CHILD | BS_PUSHBUTTON,
        230, 425, 120, 30, hwnd, (HMENU)5, hInstance, NULL);
    
    hButtonSave = CreateWindow(L"BUTTON", L"Save Tasks",
        WS_VISIBLE | WS_CHILD | BS_PUSHBUTTON,
        360, 425, 110, 30, hwnd, (HMENU)6, hInstance, NULL);
    
    CreateWindow(L"STATIC", L"Tip: Double-click a task to mark it complete",
        WS_VISIBLE | WS_CHILD | SS_CENTER,
        20, 470, 450, 20, hwnd, NULL, hInstance, NULL);
    
    LoadTasks();
    RefreshListBox();
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
            SaveTasks();
            PostQuitMessage(0);
            return 0;
            
        case WM_COMMAND:
            if (LOWORD(wParam) == 2) {
                wchar_t buffer[256];
                GetWindowText(hEditTask, buffer, 256);
                
                if (wcslen(buffer) > 0) {
                    AddTask(buffer);
                    SetWindowText(hEditTask, L"");
                    RefreshListBox();
                }
            }
            else if (LOWORD(wParam) == 4) {
                ToggleSelectedTask();
                RefreshListBox();
            }
            else if (LOWORD(wParam) == 5) {
                DeleteSelectedTask();
                RefreshListBox();
            }
            else if (LOWORD(wParam) == 6) {
                SaveTasks();
                MessageBox(hwnd, L"Tasks saved successfully!", L"Saved", MB_OK | MB_ICONINFORMATION);
            }
            else if (LOWORD(wParam) == 3 && HIWORD(wParam) == LBN_DBLCLK) {
                ToggleSelectedTask();
                RefreshListBox();
            }
            return 0;
    }
    return DefWindowProc(hwnd, uMsg, wParam, lParam);
}

void AddTask(const std::wstring& taskText) {
    Task newTask;
    newTask.text = taskText;
    newTask.completed = false;
    tasks.push_back(newTask);
}

void DeleteSelectedTask() {
    int selected = SendMessage(hListBox, LB_GETCURSEL, 0, 0);
    if (selected != LB_ERR && selected < (int)tasks.size()) {
        tasks.erase(tasks.begin() + selected);
    }
}

void ToggleSelectedTask() {
    int selected = SendMessage(hListBox, LB_GETCURSEL, 0, 0);
    if (selected != LB_ERR && selected < (int)tasks.size()) {
        tasks[selected].completed = !tasks[selected].completed;
    }
}

void RefreshListBox() {
    SendMessage(hListBox, LB_RESETCONTENT, 0, 0);
    
    for (size_t i = 0; i < tasks.size(); i++) {
        std::wstring displayText;
        if (tasks[i].completed) {
            displayText = L"[✓] " + tasks[i].text;
        } else {
            displayText = L"[ ] " + tasks[i].text;
        }
        SendMessage(hListBox, LB_ADDSTRING, 0, (LPARAM)displayText.c_str());
    }
}

void SaveTasks() {
    std::wofstream file(SAVE_FILE);
    if (file.is_open()) {
        for (const auto& task : tasks) {
            file << (task.completed ? L"1" : L"0") << L"|" << task.text << L"\n";
        }
        file.close();
    }
}

void LoadTasks() {
    std::wifstream file(SAVE_FILE);
    if (file.is_open()) {
        std::wstring line;
        while (std::getline(file, line)) {
            if (line.length() > 2) {
                Task task;
                task.completed = (line[0] == L'1');
                task.text = line.substr(2);
                tasks.push_back(task);
            }
        }
        file.close();
    }
}
