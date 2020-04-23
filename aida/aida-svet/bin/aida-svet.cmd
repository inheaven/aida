powershell -command "& {$PSDefaultParameterValues['Out-File:Encoding'] = 'utf8'}"; "& {java -cp '../target/lib/*;../target/*' ru.inheaven.aida.svet.Main | tee -Append aida-svet.log}"
