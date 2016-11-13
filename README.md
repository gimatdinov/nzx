# NZX
HTTP proxy, based on the LittleProxy (https://github.com/adamfisk/LittleProxy), configuration like the nginx (https://nginx.org)

##Features
* Java (LittleProxy) based
* Configuration like the nginx 
* Dumping request and response content
* Built-in FTP server (to view dupms)

## Run
``java -Dlogging.config=src/test/config/logback.xml -Dnzx_log=log -jar nzx-0.3.jar Test src/test/config/nzx_TEST.conf``
