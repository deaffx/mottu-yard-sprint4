package br.com.fiap.mottu.yard.controller;

import br.com.fiap.mottu.yard.model.Manutencao;
import br.com.fiap.mottu.yard.model.Moto;
import br.com.fiap.mottu.yard.service.ManutencaoService;
import br.com.fiap.mottu.yard.service.MotoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/manutencao")
@RequiredArgsConstructor
public class ManutencaoController {

    private final ManutencaoService manutencaoService;
    private final MotoService motoService;

    @GetMapping
    public String list(Model model) {
        Map<Manutencao.StatusManutencao, Long> statusCounts = new EnumMap<>(Manutencao.StatusManutencao.class);
        for (Manutencao.StatusManutencao value : Manutencao.StatusManutencao.values()) {
            statusCounts.put(value, manutencaoService.countManutencoesByStatus(value));
        }

        model.addAttribute("manutencoes", manutencaoService.findAllOrderByDataAgendadaDesc());
        model.addAttribute("statusOptions", Manutencao.StatusManutencao.values());
        model.addAttribute("tipoOptions", Manutencao.TipoManutencao.values());
        model.addAttribute("statusCounts", statusCounts);

        return "manutencao/list";
    }

    @GetMapping("/create")
    public String create(Model model) {
        Manutencao manutencao = new Manutencao();
        manutencao.setMoto(new Moto());

        model.addAttribute("manutencao", manutencao);

        model.addAttribute("motos", motoService.findByStatus(Moto.StatusMoto.PARA_MANUTENCAO));
        model.addAttribute("tipos", Manutencao.TipoManutencao.values());

        model.addAttribute("statusOptions", new Manutencao.StatusManutencao[]{
            Manutencao.StatusManutencao.AGENDADA,
            Manutencao.StatusManutencao.EM_ANDAMENTO
        });
        model.addAttribute("isEdit", false);
        return "manutencao/form";
    }

    @PostMapping("/create")
    public String create(@Valid @ModelAttribute("manutencao") Manutencao manutencao,
                         BindingResult result,
                         Model model,
                         RedirectAttributes redirectAttributes) {

        // Validação: data obrigatória se agendada
        if (manutencao.getStatusManutencao() == Manutencao.StatusManutencao.AGENDADA 
            && manutencao.getDataAgendada() == null) {
            result.rejectValue("dataAgendada", "NotNull", "Data obrigatória para status 'Agendada'");
        }

        // Validação: moto deve estar PARA_MANUTENCAO
        if (manutencao.getMoto() != null && manutencao.getMoto().getId() != null) {
            Optional<Moto> motoOpt = motoService.findById(manutencao.getMoto().getId());
            if (motoOpt.isPresent() && motoOpt.get().getStatusMoto() != Moto.StatusMoto.PARA_MANUTENCAO) {
                result.rejectValue("moto", "error.manutencao", "Apenas motos 'Para Manutenção' podem ser cadastradas");
            }
        }

        if (result.hasErrors()) {
            model.addAttribute("motos", motoService.findByStatus(Moto.StatusMoto.PARA_MANUTENCAO));
            model.addAttribute("tipos", Manutencao.TipoManutencao.values());
            model.addAttribute("statusOptions", new Manutencao.StatusManutencao[]{
                Manutencao.StatusManutencao.AGENDADA,
                Manutencao.StatusManutencao.EM_ANDAMENTO
            });
            model.addAttribute("isEdit", false);
            return "manutencao/form";
        }

        // Carregar moto completa e atualizar seu status se necessário
        Moto moto = motoService.findById(manutencao.getMoto().getId()).orElseThrow();
        manutencao.setMoto(moto);
        
        // Se manutenção já começou, mover moto para oficina
        if (manutencao.getStatusManutencao() == Manutencao.StatusManutencao.EM_ANDAMENTO) {
            manutencao.setDataIniciada(LocalDateTime.now());
            moto.setStatusMoto(Moto.StatusMoto.NA_OFICINA);
            motoService.save(moto);
        }
        
        manutencaoService.save(manutencao);
        
        String mensagem = manutencao.getStatusManutencao() == Manutencao.StatusManutencao.AGENDADA 
            ? "Manutenção agendada com sucesso!" 
            : "Manutenção iniciada com sucesso!";
        
        redirectAttributes.addFlashAttribute("success", mensagem);
        return "redirect:/manutencao";
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
        return manutencaoService.findById(id)
                .map(manutencao -> {
                    model.addAttribute("manutencao", manutencao);
                    model.addAttribute("motos", java.util.List.of(manutencao.getMoto()));
                    model.addAttribute("tipos", Manutencao.TipoManutencao.values());

                    model.addAttribute("statusOptions", Manutencao.StatusManutencao.values());
                    model.addAttribute("isEdit", true);
                    return "manutencao/form";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("error", "Manutenção não encontrada.");
                    return "redirect:/manutencao";
                });
    }

    @PostMapping("/edit/{id}")
    public String edit(@PathVariable("id") Long id,
                       @Valid @ModelAttribute("manutencao") Manutencao manutencao,
                       BindingResult result,
                       Model model,
                       RedirectAttributes redirectAttributes) {

        // Validação: data obrigatória se agendada
        if (manutencao.getStatusManutencao() == Manutencao.StatusManutencao.AGENDADA 
            && manutencao.getDataAgendada() == null) {
            result.rejectValue("dataAgendada", "NotNull", "Data obrigatória para status 'Agendada'");
        }

        if (result.hasErrors()) {
            // Recarregar apenas a moto vinculada (não permite trocar no edit)
            Moto motoVinculada = motoService.findById(manutencao.getMoto().getId()).orElseThrow();
            model.addAttribute("motos", java.util.List.of(motoVinculada));
            model.addAttribute("tipos", Manutencao.TipoManutencao.values());
            model.addAttribute("statusOptions", Manutencao.StatusManutencao.values());
            model.addAttribute("isEdit", true);
            return "manutencao/form";
        }

        manutencao.setId(id);
        manutencaoService.save(manutencao);
        redirectAttributes.addFlashAttribute("success", "Manutenção atualizada!");
        return "redirect:/manutencao";
    }

    @PreAuthorize("hasRole('OPERADOR')")
    @PostMapping("/delete/{id}")
    public String delete(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            manutencaoService.deleteById(id);
            redirectAttributes.addFlashAttribute("success", "Manutenção excluída com sucesso!");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", "Erro ao excluir manutenção: " + ex.getMessage());
        }
        return "redirect:/manutencao";
    }

    @PostMapping("/start/{id}")
    public String start(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            manutencaoService.iniciarManutencao(id);
            redirectAttributes.addFlashAttribute("success", "Manutenção iniciada com sucesso!");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", "Não foi possível iniciar a manutenção: " + ex.getMessage());
        }
        return "redirect:/manutencao";
    }

    @PostMapping("/complete/{id}")
    public String complete(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            manutencaoService.concluirManutencao(id);
            redirectAttributes.addFlashAttribute("success", "Manutenção concluída com sucesso!");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", "Não foi possível concluir a manutenção: " + ex.getMessage());
        }
        return "redirect:/manutencao";
    }

    @GetMapping("/details/{id}")
    public String details(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
        return manutencaoService.findById(id)
                .map(manutencao -> {
                    model.addAttribute("manutencao", manutencao);
                    return "manutencao/details";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("error", "Manutenção não encontrada.");
                    return "redirect:/manutencao";
                });
    }
}
