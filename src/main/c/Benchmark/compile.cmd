rem @ECHO off

SET INCLUDE=I:\Program Files (x86)\Microsoft Visual Studio 10.0\VC\include
SET LIB=I:\Program Files (x86)\Microsoft Visual Studio 10.0\VC\lib\amd64;I:\Program Files (x86)\Microsoft SDKs\Windows\v7.0A\Lib\x64;I:\Program Files\Intel\MKL\10.2.2.025\em64t\lib
SET OMP_NUM_THREADS = 3

SET C_OPT=/W3 /DIS_64BIT /DWIN64 /DWINDOWS /nologo /DUSE_OMP /O2 /MD /fp:fast /Ox /favor:AMD64

REM 1 CPU
 cl  %C_OPT% /I "I:\AMD\acml4.4.0\win64\include"  /c sgemm_c_example.c /Fosgemm_c_example1.obj
 cl  %C_OPT% sgemm_c_example1.obj "I:\AMD\acml4.4.0\win64\lib\libacml_dll.lib"  /Fesgemm_c_example1.exe

REM 3 CPU
 cl %_OPT% /I "I:\AMD\acml4.4.0\win64_mp\include" /c sgemm_c_example.c /Fosgemm_c_example3.obj
 cl %C_OPT%  sgemm_c_example3.obj "I:\AMD\acml4.4.0\win64_mp\lib\libacml_mp_dll.lib"  /Fesgemm_c_example3.exe

REM 1 CPU Double
 cl  %C_OPT% /I "I:\AMD\acml4.4.0\win64\include"  /c dgemm_c_example.c /Fodgemm_c_example1.obj
 cl  %C_OPT% dgemm_c_example1.obj "I:\AMD\acml4.4.0\win64\lib\libacml_dll.lib"  /Fedgemm_c_example1.exe

REM 3 CPU Double
 cl %_OPT% /I "I:\AMD\acml4.4.0\win64_mp\include" /c dgemm_c_example.c /Fodgemm_c_example3.obj
 cl %C_OPT%  dgemm_c_example3.obj "I:\AMD\acml4.4.0\win64_mp\lib\libacml_mp_dll.lib"  /Fedgemm_c_example3.exe


REM GPU
rem cl %C_OPT% /I "I:\AMD\acmlgpu1.1.1\win64\include" /c sgemm_c_example.c /Fosgemm_c_exampleGPU.obj
rem cl %C_OPT%  sgemm_c_exampleGPU.obj "I:\AMD\acmlgpu1.1.1\win64\lib\libacml_dll.lib"  /Fesgemm_c_exampleGPU.exe

REM MKL
rem cl %C_OPT% /I "I:\Program Files\Intel\MKL\10.2.2.025\include" /c sgemm_c_exampleMKL.c /Fosgemm_c_exampleMKL.obj
rem cl %C_OPT%  sgemm_c_exampleMKL.obj "I:\Program Files\Intel\MKL\10.2.2.025\em64t\lib\mkl_blacs_ilp64_dll.lib"  /Fesgemm_c_exampleMKL.exe