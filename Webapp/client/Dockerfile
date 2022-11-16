FROM node:10.13.0 AS build_prod
WORKDIR /usr/src/app
COPY . .
RUN npm i --legacy-peer-deps --cache ../.npm --prefer-offline --omit=dev && \
    npm run build

FROM nginx:stable as run
COPY --from=build_prod /usr/src/app/build /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
