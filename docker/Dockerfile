FROM ubuntu:18.04

MAINTAINER Sebastian Szwaczyk <sebastian.szwaczyk@wat.edu.pl>

RUN apt-get update && apt-get -y install apt-utils net-tools iputils-ping

RUN mkdir impl
WORKDIR /impl

#Mininet
RUN apt-get -y install mininet
ENV PATH=$PATH:/usr/share/openvswitch/scripts

#JAVA-install
RUN apt-get update && \
    apt-get upgrade -y && \
    apt-get install -y  software-properties-common && \
    add-apt-repository ppa:webupd8team/java -y && \
    apt-get update && \
    echo oracle-java8-installer shared/accepted-oracle-license-v1-1 select true | /usr/bin/debconf-set-selections && \
    apt-get install -y oracle-java8-installer && \
    apt-get clean

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

#Utils
RUN apt-get -y install iproute2 xorg xterm wget

#Files
RUN wget http://ipv4.download.thinkbroadband.com/1GB.zip && wget http://ipv4.download.thinkbroadband.com/512MB.zip && \
    wget http://ipv4.download.thinkbroadband.com/200MB.zip && wget http://ipv4.download.thinkbroadband.com/100MB.zip && \
    wget http://ipv4.download.thinkbroadband.com/50MB.zip && http://ipv4.download.thinkbroadband.com/20MB.zip

CMD ovs-ctl start && bash
