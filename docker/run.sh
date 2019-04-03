#!/bin/bash
xhost +
docker run -tid --privileged --rm -e DISPLAY=$DISPLAY -v /tmp/.X11-unix:/tmp/.X11-unix --name=secure-routing sszwaczyk/secure-routing:1.0.0
./attach.sh