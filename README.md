# NZX
HTTP proxy, based on LittleProxy (https://github.com/adamfisk/LittleProxy), configuration like nginx (https://nginx.org)

##Features
* Java (LittleProxy) based
* Configuration like nginx 
* Request and response content postprocessing (for example dumping)
* Built-in FTP server (to view dumps)

## Run
``java -Dlogging.config=src/test/config/logback.xml -Dnzx_log=log -jar nzx-0.3.jar Test src/test/config/nzx_TEST.conf``
