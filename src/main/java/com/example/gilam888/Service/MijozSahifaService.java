package com.example.gilam888.Service;

import com.example.gilam888.Dto.MijozSahifaDto;
import com.example.gilam888.Dto.ShartnomaQisqaDto;
import com.example.gilam888.Entity.Jadval;
import com.example.gilam888.Entity.Mijoz;
import com.example.gilam888.Entity.Shartnoma;
import com.example.gilam888.Repository.MijozRepository;
import com.example.gilam888.Repository.ShartnomaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class MijozSahifaService {

    private final MijozRepository mijozRepository;
    private final ShartnomaRepository shartnomaRepository;
    private final AmalService amalService;

    public MijozSahifaService(MijozRepository mijozRepository,
                              ShartnomaRepository shartnomaRepository,
                              AmalService amalService) {
        this.mijozRepository = mijozRepository;
        this.shartnomaRepository = shartnomaRepository;
        this.amalService = amalService;
    }

    /** Avto status: 0 kechikish -> yaxshi, 1-2 -> qisman, 3+ -> yomon */
    public static String avtoStatus(int kechikkanOylar) {
        if (kechikkanOylar <= 0) return "yaxshi";
        if (kechikkanOylar <= 2) return "qisman";
        return "yomon";
    }

    @Transactional(readOnly = true)
    public MijozSahifaDto getMijozSahifa(long mijozId) {
        Mijoz mijoz = mijozRepository.findById(mijozId)
                .orElseThrow(() -> new RuntimeException("Mijoz topilmadi: " + mijozId));

        List<Shartnoma> shartnomalar = shartnomaRepository.findByMijoz_Id(mijozId);
        LocalDateTime hozir = LocalDateTime.now();

        MijozSahifaDto dto = new MijozSahifaDto();
        dto.setId(mijoz.getId());
        dto.setIsm(mijoz.getIsm());
        dto.setFamiliya(mijoz.getFamiliya());
        dto.setSharif(mijoz.getSharif());
        dto.setViloyat(mijoz.getViloyat());
        dto.setTuman(mijoz.getTuman());
        dto.setManzil(mijoz.getManzil());
        dto.setMuljal(mijoz.getMuljal());
        dto.setTel1(mijoz.getTel1());
        dto.setTel2(mijoz.getTel2());
        dto.setTel3(mijoz.getTel3());
        dto.setPassport(mijoz.getPassport());
        dto.setPassportRasmId(mijoz.getPassportRasm() != null ? mijoz.getPassportRasm().getId() : null);
        dto.setKatmId(mijoz.getKatm() != null ? mijoz.getKatm().getId() : null);

        long jami = 0, tulangan = 0;
        int kechikkanJami = 0, ochiq = 0;
        List<ShartnomaQisqaDto> list = new ArrayList<>();

        for (Shartnoma s : shartnomalar) {
            long sJami = 0, sTulangan = 0;
            int sKechikkan = 0;

            List<Jadval> jadvallar = s.getJadvalList();
            if (jadvallar != null) {
                for (Jadval j : jadvallar) {
                    sJami += j.getSumma();
                    sTulangan += j.getTulangan();
                    boolean toliqTulangan = "tulangan".equalsIgnoreCase(
                            j.getHolat() == null ? "" : j.getHolat());
                    if (!toliqTulangan && j.getSana() != null && j.getSana().isBefore(hozir)) {
                        sKechikkan++;
                    }
                }
            }

            ShartnomaQisqaDto sq = new ShartnomaQisqaDto();
            sq.setId(s.getId());
            sq.setMahsulot(s.getMahsulot());
            sq.setSotibOlinganSana(s.getSotibOlinganSana());
            sq.setMuddat(s.getMuddat());
            sq.setStatus(s.getStatus());
            sq.setJamiSumma(sJami);
            sq.setTulangan(sTulangan);
            sq.setQoldiq(Math.max(0, sJami - sTulangan));
            sq.setKechikkanOylar(sKechikkan);
            list.add(sq);

            jami += sJami;
            tulangan += sTulangan;
            kechikkanJami += sKechikkan;
            if (!"yopilgan".equalsIgnoreCase(s.getStatus() == null ? "" : s.getStatus())) ochiq++;
        }

        dto.setShartnomalar(list);
        dto.setShartnomaSoni(list.size());
        dto.setOchiqShartnomaSoni(ochiq);
        dto.setJamiSumma(jami);
        dto.setTulanganSumma(tulangan);
        dto.setQoldiqSumma(Math.max(0, jami - tulangan));
        dto.setMuddatiOtganOylar(kechikkanJami);

        String avto = avtoStatus(kechikkanJami);
        dto.setAvtoStatus(avto);

        String qoldaStatus = mijoz.getStatus();
        if (qoldaStatus != null && !qoldaStatus.isBlank()) {
            dto.setStatus(qoldaStatus);
            dto.setQolda(true);
        } else {
            dto.setStatus(avto);
            dto.setQolda(false);
        }
        return dto;
    }

    /** Statusni qo'lda o'zgartirish. status = "avto" bo'lsa null qilinadi (avto rejimga qaytadi) */
    @Transactional
    public void setStatus(long mijozId, String status) {
        Mijoz mijoz = mijozRepository.findById(mijozId)
                .orElseThrow(() -> new RuntimeException("Mijoz topilmadi: " + mijozId));

        String yangi;
        if (status == null || status.isBlank() || "avto".equalsIgnoreCase(status)) {
            yangi = null;
        } else if (status.equals("yaxshi") || status.equals("qisman") || status.equals("yomon")) {
            yangi = status;
        } else {
            throw new RuntimeException("Noto'g'ri status: " + status);
        }

        mijoz.setStatus(yangi);
        mijozRepository.save(mijoz);

        String fish = (mijoz.getFamiliya() == null ? "" : mijoz.getFamiliya() + " ")
                + (mijoz.getIsm() == null ? "" : mijoz.getIsm());
        amalService.log("MIJOZ_STATUS",
                "Mijoz statusi o'zgartirildi: " + fish.trim() + " -> "
                        + (yangi == null ? "avto" : yangi),
                mijozId, null, null);
    }
}