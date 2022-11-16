FROM node:10.13.0

RUN mkdir -p /usr/src/app
WORKDIR /usr/src/app

COPY package.json /usr/src/app/
RUN npm i --legacy-peer-deps --cache ../.npm --prefer-offline --omit=dev

COPY . /usr/src/app
RUN cd /usr/src/app

EXPOSE 4000
CMD ["node", "app.js"]