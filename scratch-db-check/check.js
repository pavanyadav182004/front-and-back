const mysql = require('mysql2/promise');

async function check() {
  const connection = await mysql.createConnection({
    host: 'autorack.proxy.rlwy.net',
    port: 33074,
    user: 'root',
    password: 'lbnHvqbmsCsCObGTXxMlYdrxgLcbfymV',
    database: 'railway'
  });

  try {
    const [rows] = await connection.execute('SHOW COLUMNS FROM payment;');
    console.log('Columns in payment table:', rows);
    
  } catch (error) {
    console.error('Error:', error);
  } finally {
    await connection.end();
  }
}

check();
