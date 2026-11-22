-- Crea el usuario y la base de Il Magro en PostgreSQL.
--
-- La contraseña NO esta en este archivo: la elegis vos y se la pasas al ejecutarlo.
-- Desde la carpeta del proyecto, en PowerShell (te va a pedir la contraseña del usuario postgres):
--
--   & "C:\Program Files\PostgreSQL\18\bin\psql.exe" -U postgres -v clave='TU_CONTRASEÑA' -f db/crear-base.sql
--
-- Las TABLAS no se crean aca: las crea el backend solo (Flyway, src/main/resources/db/migration) la
-- primera vez que arranca contra esta base, y despues le aplica cada cambio nuevo.

\set ON_ERROR_STOP on

\if :{?clave}
\else
  \echo 'Falta la contraseña. Ejecutalo con: -v clave=''TU_CONTRASEÑA'''
  \quit
\endif

-- usuario propio de la app: no es superusuario, solo es dueño de su base
CREATE USER ilmagro WITH PASSWORD :'clave' NOSUPERUSER NOCREATEDB NOCREATEROLE;
CREATE DATABASE ilmagro OWNER ilmagro ENCODING 'UTF8';

-- nadie mas que ilmagro (y el superusuario) puede entrar a la base ni crear cosas en ella
REVOKE ALL ON DATABASE ilmagro FROM PUBLIC;
\connect ilmagro
REVOKE ALL ON SCHEMA public FROM PUBLIC;
ALTER SCHEMA public OWNER TO ilmagro;

\echo 'Listo: base ilmagro y usuario ilmagro creados. Ahora arranca el backend con DB_PASSWORD.'
