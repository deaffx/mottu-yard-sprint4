package br.com.fiap.mottu.yard.controller;

import br.com.fiap.mottu.yard.dto.MapaCalor;
import br.com.fiap.mottu.yard.service.AlocacaoInteligenteService;
import br.com.fiap.mottu.yard.service.MapaCalorService;
import br.com.fiap.mottu.yard.service.MotoService;
import br.com.fiap.mottu.yard.service.PatioService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/patios")
@RequiredArgsConstructor
public class MapaCalorController {

    private final PatioService patioService;
    private final MapaCalorService mapaCalorService;
    private final AlocacaoInteligenteService alocacaoService;
    private final MotoService motoService;

    @GetMapping("/{id}/mapa")
    public String verMapaCalor(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
        return patioService.findById(id)
                .map(patio -> {
                    MapaCalor mapa = mapaCalorService.gerarMapaCalor(patio);
                    List<String> recomendacoes = alocacaoService.gerarRecomendacoesReorganizacao(patio);
                    
                    // Buscar motos do pátio para exibir na lista
                    model.addAttribute("patio", patio);
                    model.addAttribute("mapa", mapa);
                    model.addAttribute("recomendacoes", recomendacoes);
                    model.addAttribute("motos", motoService.findByPatio(patio));
                    
                    return "patios/mapa-calor";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("error", "Pátio não encontrado!");
                    return "redirect:/patios";
                });
    }

    @GetMapping("/{id}/recomendacoes")
    public String verRecomendacoes(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
        return patioService.findById(id)
                .map(patio -> {
                    List<String> recomendacoes = alocacaoService.gerarRecomendacoesReorganizacao(patio);
                    model.addAttribute("patio", patio);
                    model.addAttribute("recomendacoes", recomendacoes);
                    return "patios/recomendacoes";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("error", "Pátio não encontrado!");
                    return "redirect:/patios";
                });
    }
}
