package _CC3S2B.Spring.TawsBackend.service;

import _CC3S2B.Spring.TawsBackend.dto.*;
import _CC3S2B.Spring.TawsBackend.model.*;
import _CC3S2B.Spring.TawsBackend.repository.*;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuotationService {

    private final DestinationRepository destinations;
    private final AirplaneRepository airplanes;
    private final HotelRepository hotels;
    private final QuoteActivityRepository activities;
    private final QuotationRepository quotations;
    private final JsonMapper jsonMapper;

    @Transactional(readOnly = true)
    public List<QuoteResponse> generate(QuoteRequest request) {
        int days = validateDates(request.departureDate(), request.returnDate());
        validateRoute(request.departureCity(), request.destination(), request.passengerCount());

        Destination destination = findDestination(request.destination());
        List<Airplane> flightList = airplanes.findByDestinationId(destination.getDestinationId());
        List<Hotel> hotelList = hotels.findByDestinationId(destination.getDestinationId());
        List<QuoteActivity> activityList = activities.findByDestinationId(destination.getDestinationId());

        int required = days == 1 ? 2 : days;
        if (flightList.isEmpty() || hotelList.isEmpty() || activityList.size() < required) {
            throw error(HttpStatus.UNPROCESSABLE_ENTITY, "No hay suficientes opciones para este viaje");
        }

        List<QuoteResponse> result = new ArrayList<>();
        Set<String> seen = new HashSet<>();

        for (int attempt = 0; result.size() < 5 && attempt < 500; attempt++) {
            Airplane airplane = pick(flightList);
            Hotel hotel = pick(hotelList);
            List<QuoteActivity> shuffled = new ArrayList<>(activityList);
            Collections.shuffle(shuffled);
            List<QuoteActivity> chosen = shuffled.subList(0, required);

            String fingerprint = hotel.getHotelId() + ":" + airplane.getAirplaneId() + ":" +
                    chosen.stream().map(a -> a.getActivityId().toString()).sorted().collect(Collectors.joining(","));

            if (!seen.add(fingerprint)) continue;

            try {
                result.add(buildQuote(UUID.randomUUID(), request, days, hotel, airplane, chosen));
            } catch (IllegalArgumentException ignored) {}
        }

        if (result.isEmpty()) {
            throw error(HttpStatus.UNPROCESSABLE_ENTITY, "No es posible organizar las actividades antes del vuelo de retorno");
        }

        return result;
    }

    @Transactional
    public String confirm(QuoteResponse selected, Integer userId) {
        if (userId == null || selected == null || selected.hotel() == null || selected.airplane() == null || selected.activities() == null) {
            throw error(HttpStatus.BAD_REQUEST, "Cotizacion incompleta");
        }

        int days = validateDates(selected.departureDate(), selected.returnDate());
        validateRoute(selected.departureCity(), selected.destination(), selected.passengerCount());

        Destination destination = findDestination(selected.destination());
        int destinationId = destination.getDestinationId();

        Hotel hotel = hotels.findById(requiredId(selected.hotel().id()))
                .orElseThrow(() -> error(HttpStatus.BAD_REQUEST, "Hotel inexistente"));

        Airplane airplane = airplanes.findById(requiredId(selected.airplane().id()))
                .orElseThrow(() -> error(HttpStatus.BAD_REQUEST, "Avion inexistente"));

        if (!Objects.equals(hotel.getDestinationId(), destinationId) || !Objects.equals(airplane.getDestinationId(), destinationId)) {
            throw error(HttpStatus.BAD_REQUEST, "Hotel o avion no pertenece al destino");
        }

        int required = days == 1 ? 2 : days;
        if (selected.activities().size() != required) {
            throw error(HttpStatus.BAD_REQUEST, "Cantidad de actividades incorrecta");
        }

        List<Integer> ids = new ArrayList<>();
        for (ActivityView view : selected.activities()) {
            if (view == null) {
                throw error(HttpStatus.BAD_REQUEST, "Actividad invalida");
            }
            ids.add(requiredId(view.id()));
        }

        if (new HashSet<>(ids).size() != ids.size()) {
            throw error(HttpStatus.BAD_REQUEST, "No se permiten actividades repetidas");
        }

        Map<Integer, QuoteActivity> byId = new HashMap<>();
        activities.findAllById(ids).forEach(a -> byId.put(a.getActivityId(), a));

        List<QuoteActivity> chosen = new ArrayList<>();
        for (Integer id : ids) {
            QuoteActivity activity = byId.get(id);
            if (activity == null || !Objects.equals(activity.getDestinationId(), destinationId)) {
                throw error(HttpStatus.BAD_REQUEST, "Actividad no disponible para el destino");
            }
            chosen.add(activity);
        }

        QuoteRequest request = QuoteRequest.builder()
                .departureCity(selected.departureCity())
                .destination(destination.getName())
                .passengerCount(selected.passengerCount())
                .departureDate(selected.departureDate())
                .returnDate(selected.returnDate())
                .build();

        QuoteResponse canonical;
        try {
            canonical = buildQuote(
                    selected.proposalId() == null ? UUID.randomUUID() : selected.proposalId(),
                    request, days, hotel, airplane, chosen
            );
        } catch (IllegalArgumentException e) {
            throw error(HttpStatus.BAD_REQUEST, e.getMessage());
        }

        Quotation quotation = Quotation.builder()
                .status("pending")
                .peopleQuantity(canonical.passengerCount())
                .departureRegion(canonical.departureCity())
                .destinationRegion(canonical.destination())
                .departureDate(canonical.departureDate())
                .returnDate(canonical.returnDate())
                .departureTime(LocalTime.of(8, 0))
                .returnTime(LocalTime.of(20, 0))
                .total(canonical.totalPrice())
                .appUserId(userId)
                .build();

        try {
            quotation.setItinerary(jsonMapper.writeValueAsString(canonical));
        } catch (JacksonException e) {
            throw new IllegalStateException("No se pudo serializar el itinerario", e);
        }

        Quotation saved = quotations.saveAndFlush(quotation);
        String quotationId = quotations.findGeneratedQuotationId(saved.getNumId());

        if (quotationId == null) {
            throw new IllegalStateException("Neon no devolvio quotation_id");
        }

        return quotationId;
    }

    private QuoteResponse buildQuote(UUID proposalId, QuoteRequest r, int days, Hotel hotel, Airplane airplane, List<QuoteActivity> chosen) {
        int people = r.passengerCount();
        int rooms = (people + 1) / 2;
        int nights = days - 1;

        LocalTime start = LocalTime.of(13, 0);
        List<ActivityView> itinerary = new ArrayList<>();

        for (int i = 0; i < chosen.size(); i++) {
            QuoteActivity activity = chosen.get(i);
            if (days != 1) start = LocalTime.of(13, 0);

            int duration = activity.getDurationMinutes();
            if (duration <= 0 || duration > 420) {
                throw new IllegalArgumentException("Duracion de actividad fuera del horario permitido");
            }

            LocalTime end = start.plusMinutes(duration);
            if (!end.isAfter(start) || end.isAfter(LocalTime.of(20, 0))) {
                throw new IllegalArgumentException("Actividad termina despues del vuelo de retorno");
            }

            LocalDate date = r.departureDate().plusDays(days == 1 ? 0 : i);

            ActivityView activityView = ActivityView.builder()
                    .id(activity.getActivityId())
                    .name(activity.getName())
                    .date(date)
                    .startTime(start)
                    .endTime(end)
                    .durationMinutes(duration)
                    .pricePerPerson(activity.getPricePerPerson())
                    .build();

            itinerary.add(activityView);
            start = end;
        }

        BigDecimal flightPrice = airplane.getRoundTripPrice().multiply(BigDecimal.valueOf(people));
        BigDecimal hotelPrice = hotel.getPricePerNight().multiply(BigDecimal.valueOf((long) nights * rooms));
        BigDecimal activitiesPrice = chosen.stream()
                .map(QuoteActivity::getPricePerPerson)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .multiply(BigDecimal.valueOf(people));

        BigDecimal total = flightPrice.add(hotelPrice).add(activitiesPrice);

        HotelView hotelView = HotelView.builder()
                .id(hotel.getHotelId())
                .name(hotel.getName())
                .pricePerNight(hotel.getPricePerNight())
                .rooms(rooms)
                .nights(nights)
                .build();

        AirplaneView airplaneView = AirplaneView.builder()
                .id(airplane.getAirplaneId())
                .airline(airplane.getAirline())
                .roundTripPricePerPerson(airplane.getRoundTripPrice())
                .outboundTime(LocalTime.of(8, 0))
                .returnTime(LocalTime.of(20, 0))
                .build();

        return QuoteResponse.builder()
                .proposalId(proposalId)
                .departureCity(r.departureCity().trim())
                .destination(r.destination().trim())
                .passengerCount(people)
                .departureDate(r.departureDate())
                .returnDate(r.returnDate())
                .days(days)
                .hotel(hotelView)
                .airplane(airplaneView)
                .activities(itinerary)
                .totalPrice(total)
                .build();
    }

    private Destination findDestination(String name) {
        return destinations.findAllByOrderByNameAsc().stream()
                .filter(d -> d.getName().equalsIgnoreCase(name.trim()))
                .findFirst()
                .orElseThrow(() -> error(HttpStatus.BAD_REQUEST, "Destino no disponible"));
    }

    private int validateDates(LocalDate departure, LocalDate arrival) {
        if (departure == null || arrival == null || departure.isBefore(LocalDate.now()) || arrival.isBefore(departure)) {
            throw error(HttpStatus.BAD_REQUEST, "Fechas invalidas");
        }

        long count = ChronoUnit.DAYS.between(departure, arrival) + 1;
        if (count > 30) {
            throw error(HttpStatus.BAD_REQUEST, "Maximo 30 dias");
        }

        return (int) count;
    }

    private void validateRoute(String origin, String destination, Integer people) {
        if (origin == null || origin.isBlank() || destination == null || destination.isBlank()
                || origin.trim().equalsIgnoreCase(destination.trim()) || people == null || people < 1 || people > 10) {
            throw error(HttpStatus.BAD_REQUEST, "Origen, destino o pasajeros invalidos");
        }

        if (origin.length() > 50 || destination.length() > 50) {
            throw error(HttpStatus.BAD_REQUEST, "Origen o destino demasiado largo");
        }
    }

    private Integer requiredId(Integer id) {
        if (id == null || id < 1) {
            throw error(HttpStatus.BAD_REQUEST, "ID de servicio invalido");
        }
        return id;
    }

    private static <T> T pick(List<T> list) {
        return list.get(ThreadLocalRandom.current().nextInt(list.size()));
    }

    private ResponseStatusException error(HttpStatus code, String message) {
        return new ResponseStatusException(code, message);
    }
}