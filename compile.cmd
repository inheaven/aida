rem @ECHO off

set INCLUDE=I:\Program Files (x86)\Microsoft Visual Studio 10.0\VC\include
set LIB=I:\Program Files (x86)\Microsoft Visual Studio 10.0\VC\lib\amd64;I:\Program Files (x86)\Microsoft SDKs\Windows\v7.0A\Lib\x64
REM set LIB=I:\Program Files (x86)\Microsoft Visual Studio 10.0\VC\lib;I:\Program Files (x86)\Microsoft SDKs\Windows\v7.0A\Lib

set C_OPT=/fp:precise /favor:AMD64

set INCLUDE_JNI=/I "I:\Program Files\Java\jdk1.6.0_22\include" /I "I:\Program Files\Java\jdk1.6.0_22\include\win32"

rem x64
cl %C_OPT% %INCLUDE_JNI% /I "I:\AMD\acml4.4.0\win64\include" src\main\c\acml_wrapper.cpp "I:\AMD\acml4.4.0\win64\lib\libacml_dll.lib" /LD /Fotarget\bin\acml_wrapper.obj /Fetarget\bin\acml_wrapper.dll 

rem MP
cl %C_OPT% %INCLUDE_JNI% /I "I:\AMD\acml4.4.0\win64_mp\include" src\main\c\acml_wrapper.cpp "I:\AMD\acml4.4.0\win64_mp\lib\libacml_mp_dll.lib" /LD /Fotarget\bin\acml_wrapper_mp.obj /Fetarget\bin\acml_wrapper_mp.dll 


rem GPU
cl %C_OPT% %INCLUDE_JNI% /I "I:\AMD\acmlgpu1.1.2\ifort64\include" src\main\c\acml_wrapper.cpp "I:\AMD\acmlgpu1.1.2\ifort64\lib\libacml_dll.lib" /LD /Fotarget\bin\acml_wrapper_gpu.obj /Fetarget\bin\acml_wrapper_gpu.dll 

rem x86
rem cl %C_OPT% %INCLUDE_JNI% /I "I:\AMD\acml4.4.0\ifort32_mp\include" src\main\c\acml_wrapper.cpp "I:\AMD\acml4.4.0\ifort32_mp\lib\libacml_mp_dll.lib" /LD /Fotarget\bin\acml_wrapper32_mp.obj /Fetarget\bin\acml_wrapper32_mp.dll 