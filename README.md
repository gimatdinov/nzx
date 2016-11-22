# NZX
HTTP proxy, based on LittleProxy (https://github.com/adamfisk/LittleProxy), configuration like nginx (https://nginx.org)

##Features
* Java (LittleProxy) based
* Configuration like nginx
* E-mail notification about errors
* Extensible postprocessing for request and response 
 * Dumping
 * Regex matching with e-mail notification
* Built-in FTP server (to view dumps) 

##Build
``mvn package``

## Run
``java -Dlogging.config=src/test/config/logback.xml -Dnzx_log=log -jar target/nzx-0.8.1.jar -n Test -c src/test/config/nzx_TEST.conf``
