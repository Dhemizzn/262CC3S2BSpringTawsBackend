package _CC3S2B.Spring.TawsBackend.repository;

import _CC3S2B.Spring.TawsBackend.model.Person;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PersonRepository extends JpaRepository<Person, Integer>  {
    boolean existsByNumDocumento(String numDocumento);
    List<Person> findByNumDocumento(String numDocumento);
    List<Person> findAllByOrderByIdPersonaDesc();
}
