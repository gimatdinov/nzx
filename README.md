# NZX
HTTP proxy, based on LittleProxy (https://github.com/adamfisk/LittleProxy), configuration like nginx (https://nginx.org)

##Features
* Java (LittleProxy) based
* Configuration like nginx
* E-mail notification about errors
* Extensible postprocessing for request and response 
 * Dumping
 * Regex matching with e-mail notification
 * Fail HTTP response processing with e-mail notification
* Proxy pass HTTP-to-HTTPS (unusual rare need) 
* Built-in FTP server (to view dumps) 

##Build
``mvn package``

## Run
``java -jar target/nzx-0.12.jar -c src/test/config/nzx-test.conf``
