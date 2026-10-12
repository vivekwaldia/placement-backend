package com.college.placement_backend.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;
import java.security.Key;
import java.util.Date;
import java.util.function.Function;

// This class generates tokens, reads them and rejects fake/expired ones
@Component
public class JwtUtil{
    // Secure 256-bit hexadecimal key
    private static final String SECRET = "5367566B59703373367639792F423F4528482B4D6251655468576D5A71347437";

    // The key expires after 10 hours (calculation in ms)
    private static final long EXPIRATION_TIME = 1000 * 60 * 60 * 10;

    // Translates SECRET to cryptographic format for the JWT library to do math with
    private Key getSigningKey(){
        return Keys.hmacShaKeyFor(SECRET.getBytes());
    }

    // Generate the token when the user successfully logs in
    public String generateToken(String username){
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // Pull the username out of the token
    public String extractUsername(String token){
        return extractClaim(token, claims -> claims.getSubject());
    }

    // Pull the expiration date out of the token
    public Date extractExpiration(String token){
        return extractClaim(token, claims -> claims.getExpiration());
    }

    // Open the token, verify the signature using SECRET, extract the internal data (claims)
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver){
        final Claims claims = Jwts.parserBuilder()
                                .setSigningKey(getSigningKey())
                                .build()
                                .parseClaimsJws(token)
                                .getBody();
        // Returns the specific piece of data the user asked for
        return claimsResolver.apply(claims);
    }

    // Check current server time and token expiration time
    private Boolean isTokenExpired(String token){
        return extractExpiration(token).before(new Date());
    }

    // Final step before allowing the user to access the API endpoint:-
    // Does the username in the token match the username making request?
    // Is the token still unexpired?
    public Boolean validateToken(String token, String username){
        final String extractedUsername = extractUsername(token);
        return extractedUsername.equals(username) && !isTokenExpired(token);
    }
}