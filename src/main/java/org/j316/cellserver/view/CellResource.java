package org.j316.cellserver.view;

import org.j316.cellserver.com.CellCom;
import org.j316.cellserver.view.binding.CellOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;


@Controller
public class CellResource {

    @Autowired
    private CellCom cellCom;

    @Autowired
    private CellOperation operation;

    @GetMapping("/")
    public String init(Model model) {
       String pingResult =  cellCom.ping();
        model.addAttribute("cellOperation", operation);
        model.addAttribute("result", pingResult);
        return "index";
    }


    @RequestMapping(value="/", method = RequestMethod.POST,  params="action=send")
    public String sendMessage(@ModelAttribute CellOperation cellOperation, @RequestParam(value="action", required=true) String action, Model model) {
        this.operation.setSendValue(cellOperation.getSendValue());

        String result = cellCom.sendTxt(cellOperation.getSendValue());
        model.addAttribute("cellOperation", operation);
        model.addAttribute("result", result);

        return "index";
    }

    @RequestMapping(value="/", method = RequestMethod.POST,  params="action=clear")
    public String clearMessage(@ModelAttribute CellOperation cellOperation, @RequestParam(value="action", required=true) String action, Model model) {
        this.operation.setSendValue("");

        String result = cellCom.clear();




        model.addAttribute("cellOperation", operation);
        model.addAttribute("result", result);

        return "index";
    }
}
