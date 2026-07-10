import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class test_bcrypt {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String hash = "$2a$10$N.zmdr9k7uOCQb3ZQMKT.eX7mI9v0g6nLXB4Z5z0a8Yv0g6nLXB4Z";
        
        String[] passwords = {"admin123", "Admin@2026!HRMS", "123456", "admin", "Admin123"};
        
        for (String pwd : passwords) {
            boolean matches = encoder.matches(pwd, hash);
            System.out.println(pwd + ": " + matches);
            if (matches) {
                System.out.println("FOUND! Password is: " + pwd);
                break;
            }
        }
        
        // Generate a new hash for admin123
        String newHash = encoder.encode("admin123");
        System.out.println("\nNew hash for admin123: " + newHash);
        System.out.println("\nSQL: UPDATE sys_user SET password = '" + newHash + "' WHERE username = 'admin';");
    }
}
