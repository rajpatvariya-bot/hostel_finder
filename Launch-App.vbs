' ==================================================
' Hostel Finder - VBS Quick Launcher
' Double-click this file to start the application
' ==================================================

Set objShell = CreateObject("WScript.Shell")
Set objFSO = CreateObject("Scripting.FileSystemObject")

projectRoot = objFSO.GetParentFolderName(WScript.ScriptFullName)

' Information message
msgbox "Hostel Finder Application Launcher" & vbCrLf & vbCrLf & _
"This will start:" & vbCrLf & _
"1. Backend Server (Spring Boot)" & vbCrLf & _
"2. Frontend Server (HTTP)" & vbCrLf & _
"3. Open your browser" & vbCrLf & vbCrLf & _
"Keep the terminal windows open!" & vbCrLf & vbCrLf & _
"Click OK to continue...", vbInformation, "Hostel Finder Launcher"

' Run the batch file
objShell.Run """" & projectRoot & "\run.bat""", 1, False

' Open the app in browser after a delay
WScript.Sleep(12000)
objShell.Run "http://localhost:3000/pages/index.html"
