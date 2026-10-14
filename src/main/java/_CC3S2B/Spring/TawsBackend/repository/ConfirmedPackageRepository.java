package _CC3S2B.Spring.TawsBackend.repository;
import _CC3S2B.Spring.TawsBackend.dto.ConfirmedView;
import _CC3S2B.Spring.TawsBackend.dto.MyPackageView;
import _CC3S2B.Spring.TawsBackend.dto.QuoteResponse;
import org.springframework.stereotype.Repository;
import org.springframework.jdbc.core.JdbcTemplate;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.core.JacksonException;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;
@Repository
public class ConfirmedPackageRepository {
 private final JdbcTemplate jdbc;
 private final JsonMapper mapper;
 public ConfirmedPackageRepository(JdbcTemplate jdbc, JsonMapper mapper) { this.jdbc=jdbc;this.mapper=mapper; }
 public void saveProposal(QuoteResponse p, int uid) {
  try {
   jdbc.update("INSERT INTO taws.quote_proposal(proposal_id,owner_user_id,payload,expires_at) VALUES (?, ?, CAST(? AS jsonb), now()+interval '1 hour')",p.proposalId(),uid,mapper.writeValueAsString(p));
  } catch(JacksonException e) { throw new IllegalStateException(e); }
 }
 public QuoteResponse lockProposal(UUID id,int uid) {
  var rows=jdbc.queryForList("SELECT payload::text AS payload FROM taws.quote_proposal WHERE proposal_id=? AND owner_user_id=? AND NOT confirmed AND expires_at>now() FOR UPDATE",id,uid);
  if(rows.isEmpty())throw new ResponseStatusException(HttpStatus.CONFLICT,"Propuesta expirada, ajena o ya confirmada");
  try { return mapper.readValue((String)rows.get(0).get("payload"),QuoteResponse.class); }
  catch(JacksonException e){throw new IllegalStateException(e);}
 }
 public ConfirmedView confirm(QuoteResponse q, int uid) {
  // Esquema real: quotation_id y package_id son columnas GENERATED ALWAYS AS STORED.
  String quotationId=jdbc.queryForObject("""
   INSERT INTO taws.quotation(status,people_quantity,departure_region,destination_region,departure_date,return_date,departure_time,return_time,total)
   VALUES ('pending',?,?,?,?,?,'08:00','20:00',?) RETURNING quotation_id
   """,String.class,q.passengerCount(),q.departureCity(),q.destination(),q.departureDate(),q.returnDate(),q.totalPrice());
  String packageId=jdbc.queryForObject("""
   INSERT INTO taws.travel_package(creation_date,quotation_id,app_user_id)
   VALUES (CURRENT_DATE,?,?) RETURNING package_id
   """,String.class,quotationId,uid);
  try {
   jdbc.update("INSERT INTO taws.travel_package_detail(package_id,itinerary) VALUES (?,CAST(? AS jsonb))",packageId,mapper.writeValueAsString(q));
  }catch(JacksonException e){throw new IllegalStateException(e);}
  jdbc.update("UPDATE taws.quote_proposal SET confirmed=true WHERE proposal_id=?",q.proposalId());
  return new ConfirmedView(packageId.trim(),quotationId,"pending",q);
 }
 public ConfirmedView findPackage(String packageId,int uid) {
  var rows=jdbc.queryForList("""
   SELECT p.package_id,p.quotation_id,q.status,d.itinerary::text AS payload
   FROM taws.travel_package p JOIN taws.quotation q ON q.quotation_id=p.quotation_id
   JOIN taws.travel_package_detail d ON d.package_id=p.package_id
   WHERE p.package_id=? AND p.app_user_id=?
   """,packageId,uid);
  if(rows.isEmpty())throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Paquete no encontrado");
  var row=rows.get(0);
  try{return new ConfirmedView(((String)row.get("package_id")).trim(),(String)row.get("quotation_id"),(String)row.get("status"),mapper.readValue((String)row.get("payload"),QuoteResponse.class));}
  catch(JacksonException e){throw new IllegalStateException(e);}
 }
 public List<MyPackageView> myPackages(int uid) {
  return jdbc.query("""
   SELECT p.package_id,p.quotation_id,q.status,q.total,q.departure_date,q.return_date,q.destination_region
   FROM taws.travel_package p JOIN taws.quotation q ON q.quotation_id=p.quotation_id
   WHERE p.app_user_id=? ORDER BY p.creation_date DESC,p.package_id DESC
   """,(rs,n)->new MyPackageView(rs.getString(1).trim(),rs.getString(2),rs.getString(3),rs.getBigDecimal(4),rs.getDate(5).toLocalDate(),rs.getDate(6).toLocalDate(),rs.getString(7)),uid);
 }
}
