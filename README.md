# activity-monitor

This projects records info about running processes on given computer. 
Information is obtained by running system command

Windows:
tasklist /FI "STATUS eq running" /FO CSV /NH

Data is saved into database. Report is executed on predefined schedule.


Some other usefull commands on Windows.
Find where given program is running:   where /R C:\ notepad.exe
Kill program:                          taskkill /pid /F 17568
Start Program minimalized:             start /MIN notepad.exe