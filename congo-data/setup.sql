CREATE TABLE RESOURCES(
  Organisation VARCHAR(50) NOT NULL,
  Resource VARCHAR(50) NOT NULL,
  Quantity INT DEFAULT 0,
  Description VARCHAR(50),
  CONSTRAINT resources_pkey PRIMARY KEY(Organisation, Resource)
);

CREATE TABLE LOCATIONS(
  Organisation VARCHAR(50) NOT NULL,
  Resource VARCHAR(50) NOT NULL,
  Quantity INT DEFAULT 0,
  Location VARCHAR(50) NOT NULL,
  CONSTRAINT locations_pkey PRIMARY KEY(Organisation, Resource),
  CONSTRAINT locations_fkey FOREIGN KEY(Organisation, Resource) REFERENCES RESOURCES
);
