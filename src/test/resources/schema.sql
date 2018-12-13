CREATE TABLE reservations (
  id varchar(36) NOT NULL,
  guest_name varchar(256) DEFAULT NULL,
  guest_mail varchar(256) DEFAULT NULL,
  arrival_date date DEFAULT NULL,
  departure_date date DEFAULT NULL,
  PRIMARY KEY (id)
);

CREATE INDEX arrival ON reservations(arrival_date);
CREATE INDEX departure ON reservations(departure_date);