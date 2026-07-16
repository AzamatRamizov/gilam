package com.example.gilam888.Service;

import com.example.gilam888.Configurations.ApiResponse;
import com.example.gilam888.Dto.BugungiEslatmaDto;
import com.example.gilam888.Dto.QongiroqSaveDto;
import com.example.gilam888.Dto.QongiroqTarixiDto;
import com.example.gilam888.Entity.Jadval;
import com.example.gilam888.Entity.Mijoz;
import com.example.gilam888.Entity.QongiroqAudio;
import com.example.gilam888.Entity.QongiroqTarixi;
import com.example.gilam888.Entity.Shartnoma;
import com.example.gilam888.Repository.MijozRepository;
import com.example.gilam888.Repository.QongiroqAudioRepository;
import com.example.gilam888.Repository.QongiroqTarixiRepository;
import com.example.gilam888.Repository.ShartnomaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QongiroqService {
    private final QongiroqTarixiRepository qongiroqTarixiRepository;
    private final MijozRepository mijozRepository;
    private final QongiroqAudioRepository qongiroqAudioRepository;
    private final ShartnomaRepository shartnomaRepository;
    private final AmalService amalService;

    public ApiResponse addQongiroq(QongiroqSaveDto dto, MultipartFile audio, String operator) {
        if (dto.getMijozId() == null) {
            return new ApiResponse("mijozId majburiy", false);
        }
        if (dto.getNatija() == null || dto.getNatija().isBlank()) {
            return new ApiResponse("Natija tanlanmagan", false);
        }

        Mijoz mijoz = mijozRepository.findById(dto.getMijozId()).orElse(null);
        if (mijoz == null) {
            return new ApiResponse("Mijoz topilmadi", false);
        }

        QongiroqTarixi entity = new QongiroqTarixi();
        entity.setMijoz(mijoz);
        entity.setNatija(dto.getNatija());
        entity.setIzoh(dto.getIzoh());
        entity.setVadaSana(dto.getVadaSana());
        entity.setSana(LocalDateTime.now());
        entity.setOperator(operator);
        entity.setAudioBormi(audio != null && !audio.isEmpty());

        qongiroqTarixiRepository.save(entity); // avval saqlanadi -> id generatsiya bo'ladi

        if (audio != null && !audio.isEmpty()) {
            try {
                QongiroqAudio audioEntity = new QongiroqAudio();
                audioEntity.setId(entity.getId()); // shared PK
                audioEntity.setBayt(audio.getBytes());
                audioEntity.setContentType(audio.getContentType() != null ? audio.getContentType() : "audio/webm");
                qongiroqAudioRepository.save(audioEntity);
            } catch (IOException e) {
                return new ApiResponse("Audio faylni o'qishda xatolik: " + e.getMessage(), false);
            }
        }

        amalService.log("QONGIROQ",
                "Qo'ng'iroq natijasi kiritildi: " + (mijoz.getFamiliya() != null ? mijoz.getFamiliya() + " " : "") + (mijoz.getIsm() != null ? mijoz.getIsm() : "")
                        + " — " + dto.getNatija()
                        + (dto.getVadaSana() != null ? " (va'da: " + dto.getVadaSana() + ")" : "")
                        + (dto.getIzoh() != null && !dto.getIzoh().isBlank() ? ". Izoh: " + dto.getIzoh() : ""),
                mijoz.getId(), null, null);
        return new ApiResponse("Qo'ng'iroq natijasi saqlandi", true);
    }

    public List<QongiroqTarixiDto> getTarixi(Long mijozId) {
        return qongiroqTarixiRepository.findByMijoz_IdOrderBySanaDesc(mijozId)
                .stream()
                .map(q -> {
                    QongiroqTarixiDto d = new QongiroqTarixiDto();
                    d.setId(q.getId());
                    d.setSana(q.getSana());
                    d.setNatija(q.getNatija());
                    d.setIzoh(q.getIzoh());
                    d.setOperator(q.getOperator());
                    d.setVadaSana(q.getVadaSana());
                    if (Boolean.TRUE.equals(q.getAudioBormi())) {
                        d.setAudioUrl("/admin/qongiroq-audio/" + q.getId());
                    }
                    return d;
                })
                .collect(Collectors.toList());
    }

    public QongiroqAudio getAudio(Long tarixiId) {
        return qongiroqAudioRepository.findById(tarixiId).orElse(null);
    }

    // ══════ BUGUNGI ESLATMALAR ══════
    // Mantiq: mijozning ENG OXIRGI qo'ng'irog'i "Va'da berdi" bo'lsa va va'da qilingan sana BUGUN bo'lsa —
    // shu mijoz eslatmalar ro'yxatiga tushadi.
    public List<BugungiEslatmaDto> getBugungiEslatmalar() {
        LocalDate bugun = LocalDate.now();

        List<QongiroqTarixi> nomzodlar = qongiroqTarixiRepository.findByVadaSanaAndNatija(bugun, "Va'da berdi");

        List<BugungiEslatmaDto> natija = new ArrayList<>();
        for (QongiroqTarixi q : nomzodlar) {
            Long mijozId = q.getMijoz().getId();

            // Agar shu mijozdan keyinroq (yangiroq) yana bir qo'ng'iroq bo'lgan bo'lsa,
            // demak bu eng oxirgi qo'ng'iroq emas — o'tkazib yuboriladi
            boolean keyinYanaQongiroqBormi = qongiroqTarixiRepository.existsByMijoz_IdAndSanaAfter(mijozId, q.getSana());
            if (keyinYanaQongiroqBormi) continue;

            Mijoz mijoz = q.getMijoz();

            List<Shartnoma> shartnomalar = shartnomaRepository.findByMijoz_Id(mijozId);
            Shartnoma shartnoma = shartnomalar.stream()
                    .filter(s -> !"yopilgan".equals(s.getStatus()))
                    .findFirst()
                    .orElse(null);

            // Agar mijozning FAOL (yopilmagan) shartnomasi qolmagan bo'lsa —
            // demak qarzi yo'q, eslatma kerak emas
            if (shartnoma == null) continue;

            long qolganQarz = 0;
            Long shartnomaId = shartnoma.getId();
            String status = shartnoma.getStatus();
            List<Jadval> jadvalList = shartnoma.getJadvalList();
            if (jadvalList != null) {
                qolganQarz = jadvalList.stream()
                        .filter(j -> "tulanmagan".equals(j.getHolat()))
                        .mapToLong(j -> Math.max(0, j.getSumma() - j.getTulangan()))
                        .sum();
            }

            natija.add(new BugungiEslatmaDto(
                    mijozId,
                    mijoz.getIsm(),
                    mijoz.getFamiliya(),
                    mijoz.getTel1(),
                    mijoz.getTel2(),
                    mijoz.getTel3(),
                    shartnomaId,
                    qolganQarz,
                    status,
                    q.getVadaSana(),
                    q.getIzoh()
            ));
        }

        natija.sort(Comparator.comparing(d -> (d.getFamiliya() == null ? "" : d.getFamiliya())));
        return natija;
    }
}
