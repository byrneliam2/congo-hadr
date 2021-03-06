--
-- PostgreSQL database dump
--

SET statement_timeout = 0;
SET lock_timeout = 0;
SET client_encoding = 'SQL_ASCII';
SET standard_conforming_strings = on;
SET check_function_bodies = false;
SET client_min_messages = warning;

--
-- Name: plpgsql; Type: EXTENSION; Schema: -; Owner: 
--

CREATE EXTENSION IF NOT EXISTS plpgsql WITH SCHEMA pg_catalog;


--
-- Name: EXTENSION plpgsql; Type: COMMENT; Schema: -; Owner: 
--

COMMENT ON EXTENSION plpgsql IS 'PL/pgSQL procedural language';


SET search_path = public, pg_catalog;

--
-- Name: plpgsql_call_handler(); Type: FUNCTION; Schema: public; Owner: pgsql
--

CREATE FUNCTION plpgsql_call_handler() RETURNS language_handler
    LANGUAGE c
    AS '$libdir/plpgsql', 'plpgsql_call_handler';


ALTER FUNCTION public.plpgsql_call_handler() OWNER TO pgsql;

SET default_tablespace = '';

SET default_with_oids = false;

--
-- Name: locations; Type: TABLE; Schema: public; Owner: byrneliam2; Tablespace: 
--

CREATE TABLE locations (
    organisation character varying(50) NOT NULL,
    resource character varying(50) NOT NULL,
    quantity integer DEFAULT 0,
    location character varying(50) NOT NULL
);


ALTER TABLE locations OWNER TO byrneliam2;

--
-- Name: resources; Type: TABLE; Schema: public; Owner: byrneliam2; Tablespace: 
--

CREATE TABLE resources (
    organisation character varying(50) NOT NULL,
    resource character varying(50) NOT NULL,
    quantity integer DEFAULT 0,
    description character varying(50)
);


ALTER TABLE resources OWNER TO byrneliam2;

--
-- Data for Name: locations; Type: TABLE DATA; Schema: public; Owner: byrneliam2
--

COPY locations (organisation, resource, quantity, location) FROM stdin;
Red Cross	water	200	Tenmaru
OxFam	water	350	Norsup
Amnesty	tarpaulin	500	Malekula
Amnesty	taro	500	Tenmaru
Red Cross	tarpaulin	700	Melip
UN	yams	1000	Bunlap
Groupx	hygiene kits	500	Wuro
Groupx	food cans	1000	Lamap
Groupx	sleeping mats	500	Leume
Groupt	med kits	100	Lamap
Groupt	water	800	Fanla
UN	tarpaulin	500	Fanla
Amnesty	kitchen sets	100	Malekula
Red Cross	med kits	100	Bunlap
Amnesty	shelter tools	50	Ambrym
OxFam	med kits	80	Wuro
Groupx	shelter tools	60	Leume
Amnesty	food cans	500	Melip
OxFam	tarpaulin	50	Tenmaru
Red Cross	rice	500	Wuro
Groupx	rice	400	Norsup
OxFam	rice	700	Melip
YY	yams	500	Ambrym
XXX	tarpaulin	700	Norsup
Red Cross	solar power kits	300	Tenmaru
Groupt	hygiene kits	700	Fanla
XXX	food cans	1000	Wuro
Global	rice	2000	Wuro
Global	soap	3000	Ambrym
Global	hygiene kits	1000	Bunlap
Global	tarpaulin	500	Malekula
Global	med kits	80	Norsup
Blue Cross	rice	5000	Leume
Groupx	blankets	700	Lamap
Red Cross	taro	800	Fanla
XXX	taro	500	Bunlap
Global	yams	1000	Ambrym
Groupt	solar power kits	600	Bunlap
UN	plastic sheets	100	Fanla
Red Cross	radio sets	500	Malekula
OxFam	radio sets	200	Fanla
UNICEF	buckets	500	Wuro
UNICEF	rice	300	Wuro
UNICEF	radio sets	250	Fanla
Westpac	solar power kits	300	Wuro
\.


--
-- Data for Name: resources; Type: TABLE DATA; Schema: public; Owner: byrneliam2
--

COPY resources (organisation, resource, quantity, description) FROM stdin;
Red Cross	water	200	drinking water
OxFam	water	350	wash
Amnesty	tarpaulin	500	shelter
Amnesty	taro	500	food
Red Cross	tarpaulin	700	shelter
UN	yams	1000	food
Groupx	hygiene kits	500	wash
Groupx	food cans	1000	food
Groupx	sleeping mats	500	shelter
Groupt	med kits	100	medicinal
Groupt	water	800	wash
UN	tarpaulin	500	shelter
Amnesty	kitchen sets	100	food
Red Cross	med kits	100	medicinal
Amnesty	shelter tools	50	shelter
OxFam	med kits	80	medicinal
Groupx	shelter tools	60	shelter
Amnesty	food cans	500	food
OxFam	tarpaulin	50	shelter
Red Cross	rice	500	shelter
Groupx	rice	400	shelter
OxFam	rice	700	shelter
YY	yams	500	food
XXX	tarpaulin	700	shelter
Red Cross	solar power kits	300	power
Groupt	hygiene kits	700	shelter
XXX	food cans	1000	food
Global	rice	2000	food
Global	soap	3000	wash
Global	hygiene kits	1000	wash
Global	tarpaulin	500	shelter
Global	med kits	80	medicinal
Blue Cross	rice	5000	shelter
Groupx	blankets	700	shelter
Red Cross	taro	800	food
XXX	taro	500	food
Global	yams	1000	food
Groupt	solar power kits	600	power
UN	plastic sheets	100	shelter
Red Cross	radio sets	500	comm
OxFam	radio sets	200	comm
UNICEF	buckets	500	wash
UNICEF	rice	300	food
UNICEF	radio sets	250	comm
Westpac	solar power kits	300	power
\.


--
-- Name: locations_pkey; Type: CONSTRAINT; Schema: public; Owner: byrneliam2; Tablespace: 
--

ALTER TABLE ONLY locations
    ADD CONSTRAINT locations_pkey PRIMARY KEY (organisation, resource);


--
-- Name: resources_pkey; Type: CONSTRAINT; Schema: public; Owner: byrneliam2; Tablespace: 
--

ALTER TABLE ONLY resources
    ADD CONSTRAINT resources_pkey PRIMARY KEY (organisation, resource);


--
-- Name: locations_fkey; Type: FK CONSTRAINT; Schema: public; Owner: byrneliam2
--

ALTER TABLE ONLY locations
    ADD CONSTRAINT locations_fkey FOREIGN KEY (organisation, resource) REFERENCES resources(organisation, resource);


--
-- Name: SCHEMA public; Type: ACL; Schema: -; Owner: pgsql
--

REVOKE ALL ON SCHEMA public FROM PUBLIC;
REVOKE ALL ON SCHEMA public FROM pgsql;
GRANT ALL ON SCHEMA public TO pgsql;
GRANT ALL ON SCHEMA public TO PUBLIC;


--
-- PostgreSQL database dump complete
--

