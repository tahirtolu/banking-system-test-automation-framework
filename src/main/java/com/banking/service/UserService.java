package com.banking.service;

import com.banking.dto.LoginDTO;
import com.banking.dto.UserRegistrationDTO;
import com.banking.entity.User;
import com.banking.repository.UserRepository;
import com.banking.util.JwtUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.DatabaseMetaData;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final DataSource dataSource;
    
    @PersistenceContext
    private EntityManager entityManager;
    
    /**
     * Veritabanı tipini kontrol eder (SQLite için native SQL, H2 için normal save)
     */
    private boolean isSQLite() {
        try {
            DatabaseMetaData metaData = dataSource.getConnection().getMetaData();
            String databaseProductName = metaData.getDatabaseProductName().toLowerCase();
            return databaseProductName.contains("sqlite");
        } catch (Exception e) {
            // Hata durumunda varsayılan olarak SQLite kabul et (production için)
            return true;
        }
    }

    @Transactional
    public User registerUser(UserRegistrationDTO registrationDTO) {
        if (userRepository.existsByUsername(registrationDTO.getUsername())) {
            throw new RuntimeException("Kullanıcı adı zaten kullanılıyor");
        }
        if (userRepository.existsByEmail(registrationDTO.getEmail())) {
            throw new RuntimeException("E-posta adresi zaten kullanılıyor");
        }

        User user = new User();
        user.setUsername(registrationDTO.getUsername());
        user.setPassword(passwordEncoder.encode(registrationDTO.getPassword()));
        user.setEmail(registrationDTO.getEmail());
        user.setFirstName(registrationDTO.getFirstName());
        user.setLastName(registrationDTO.getLastName());
        user.setPhoneNumber(registrationDTO.getPhoneNumber());

        // SQLite için native SQL workaround, H2 için normal save()
        if (isSQLite()) {
            // SQLite JDBC driver doesn't support GeneratedKeys ResultSet properly
            // Workaround: Use native SQL with last_insert_rowid() to get the ID
            try {
                // Flush any pending changes before native query
                entityManager.flush();
                
                // Use native SQL INSERT to avoid getGeneratedKeys() issue
                String insertSql = "INSERT INTO users (username, password, email, first_name, last_name, phone_number, created_at) " +
                        "VALUES (?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)";
                
                entityManager.createNativeQuery(insertSql)
                        .setParameter(1, user.getUsername())
                        .setParameter(2, user.getPassword())
                        .setParameter(3, user.getEmail())
                        .setParameter(4, user.getFirstName())
                        .setParameter(5, user.getLastName())
                        .setParameter(6, user.getPhoneNumber())
                        .executeUpdate();
                
                // Flush to ensure INSERT is committed to database before querying last_insert_rowid()
                entityManager.flush();
                
                // Get the last inserted ID using SQLite's last_insert_rowid()
                Long id = ((Number) entityManager.createNativeQuery("SELECT last_insert_rowid()")
                        .getSingleResult()).longValue();
                
                // Query the saved user with ID
                user = userRepository.findById(id)
                        .orElseThrow(() -> new RuntimeException("Kullanıcı kaydedilemedi - ID alınamadı"));
            } catch (Exception e) {
                // If native SQL fails, check if user was saved by username
                User savedUser = userRepository.findByUsername(registrationDTO.getUsername()).orElse(null);
                if (savedUser != null) {
                    return savedUser;
                }
                throw new RuntimeException("Kullanıcı kaydedilemedi: " + e.getMessage(), e);
            }
        } else {
            // H2 veya diğer veritabanları için normal save() kullan
            return userRepository.save(user);
        }
        
        return user;
    }

    public String login(LoginDTO loginDTO) {
        User user = userRepository.findByUsername(loginDTO.getUsername())
                .orElseThrow(() -> new RuntimeException("Kullanıcı adı veya şifre hatalı"));

        if (!passwordEncoder.matches(loginDTO.getPassword(), user.getPassword())) {
            throw new RuntimeException("Kullanıcı adı veya şifre hatalı");
        }

        return jwtUtil.generateToken(user.getUsername());
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));
    }

    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));
    }
}

