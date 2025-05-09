package me.mmtr.koala.jpa;

import me.mmtr.koala.KoalaApplication;
import me.mmtr.koala.model.User;
import me.mmtr.koala.repository.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = KoalaApplication.class)
public class JPAIntegrationTests {

    @Autowired
    private UserRepository userRepository;

    @Test
    public void shouldOKWhenSaveAndRetrieveEntity() {
        User user = new User();
        user.setName("Test");

        userRepository.save(user);
        User foundUser = userRepository.findById(user.getId()).orElse(null);

        Assertions.assertNotNull(foundUser);
        Assertions.assertEquals("Test", foundUser.getName());
    }
}
