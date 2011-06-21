rem @ECHO off

set INCLUDE=C:\Program Files (x86)\Microsoft Visual Studio 10.0\VC\include
set LIB=C:\Program Files (x86)\Microsoft Visual Studio 10.0\VC\lib\amd64;C:\Program Files (x86)\Microsoft SDKs\Windows\v7.0A\Lib\x64
REM set LIB=C:\Program Files (x86)\Microsoft Visual Studio 10.0\VC\lib;C:\Program Files (x86)\Microsoft SDKs\Windows\v7.0A\Lib

set CL_DIR="C:\Program Files (x86)\Microsoft Visual Studio 10.0\VC\bin\amd64\"

set C_OPT=/fp:precise /favor:AMD64 /Ox /GA

set INCLUDE_JNI=/I "C:\Program Files\Java\jdk1.6.0_26\include" /I "C:\Program Files\Java\jdk1.6.0_26\include\win32"

rem x64
%CL_DIR%cl %C_OPT% %INCLUDE_JNI% /I "C:\AMD\acml4.4.0\win64\include" src\main\c\acml_wrapper.cpp "C:\AMD\acml4.4.0\win64\lib\libacml_dll.lib" /LD /Fotarget\bin\acml_wrapper.obj /Fetarget\bin\acml_wrapper.dll 

rem MP
%CL_DIR%cl %C_OPT% %INCLUDE_JNI% /I "C:\AMD\acml4.4.0\win64_mp\include" src\main\c\acml_wrapper.cpp "C:\AMD\acml4.4.0\win64_mp\lib\libacml_mp_dll.lib" /LD /Fotarget\bin\acml_wrapper_mp.obj /Fetarget\bin\acml_wrapper_mp.dll 


rem GPU
%CL_DIR%cl %C_OPT% %INCLUDE_JNI% /I "C:\AMD\acmlgpu1.1.2\win64\include" src\main\c\acml_wrapper.cpp "C:\AMD\acmlgpu1.1.2\win64\lib\libacml_dll.lib" /LD /Fotarget\bin\acml_wrapper_gpu.obj /Fetarget\bin\acml_wrapper_gpu.dll 

rem x86
rem cl %C_OPT% %INCLUDE_JNI% /I "C:\AMD\acml4.4.0\ifort32_mp\include" src\main\c\acml_wrapper.cpp "C:\AMD\acml4.4.0\ifort32_mp\lib\libacml_mp_dll.lib" /LD /Fotarget\bin\acml_wrapper32_mp.obj /Fetarget\bin\acml_wrapper32_mp.dll 