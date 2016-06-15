@ECHO off

set INCLUDE=C:\Program Files (x86)\Microsoft Visual Studio 14.0\VC\include
set LIB=C:\Program Files (x86)\Microsoft Visual Studio 14.0\VC\lib\amd64

set CL_DIR="C:\Program Files (x86)\Microsoft Visual Studio 14.0\VC\bin\amd64\"

set C_OPT=/Ox /favor:AMD64 /nologo /EHsc -MP

set INCLUDE_JNI=/I "C:\Program Files\Java\jdk1.8.0_66\include" /I "C:\Program Files\Java\jdk1.8.0_66\include\win32"

rem x64
call %CL_DIR%vcvars64.bat
%CL_DIR%cl %C_OPT% %INCLUDE_JNI% /I "C:\AMD\acml\ifort64_mp\include" src\main\c\acml_wrapper.cpp "C:\AMD\acml\ifort64_mp\lib\libacml_mp_dll.lib"  /LD /Fetarget\bin\acml_wrapper.dll

rem MP
rem %CL_DIR%cl %C_OPT% %INCLUDE_JNI% /I "C:\AMD\acml4.4.0\win64_mp\include" src\main\c\acml_wrapper.cpp "C:\AMD\acml4.4.0\win64_mp\lib\libacml_mp_dll.lib" /LD /Fotarget\bin\acml_wrapper_mp.obj /Fetarget\bin\acml_wrapper_mp.dll


rem GPU
rem %CL_DIR%cl %C_OPT% %INCLUDE_JNI% /I "C:\AMD\acmlgpu1.1.2\win64\include" src\main\c\acml_wrapper.cpp "C:\AMD\acmlgpu1.1.2\win64\lib\libacml_dll.lib" /LD /Fotarget\bin\acml_wrapper_gpu.obj /Fetarget\bin\acml_wrapper_gpu.dll

rem x86
rem cl %C_OPT% %INCLUDE_JNI% /I "C:\AMD\acml4.4.0\ifort32_mp\include" src\main\c\acml_wrapper.cpp "C:\AMD\acml4.4.0\ifort32_mp\lib\libacml_mp_dll.lib" /LD /Fotarget\bin\acml_wrapper32_mp.obj /Fetarget\bin\acml_wrapper32_mp.dll 