package com.wafflestudio.nostalgia.domain.user.service

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("auth.jwt")
data class AuthProperties (
    val issuer: String,
    val jwtSecret: String,
    val jwtExpiration: Long,
    val refreshExpiration: Long,
)