package br.com.fiap.mottu.yard.controller;

import br.com.fiap.mottu.yard.model.Manutencao;
import br.com.fiap.mottu.yard.model.Moto;
import br.com.fiap.mottu.yard.service.ManutencaoService;
import br.com.fiap.mottu.yard.service.MotoService;
import br.com.fiap.mottu.yard.service.PatioService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.EnumMap;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final MotoService motoService;
    private final PatioService patioService;
    private final ManutencaoService manutencaoService;

    @GetMapping("/")
    public String home() {
        return "redirect:/dashboard";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
    model.addAttribute("totalMotos", motoService.countTotalMotos());
    model.addAttribute("motosParaRegularizar", motoService.countMotosByStatus(Moto.StatusMoto.PARA_REGULARIZAR));
    model.addAttribute("motosParaManutencao", motoService.countMotosByStatus(Moto.StatusMoto.PARA_MANUTENCAO));
    model.addAttribute("motosNaOficina", motoService.countMotosByStatus(Moto.StatusMoto.NA_OFICINA));
    model.addAttribute("motosParaAlugar", motoService.countMotosByStatus(Moto.StatusMoto.PARA_ALUGAR));

        Map<Manutencao.StatusManutencao, Long> statusCounts = new EnumMap<>(Manutencao.StatusManutencao.class);
        for (Manutencao.StatusManutencao value : Manutencao.StatusManutencao.values()) {
            statusCounts.put(value, manutencaoService.countManutencoesByStatus(value));
        }
        model.addAttribute("statusManutencao", Manutencao.StatusManutencao.values());
        model.addAttribute("statusCounts", statusCounts);
        model.addAttribute("manutencoesPendentes", manutencaoService.findManutencoesPendentes());
        model.addAttribute("patios", patioService.findAll());
        model.addAttribute("motosRecentes", motoService.findRecentMotos(5));

        return "home/dashboard";
    }
}