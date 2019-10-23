#!/bin/bash

# Launch gui

export CLASSPATH="$CLASSPATH:$PWD/bin"
mkdir -p log

java jade.Boot -gui &> log/gui &
sleep 1.5s

java jade.Boot -container Nuno:agents.Technician &> log/nuno &
sleep 1.5s

java jade.Boot -container Miguel:agents.Technician &> log/miguel &
sleep 1.5s

java jade.Boot -container Bruno:agents.Client &> log/bruno &

jobs -p

read

jobs -p | xargs kill
