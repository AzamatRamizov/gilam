package com.example.gilam888.Controller;

import com.example.gilam888.Configurations.ApiResponse;
import com.example.gilam888.Dto.KatmSorovDto;
import com.example.gilam888.Dto.KatmSorovKirimDto;
import com.example.gilam888.Repository.UsersRepository;
import com.example.gilam888.Service.KatmSorovService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * KATM so'rovlari — ombor (baza) dasturi bilan aloqa.
 *
 * /api/katm/sorov  — ombordan so'rov qabul qilish (login talab qilinmaydi,
 *                    X-Katm-Token sarlavhasi bilan himoyalangan)
 * /admin/katm-*    — operator uchun oyna va amallar
 */
@Controller
public class KatmController {

    private final KatmSorovService katmSorovService;
    private final UsersRepository usersRepository;

    @Value("${katm.token:}")
    private String token;

    public KatmController(KatmSorovService katmSorovService, UsersRepository usersRepository) {
        this.katmSorovService = katmSorovService;
        this.usersRepository = usersRepository;
    }

    // ================= OMBORDAN QABUL QILISH =================

    @PostMapping("/api/katm/sorov")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> sorovQabul(
            @RequestHeader(value = "X-Katm-Token", required = false) String kelganToken,
            @RequestBody KatmSorovKirimDto dto) {

        Map<String, Object> javob = new HashMap<>();

        if (token == null || token.isBlank() || !token.equals(kelganToken)) {
            javob.put("message", "Token noto'g'ri");
            javob.put("holat", false);
            return ResponseEntity.status(401).body(javob);
        }
        if (dto == null || dto.sotuvId() == null) {
            javob.put("message", "sotuvId yo'q");
            javob.put("holat", false);
            return ResponseEntity.badRequest().body(javob);
        }

        long id = katmSorovService.qabulQil(dto);
        javob.put("message", "So'rov qabul qilindi");
        javob.put("holat", true);
        javob.put("sorovId", id);
        return ResponseEntity.ok(javob);
    }

    // ================= OPERATOR OYNASI =================

    @GetMapping("/admin/katm-sorovlar")
    public String sahifa() {
        return "katm-sorovlar";
    }

    @GetMapping("/admin/get-katm-sorovlar")
    @ResponseBody
    public List<KatmSorovDto> royxat(@RequestParam(required = false) String holat) {
        return katmSorovService.royxat(holat);
    }

    @GetMapping("/admin/get-katm-sorov/{id}")
    @ResponseBody
    public ResponseEntity<KatmSorovDto> bitta(@PathVariable Long id) {
        KatmSorovDto dto = katmSorovService.bitta(id);
        return dto == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(dto);
    }

    /** Bir nechta so'rov (bitta shartnomaga birlashtirish uchun): ?ids=1,2,3 */
    @GetMapping("/admin/get-katm-sorovlar-tanlangan")
    @ResponseBody
    public List<KatmSorovDto> tanlanganlar(@RequestParam List<Long> ids) {
        return katmSorovService.bittalar(ids);
    }

    /** Yangi (javob kutayotgan) so'rovlar soni — sidebar belgisi uchun */
    @GetMapping("/admin/katm-yangilar-soni")
    @ResponseBody
    public Map<String, Object> yangilar() {
        return Map.of("soni", katmSorovService.yangilarSoni());
    }

    /** Shartnoma tuzilgach chaqiriladi (add-page saqlagandan keyin) */
    @PostMapping("/admin/katm-sorov-bajarildi/{id}")
    @ResponseBody
    public ResponseEntity<ApiResponse> bajarildi(@PathVariable Long id,
                                                 @RequestParam(required = false) Long shartnomaId) {
        ApiResponse res = katmSorovService.bajarildi(id, shartnomaId, kim());
        return res.isHolat() ? ResponseEntity.ok(res) : ResponseEntity.badRequest().body(res);
    }

    @PostMapping("/admin/katm-sorov-rad/{id}")
    @ResponseBody
    public ResponseEntity<ApiResponse> rad(@PathVariable Long id,
                                           @RequestBody(required = false) Map<String, String> tana) {
        String izoh = tana == null ? null : tana.get("izoh");
        ApiResponse res = katmSorovService.radEtildi(id, izoh, kim());
        return res.isHolat() ? ResponseEntity.ok(res) : ResponseEntity.badRequest().body(res);
    }

    /** Omborga javob bormay qolgan bo'lsa — qayta yuborish */
    @PostMapping("/admin/katm-sorov-qayta/{id}")
    @ResponseBody
    public ResponseEntity<ApiResponse> qayta(@PathVariable Long id) {
        ApiResponse res = katmSorovService.qaytaXabar(id);
        return res.isHolat() ? ResponseEntity.ok(res) : ResponseEntity.badRequest().body(res);
    }

    // ================= YORDAMCHI =================

    private String kim() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || auth.getName() == null) return null;
            return usersRepository.findByUsername(auth.getName())
                    .map(u -> u.getFish() == null ? auth.getName() : u.getFish())
                    .orElse(auth.getName());
        } catch (Exception e) {
            return null;
        }
    }
}
