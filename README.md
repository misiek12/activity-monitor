# activity-monitor

This projects records info about running processes into  system event log. 
There are several windows commands that are used to get data and run automated tests

Get info about all running processes
tasklist /FI "STATUS eq running" /FO CSV /NH

Find where given program is running
where /R C:\ notepad.exe

Kill program
taskkill /pid /F 17568

Start Program minimalized
start /MIN notepad.exe