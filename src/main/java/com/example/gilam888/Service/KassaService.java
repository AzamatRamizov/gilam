package com.example.gilam888.Service;

import com.example.gilam888.Dto.ChartPointDto;
import com.example.gilam888.Dto.KassaStatDto;
import com.example.gilam888.Dto.PaymentDto;
import com.example.gilam888.Entity.Jadval;
import com.example.gilam888.Entity.Magazin;
import com.example.gilam888.Entity.PaymentHistory;
import com.example.gilam888.Entity.Shartnoma;
import com.example.gilam888.Repository.JadvalRepository;
import com.example.gilam888.Repository.MagazinRepository;
import com.example.gilam888.Repository.PaymentRepository;
import com.example.gilam888.Repository.ShartnomaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

@Service
@RequiredArgsConstructor
public class KassaService {

    private static final String[] OY_TOLIQ = {
            "Yanvar","Fevral","Mart","Aprel","May","Iyun",
            "Iyul","Avgust","Sentyabr","Oktyabr","Noyabr","Dekabr"
    };

    private final PaymentRepository paymentRepository;

    // TODO: bularni o'zingizdagi haqiqiy repolar/servicelar bilan almashtiring
    // private final DokonRepository dokonRepository;
    // private final JadvalRepository jadvalRepository;

    private static final String[] OY_QISQA = {"yan","fev","mar","apr","may","iyun","iyul","avg","sen","okt","noy","dek"};
    private final MagazinRepository magazinRepository;
    private final ShartnomaRepository shartnomaRepository;
    private final JadvalRepository jadvalRepository;

    public KassaStatDto getKassaStat(String period, LocalDateTime customFrom, LocalDateTime customTo) {
        LocalDateTime now = LocalDateTime.now();

        Range current  = resolveRange(period, now, customFrom, customTo);
        Range previous = resolvePreviousRange(period, current);

        List<PaymentHistory> payments = paymentRepository.findBySanaBetween(current.from, current.to);

        long jami = payments.stream().mapToLong(PaymentHistory::getSumma).sum();
        long son  = payments.size();
        long ortacha = son == 0 ? 0 : jami / son;

        String jamiDiff = null;
        if (previous != null) {
            long prevJami = paymentRepository.sumSummaBetween(previous.from, previous.to);
            jamiDiff = calcDiff(jami, prevJami);
        }

        Map<String, Long> turiSumma = new HashMap<>();
        for (PaymentHistory p : payments) {
            String t = p.getTuri() == null ? "Boshqa" : p.getTuri();
            turiSumma.merge(t, p.getSumma(), Long::sum);
        }
        long naqdSumma     = turiSumma.getOrDefault("Naqd", 0L);
        long terminalSumma = turiSumma.getOrDefault("Terminal", 0L);
        long otkazmaSumma  = turiSumma.getOrDefault("O'tkazma", 0L);

        int naqdPct     = jami == 0 ? 0 : (int) Math.round(naqdSumma * 100.0 / jami);
        int terminalPct = jami == 0 ? 0 : (int) Math.round(terminalSumma * 100.0 / jami);
        int otkazmaPct  = jami == 0 ? 0 : Math.max(0, 100 - naqdPct - terminalPct);

        // ── Shu oy to'lovi bo'lishi kerak (joriy kalendar oy, davrga bog'liq emas) ──
        LocalDateTime oyBoshi = now.withDayOfMonth(1).toLocalDate().atStartOfDay();
        LocalDateTime oyOxiri = oyBoshi.plusMonths(1);
        long oylikKerakSumma = jadvalRepository.sumOylikKerakSumma(oyBoshi, oyOxiri);
        long oylikKerakSon   = jadvalRepository.countOylikKerak(oyBoshi, oyOxiri);

        // ── Shartnoma-asosidagi statistikalar (davrga bog'liq emas, global) ──
        long yopilganShartnomaSoni     = shartnomaRepository.countByStatus("yopilgan");
        long activShartnomaSoni        = shartnomaRepository.countByStatus("ochiq");
        long muddatiOtganShartnomaSoni = shartnomaRepository.countMuddatiOtganShartnoma(now);

        ChartResult chartResult = buildChart(period, now, current);

        return KassaStatDto.builder()
                .jami(jami)
                .son(son)
                .ortacha(ortacha)
                .jamiDiff(jamiDiff)
                .oylikKerakSumma(oylikKerakSumma)
                .oylikKerakSon(oylikKerakSon)
                .yopilganShartnomaSoni(yopilganShartnomaSoni)
                .activShartnomaSoni(activShartnomaSoni)
                .muddatiOtganShartnomaSoni(muddatiOtganShartnomaSoni)
                .naqdPct(naqdPct)
                .terminalPct(terminalPct)
                .otkazmaPct(otkazmaPct)
                .naqdSumma(naqdSumma)
                .terminalSumma(terminalSumma)
                .otkazmaSumma(otkazmaSumma)
                .chartLabel(chartResult.label())
                .chart(chartResult.points())
                .payments(toPaymentDtos(payments))
                .build();
    }

    // ================= Sana oralig'i =================

    private record Range(LocalDateTime from, LocalDateTime to) {}

    private Range resolveRange(String period, LocalDateTime now, LocalDateTime customFrom, LocalDateTime customTo) {
        if ("oy".equals(period) || "maxsus".equals(period)) {
            if (customFrom != null && customTo != null) {
                return new Range(customFrom, customTo);
            }
            if ("maxsus".equals(period)) {
                // customFrom/customTo kelmasa, "maxsus" ma'nosiz — xatolik qaytaramiz
                throw new IllegalArgumentException("Maxsus davr uchun boshi va oxiri sanalari majburiy");
            }
            // "oy" uchun customFrom/customTo bo'lmasa, pastdagi "oy" case ishlaydi (joriy oy)
        }

        LocalDate today = now.toLocalDate();
        switch (period) {
            case "bugun":
                return new Range(today.atStartOfDay(), today.plusDays(1).atStartOfDay());
            case "hafta": {
                LocalDate monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
                return new Range(monday.atStartOfDay(), today.plusDays(1).atStartOfDay());
            }
            case "oy": {
                LocalDate first = today.withDayOfMonth(1);
                return new Range(first.atStartOfDay(), first.plusMonths(1).atStartOfDay());
            }
            case "yil": {
                LocalDate first = today.withDayOfYear(1);
                return new Range(first.atStartOfDay(), first.plusYears(1).atStartOfDay());
            }
            case "barchasi":
            default: {
                LocalDateTime earliest = paymentRepository.findEarliestSana();
                LocalDateTime from = earliest != null ? earliest : today.atStartOfDay();
                return new Range(from, now.plusDays(1));
            }
        }
    }

    private Range resolvePreviousRange(String period, Range current) {
        return switch (period) {
            case "bugun"    -> new Range(current.from().minusDays(1), current.to().minusDays(1));
            case "hafta"    -> new Range(current.from().minusWeeks(1), current.to().minusWeeks(1));
            case "oy"       -> new Range(current.from().minusMonths(1), current.to().minusMonths(1));
            case "yil"      -> new Range(current.from().minusYears(1), current.to().minusYears(1));
            default         -> null; // "barchasi" uchun solishtirish yo'q
        };
    }

    private String calcDiff(long current, long previous) {
        if (previous == 0) return current > 0 ? "+100%" : null;
        double pct = ((double) (current - previous) / previous) * 100.0;
        String sign = pct >= 0 ? "+" : "";
        return sign + Math.round(pct * 10.0) / 10.0 + "%";
    }

    // ================= Chart =================

    private record ChartResult(String label, List<ChartPointDto> points) {}

    private ChartResult buildChart(String period, LocalDateTime now, Range current) {
        List<ChartPointDto> points = new ArrayList<>();
        String label;

        switch (period) {
            case "bugun":
            case "hafta": {
                label = "Joriy hafta";
                LocalDate today = now.toLocalDate();
                LocalDate monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
                String[] kunlar = {"Du","Se","Ch","Pa","Ju","Sh","Ya"};
                for (int i = 0; i < 7; i++) {
                    LocalDate day = monday.plusDays(i);
                    long val = day.isAfter(today) ? 0
                            : paymentRepository.sumSummaBetween(day.atStartOfDay(), day.plusDays(1).atStartOfDay());
                    String state = day.isEqual(today) ? "today" : (day.isAfter(today) ? "dim" : "past");
                    points.add(new ChartPointDto(kunlar[i], val, state));
                }
                break;
            }
            case "oy": {
                LocalDate first = current.from().toLocalDate();
                LocalDate lastDay = current.to().toLocalDate().minusDays(1); // to' — exclusive bo'lgani uchun
                LocalDate today = now.toLocalDate();

                label = "Haftalik (" + OY_TOLIQ[first.getMonthValue() - 1] + " " + first.getYear() + ")";

                LocalDate cursor = first;
                int weekNo = 1;
                while (!cursor.isAfter(lastDay)) {
                    LocalDate weekEnd = cursor.plusDays(6).isAfter(lastDay) ? lastDay : cursor.plusDays(6);
                    long val = paymentRepository.sumSummaBetween(cursor.atStartOfDay(), weekEnd.plusDays(1).atStartOfDay());

                    String state;
                    if (weekEnd.isBefore(today)) state = "past";
                    else if (!today.isBefore(cursor) && !today.isAfter(weekEnd)) state = "today";
                    else state = "dim";

                    points.add(new ChartPointDto(weekNo + "-h", val, state));
                    cursor = weekEnd.plusDays(1);
                    weekNo++;
                }
                break;
            }
            case "yil": {
                label = "Oylik (joriy yil)";
                int year = now.getYear();
                for (int m = 1; m <= 12; m++) {
                    YearMonth ym = YearMonth.of(year, m);
                    LocalDate start = ym.atDay(1);
                    LocalDate end = ym.atEndOfMonth();
                    boolean future = start.isAfter(now.toLocalDate());
                    long val = future ? 0 : paymentRepository.sumSummaBetween(start.atStartOfDay(), end.plusDays(1).atStartOfDay());
                    String state = ym.equals(YearMonth.from(now)) ? "today" : (future ? "dim" : "past");
                    points.add(new ChartPointDto(OY_QISQA[m - 1], val, state));
                }
                break;
            }
            case "barchasi":
            default: {
                label = "Yillik";
                LocalDateTime earliest = paymentRepository.findEarliestSana();
                int startYear = earliest != null ? earliest.getYear() : now.getYear();
                int endYear = now.getYear();
                for (int y = startYear; y <= endYear; y++) {
                    LocalDate start = LocalDate.of(y, 1, 1);
                    LocalDate end = LocalDate.of(y, 12, 31);
                    long val = paymentRepository.sumSummaBetween(start.atStartOfDay(), end.plusDays(1).atStartOfDay());
                    points.add(new ChartPointDto(String.valueOf(y), val, y == endYear ? "today" : "past"));
                }
                break;
            }
        }
        return new ChartResult(label, points);
    }

    // ================= Payments DTO =================

    private List<PaymentDto> toPaymentDtos(List<PaymentHistory> payments) {
        if (payments.isEmpty()) return List.of();

        return payments.stream()
                .sorted(Comparator.comparing(PaymentHistory::getSana).reversed())
                .map(p -> {
                    PaymentDto dto = new PaymentDto();
                    dto.setSana(p.getSana() != null ? p.getSana().toString() : null);
                    dto.setSumma(p.getSumma());
                    dto.setTuri(p.getTuri());

                    // Dokon
                    Optional<Magazin> dokon = magazinRepository.findById(p.getDokonId());
                    dto.setDokon(dokon.map(Magazin::getNomi).orElse("Noaniq"));

                    // Shartnoma va mijoz
                    Optional<Jadval> jadval = jadvalRepository.findById(p.getJadvalId());
                    if (jadval.isPresent()) {
                        Optional<Shartnoma> shartnomaOpt = shartnomaRepository.findByJadvalListContaining(jadval.get());
                        if (shartnomaOpt.isPresent()) {
                            Shartnoma shartnoma = shartnomaOpt.get();
                            dto.setShartnomaId(shartnoma.getId());
                            dto.setMijoz(
                                    shartnoma.getMijoz().getIsm() + " " +
                                            shartnoma.getMijoz().getFamiliya() + " " +
                                            shartnoma.getMijoz().getSharif()
                            );
                        } else {
                            dto.setMijoz("—");
                            dto.setShartnomaId(null);
                        }
                    } else {
                        dto.setMijoz("—");
                        dto.setShartnomaId(null);
                    }

                    return dto;
                })
                .toList();
    }
}
