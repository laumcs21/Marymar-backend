package com.marymar.app.business.Service;

import com.marymar.app.business.DTO.ProductoCreateDTO;
import com.marymar.app.business.DTO.ProductoResponseDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ProductoService {

    ProductoResponseDTO crear(ProductoCreateDTO dto, List<MultipartFile> imagenes);

    ProductoResponseDTO obtenerPorId(Long id);

    List<ProductoResponseDTO> obtenerTodos();

    ProductoResponseDTO actualizar(Long id,
                                   ProductoCreateDTO dto,
                                   List<MultipartFile> imagenes);

    void desactivar(Long id);

    List<ProductoResponseDTO> obtenerPorCategoria(Long categoriaId);

    void eliminarDefinitivo(Long id);
}

