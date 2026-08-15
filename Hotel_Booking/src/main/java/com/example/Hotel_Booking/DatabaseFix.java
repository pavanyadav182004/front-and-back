package com.example.Hotel_Booking;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DatabaseFix implements CommandLineRunner {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) throws Exception {
        try {
            jdbcTemplate.execute("ALTER TABLE hotels MODIFY COLUMN address TEXT;");
            System.out.println("Successfully altered 'address' column to TEXT.");
        } catch (Exception e) {
            System.out.println("Could not alter 'address' column: " + e.getMessage());
        }
        
        try {
            jdbcTemplate.execute("ALTER TABLE hotels MODIFY COLUMN location TEXT;");
            System.out.println("Successfully altered 'location' column to TEXT.");
        } catch (Exception e) {
            System.out.println("Could not alter 'location' column: " + e.getMessage());
        }
        
        try {
            jdbcTemplate.execute("ALTER TABLE hotels MODIFY COLUMN name VARCHAR(500);");
            System.out.println("Successfully altered 'name' column to VARCHAR(500).");
        } catch (Exception e) {
            System.out.println("Could not alter 'name' column: " + e.getMessage());
        }
        
        try {
            jdbcTemplate.execute("ALTER TABLE hotel_images MODIFY COLUMN images LONGTEXT;");
            System.out.println("Successfully altered 'images' column in hotel_images to LONGTEXT.");
        } catch (Exception e) {
            System.out.println("Could not alter 'images' column in hotel_images: " + e.getMessage());
        }
        
        try {
            jdbcTemplate.execute("ALTER TABLE hotels_images MODIFY COLUMN images LONGTEXT;");
            System.out.println("Successfully altered 'images' column in hotels_images to LONGTEXT.");
        } catch (Exception e) {
            System.out.println("Could not alter 'images' column in hotels_images: " + e.getMessage());
        }
    }
}
