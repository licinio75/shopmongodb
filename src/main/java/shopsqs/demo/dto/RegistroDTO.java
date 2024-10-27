package shopsqs.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class RegistroDTO {
    private String email;
    private String password;
    private String nombre;
}
