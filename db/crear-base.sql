-- Crear usuario y base para desarrollo en el PostgreSQL local.
-- Ejecutar como superusuario:  psql -U postgres -f db/crear-base.sql
CREATE USER ilmagro WITH PASSWORD 'ilmagro';
CREATE DATABASE ilmagro OWNER ilmagro;
