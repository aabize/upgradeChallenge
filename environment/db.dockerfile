FROM mysql:5.7

ENV MYSQL_DATABASE=campsite

ADD schema.sql /docker-entrypoint-initdb.d

EXPOSE 3306
