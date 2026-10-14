package _CC3S2B.Spring.TawsBackend.service;

import _CC3S2B.Spring.TawsBackend.dto.*;
import _CC3S2B.Spring.TawsBackend.model.*;
import _CC3S2B.Spring.TawsBackend.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.math.BigDecimal;
import java.time.*;
import java.time.temporal.ChronoUnit;

@Service
public class QuotationService {

 private final DestinationRepository destinations;
 private final AirplaneRepository airplanes;
 private final HotelRepository hotels;
 private final QuoteActivityRepository activities;
 private final ConfirmedPackageRepository confirmations;

 public QuotationService(
         DestinationRepository d,
         AirplaneRepository a,
         HotelRepository h,
         QuoteActivityRepository ac,
         ConfirmedPackageRepository c) {
  this.destinations = d;
  this.airplanes = a;
  this.hotels = h;
  this.activities = ac;
  this.confirmations = c;
 }

 public List<Destination> destinations() {
  return destinations.findAllByOrderByNameAsc();
 }

 @Transactional
 public List<QuoteResponse> generate(QuoteRequest r, Integer userId) {
  if (r.departureDate().isBefore(LocalDate.now()) || r.returnDate().isBefore(r.departureDate())) {
   throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Fechas invalidas");
  }

  long count = ChronoUnit.DAYS.between(r.departureDate(), r.returnDate()) + 1;
  if (count > 30) {
   throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Maximo 30 dias");
  }

  if (r.departureCity().trim().equalsIgnoreCase(r.destination().trim())) {
   throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Origen y destino deben diferir");
  }

  Destination destination = destinations.findAllByOrderByNameAsc().stream()
          .filter(d -> d.getName().equalsIgnoreCase(r.destination().trim()))
          .findFirst()
          .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Destino no disponible"));

  var flightList = airplanes.findByDestinationId(destination.getDestinationId());
  var hotelList = hotels.findByDestinationId(destination.getDestinationId());
  var activityList = activities.findByDestinationId(destination.getDestinationId());

  int days = (int) count;
  int required = days == 1 ? 2 : days;

  if (flightList.isEmpty() || hotelList.isEmpty() || activityList.size() < required) {
   throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Catalogo insuficiente para esos dias");
  }

  int uid = userId;
  int people = r.passengerCount();
  int rooms = (people + 1) / 2;
  int nights = days - 1;

  List<QuoteResponse> output = new ArrayList<>();
  Set<String> seen = new HashSet<>();

  for (int attempt = 0; output.size() < 5 && attempt < 500; attempt++) {
   var flight = pick(flightList);
   var hotel = pick(hotelList);
   var shuffled = new ArrayList<>(activityList);
   Collections.shuffle(shuffled);
   var chosen = shuffled.subList(0, required);

   String fingerprint = hotel.getHotelId() + ":" + flight.getAirplaneId() + ":" +
           chosen.stream().map(a -> a.getActivityId().toString()).sorted().collect(Collectors.joining(","));

   if (!seen.add(fingerprint)) continue;

   LocalTime starts = LocalTime.of(13, 0);
   List<ActivityView> itinerary = new ArrayList<>();
   boolean valid = true;

   for (int i = 0; i < chosen.size(); i++) {
    var a = chosen.get(i);
    if (days != 1) starts = LocalTime.of(13, 0);
    LocalTime ends = starts.plusMinutes(a.getDurationMinutes());

    // Evitamos que una actividad termine después de la salida del vuelo de regreso.
    if (a.getDurationMinutes() > 420 || ends.isBefore(starts) || ends.isAfter(LocalTime.of(20, 0))) {
     valid = false;
     break;
    }

    LocalDate when = r.departureDate().plusDays(days == 1 ? 0 : i);
    itinerary.add(new ActivityView(a.getActivityId(), a.getName(), when, starts, ends, a.getDurationMinutes(), a.getPricePerPerson()));
    starts = ends;
   }

   if (!valid) continue;

   BigDecimal total = flight.getRoundTripPrice().multiply(BigDecimal.valueOf(people))
           .add(hotel.getPricePerNight().multiply(BigDecimal.valueOf((long) nights * rooms)))
           .add(chosen.stream().map(a -> a.getPricePerPerson()).reduce(BigDecimal.ZERO, BigDecimal::add).multiply(BigDecimal.valueOf(people)));

   var q = new QuoteResponse(
           UUID.randomUUID(),
           r.departureCity().trim(),
           destination.getName(),
           people,
           r.departureDate(),
           r.returnDate(),
           days,
           new HotelView(hotel.getHotelId(), hotel.getName(), hotel.getPricePerNight(), rooms, nights),
           new AirplaneView(flight.getAirplaneId(), flight.getAirline(), flight.getRoundTripPrice(), LocalTime.of(8, 0), LocalTime.of(20, 0)),
           itinerary,
           total
   );

   confirmations.saveProposal(q, uid);
   output.add(q);
  }

  if (output.isEmpty()) {
   throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "No es posible acomodar las actividades antes del regreso");
  }

  return output;
 }

 @Transactional
 public ConfirmedView confirm(UUID proposalId, Integer userId) {
  int uid = userId;
  return confirmations.confirm(confirmations.lockProposal(proposalId, uid), uid);
 }

 @Transactional(readOnly = true)
 public ConfirmedView detail(String packageId, Integer userId) {
  return confirmations.findPackage(packageId, userId);
 }

 @Transactional(readOnly = true)
 public List<MyPackageView> mine(Integer userId) {
  return confirmations.myPackages(userId);
 }

 private static <T> T pick(List<T> list) {
  return list.get(ThreadLocalRandom.current().nextInt(list.size()));
 }
}