# PostgreSQL Database Setup with Flyway and Custom ID Generation

This directory contains Flyway migration scripts for setting up and managing the PostgreSQL database with custom ID generation for the core module.

## Setup Steps

1. **Install PostgreSQL** (if not already installed)
   ```bash
   # For Ubuntu
   sudo apt-get update
   sudo apt-get install postgresql postgresql-contrib

   # For macOS using Homebrew
   brew install postgresql
   ```

2. **Create the Database**
   ```bash
   # Login to PostgreSQL
   sudo -u postgres psql

   # Create the database
   CREATE DATABASE core;

   # Create a user (optional, you can use the default postgres user)
   CREATE USER your_username WITH ENCRYPTED PASSWORD 'your_password';

   # Grant privileges
   GRANT ALL PRIVILEGES ON DATABASE core TO your_username;

   # Exit PostgreSQL
   \q
   ```

3. **Update Application Configuration**

   Update the database connection details in `application.yml`:
   ```yaml
   spring:
     datasource:
       url: jdbc:postgresql://localhost:5432/core
       username: your_username  # Replace with your actual username
       password: your_password  # Replace with your actual password
   ```

4. **Run the Application**

   When you run the application, Flyway will automatically apply the migration scripts in this directory to set up the database schema.

## Migration Scripts

- `V1__init.sql`: Initial database schema with custom ID generation
  - Creates the 'core' schema first
  - Implements custom ID generation function in the 'core' schema
  - Creates tables (users, authorities, user_authorities) in the 'core' schema
  - Inserts default authority values

## Custom ID Generation

The implementation uses a custom ID generation approach with the following components:

1. **Custom ID Structure**:
   - 41 bits for time in milliseconds (gives 41 years with a custom epoch)
   - 13 bits for shard ID (currently using a fixed value of 1)
   - 10 bits for an auto-incrementing sequence (modulus 1024)
2. **Benefits**:
   - Time-sortable IDs: IDs are roughly sortable by creation time
   - Future scalability: The ID structure supports sharding if needed in the future
   - Unique IDs: The combination of time, shard ID, and sequence ensures uniqueness

## Adding New Migrations

To add a new migration script:

1. Create a new SQL file in this directory following the Flyway naming convention:
   ```
   V{version}__{description}.sql
   ```
   For example: `V2__add_user_preferences.sql`

2. Write your SQL statements in the file

3. Run the application, and Flyway will automatically apply the new migration

## Manual Migration

If you need to run migrations manually:

```bash
# Using Maven
mvn flyway:migrate -Dflyway.configFiles=src/main/resources/application.yml

# Using Flyway CLI
flyway -configFiles=src/main/resources/application.yml migrate
```

## Troubleshooting

- If you encounter errors during migration, check the Flyway schema history table:
  ```sql
  SELECT * FROM core.flyway_schema_history;
  ```

- To repair the Flyway schema history table after a failed migration:
  ```bash
  mvn flyway:repair -Dflyway.configFiles=src/main/resources/application.yml
  ```
