powershell -command "& {$PSDefaultParameterValues['Out-File:Encoding'] = 'utf8'}"; "& {java -cp 'lib/*;*' ru.aida.color.Yocto | tee -Append aida-color.log}"
