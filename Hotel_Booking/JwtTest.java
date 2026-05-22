import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.security.Key;

public class JwtTest {
    public static void main(String[] args) {
        Key key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
        
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", "ADMIN");
        
        String token = Jwts.builder()
                .setClaims(claims)
                .setSubject("admin@test.com")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60))
                .signWith(key)
                .compact();
                
        System.out.println("Token generated.");
        
        var extractedClaims = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
        System.out.println("Role: " + extractedClaims.get("role", String.class));
        System.out.println("Email: " + extractedClaims.getSubject());
    }
}
