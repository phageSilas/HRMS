import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordGen {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String password = "admin123";
        String hash = encoder.encode(password);
        System.out.println("=====================================");
        System.out.println("Password: " + password);
        System.out.println("BCrypt Hash: " + hash);
        System.out.println("=====================================");
        System.out.println("\nSQL to update:");
        System.out.println("UPDATE sys_user SET password = '" + hash + "' WHERE username = 'admin';");
    }
}
