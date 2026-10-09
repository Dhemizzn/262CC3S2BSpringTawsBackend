package _CC3S2B.Spring.TawsBackend.repository;
import _CC3S2B.Spring.TawsBackend.model.Module;

import org.springframework.data.jpa.repository.JpaRepository; //Crear un repository para trabajar con la BD sin escribir manualmente querys.
import org.springframework.data.jpa.repository.Query; // Te permite escribir una consulta JPQL personalizada.
import org.springframework.data.repository.query.Param; // Sirve para conectar un parámetro de Java con un parámetro nombrado dentro de @Query
import org.springframework.stereotype.Repository; // Indica que esa clase es un componente de acceso a datos.
import java.util.List;

@Repository
public interface ModuleRepository extends JpaRepository<Module, Integer> {
    Module m = null;
    @Query("SELECT m FROM Module m JOIN RolModule rm ON m.moduleId = rm.moduleId WHERE rm.isActive = true AND rm.rolId = :roleId")
    List<Module> findByRoleId(@Param("roleId") Integer roleId);
}
