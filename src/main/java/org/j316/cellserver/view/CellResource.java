package org.j316.cellserver.view;

import org.j316.cellserver.adapter.ChoranzeigeCommunicationPort;
import org.j316.cellserver.view.binding.CellOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;


@Controller
public class CellResource {

  @Autowired
  private ChoranzeigeCommunicationPort cellCom;

  @Autowired
  private CellOperation operation;

  @GetMapping("/")
  public String init(Model model) {
    String pingResult = cellCom.ping();
    model.addAttribute("cellOperation", operation);
    model.addAttribute("result", pingResult);
    return "index";
  }


  @RequestMapping(value = "/", method = RequestMethod.POST, params = "action=send")
  public String sendMessage(
      @ModelAttribute CellOperation cellOperation,
      @RequestParam(value = "action", required = true) String action,
      Model model) {
    this.operation.setSendValue(cellOperation.getSendValue());

    String result = cellCom.sendTxt(cellOperation.getSendValue());
    model.addAttribute("cellOperation", operation);
    model.addAttribute("result", result);

    return "index";
  }

  @RequestMapping(value = "/", method = RequestMethod.POST, params = "action=clear")
  public String clearMessage(
      @ModelAttribute CellOperation cellOperation,
      @RequestParam(value = "action", required = true) String action,
      Model model) {
    this.operation.setSendValue("");

    String result = cellCom.clear();

    model.addAttribute("cellOperation", operation);
    model.addAttribute("result", result);

    return "index";
  }
}
