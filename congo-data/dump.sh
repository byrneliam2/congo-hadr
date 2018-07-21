#!/bin/bash

need postgresql
pg_dump startup-hadr > startup-hadr.sql
