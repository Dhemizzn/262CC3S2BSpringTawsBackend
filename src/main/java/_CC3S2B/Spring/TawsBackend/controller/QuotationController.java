package _CC3S2B.Spring.TawsBackend.controller;

import _CC3S2B.Spring.TawsBackend.dto.*;
import _CC3S2B.Spring.TawsBackend.model.User;
import _CC3S2B.Spring.TawsBackend.service.QuotationService;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import jakarta.validation.Valid;
import java.util.*;
@RestController
@RequestMapping("/api/quotations")
public class QuotationController {
 private final QuotationService service;
 public QuotationController(QuotationService service){this.service=service;}
 @GetMapping("/destinations")
 public List<DestinationOption> destinations(){return service.destinations().stream().map(d->new DestinationOption(d.getDestinationId(),d.getName())).toList();}
 @PostMapping("/generate")
 public List<QuoteResponse> generate(@Valid @RequestBody QuoteRequest request, Authentication authentication){return service.generate(request,authenticated(authentication));}
 @PostMapping("/confirm")
 public ConfirmedView confirm(@Valid @RequestBody ConfirmRequest request, Authentication authentication){return service.confirm(request.proposalId(),authenticated(authentication));}
 @GetMapping("/my")
 public List<MyPackageView> mine(Authentication authentication){return service.mine(authenticated(authentication));}
 @GetMapping("/packages/{id}")
 public ConfirmedView details(@PathVariable String id,Authentication authentication){return service.detail(id,authenticated(authentication));}
 private Integer authenticated(Authentication a){
  if(a==null || !a.isAuthenticated() || !(a.getPrincipal() instanceof User user))
   throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Inicia sesion");
  return user.getUserId();
 }
 public record DestinationOption(Integer id,String name){}
}
