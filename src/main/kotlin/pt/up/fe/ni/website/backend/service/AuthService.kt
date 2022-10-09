package pt.up.fe.ni.website.backend.service

import java.time.Instant
import java.util.stream.Collectors
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.oauth2.jwt.JwtClaimsSet
import org.springframework.security.oauth2.jwt.JwtEncoder
import org.springframework.security.oauth2.jwt.JwtEncoderParameters
import org.springframework.stereotype.Service

@Service
class AuthService(val encoder: JwtEncoder) {
    fun generateToken(authentication: Authentication): String {
        val scope = authentication
            .authorities
            .stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.joining(" "))
        val claims = JwtClaimsSet
            .builder()
            .issuer("self")
            .issuedAt(Instant.now())
            .subject(authentication.name)
            .claim("scope", scope)
            .build()
        return encoder.encode(JwtEncoderParameters.from(claims)).tokenValue
    }
}
