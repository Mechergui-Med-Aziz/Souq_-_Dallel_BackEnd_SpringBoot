package com.backend.server.controller;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

import jakarta.mail.MessagingException;
import org.json.JSONObject;
import org.springframework.core.io.Resource; // For Resource type
import org.springframework.core.io.UrlResource; // For UrlResource
import org.springframework.http.HttpHeaders; // For HTTP headers
import org.springframework.http.ResponseEntity; // For ResponseEntity
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.util.StringUtils;
// For handling GET requests
// For accessing path variables
// For marking the class as a REST controller
import org.springframework.web.bind.annotation.*;
import com.backend.server.config.JWT;
import com.backend.server.entity.LoginRequest;
import com.backend.server.entity.LoginResponse;
import com.backend.server.entity.User;
import com.backend.server.service.EmailService;
import com.backend.server.service.UserService;

import lombok.Data;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.multipart.MultipartFile;

@Data
class EmailRequest {
    private String to;
    private String subject;
    private String body;

    // Getters and setters
}

@RestController
@RequestMapping(path="/user")
public class UserController {
    @Autowired
    UserService userSer;

    @Autowired
    private JWT myJwt;

    @Autowired 
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    private EmailService emailService;

    @PostMapping("/send-email")
    public String sendEmail(@RequestBody EmailRequest request) {
        try {
            emailService.sendEmail(request.getTo(), request.getSubject(), request.getBody());
            return "Email sent successfully";
        } catch (MessagingException e) {
            return "Failed to send email: " + e.getMessage();
        }
    }
    
    @GetMapping("/get/{id}")
    public ResponseEntity<?> getOneById(@PathVariable ("id")Long id){
        try{
            User u = userSer.findById(id) ;
            return ResponseEntity.accepted().body(u);
        }catch(Exception e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
    @GetMapping("/get/all")
    public ResponseEntity<?> getAll(){
        try{
            List<User> u = userSer.findAll() ;
            return ResponseEntity.accepted().body(u);
        }catch(Exception e){
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/get/all/exceptadmins")
    public ResponseEntity<?> getAllEexceptadmins(){
        try{
            List<User> u = userSer.findAllExceptAdmins() ;
            return ResponseEntity.accepted().body(u);
        }catch(Exception e){
            return ResponseEntity.badRequest().build();
        }
    }

    private String saveUserImage(MultipartFile image) throws IOException {
        // Define the directory where the images will be stored
        String uploadDir = "upload/avatar/";

        // Create the directory if it doesn't exist
        Files.createDirectories(Paths.get(uploadDir));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        // Create a unique image name (avatar-{userId}.extension)
        String originalFilename = StringUtils.cleanPath(Objects.requireNonNull(image.getOriginalFilename()));
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String newFilename = "avatar-" + LocalDateTime.now().format(formatter) + extension;

        // Save the file to the target directory
        Files.copy(image.getInputStream(), Paths.get(uploadDir + newFilename), StandardCopyOption.REPLACE_EXISTING);
        return newFilename;
    }

    @GetMapping("/upload/avatar/{imageName:.+}")
    public ResponseEntity<Resource> serveFile(@PathVariable String imageName) throws MalformedURLException {
        Path file = Paths.get("upload/avatar/" + imageName);
        Resource resource = new UrlResource(file.toUri());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    @PostMapping("/signup")
    public ResponseEntity<?> SignUp(
            @RequestParam("username") String username,
            @RequestParam("email") String email,
            @RequestParam("password") String password,
            @RequestParam("firstname") String firstname,
            @RequestParam("lastname") String lastname,
            @RequestParam("phoneNumber") String phoneNumber,
            @RequestParam("address") String address,
            @RequestParam("image") MultipartFile image // Add the image as MultipartFile
    ) {
        try {
            // Check if the user already exists
            if (userSer.findOneByEmail(email) != null || userSer.findOneByUsername(username) != null) {
                JSONObject ExistResponse = new JSONObject();
                ExistResponse.put("message", "Your e-mail or username is already used!");
                ExistResponse.put("status", "error");
                return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(ExistResponse.toString());
            }

            // Create a new user object
            User u = new User();
            u.setUsername(username);
            u.setEmail(email);
            u.setPassword(bCryptPasswordEncoder.encode(password));
            u.setFirstname(firstname);
            u.setLastname(lastname);
            u.setPhoneNumber(Integer.valueOf(phoneNumber));
            u.setAddress(address);
            u.setStatus(User.Status.ACTIVE); // Default status
            u.setRole(User.Role.USER);        // Default role
            u.setCreatedAt(LocalDateTime.now());
            // After saving the user, process the image upload
            if (image != null && !image.isEmpty()) {
                u.setImageUrl(saveUserImage(image)); // Save image with user_id
            }
            userSer.save(u);
            return ResponseEntity.accepted().build();

        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/transporter/add")
    public ResponseEntity<?> addTransporter(
            @RequestParam("username") String username,
            @RequestParam("email") String email,
            @RequestParam("password") String password,
            @RequestParam("firstname") String firstname,
            @RequestParam("lastname") String lastname,
            @RequestParam("phoneNumber") String phoneNumber,
            @RequestParam("address") String address,
            @RequestParam("image") MultipartFile image // Add the image as MultipartFile
    ) {
        try {
            // Check if the user already exists
            if (userSer.findOneByEmail(email) != null || userSer.findOneByUsername(username) != null) {
                JSONObject ExistResponse = new JSONObject();
                ExistResponse.put("message", "Your e-mail or username is already used!");
                ExistResponse.put("status", "error");
                return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(ExistResponse.toString());
            }
            // Create a new user object
            
            User u = new User();
            String pass = u.getPassword();
            u.setUsername(username);
            u.setEmail(email);
            u.setPassword(bCryptPasswordEncoder.encode(password));
            u.setFirstname(firstname);
            u.setLastname(lastname);
            u.setPhoneNumber(Integer.valueOf(phoneNumber));
            u.setAddress(address);
            u.setStatus(User.Status.ACTIVE); // Default status
            u.setRole(User.Role.TRANSPORTER); // Default role
            u.setCreatedAt(LocalDateTime.now());
            
            // After saving the user, process the image upload
            if (image != null && !image.isEmpty()) {
                u.setImageUrl(saveUserImage(image)); // Save image with user_id
            }
            userSer.save(u);
            emailService.sendEmail(
                u.getEmail(),
                "Your account created successfuly",
                "<p>Dear " + u.getFirstname()+" "+u.getLastname() +",</p>"
                + "<p>We are pleased to inform you that your account was created successfuly</p>"
                + "<p>As a transporter with S&D this is your login:</p>"
                + "<p><strong>Username:</strong> " + u.getUsername() + "</p>"
                + "<p><strong>Password:</strong> " + pass + "</p>"
                + "<p>Please reset your password when you loggin in</p>"
                + "<p>Warm regards,<br/>The S&D Team</p>"
            );
            return ResponseEntity.accepted().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PostMapping("/login")
    public ResponseEntity<?> Login(@RequestBody LoginRequest l){
        try{
            User u ;
            if(l.getEmail() != null) u = userSer.findOneByEmail(l.getEmail()) ;
            else if(l.getUsername() != null)  u = userSer.findOneByUsername(l.getUsername()) ;
            else return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            if (u.getStatus() == User.Status.BLOCKED) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            if( bCryptPasswordEncoder.matches(l.getPassword(), u.getPassword()) ){
                String jwt = myJwt.generateToken(u);
                LoginResponse Lr = new LoginResponse(jwt,u);
                return ResponseEntity.status(HttpStatus.ACCEPTED).body(Lr);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
        }catch(Exception e){
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/google-login")
    public ResponseEntity<?> getUserInfo(@AuthenticationPrincipal OAuth2User principal) {
        if (principal != null) {
            String email = principal.getAttribute("email");
            String lastname = principal.getAttribute("family_name");
            String firstname = principal.getAttribute("given_name");

            User userInfo = new User();
            userInfo.setEmail(email);
            userInfo.setFirstname(firstname);
            userInfo.setLastname(lastname);
            User user = userSer.findOrCreateUser(userInfo);

            String jwtToken = myJwt.generateToken(user);
            LoginResponse Lr = new LoginResponse(jwtToken,user);
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(Lr);
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User is not authenticated.");
    }
    
    @PutMapping("/update/{id}")
    public ResponseEntity<?> BlockOneById(@PathVariable("id") Long id,@RequestBody User u){
        try{
            if(u.getPassword() != null)
                u.setPassword(bCryptPasswordEncoder.encode(u.getPassword()));
            User UserAfterUpdates = userSer.updateById(id,u);
            return ResponseEntity.accepted().body(UserAfterUpdates);
        }catch(Exception e){
            System.out.println(e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/status")
    public ResponseEntity<?> ChangeStatus(@RequestParam("id") Long id){
        try{
            User up = userSer.findById(id);
            if(up == null) return ResponseEntity.notFound().build();
            up.setStatus( up.getStatus() == User.Status.ACTIVE ? User.Status.BLOCKED : User.Status.ACTIVE );
            userSer.updateById(up.getId(), up);
            return ResponseEntity.accepted().body(null);
        } catch ( Exception e ) {
            return ResponseEntity.badRequest().build();
        }
    }

}