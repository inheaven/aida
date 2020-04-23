rem powershell -command "& {$PSDefaultParameterValues['Out-File:Encoding'] = 'utf8'}"; "& {java -cp 'lib/*;*' ru.inheaven.aida.color.YoctoRandom | tee -Append aida-color.log}"
java -classpath 'lib/*' ru.inheaven.aida.color.YoctoRandom
