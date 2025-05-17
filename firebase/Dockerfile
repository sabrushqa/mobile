FROM node:18-slim

RUN apt-get update && apt-get install -y openjdk-17-jdk

# Installation Firebase CLI etc...

WORKDIR /home/firebase
COPY . .

RUN npm install

CMD ["firebase", "emulators:start"]
