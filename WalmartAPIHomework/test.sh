##the the normal case
javac -cp json-simple.jar  Recommend.java 
java -cp .:json-simple.jar Recommend ipad
##the the empty case
javac -cp json-simple.jar  Recommend.java 
java -cp .:json-simple.jar Recommend 
##the the invalid case
javac -cp json-simple.jar  Recommend.java 
java -cp .:json-simple.jar Recommend 3344555
##the the invalid case
javac -cp json-simple.jar  Recommend.java 
java -cp .:json-simple.jar Recommend ipad iphone


