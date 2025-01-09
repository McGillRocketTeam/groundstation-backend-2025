FROM maven:3.8.5-openjdk-17-slim AS builder
WORKDIR /yamcs

# https://github.com/labjack/ljm_docker
RUN apt-get update
RUN apt-get install -y wget

RUN wget https://files.labjack.com/installers/LJM/Linux/x64/minimal/beta/labjack_ljm_minimal_2020_03_30_x86_64_beta.tar.gz
RUN tar zxf ./labjack_ljm_minimal_2020_03_30_x86_64_beta.tar.gz
RUN ./labjack_ljm_minimal_2020_03_30_x86_64/labjack_ljm_installer.run -- --no-restart-device-rules

COPY pom.xml pom.xml
COPY src src

RUN mvn clean package -DSkipTests


FROM openjdk:17-jdk-slim AS runner
WORKDIR /yamcs

RUN apt-get update
RUN apt-get install -y libusb-1.0-0-dev udev

COPY --from=builder /usr/local/lib /usr/local/lib
COPY --from=builder /usr/local/include /usr/local/include
COPY --from=builder /usr/local/share /usr/local/share
COPY --from=builder /etc/ld.so.conf /etc/ld.so.conf
RUN ldconfig

COPY --from=builder /yamcs/target/*.tar.gz gs_backend.tar.gz
RUN tar -xvzf gs_backend.tar.gz && rm gs_backend.tar.gz && mv gs_backend-* gs_backend

# port for APIs to frontend
EXPOSE 8090
# include other ports we might be getting data on
EXPOSE 10015/udp

CMD ["gs_backend/bin/yamcsd"]

# TODO TEST IF COMPORTS ARE ACCESSIBLE BY CONTAINER!
# TODO FIGURE OUT WHERE LABJACK CSV FILE IS STORED AND MAKE A VOLUME FOR IT
#docker run --privileged --volume=yamcs-telemetry:/yamcs/gs_backend/yamcs-data --workdir=/yamcs -p 10015:10015/udp -p 8090:8090 -d gs_backend:latest