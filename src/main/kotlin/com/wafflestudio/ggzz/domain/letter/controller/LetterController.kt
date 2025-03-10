package com.wafflestudio.ggzz.domain.letter.controller

import com.google.gson.Gson
import com.wafflestudio.ggzz.domain.letter.dto.LetterDto
import com.wafflestudio.ggzz.domain.letter.dto.LetterDto.CreateRequest
import com.wafflestudio.ggzz.domain.letter.dto.LetterDto.Response
import com.wafflestudio.ggzz.domain.letter.service.LetterService
import com.wafflestudio.ggzz.domain.user.model.CurrentUser
import com.wafflestudio.ggzz.global.common.dto.ListResponse
import io.swagger.v3.oas.annotations.Operation
import jakarta.validation.Valid
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import org.hibernate.validator.constraints.Range
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@Validated
@RestController
@RequestMapping("/api/v1/letters")
class LetterController(
    private val letterService: LetterService
) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    @Operation(summary = "편지 올리기")
    @PostMapping
    fun postLetter(@CurrentUser userId: Long, @RequestBody @Valid request: CreateRequest): ResponseEntity<Response> {
        logger.info("POST /api/v1/letters, {}", request)
        return ResponseEntity.ok(letterService.postLetter(userId, request))
    }

    @Operation(summary = "현재 위치 기준으로 편지 리스트 가져오기")
    @GetMapping
    fun getLetters(
        @RequestParam @NotNull @Range(min=-180, max=180) longitude: Double?,
        @RequestParam @NotNull @Range(min=-90, max=90) latitude: Double?,
        @RequestParam(required = false, defaultValue = "200") @Positive range: Int?,
    ): ResponseEntity<ListResponse<Response>> {
        logger.info("GET /api/v1/letters, longitude={}, latitude={}, range={}", longitude, latitude, range)
        return ResponseEntity.ok(letterService.getLetters(longitude!! to latitude!!, range!!))
    }

    @Operation(summary = "현재 위치 기준으로 편지 확인하기", description = "현재 위치 기준 30m 이내")
    @GetMapping("/{id}")
    fun getLetter(
        @PathVariable id: Long,
        @RequestParam @NotNull @Range(min=-180, max=180) longitude: Double?,
        @RequestParam @NotNull @Range(min=-90, max=90) latitude: Double?,
    ): ResponseEntity<LetterDto.DetailResponse> {
        logger.info("GET /api/v1/letters/$id, longitude={}, latitude={}", longitude, latitude)
        return ResponseEntity.ok(letterService.getLetter(id, longitude!! to latitude!!))
    }

    @Operation(summary = "편지 삭제하기")
    @DeleteMapping("/{id}")
    fun deleteLetter(
        @CurrentUser userId: Long,
        @PathVariable id: Long
    ): ResponseEntity<LetterDto.DetailResponse> {
        logger.info("DELETE /api/v1/letters/$id")
        letterService.deleteLetter(userId, id)
        return ResponseEntity.ok().build()
    }

    @Operation(summary = "편지 이미지나 음성 올리기")
    @PutMapping("/{id}/source")
    fun putResource(
        @CurrentUser userId: Long,
        @PathVariable id: Long,
        @RequestPart image: MultipartFile?,
        @RequestPart voice: MultipartFile?,
    ): ResponseEntity<Any> {
        logger.info("PUT /api/v1/letters/$id/source," +
                " image-filename={}, image-type={}, voice-filename={}, voice-type={}",
             image?.originalFilename, image?.contentType, voice?.originalFilename, voice?.contentType)
        letterService.addSourceToLetter(userId, id, image, voice)
        return ResponseEntity.ok(Gson().toJson(mapOf(
            "image" to (image != null),
            "voice" to (voice != null)
        )))
    }
}