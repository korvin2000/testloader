# lol-loader
Der eigentliche loader.

Die Application.properties Dateien für die anderen Umgebungen ebenfalls anpassen.
````properties
### SFTP only params
lol.ftp.strictHostKeyChecking=no
lol.ftp.knownHosts=
lol.ftp.useSftp=false
# identityFullPath zeigt auf ein IDA Security Datei
lol.ftp.identityFullPath=
#### optional values, but required as property (leave just blank if you are not in the mood)
lol.ftp.serverHostKey=
lol.ftp.kex=
lol.ftp.cipherS2C=
lol.ftp.cipherC2S=
lol.ftp.macS2C=
lol.ftp.macC2S=
````

