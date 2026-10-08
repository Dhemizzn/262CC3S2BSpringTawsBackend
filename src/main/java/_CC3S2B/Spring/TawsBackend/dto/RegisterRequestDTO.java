package _CC3S2B.Spring.TawsBackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterRequestDTO {
    private String firstName;
    private String paternalSurname;
    private String maternalSurname;
    private String dni;
    private String phone;
    private String email;
    private String direction;
    private Integer roleId;
}
