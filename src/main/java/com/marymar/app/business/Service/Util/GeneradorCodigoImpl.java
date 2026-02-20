package com.marymar.app.business.Service.Util;

import com.marymar.app.business.Service.EmailService;
import com.marymar.app.persistence.Entity.VerificacionCodigo;
import com.marymar.app.persistence.Repository.VerificacionCodigoRepository;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class GeneradorCodigoImpl implements GeneradorCodigo {

    private final SecureRandom random = new SecureRandom();
    private final VerificacionCodigoRepository verificacionCodigoRepository;
    private final EmailService emailService;

    public GeneradorCodigoImpl(VerificacionCodigoRepository verificacionCodigoRepository,
                               EmailService emailService) {
        this.verificacionCodigoRepository = verificacionCodigoRepository;
        this.emailService = emailService;
    }

    @Override
    public String generarCodigo(String email) {

        // Invalidar códigos anteriores activos
        List<VerificacionCodigo> codigosActivos =
                verificacionCodigoRepository.findByEmailAndUsedFalse(email);

        for (VerificacionCodigo codigo : codigosActivos) {
            codigo.setUsed(true);
        }
        verificacionCodigoRepository.saveAll(codigosActivos);

        // Generar código seguro de 6 dígitos
        String code = String.format("%06d", random.nextInt(1_000_000));

        //  Crear nueva verificación
        VerificacionCodigo verification = new VerificacionCodigo();
        verification.setEmail(email);
        verification.setCode(code);
        verification.setExpiration(LocalDateTime.now().plusMinutes(10));
        verification.setUsed(false);

        verificacionCodigoRepository.save(verification);

        emailService.send(email, code);

        return "Código enviado al correo";
    }

    @Override
    public void validarCodigo(String email, String code) {

        VerificacionCodigo verification = verificacionCodigoRepository
                .findByEmailAndCodeAndUsedFalse(email, code)
                .orElseThrow(() -> new RuntimeException("Código inválido"));

        // Verificar expiración
        if (verification.getExpiration().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("El código ha expirado");
        }

        // Marcar como usado
        verification.setUsed(true);
        verificacionCodigoRepository.save(verification);
    }
}

