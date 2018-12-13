CREATE USER 'campsite'@'%' IDENTIFIED BY 'campsite';
GRANT ALL ON campsite.* TO 'campsite'@'%' IDENTIFIED BY 'campsite';
GRANT ALL ON campsite.* TO 'campsite'@'localhost' IDENTIFIED BY 'campsite';
FLUSH PRIVILEGES;

CREATE TABLE reservations (
  id varchar(36) NOT NULL,
  guest_name varchar(256) DEFAULT NULL,
  guest_mail varchar(256) DEFAULT NULL,
  arrival_date date DEFAULT NULL,
  departure_date date DEFAULT NULL,
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE INDEX arrival ON reservations(arrival_date);
CREATE INDEX departure ON reservations(departure_date);