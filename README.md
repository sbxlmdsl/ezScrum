![logo](https://raw.githubusercontent.com/ezScrum/ezScrum/master/WebContent/images/readme/ezscrum_log_big.png)ezScrum
=======

ezScrum is a process management tool for Scrum.

- Easy to use
- Easy to manage
- Web based
- Open source


Version
------------
1.8.1


License
------------
GPL V2


Upgrade ezScrum
------------
In order to upgrade ezScrum and save all old project data for you, we need your assistance to fill out a questionnaire.
[https://ezscrum.typeform.com/to/iPXgBP
](https://ezscrum.typeform.com/to/iPXgBP "https://ezscrum.typeform.com/to/iPXgBP")

Snapshot
------------
![Snapshot](https://raw.githubusercontent.com/ezScrum/ezScrum/master/WebContent/images/readme/snapshot.png)


How to start
------------
1. Install JAVA 1.7
2. Install MySQL server
3. Download the newest ezScrum from <a href="https://sourceforge.net/projects/ezscrum/">HERE</a>.
4. Set database configurations in ```ezScrum.ini```.

    ```
    ServerUrl = <<DB_IP_ADDRESS>>
    Account = <<DB_ACCOUNT>>
    Password = <<DB_PASSWORD>>
    DatabaseName = <<DB_NAME>>
    ```

5. Set IP address in ```JettyServer.xml```. Replace ``localhost`` to your IP address.

    ```
    <Set name="host">
        <SystemProperty name="jetty.host" default="localhost"/>
    </Set>
    ```

    to

    ```
    <Set name="host">
        <SystemProperty name="jetty.host" default="<<IP_ADDRESS>>"/>
    </Set>
    ```
6. Setting Firebase project in WebContent/firebase.json 

	 ```
    "apiKey": <Your_Api_Key>,
	"authDomain":  <Your_Auth_Domain>,
	"databaseURL":  <Your_Database_URL>,
	"projectId":  <Your_Project_Id>,
	"storageBucket":  <Your_Storage_Bucket>,
	"messagingSenderId":  <Your_Messaging_SenderId>
    ```


	####OS: Windows
    (1) Set User Account Control (UAC) to lowest level.

    (2) Double click ```InstallApp-NT.bat``` to install service.

    (3) Double click ```ServiceStart.bat``` to start ezScrum service.

    ####OS: Ubuntu

	(1) Change directory to ezScrum_Ubuntu : 
	
	    ``` bash
	    $ cd ezScrum_Ubuntu/
	    ```
	
	(2) Install dos2unix : 

		```bash
		$ sudo apt-get install tofrodos
		```
	
		```bash
		$ sudo ln -s /usr/bin/fromdos /usr/bin/dos2unix
		```
	(3) Change ./setup.sh mod to execute : 
		```bash 
	    $ chmod 755 ./setup.sh
	    ```
	(4) Use dos2unix command only once.

	(5) Set up
	
		```bash
		$ sudo dos2unix ./setup.sh
		```
		
		```bash
		$ sudo ./setup.sh
		```
    #####(6) start

	  ```bash
	   $ sudo ./launch
	   ```
	#####(7)  Stop ezScrum service

	   ```bash
	   $ sudo ./shutDown.sh```
	   
	   Use ```echo '<<YOUR PASSWORD>>' | sudo -S  losf -i tcp:<<YOUR PORT NUMBER>> -s tcp:listen``` to check whether ezScrum already shutdown.
       
       If not shutdown, use ```echo '<<YOUR PASSWORD>>' | sudo -S  losf -i tcp:<<YOUR PORT NUMBER>> -s tcp:listen``` to get pid, then use ```sudo kill -9 <<YOUR　PID>>``` to shutdown.
	
	#####(8) remove ezScrum

	   ```bash
	   $ sudo ./remove.sh
	   ```
6. Open the browser and go to ``http://127.0.0.1:8080/ezScrum`` or ``http://<<IP_ADDRESS>>:8080/ezScrum``

Readme
----------
You can also check out online version <a href="https://github.com/ezScrum/ezScrum/blob/master/README.md">HERE</a>.
