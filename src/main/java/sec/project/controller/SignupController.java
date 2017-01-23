package sec.project.controller;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.encoding.Md5PasswordEncoder;
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
    private HttpSession session;
    
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
        String userAuthenticated = (String) session.getAttribute("userauthenticated");
        if (userAuthenticated != null) {
            model.addAttribute("users", signupRepository.findAll());
            model.addAttribute("userauth", userAuthenticated);
            return "users";
        } else {
            return "redirect:/adminlogin";
        }
        
    }
    
        @RequestMapping(value = "/user", method = RequestMethod.DELETE)
    public String deleteUser(@RequestParam String email) {
        signupRepository.delete(signupRepository.findByEmail(email));
        if (session.getAttribute("userauthenticated") != null)
            return "redirect:/users";
        else
            return "redirect:/form";
    }
    
        @RequestMapping(value = "/user", method = RequestMethod.POST)
    public String modifyUser(@RequestParam String email, @RequestParam String name, @RequestParam String address, Model model) {
        Signup s = signupRepository.findByEmail(email);
        s.setName(name);
        s.setAddress(address);
        signupRepository.save(s);
        
        model.addAttribute("user", s);
        return "signup";
    }
    
    @RequestMapping(value = "/adminlogin", method = RequestMethod.GET)
    public String loginForm() {
        return "adminlogin";
    }
    
    @RequestMapping(value = "/adminlogin", method = RequestMethod.POST)
    public String adminLogin(@RequestParam String username, @RequestParam String password, Model model) {
        
        try {
            // Open connection
            Connection connection = DriverManager.getConnection("jdbc:h2:mem:testdb", "sa", "");
            
            // Execute query and retrieve the query results
            ResultSet resultSet = connection.createStatement().executeQuery("SELECT * FROM ACCOUNT WHERE USERNAME='"+username+"' and PASSWORD='"+(new Md5PasswordEncoder()).encodePassword(password, null)+"'");
            
            // Do something with the results -- here, we print the books
            if (resultSet.next()) {
                String dbuser = resultSet.getString("username");
                String dbhash = resultSet.getString("password");
                
                // Close the connection
                resultSet.close();
                connection.close();
                
                System.out.println(dbuser+ " autenticated with pass: "+dbhash);
                session.setAttribute("userauthenticated", dbuser);
                
                return "redirect:/users";
            } else {
                System.out.println(username+ " autenticated failed!");
                
                // Close the connection
                resultSet.close();
                connection.close();
                
                return "redirect:/adminlogin?error";
            }
        } catch (SQLException ex) {
            return "redirect:/adminlogin?error";
        }
    }
    
}
