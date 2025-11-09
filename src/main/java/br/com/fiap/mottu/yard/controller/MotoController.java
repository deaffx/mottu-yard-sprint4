package br.com.fiap.mottu.yard.controller;

import br.com.fiap.mottu.yard.dto.VagaSugerida;
import br.com.fiap.mottu.yard.exception.BusinessException;
import br.com.fiap.mottu.yard.model.Moto;
import br.com.fiap.mottu.yard.model.Patio;
import br.com.fiap.mottu.yard.service.AlocacaoInteligenteService;
import br.com.fiap.mottu.yard.service.MapaCalorService;
import br.com.fiap.mottu.yard.service.MotoService;
import br.com.fiap.mottu.yard.service.PatioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
@RequestMapping("/motos")
@RequiredArgsConstructor
public class MotoController {

    private final MotoService motoService;
    private final PatioService patioService;
    private final AlocacaoInteligenteService alocacaoService;
    private final MapaCalorService mapaCalorService;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("motos", motoService.findAll());
        return "motos/list";
    }

    @GetMapping("/create")
    public String create(@RequestParam(required = false) Long patioId, Model model) {
        Moto moto = new Moto();
        model.addAttribute("moto", moto);
        model.addAttribute("patios", patioService.findAll());
        
        // Se pátio foi selecionado, gerar sugestão de localização
        if (patioId != null) {
            patioService.findById(patioId).ifPresent(patio -> {
                moto.setPatioAtual(patio);
                VagaSugerida sugestao = alocacaoService.sugerirMelhorVaga(moto, patio);
                if (sugestao != null) {
                    model.addAttribute("vagaSugerida", sugestao);
                }
            });
        }
        
        return "motos/form";
    }

    @PostMapping("/create")
    public String create(@Valid @ModelAttribute Moto moto, 
                        BindingResult result, 
                        Model model,
                        RedirectAttributes redirectAttributes) {
        
        // Carregar o pátio completo se o ID foi fornecido
        Patio patioOriginal = null;
        if (moto.getPatioAtual() != null && moto.getPatioAtual().getId() != null) {
            Optional<Patio> patioOpt = patioService.findById(moto.getPatioAtual().getId());
            if (patioOpt.isPresent()) {
                patioOriginal = patioOpt.get();
                moto.setPatioAtual(patioOriginal);
            }
        }
        
        if (result.hasErrors()) {
            model.addAttribute("patios", patioService.findAll());
            // Reenviar sugestão se houver erro
            if (patioOriginal != null) {
                VagaSugerida sugestao = alocacaoService.sugerirMelhorVaga(moto, patioOriginal);
                model.addAttribute("vagaSugerida", sugestao);
            }
            return "motos/form";
        }

        // Verificar se placa já existe
        if (motoService.findByPlaca(moto.getPlaca()).isPresent()) {
            result.rejectValue("placa", "error.moto", "Placa já existe");
            model.addAttribute("patios", patioService.findAll());
            if (patioOriginal != null) {
                VagaSugerida sugestao = alocacaoService.sugerirMelhorVaga(moto, patioOriginal);
                model.addAttribute("vagaSugerida", sugestao);
            }
            return "motos/form";
        }

        // Validar capacidade do pátio
        if (moto.getPatioAtual() != null && !patioService.hasCapacidade(moto.getPatioAtual())) {
            Long ocupacaoAtual = patioService.getOcupacaoAtual(moto.getPatioAtual());
            result.rejectValue("patioAtual", "error.moto", 
                "Pátio atingiu a capacidade máxima! " +
                "Ocupação: " + ocupacaoAtual + "/" + moto.getPatioAtual().getCapacidadeMaxima());
            model.addAttribute("patios", patioService.findAll());
            return "motos/form";
        }

        // Se localização não foi definida, sugerir automaticamente
        if ((moto.getSetor() == null || moto.getVaga() == null) && patioOriginal != null) {
            VagaSugerida sugestao = alocacaoService.sugerirMelhorVaga(moto, patioOriginal);
            if (sugestao != null) {
                moto.setSetor(sugestao.getSetor());
                moto.setVaga(sugestao.getVaga());
                moto.setLocalizacaoObservacao(sugestao.getMotivo());
            }
        }

        motoService.save(moto);
        
        // Registrar movimentação no histórico
        if (patioOriginal != null && moto.hasLocalizacao()) {
            mapaCalorService.registrarMovimentacao(
                moto,
                null, null, null,  // Origem vazia (nova moto)
                patioOriginal, moto.getSetor(), moto.getVaga(),
                "Cadastro inicial de moto"
            );
        }
        
        String mensagem = "Moto cadastrada com sucesso!";
        if (moto.hasLocalizacao()) {
            mensagem += String.format(" Localização: %s", moto.getLocalizacaoCompleta());
        }
        redirectAttributes.addFlashAttribute("success", mensagem);
        
        return "redirect:/motos";
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
        return motoService.findById(id)
                .map(moto -> {
                    model.addAttribute("moto", moto);
                    model.addAttribute("patios", patioService.findAll());
                    return "motos/form";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("error", "Moto não encontrada!");
                    return "redirect:/motos";
                });
    }

    @PostMapping("/edit/{id}")
    public String edit(@PathVariable("id") Long id,
                      @Valid @ModelAttribute Moto moto,
                      BindingResult result,
                      Model model,
                      RedirectAttributes redirectAttributes) {
        
        // Buscar a moto original para comparar o pátio
        Moto motoOriginal = motoService.findById(id).orElse(null);
        
        // Carregar o pátio completo se o ID foi fornecido
        if (moto.getPatioAtual() != null && moto.getPatioAtual().getId() != null) {
            patioService.findById(moto.getPatioAtual().getId()).ifPresent(moto::setPatioAtual);
        }
        
        if (result.hasErrors()) {
            model.addAttribute("patios", patioService.findAll());
            return "motos/form";
        }

        // Verificar se placa já existe em outra moto
        Optional<Moto> motoExistente = motoService.findByPlaca(moto.getPlaca());
        if (motoExistente.isPresent() && !motoExistente.get().getId().equals(id)) {
            result.rejectValue("placa", "error.moto", "Esta placa já está cadastrada em outra moto");
            model.addAttribute("patios", patioService.findAll());
            return "motos/form";
        }

        // Validar capacidade se pátio mudou
        if (moto.getPatioAtual() != null && motoOriginal != null) {
            Long patioNovoId = moto.getPatioAtual().getId();
            Long patioAntigoId = motoOriginal.getPatioAtual() != null ? motoOriginal.getPatioAtual().getId() : null;
            
            // Se mudou de pátio, validar capacidade
            if (!patioNovoId.equals(patioAntigoId) && !patioService.hasCapacidade(moto.getPatioAtual())) {
                Long ocupacao = patioService.getOcupacaoAtual(moto.getPatioAtual());
                result.rejectValue("patioAtual", "error.moto", 
                    "Pátio lotado! Ocupação: " + ocupacao + "/" + moto.getPatioAtual().getCapacidadeMaxima());
                model.addAttribute("patios", patioService.findAll());
                return "motos/form";
            }
        }

        moto.setId(id);
        motoService.save(moto);
        redirectAttributes.addFlashAttribute("success", "Moto atualizada com sucesso!");
        
        return "redirect:/motos";
    }

    @GetMapping("/details/{id}")
    public String details(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
        return motoService.findById(id)
                .map(moto -> {
                    model.addAttribute("moto", moto);
                    // Buscar histórico de movimentações
                    model.addAttribute("historicoMovimentacoes", mapaCalorService.buscarHistoricoMoto(moto));
                    return "motos/details";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("error", "Moto não encontrada!");
                    return "redirect:/motos";
                });
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            motoService.deleteById(id);
            redirectAttributes.addFlashAttribute("success", "Moto excluída com sucesso!");
        } catch (BusinessException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Não foi possível excluir a moto. Tente novamente.");
        }
        
        return "redirect:/motos";
    }
}