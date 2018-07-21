#!/bin/bash

need postgresql
pg_dump congo-hadr > congo-hadr.sql
