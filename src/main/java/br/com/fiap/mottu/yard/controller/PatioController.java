package br.com.fiap.mottu.yard.controller;

import br.com.fiap.mottu.yard.exception.BusinessException;
import br.com.fiap.mottu.yard.model.Patio;
import br.com.fiap.mottu.yard.service.PatioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/patios")
@RequiredArgsConstructor
public class PatioController {

    private final PatioService patioService;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("patios", patioService.findAll());
        model.addAttribute("patioService", patioService);
        return "patios/list";
    }

    @GetMapping("/create")
    public String create(Model model) {
        model.addAttribute("patio", new Patio());
        return "patios/form";
    }

    @PostMapping("/create")
    public String create(@Valid @ModelAttribute Patio patio, 
                        BindingResult result, 
                        RedirectAttributes redirectAttributes) {
        
        if (result.hasErrors()) {
            return "patios/form";
        }

        patioService.save(patio);
        redirectAttributes.addFlashAttribute("success", "Pátio cadastrado com sucesso!");
        
        return "redirect:/patios";
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
        return patioService.findById(id)
                .map(patio -> {
                    model.addAttribute("patio", patio);
                    return "patios/form";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("error", "Pátio não encontrado!");
                    return "redirect:/patios";
                });
    }

    @PostMapping("/edit/{id}")
    public String edit(@PathVariable("id") Long id,
                      @Valid @ModelAttribute Patio patio,
                      BindingResult result,
                      RedirectAttributes redirectAttributes) {
        
        if (result.hasErrors()) {
            return "patios/form";
        }

        patio.setId(id);
        patioService.save(patio);
        redirectAttributes.addFlashAttribute("success", "Pátio atualizado com sucesso!");
        
        return "redirect:/patios";
    }

    @GetMapping("/details/{id}")
    public String details(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
        return patioService.findById(id)
                .map(patio -> {
                    long ocupacaoAtual = patioService.getOcupacaoAtual(patio);
                    double taxaOcupacao = patioService.getTaxaOcupacao(patio);
                    int larguraBarra = (int) Math.min(Math.round(taxaOcupacao), 100);

                    model.addAttribute("patio", patio);
                    model.addAttribute("ocupacaoAtual", ocupacaoAtual);
                    model.addAttribute("taxaOcupacao", taxaOcupacao);
                    model.addAttribute("taxaOcupacaoBarra", "width:" + larguraBarra + "%");
                    return "patios/details";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("error", "Pátio não encontrado!");
                    return "redirect:/patios";
                });
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            patioService.deleteById(id);
            redirectAttributes.addFlashAttribute("success", "Pátio excluído com sucesso!");
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Não foi possível excluir o pátio. Tente novamente.");
        }
        
        return "redirect:/patios";
    }
}