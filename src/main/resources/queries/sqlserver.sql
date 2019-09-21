-- Find all Databases in this SQL Server
SELECT *
FROM   sys.databases
WHERE  name NOT IN ('master', 'tempdb', 'model', 'msdb');

-- Find all schemas in a DB
SELECT distinct name
FROM   AdventureWorks.sys.schemas
WHERE  name NOT IN ('db_owner',
					'db_accessadmin',
					'db_securityadmin',
					'db_ddladmin',
					'db_backupoperator',
					'db_datareader',
					'db_datawriter',
					'db_denydatareader',
					'db_denydatawriter',
					'information_schema',
					'sys')
ORDER BY 1;					

-- Find all Tables in Database.
SELECT *
FROM   AdventureWorks.INFORMATION_SCHEMA.TABLES
ORDER BY TABLE_CATALOG, TABLE_SCHEMA, TABLE_TYPE, TABLE_NAME;