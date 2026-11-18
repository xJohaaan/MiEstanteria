package com.miestanteria.backend.service;

import com.miestanteria.backend.dto.ComunidadDtos.FeedDto;
import com.miestanteria.backend.dto.ComunidadDtos.RecomendacionDto;
import com.miestanteria.backend.dto.ComunidadDtos.RecomendarRequest;
import com.miestanteria.backend.dto.ComunidadDtos.UsuarioResumen;
import com.miestanteria.backend.exception.ApiException;
import com.miestanteria.backend.model.EstadoUsuario;
import com.miestanteria.backend.model.Recomendacion;
import com.miestanteria.backend.model.Usuario;
import com.miestanteria.backend.repository.RecomendacionRepository;
import com.miestanteria.backend.repository.UsuarioRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Recomendaciones entre usuarios (RF07) y muro de actividad de la comunidad. */
@Service
public class ComunidadService {

    private final RecomendacionRepository recomendaciones;
    private final UsuarioRepository usuarios;
    private final LibroService libroService;
    private final ResenaService resenaService;

    public ComunidadService(RecomendacionRepository recomendaciones, UsuarioRepository usuarios,
                            LibroService libroService, ResenaService resenaService) {
        this.recomendaciones = recomendaciones;
        this.usuarios = usuarios;
        this.libroService = libroService;
        this.resenaService = resenaService;
    }

    /** Envia una recomendacion. Regla: nadie puede recomendarse un libro a si mismo. */
    @Transactional
    public RecomendacionDto recomendar(Long idEmisor, RecomendarRequest req) {
        if (idEmisor.equals(req.receptorId())) {
            throw ApiException.badRequest("No puedes recomendarte un libro a ti mismo");
        }
        Usuario emisor = usuarios.findById(idEmisor).orElseThrow(() -> ApiException.unauthorized("La cuenta ya no existe"));
        Usuario receptor = usuarios.findById(req.receptorId())
                .filter(u -> u.getEstado() == EstadoUsuario.ACTIVO)
                .orElseThrow(() -> ApiException.notFound("Ese usuario no existe"));
        Recomendacion r = new Recomendacion();
        r.setEmisor(emisor);
        r.setReceptor(receptor);
        r.setLibro(libroService.obtenerOCrear(req.libro()));
        r.setMensaje(req.mensaje() == null || req.mensaje().isBlank() ? null : req.mensaje().trim());
        r.setVisto(false);
        return RecomendacionDto.from(recomendaciones.save(r));
    }

    @Transactional(readOnly = true)
    public List<RecomendacionDto> recibidas(Long idUsuario) {
        return recomendaciones.findRecibidas(idUsuario, PageRequest.of(0, 50)).stream().map(RecomendacionDto::from).toList();
    }

    /** Marca una recomendacion como vista (baja el contador de notificaciones). */
    @Transactional
    public void marcarVista(Long idUsuario, Long id) {
        Recomendacion r = recomendaciones.findRecibida(id, idUsuario)
                .orElseThrow(() -> ApiException.notFound("La recomendacion no existe"));
        r.setVisto(true);
    }

    @Transactional(readOnly = true)
    public FeedDto feed(Long idUsuario) {
        return new FeedDto(
                recomendaciones.countByReceptorIdAndVistoFalse(idUsuario),
                recomendaciones.findRecibidas(idUsuario, PageRequest.of(0, 20)).stream().map(RecomendacionDto::from).toList(),
                resenaService.recientes(idUsuario, 30)
        );
    }

    /** Busca lectores por nombre (minimo 2 letras) para elegir a quien recomendar. */
    @Transactional(readOnly = true)
    public List<UsuarioResumen> buscarUsuarios(Long idUsuario, String texto) {
        if (texto == null || texto.trim().length() < 2) return List.of();
        return usuarios.buscarPorNombre(texto.trim(), idUsuario, PageRequest.of(0, 20)).stream()
                .map(UsuarioResumen::from).toList();
    }
}
