# NZX
HTTP proxy, based on the LittleProxy (https://github.com/adamfisk/LittleProxy), configuration like the nginx (https://nginx.org)

## Run
java -Dlogging.config=src/test/config/logback.xml -Dnzx_log=log -jar nzx-0.3.jar Test src/test/config/nzx.conf
