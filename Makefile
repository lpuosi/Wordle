default:
	javac -cp ".:Server/lib/gson-2.10.1.jar" Server/*.java
	javac Client/*.java
clean: 
	rm Client/*.class
	rm Server/*.class	
server:
	java -cp ".:Server/lib/gson-2.10.1.jar" Server.ServerMain
client:
	java Client.ClientMain


