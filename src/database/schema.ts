import { config } from 'dotenv';
import { DataSource, DataSourceOptions } from 'typeorm';
import { User } from '@/users/entities/user.entity';
import { getDatabaseSynchronize } from './synchronize';

if (process.env.NODE_ENV !== 'test') {
  config({ path: '.env' });
}

export const createSchema = async () => {
  const options: DataSourceOptions = {
    type: 'postgres',
    host: process.env.DATABASE_MASTER,
    port: parseInt(process.env.DATABASE_PORT || '5432', 10),
    username: process.env.DATABASE_USER,
    password: process.env.DATABASE_PASSWORD,
    database: process.env.DATABASE_NAME,
    synchronize: getDatabaseSynchronize(),
    dropSchema: false,
    schema: 'public',
    entities: [User],
  };

  const dataSource = new DataSource(options);
  try {
    await dataSource.initialize();
    if (options.synchronize) {
      console.log('Database schema created successfully.');
    } else {
      console.log(
        'Database connection initialized; schema synchronization is disabled (set DATABASE_SYNCHRONIZE=true to enable it).',
      );
    }
    await dataSource.destroy();
  } catch (err) {
    console.error('Schema creation failed:', err);
    throw err;
  }
};

export const handleMain = (
  moduleRef: NodeJS.Module,
  mainModule: NodeJS.Module | undefined = require.main,
) => {
  if (mainModule === moduleRef) {
    createSchema().catch(() => {
      process.exit(1);
    });
  }
};

handleMain(module);
