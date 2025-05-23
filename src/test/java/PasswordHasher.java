import org.mindrot.jbcrypt.BCrypt;

public class PasswordHasher {
    public static String hashPassword(String plainPassword) {
        String salt = BCrypt.gensalt(10);
        return BCrypt.hashpw(plainPassword, salt);
    }

    public static void main(String[] args) {
        String[] usernames = {"admin", "joao", "maria", "ana"};
        String[] passwords = {"admin", "joao", "maria", "ana"};

        for (int i = 0; i < usernames.length; i++) {
            String hashed = hashPassword(passwords[i]);
            System.out.printf("INSERT INTO users (username, password_hash, role) VALUES ('%s', '%s', '%s');%n",
                    usernames[i], hashed, (usernames[i].equals("admin") ? "ADMIN" : "EMPLOYEE"));
        }
    }
}
