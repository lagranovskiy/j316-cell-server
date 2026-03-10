package org.j316.cellserver.view;

import org.j316.cellserver.adapter.ChoranzeigeCommunicationPort;
import org.j316.cellserver.view.binding.CellOperation;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class CellResource {

  private final ChoranzeigeCommunicationPort cellCom;
  private final CellOperation operation;

  public CellResource(ChoranzeigeCommunicationPort cellCom, CellOperation operation) {
    this.cellCom = cellCom;
    this.operation = operation;
  }

  @GetMapping("/")
  public String init(Model model, @AuthenticationPrincipal OAuth2User user) {
    model.addAttribute("cellOperation", operation);
    model.addAttribute("result", cellCom.ping());
    return "index";
  }

  @PostMapping(value = "/", params = "action=send")
  public String sendMessage(@ModelAttribute CellOperation cellOperation, Model model, @AuthenticationPrincipal OAuth2User user) {
    operation.setSendValue(cellOperation.getSendValue());
    model.addAttribute("cellOperation", operation);
    model.addAttribute("result", cellCom.sendTxt(cellOperation.getSendValue()));
    return "index";
  }

  @PostMapping(value = "/", params = "action=clear")
  public String clearMessage(Model model) {
    operation.setSendValue("");
    model.addAttribute("cellOperation", operation);
    model.addAttribute("result", cellCom.clear());
    return "index";
  }
}
