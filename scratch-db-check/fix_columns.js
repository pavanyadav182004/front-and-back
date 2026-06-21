const mysql = require('mysql2/promise');

async function fix() {
  const connection = await mysql.createConnection({
    host: 'autorack.proxy.rlwy.net',
    port: 33074,
    user: 'root',
    password: 'lbnHvqbmsCsCObGTXxMlYdrxgLcbfymV',
    database: 'railway'
  });

  try {
    // Drop foreign key
    await connection.execute('ALTER TABLE payment DROP FOREIGN KEY FKlouu98csyullos9k25tbpk4va;');
    
    // Drop columns
    await connection.execute('ALTER TABLE payment DROP COLUMN order_id;');
    await connection.execute('ALTER TABLE payment DROP COLUMN peyment_mode;');
    await connection.execute('ALTER TABLE payment DROP COLUMN peyment_status;');
    
    console.log('Successfully cleaned up payment table.');
  } catch (error) {
    console.error('Error:', error);
  } finally {
    await connection.end();
  }
}

fix();
