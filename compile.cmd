rem @ECHO off

set INCLUDE=I:\Program Files (x86)\Microsoft Visual Studio 10.0\VC\include
set LIB=I:\Program Files (x86)\Microsoft Visual Studio 10.0\VC\lib\amd64;I:\Program Files (x86)\Microsoft SDKs\Windows\v7.0A\Lib\x64

set C_OPT=/W3 /DIS_64BIT /DWIN64 /DWINDOWS /nologo /DUSE_OMP /O2 /MD /fp:precise /Ox /favor:AMD64

set INCLUDE_JNI=/I "I:\Program Files\Java\jdk1.6.0_22\include" /I "I:\Program Files\Java\jdk1.6.0_22\include\win32"

rem MP
 cl %C_OPT% %INCLUDE_JNI% /I "I:\AMD\acml4.4.0\win64_mp\include" src\main\c\acml_wrapper.cpp "I:\AMD\acml4.4.0\win64_mp\lib\libacml_mp_dll.lib" /LD /Fotarget\bin\acml_wrapper_mp.obj /Fetarget\bin\acml_wrapper_mp.dll 

rem GPU
 cl %C_OPT% %INCLUDE_JNI% /I "I:\AMD\acmlgpu1.1.1\win64\include" src\main\c\acml_wrapper.cpp "I:\AMD\acmlgpu1.1.1\win64\lib\libacml_dll.lib" /LD /Fotarget\bin\acml_wrapper_gpu.obj /Fetarget\bin\acml_wrapper_gpu.dll 
