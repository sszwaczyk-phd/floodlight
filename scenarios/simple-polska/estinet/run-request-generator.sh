#!/bin/bash
java -jar /home/estinet/PhD/impl/requests-generator/target/requests-generator-1.0-SNAPSHOT.jar -sf /home/estinet/PhD/impl/floodlight/scenarios/simple-polska/estinet/services.json -lf user-$1 -st ./user-$1-exit.xlsx -er ./user-$1-every-request.xlsx -g uniform -ming 5 -maxg 10
