powershell -command "& {$PSDefaultParameterValues['Out-File:Encoding'] = 'utf8'}"; "& {java -cp 'lib/*;*' ru.aida.bittrex.Module | tee -Append ./aida-bittrex.log}"
