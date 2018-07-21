CREATE TABLE RESOURCES(
  Organisation VARCHAR(50) NOT NULL,
  Resource VARCHAR(50) NOT NULL,
  Quantity INT DEFAULT 0,
  Description VARCHAR(50),
  CONSTRAINT resources_pkey PRIMARY KEY(Organisation, Resource)
);