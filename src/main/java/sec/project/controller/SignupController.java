package sec.project.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import sec.project.domain.Signup;
import sec.project.repository.SignupRepository;

@Controller
public class SignupController {

    @Autowired
    private SignupRepository signupRepository;
    private int capacity = 3;

    @RequestMapping("*")
    public String defaultMapping() {
        return "redirect:/form";
    }

    @RequestMapping(value = "/form", method = RequestMethod.GET)
    public String loadForm(Model model) {
        model.addAttribute("available", capacity - signupRepository.count() );
        return "form";
    }

    @RequestMapping(value = "/form", method = RequestMethod.POST)
    public String submitForm(@RequestParam String name, @RequestParam String email, @RequestParam String address, Model model) {
        //check signup by email.
        Signup s = signupRepository.findByEmail(email);
        if (s == null) {
            signupRepository.save(new Signup(name, email, address));
            return "done";
        } else {
            model.addAttribute("user", s);
            return "signup";
        }
    }
    
    @RequestMapping(value = "/users", method = RequestMethod.GET)
    public String queryUsers(Model model) {
        model.addAttribute("users", signupRepository.findAll());
        return "users";
    }
    
        @RequestMapping(value = "/user", method = RequestMethod.DELETE)
    public String deleteUser(@RequestParam String email, @RequestParam(required=false) Boolean admin) {
        signupRepository.delete(signupRepository.findByEmail(email));
        if (admin != null)
            return "redirect:/users";
        else
            return "redirect:/form";
    }
    
}
