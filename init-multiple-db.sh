#!/bin/bash

set -e
set -u

function create_user_and_database() {
    local database=$1
    local user=$1
    local password=$2
    echo "Creating user '$user' and database '$database'"
    psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "postgres" <<-EOSQL
            -- Create user if not exists
            DO \$\$
            BEGIN
                IF NOT EXISTS (SELECT FROM pg_catalog.pg_user WHERE usename = '$user') THEN
                    CREATE USER "$user" WITH PASSWORD '$password';
                ELSE
                    ALTER USER "$user" WITH PASSWORD '$password';
                END IF;

                -- Grant REPLICATION role to payment user only
                IF '$user' = 'payment' THEN
                    ALTER ROLE "$user" WITH REPLICATION SUPERUSER;
                END IF;
            END
            \$\$;

            -- Create database if not exists
            SELECT 'CREATE DATABASE "$database"'
            WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = '$database')\gexec

            -- Grant all privileges
            GRANT ALL PRIVILEGES ON DATABASE "$database" TO "$user";

            -- Connect to new database to grant schema privileges
            \c "$database";

            -- Grant privileges on all schemas (including public)
            GRANT ALL PRIVILEGES ON SCHEMA public TO "$user";
            GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO "$user";
            GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO "$user";

            -- Set default privileges for future objects
            ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL PRIVILEGES ON TABLES TO "$user";
            ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL PRIVILEGES ON SEQUENCES TO "$user";
EOSQL
}

if [ -n "${POSTGRES_DBS:-}" ]; then
    echo "Multiple database creation requested: $POSTGRES_DBS"
    for db_config in $(echo "$POSTGRES_DBS" | tr ',' ' '); do
        IFS=':' read -r db password <<< "$db_config"
        create_user_and_database "$db" "$password"
    done
    echo "Multiple databases created"
fi