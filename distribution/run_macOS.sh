#!/bin/bash

java -version
java -jar -XstartOnFirstThread -Xms1024M bin/BouncyRoadMania.jar

read -n1 "Press any key to continue..."
