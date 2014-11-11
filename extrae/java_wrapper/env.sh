#!/bin/bash

export JAVA_HOME=/home/$USER/MS/aplic/jdk1.7.0_25

export EXTRAE_ON=1
export EXTRAE_DIR=/tmp
export EXTRAE_HOME=/home/$USER/MS/extrae/dist
export EXTRAE_LABELS=/home/$USER/MS/labels.txt


export LD_LIBRARY_PATH=/home/$USER/MS/extrae/dist/lib:/home/$USER/MS/extrae/java_wrapper/lib
export PATH=$JAVA_HOME/bin:$PATH
