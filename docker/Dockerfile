FROM ubuntu:16.04

MAINTAINER Sebastian Szwaczyk <sebastian.szwaczyk@wat.edu.pl>

RUN apt-get update && apt-get -y install apt-utils net-tools iputils-ping
RUN DEBIAN_FRONTEND=noninteractive apt install -q -y iproute2 xorg xterm wget tcptraceroute nano
RUN DEBIAN_FRONTEND=noninteractive apt-get install wireshark -y

RUN mkdir impl
WORKDIR /impl

#Mininet
RUN apt-get -y install mininet
ENV PATH=$PATH:/usr/share/openvswitch/scripts

#JAVA-install
RUN echo debconf shared/accepted-oracle-license-v1-1 select true | debconf-set-selections && \
    echo debconf shared/accepted-oracle-license-v1-1 seen true | debconf-set-selections && \
    apt-get install -y default-jdk
RUN apt-get install openjfx

#Files
RUN wget http://ipv4.download.thinkbroadband.com/1GB.zip && wget http://ipv4.download.thinkbroadband.com/512MB.zip && \
    wget http://ipv4.download.thinkbroadband.com/200MB.zip && wget http://ipv4.download.thinkbroadband.com/100MB.zip && \
    wget http://ipv4.download.thinkbroadband.com/50MB.zip && wget http://ipv4.download.thinkbroadband.com/20MB.zip && \
    wget http://ipv4.download.thinkbroadband.com/10MB.zip && wget http://ipv4.download.thinkbroadband.com/5MB.zip

#Floodlight
RUN apt-get -y install git build-essential maven
RUN git clone https://github.com/sszwaczyk/floodlight.git
RUN mkdir /var/lib/floodlight
RUN chmod 777 /var/lib/floodlight
RUN cd floodlight && mvn -DskipTests clean package

#Requests generator
RUN git clone https://github.com/sszwaczyk/requests-generator.git && cd requests-generator && mvn -DskipTests clean package

#Http server
RUN git clone https://github.com/sszwaczyk/http-server.git && cd http-server && mvn -DskipTests clean package

CMD ovs-ctl start && bash
